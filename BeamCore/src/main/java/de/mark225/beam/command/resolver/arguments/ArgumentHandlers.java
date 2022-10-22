package de.mark225.beam.command.resolver.arguments;

public final class ArgumentHandlers {

    private ArgumentHandlers() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    @RegisterArgumentHandler(value = "literal", usageIndicators = {"", ""})
    public static void handleLiteral(String argumentDefinitionParameters, String argument) throws ArgumentParseException{
        if(!argumentDefinitionParameters.equalsIgnoreCase(argument))
            throw new ArgumentParseException("Expected literal '%s' but got '%s'".formatted(argumentDefinitionParameters, argument));
    }

    @RegisterArgumentHandler("boolean")
    public static boolean handleBoolean(String argumentDefinitionParameters, String argument) throws ArgumentParseException {
        if("true".equalsIgnoreCase(argument)) {
            return true;
        } else if("false".equalsIgnoreCase(argument)) {
            return false;
        } else {
            throw new ArgumentParseException("Expected 'true' or 'false' but got '%s'".formatted(argument));
        }
    }

    @RegisterArgumentHandler("int")
    public static int handleInt(String argumentDefinitionParameters, String argument) throws ArgumentParseException {
        try {
            int integer = Integer.parseInt(argument);
            if(argumentDefinitionParameters != null){
                String[] split = argumentDefinitionParameters.split(":");
                int min = Integer.parseInt(split[0]);
                int max = split.length > 1 ? Integer.parseInt(split[1]) : 0;
                if(integer < min || (split.length > 1 && integer > max)){
                    throw new ArgumentParseException("Expected a number between %d and %d but got %d".formatted(min, max, integer));
                }
            }
            return integer;
        }catch (NumberFormatException e) {
            throw new ArgumentParseException("Expected Integer but got '%s'".formatted(argument), e);
        }
    }

    @RegisterArgumentHandler("double")
    public static double handleDouble(String argumentDefinitionParameters, String argument) throws ArgumentParseException {
        try {
            double d = Double.parseDouble(argument);
            if(argumentDefinitionParameters != null){
                String[] split = argumentDefinitionParameters.split(":");
                double min = Double.parseDouble(split[0]);
                double max = split.length > 1 ? Double.parseDouble(split[1]) : 0;
                if(d < min || (split.length > 1 && d > max)){
                    throw new ArgumentParseException("Expected a number between %f and %f but got %f".formatted(min, max, d));
                }
            }
            return d;
        }catch (NumberFormatException e) {
            throw new ArgumentParseException("Expected Double but got '%s'".formatted(argument), e);
        }
    }

    @RegisterArgumentHandler("select")
    public static String handleSelect(String argumentDefinitionParameters, String argument) throws ArgumentParseException {
        if(argumentDefinitionParameters == null) throw new ArgumentParseException("No select options defined");
        String[] options = argumentDefinitionParameters.split(":");
        for(String option : options){
            if(option.equalsIgnoreCase(argument)) return option;
        }
        throw new ArgumentParseException("Expected one of %s but got '%s'".formatted(String.join(", ", options), argument));
    }

}
