package no.kantega.niagara.workshop.server;


public class ProgressInput {

    public final String message;

    public ProgressInput(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "ProgressInput{" +
          "message='" + message + '\'' +
          '}';
    }
}

