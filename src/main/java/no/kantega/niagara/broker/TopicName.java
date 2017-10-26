package no.kantega.niagara.broker;

import no.kantega.niagara.work.server.SessionId;

public class TopicName {
    public final String name;

    public TopicName(String name) {
        this.name = name;
    }


    public static TopicName progressTopic(SessionId id){
        return new TopicName("/progress/"+id.value);
    }

    public static TopicName solution(String sessionId){
        return new TopicName("/progress/"+sessionId);
    }
}
