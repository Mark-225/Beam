package de.mark225.beam.command.resolver.arguments;

import java.util.List;

@FunctionalInterface
public interface AutocompleteHandler {
    List<String> generateSuggestions(String argumentDefinition, String argument, Object[] context);
}
