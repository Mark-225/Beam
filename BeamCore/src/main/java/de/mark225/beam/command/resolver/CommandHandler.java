package de.mark225.beam.command.resolver;

import de.mark225.beam.data.BeamCommandSender;

@FunctionalInterface
public interface CommandHandler {
    String handle(BeamCommandSender sender, String usedAlias, Object[] args) throws CommandExecuteException;
}
