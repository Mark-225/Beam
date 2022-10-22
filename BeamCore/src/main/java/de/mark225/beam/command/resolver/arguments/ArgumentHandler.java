package de.mark225.beam.command.resolver.arguments;

@FunctionalInterface
public interface ArgumentHandler {

    Object handle(String argumentDefinitionParameters, String argument, Object[] context) throws ArgumentParseException;

}
