package no.kantega.niagara.work.server;

import fj.P2;
import fj.data.List;

import static fj.P.p;

public class Team {

    public final SessionId sessionId;
    public final String    name;
    public final Progress  progress;

    public Team(SessionId sessionId, String name, Progress progress) {
        this.sessionId = sessionId;
        this.name = name;
        this.progress = progress;
    }

    public static P2<Team, List<ProgressOutput>> newTeam(SessionId id, String name) {
        P2<Progress, List<ProgressOutput>> begin =
          WorkshopTasks.tasks.begin();

        Team t =
          new Team(id, name, begin._1());

        return p(t, begin._2());
    }

    public P2<Team, List<ProgressOutput>> apply(ProgressInput input) {
        return
          progress.advance(input).map1(this::withProgress);
    }

    public Team withProgress(Progress progress){
        return new Team(sessionId,name,progress);
    }


}
