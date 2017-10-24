package no.kantega.niagara.work.server;

import fj.P2;
import fj.data.List;
import fj.data.TreeMap;
import org.kantega.niagara.Mealy;

import static fj.P.p;

public class WorkshopState implements Mealy<ProgressInput, List<WorkshopStateMsg>> {


    public final TreeMap<SessionId, TeamState> teams;

    private WorkshopState(TreeMap<SessionId, TeamState> teamProgress) {
        this.teams = teamProgress;
    }

    static WorkshopState newWorkshop() {
        return new WorkshopState(TreeMap.empty(SessionId.sessionIdOrd));
    }

    static WorkshopState wst(TreeMap<SessionId, TeamState> teamProgress) {
        return new WorkshopState(teamProgress);
    }

    public P2<WorkshopState, List<WorkshopStateMsg>> handle(ProgressInput msg) {
        P2<TeamState, List<WorkshopStateMsg>> next =
          teams.get(msg.sessionId)
            .map(team -> team.advance(msg.message))
            .orSome(TeamState.newTeamState())
            .map2(list -> list.map(progressMsg -> new WorkshopStateMsg(msg.sessionId, progressMsg)));

        return p(WorkshopState.wst(teams.set(msg.sessionId, next._1())), next._2());
    }


    @Override
    public Transition<ProgressInput, List<WorkshopStateMsg>> apply(ProgressInput msg) {
        return Mealy.transition(handle(msg));
    }
}
