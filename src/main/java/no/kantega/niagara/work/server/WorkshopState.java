package no.kantega.niagara.work.server;

import fj.data.List;
import fj.data.TreeMap;
import org.kantega.niagara.Mealy;

import static org.kantega.niagara.Mealy.transition;

public class WorkshopState implements Mealy<IncMsg, List<ProgressMsg>> {

    private final TreeMap<String, Progress> teamProgress;

    public WorkshopState(TreeMap<String, Progress> teamProgress) {
        this.teamProgress = teamProgress;
    }

    @Override
    public Transition<IncMsg, List<ProgressMsg>> apply(IncMsg s) {
        TreeMap<String, Progress> currentProgress =
          s.payload.startsWith("start") ?
            teamProgress.set(s.sessionId, WorkshopTasks.tasks) :
            teamProgress;

        return currentProgress.get(s.sessionId)
          .map(progress -> progress.advance(s.payload))
          .option(
            transition(List.single(ProgressMsg.msg("The session is unknown")), this),
            next -> transition(next._1(), new WorkshopState(teamProgress.set(s.sessionId, next._2())))
          );
    }


}
