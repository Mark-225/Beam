package de.mark225.beam.command.resolver;

public class CommandInitException extends Exception {

    public CommandInitException(String message) {
        super(message);
    }

    public CommandInitException(String message, Throwable cause) {
        super(message, cause);
    }
}
