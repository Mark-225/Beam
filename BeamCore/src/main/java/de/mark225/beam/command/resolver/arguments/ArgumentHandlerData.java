package de.mark225.beam.command.resolver.arguments;

public class ArgumentHandlerData {
    private final ArgumentHandler handler;
    private final AutocompleteHandler autocompleteHandler;
    private final String id;
    private final Class<?> returnType;
    private final Class<?>[] contextTypes;
    private final String[] usageIndicators;

    public ArgumentHandlerData(ArgumentHandler handler, AutocompleteHandler autocompleteHandler, String id, Class<?> returnType, Class<?>[] contextTypes, String[] usageIndicators) {
        this.handler = handler;
        this.autocompleteHandler = autocompleteHandler;
        this.id = id;
        this.returnType = returnType;
        this.contextTypes = contextTypes;
        this.usageIndicators = usageIndicators;
    }

    public ArgumentHandler getHandler() {
        return handler;
    }

    public AutocompleteHandler getAutocompleteHandler() {
        return autocompleteHandler;
    }

    public String getId() {
        return id;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public Class<?>[] getContextTypes() {
        return contextTypes;
    }

    public String[] getUsageIndicators() {
        return usageIndicators;
    }
}
