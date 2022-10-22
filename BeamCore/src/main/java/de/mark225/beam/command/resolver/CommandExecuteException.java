package de.mark225.beam.command.resolver;

public class CommandExecuteException extends Exception {

    public CommandExecuteException(String message) {
        super(message);
    }

    public CommandExecuteException(String message, Throwable cause) {
        super(message, cause);
    }
}
