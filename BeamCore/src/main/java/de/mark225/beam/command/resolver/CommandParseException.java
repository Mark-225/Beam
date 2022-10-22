package de.mark225.beam.command.resolver;

public class CommandParseException extends Exception {

    public CommandParseException(String message) {
        super(message);
    }

    public CommandParseException(String message, Throwable cause) {
        super(message, cause);
    }

}
