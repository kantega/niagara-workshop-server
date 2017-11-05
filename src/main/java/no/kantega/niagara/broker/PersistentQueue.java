package no.kantega.niagara.broker;

import fj.Unit;
import fj.data.List;
import org.kantega.niagara.*;
import org.kantega.niagara.exchange.AsyncDroppingInputQueue;
import org.kantega.niagara.exchange.Topic;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;

public class PersistentQueue implements Sink<ProducerRecord> {

    static final Path defaultLogDir =
      Paths.get(System.getProperty("user.home") + "/niagara/data").toAbsolutePath();

    final ExecutorService executor;

    private final Topic<ConsumerRecord> outbound = new Topic<>();
    private final AtomicLong            counter  = new AtomicLong();

    final Connection journal;

    private PersistentQueue(Path path, ExecutorService executor) {
        this.executor = executor;
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new Error(e);
        }
        File f = path.toFile();
        f.mkdirs();

        if (!f.exists() || !f.isDirectory())
            throw new RuntimeException(f.getAbsolutePath() + " is not a directrory");


        try {
            journal = DriverManager.getConnection("jdbc:h2:" + f.toPath().toString());
            journal
              .prepareStatement("create table if not exists journal(id varchar(255), counter bigint auto_increment, topic varchar(255), message clob)")
              .execute();

            ResultSet rs =
              journal.prepareStatement("select max(counter) as c from journal").executeQuery();

            while (rs.next())
                counter.set(rs.getLong("c"));

        } catch (SQLException e) {
            throw new RuntimeException("Could not create journal", e);
        }
    }

    public static <A> PersistentQueue log(ExecutorService executor) {
        return new PersistentQueue(defaultLogDir, executor);
    }

    public Task<Unit> consume(ProducerRecord producerRecord) {
        return Task.tryTask(() -> {
            try {
                PreparedStatement ps = journal.prepareStatement("insert into journal (id,topic,message) values (?,?,?)");
                UUID              id = UUID.randomUUID();
                ps.setString(1, id.toString());
                ps.setString(2, producerRecord.topic.name);
                ps.setString(3, producerRecord.msg);
                ps.execute();

                return new ConsumerRecord(id.toString(), counter.incrementAndGet(), producerRecord.topic, producerRecord.msg);
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        })
          .bind(consumerRecord ->
            outbound.publish(consumerRecord).thenJust(consumerRecord)
          ).using(executor).toUnit();
    }

    /**
     * @return
     */
    public Source<ConsumerRecord> subscribe(long offset) {
        AsyncDroppingInputQueue<ConsumerRecord> queue =
          new AsyncDroppingInputQueue<>(1000, executor);


        return (closer, handler) ->
          outbound.subscribe().closeOn(closer).to(queue).toTask().execute()
            .or(replay(offset).append(queue::subscribe).open(closer, handler));
    }

    public Source<ConsumerRecord> subscribeToLast() {
        return outbound.subscribe();
    }

    public Task<List<ConsumerRecord>> messages(long offset) {
        return () -> {
            try {
                PreparedStatement ps = journal.prepareStatement("select id,counter,topic,message from journal where counter >= ? order by counter");
                ps.setLong(1, offset);
                ResultSet                 rs     = ps.executeQuery();
                ArrayList<ConsumerRecord> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(new ConsumerRecord(rs.getString("id"), rs.getLong("counter"), new TopicName(rs.getString("topic")), rs.getString("message")));
                }
                return Eventually.value(List.iterableList(result));
            } catch (SQLException e) {
                return Eventually.fail(e);
            }
        };
    }

    private Source<ConsumerRecord> replay(long offset) {
        return Sources
          .tryCallback(handler -> {
                PreparedStatement ps = journal.prepareStatement("select id,counter,topic,message from journal where counter >= ? order by counter");
                ps.setLong(1, offset);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    handler.f(() -> new ConsumerRecord(rs.getString("id"), rs.getLong("counter"), new TopicName(rs.getString("topic")), rs.getString("message")));
                }
            }
          );
    }

    public Task<Unit> close() {
        return Task.tryRunnableTask(journal::close);
    }

}
