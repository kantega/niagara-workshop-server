package no.kantega.niagara.work.server;

import fj.P2;
import fj.data.List;

public class TeamState {

    public final Progress progress;

    public TeamState(
      Progress progress) {
        this.progress = progress;
    }


    public TeamState withProgress(Progress progress) {
        return new TeamState(progress);
    }

    public static P2<TeamState, List<ProgressOutput>> newTeamState() {
        return
          WorkshopTasks.tasks
            .untilNextAwait()
            .swap()
            .map1(TeamState::new);
    }

    P2<TeamState, List<ProgressOutput>> advance(String msg) {
        return progress.advance(msg).swap().map1(TeamState::new);
    }
}
