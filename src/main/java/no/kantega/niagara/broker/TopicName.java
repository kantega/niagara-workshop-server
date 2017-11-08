package no.kantega.niagara.broker;

import no.kantega.niagara.workshop.server.SessionId;

public class TopicName {
  public final String name;

  public TopicName(String name) {
    this.name = name;
  }


  public static TopicName progressTopic(SessionId id) {
    return new TopicName("/progress/" + id.value);
  }

  public static TopicName solution(String sessionId) {
    return new TopicName("/solution/" + sessionId);
  }

  public static TopicName start(String sessionId) {
    return new TopicName("/start/" + sessionId);
  }

  public static TopicName echo() {
    return new TopicName("/echo");
  }

  public static TopicName memberships() {
    return new TopicName("/memberships");
  }

  public static TopicName logonOff() {
    return new TopicName("/logonoff");
  }
}
