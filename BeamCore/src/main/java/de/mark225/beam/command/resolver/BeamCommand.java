package de.mark225.beam.command.resolver;

import de.mark225.beam.command.resolver.arguments.ArgumentHandlerData;
import de.mark225.beam.command.resolver.arguments.ArgumentParseException;
import de.mark225.beam.command.resolver.arguments.ArgumentRegistry;
import de.mark225.beam.command.resolver.arguments.AutocompleteHandler;
import de.mark225.beam.data.BeamCommandSender;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BeamCommand {
    private String label;
    private String[] aliases;
    private String description;
    private String usage;
    private String permission;
    private ArgumentHandlerData[] argumentHandlers;
    private String[] argumentLabels;
    private String[] argumentDefinitions;
    private String[] argumentDefinitionParameters;
    private int[][] contextReferences;

    private int nonVoidArgumentCount;

    private CommandHandler handler;

    public static BeamCommand fromAnnotation(RegisterBeamCommand annotation){
        return new BeamCommand(
                annotation.label(),
                annotation.aliases(),
                annotation.description(),
                annotation.permission(),
                annotation.argumentLabels(),
                annotation.arguments()
        );
    }

    private BeamCommand(String label, String[] aliases, String description, String permission, String[] argumentLabels, String[] argumentDefinitions) {
        this.label = label;
        this.aliases = aliases;
        this.description = description;
        this.permission = permission;
        this.argumentLabels = argumentLabels;
        this.argumentDefinitions = argumentDefinitions;
    }

    public void init(ArgumentRegistry argumentRegistry, Method handlerMethod) throws CommandInitException {
        if(argumentDefinitions.length != argumentLabels.length){
            throw new CommandInitException("Argument definition count does not match argument label count");
        }
        argumentHandlers = new ArgumentHandlerData[argumentDefinitions.length];
        argumentDefinitionParameters = new String[argumentDefinitions.length];
        contextReferences = new int[argumentDefinitions.length][];
        for(int i = 0; i < argumentDefinitions.length; i++){
            String argumentDefinition = argumentDefinitions[i];
            String[] split = argumentDefinition.split("/");
            String metaData = split[0];
            String parameters = Arrays.stream(split).skip(1).collect(Collectors.joining("/"));
            argumentDefinitionParameters[i] = "".equals(parameters) ? null : parameters;
            String[] splitMetaData = metaData.split(":");
            String argumentType = splitMetaData[0];
            ArgumentHandlerData argumentHandlerData = argumentRegistry.getArgumentHandlerData(argumentType).orElseThrow(() -> new CommandInitException("No argument handler found for type '%s'".formatted(argumentType)));
            if(argumentHandlerData.getContextTypes().length != splitMetaData.length - 1){
                throw new CommandInitException("Argument handler '%s' requires %d context types but got %d".formatted(argumentType, argumentHandlerData.getContextTypes().length, splitMetaData.length - 1));
            }
            int[] argumentContextReferences = new int[argumentHandlerData.getContextTypes().length];
            for(int j = 0; j < argumentHandlerData.getContextTypes().length; j++){
                Class<?> contextType = argumentHandlerData.getContextTypes()[j];
                String contextReferenceString = splitMetaData[j + 1];
                int contextReference;
                try {
                    contextReference = Integer.parseInt(contextReferenceString);
                }catch (NumberFormatException e){
                    throw new CommandInitException("Context reference '%s' is not a number".formatted(contextReferenceString), e);
                }
                if(contextReference > i)
                    throw new CommandInitException("Context reference '%d' is greater than the current argument index '%d'".formatted(contextReference, i));
                if(contextReference > 0 && !contextType.isAssignableFrom(argumentHandlers[contextReference-1].getReturnType()))
                    throw new CommandInitException("Context reference '%d' is not assignable to the context type '%s'".formatted(contextReference, contextType.getName()));
                if(contextReference < -1)
                    throw new CommandInitException("Context reference '%d' is less than -1".formatted(contextReference));
                argumentContextReferences[j] = contextReference;
            }
            contextReferences[i] = argumentContextReferences;
            argumentHandlers[i] = argumentHandlerData;
            if(!argumentHandlerData.getReturnType().equals(Void.TYPE))
                nonVoidArgumentCount++;
        }
        usage = IntStream.range(0, argumentHandlers.length).mapToObj(i -> {
            String[] usageIndicators = argumentHandlers[i].getUsageIndicators();
            String argumentLabel = argumentLabels[i];
            return usageIndicators[0] + argumentLabel + usageIndicators[1];
        }).collect(Collectors.joining(" "));

        if(!String.class.isAssignableFrom(handlerMethod.getReturnType()))
            throw new CommandInitException("Command handler method return type must be String");
        if(handlerMethod.getParameterTypes().length != nonVoidArgumentCount + 2)
            throw new CommandInitException("Command handler method '%s' has %d parameters but requires %d".formatted(handlerMethod.getName(), handlerMethod.getParameterTypes().length, nonVoidArgumentCount + 2));
        if(!handlerMethod.getParameterTypes()[0].equals(BeamCommandSender.class))
            throw new CommandInitException("Command handler method '%s' first parameter is not of type '%s'".formatted(handlerMethod.getName(), BeamCommandSender.class.getName()));
        if(!handlerMethod.getParameterTypes()[1].equals(String.class))
            throw new CommandInitException("Command handler method '%s' second parameter is not of type '%s'".formatted(handlerMethod.getName(), String.class.getName()));
        List<? extends Class<?>> returnTypes = Arrays.stream(argumentHandlers).map(ArgumentHandlerData::getReturnType).filter(Void.TYPE::isAssignableFrom).toList();
        List<? extends Class<?>> parameterTypes = Arrays.stream(handlerMethod.getParameterTypes()).skip(2).toList();
        if(!returnTypes.equals(parameterTypes))
            throw new CommandInitException("Command handler method '%s' parameter types do not match argument handler return types".formatted(handlerMethod.getName()));
        handler = (sender, label, args) -> {
            Object[] methodArgs = new Object[args.length + 2];
            methodArgs[0] = sender;
            methodArgs[1] = label;
            System.arraycopy(args, 0, methodArgs, 2, args.length);
            try {
                return (String) handlerMethod.invoke(null, methodArgs);
            } catch (Exception e) {
                throw new CommandExecuteException("An error occurred while executing command handler method '%s'".formatted(handlerMethod.getName()), e);
            }
        };
    }

    public String getLabel() {
        return label;
    }

    public String[] getAliases() {
        return aliases;
    }

    public String getDescription() {
        return description;
    }

    public String getUsage() {
        return usage;
    }

    public String getPermission() {
        return permission;
    }

    public ArgumentHandlerData[] getArgumentHandlers() {
        return argumentHandlers;
    }

    public String[] getArgumentLabels() {
        return argumentLabels;
    }

    public CommandHandler getHandler() {
        return handler;
    }

    public Object[] parse(BeamCommandSender sender, String[] args) throws CommandParseException {
        return parse(sender, args, args.length);
    }

    public Object[] parse(BeamCommandSender sender, String[] args, int limit) throws CommandParseException{
        if(limit > argumentHandlers.length) limit = argumentHandlers.length;
        if(args.length < limit) throw new CommandParseException("Not enough arguments");
        Object[] parsedArguments = new Object[limit];
        for(int i = 0; i < limit; i++) {
            ArgumentHandlerData argumentHandlerData = argumentHandlers[i];
            Object[] context = new Object[contextReferences[i].length];
            for (int j = 0; j < contextReferences[i].length; j++) {
                int contextReference = contextReferences[i][j];
                if (contextReference == -1) {
                    context[j] = null;
                } else if (contextReference == 0) {
                    context[j] = sender;
                } else {
                    context[j] = parsedArguments[contextReference - 1];
                }
            }
            try {
                parsedArguments[i] = argumentHandlerData.getHandler().handle(argumentDefinitionParameters[i], args[i], context);
            } catch (ArgumentParseException e) {
                throw new CommandParseException("Failed to parse argument '%s' at index %d".formatted(args[i], i), e);
            }
        }
        return parsedArguments;
    }

    public String execute(BeamCommandSender sender, String usedLabel, String[] args) throws CommandExecuteException {
        try{
            Object[] parsedArguments = parse(sender, args);
            List<Object> parsedArgumentsList = new ArrayList<>();
            for(int i = 0; i < parsedArguments.length; i++){
                if(!argumentHandlers[i].getReturnType().equals(Void.TYPE))
                    parsedArgumentsList.add(parsedArguments[i]);
            }
            return handler.handle(sender, usedLabel, parsedArgumentsList.toArray());
        }catch(CommandParseException e){
            throw new CommandExecuteException("Failed to parse arguments", e);
        }
    }

    public List<String> tabComplete(BeamCommandSender sender, String[] args) {
        AutocompleteHandler autocompleteHandler = argumentHandlers[args.length-1].getAutocompleteHandler();
        if(autocompleteHandler == null) return Collections.emptyList();
        Object[] parsedArguments;
        try{
            parsedArguments = parse(sender, args, args.length - 1);
        }catch(CommandParseException e){
            //Command not applicable for tab completion
            return Collections.emptyList();
        }
        Object[] context = Arrays.stream(contextReferences[args.length-1]).mapToObj(i -> {
            if(i == -1) return null;
            else if(i == 0) return sender;
            else return parsedArguments[i-1];
        }).toArray();
        return autocompleteHandler.generateSuggestions(argumentDefinitionParameters[args.length-1], args[args.length-1], context);
    }

}
