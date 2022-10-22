package de.mark225.beam.command.resolver;

import de.mark225.beam.command.resolver.arguments.ArgumentRegistry;
import de.mark225.beam.data.BeamCommandSender;
import de.mark225.beam.util.BeamUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class CommandRegistry {
    private ArgumentRegistry argumentRegistry = new ArgumentRegistry();

    private Map<String, List<BeamCommand>> registeredCommands = new ConcurrentHashMap<>();

    private void registerCommand(Method annotatedMethod){
        RegisterBeamCommand annotation = annotatedMethod.getAnnotation(RegisterBeamCommand.class);
        BeamCommand command = BeamCommand.fromAnnotation(annotation);
        try{
            command.init(argumentRegistry, annotatedMethod);
            insertCommandEntry(annotation.label(), command);
            for(String alias : annotation.aliases()){
                insertCommandEntry(alias, command);
            }
        }catch(CommandInitException e){
            BeamUtils.getLogger().log(Level.WARNING, "Failed to initialize command %s".formatted(annotation.label()), e);
        }
    }

    private void insertCommandEntry(String label, BeamCommand command){
        label = label.toLowerCase();
        if(!registeredCommands.containsKey(label)){
            registeredCommands.put(label, new CopyOnWriteArrayList<>());
        }
        registeredCommands.get(label).add(command);
    }

    public ArgumentRegistry getArgumentRegistry() {
        return argumentRegistry;
    }

    public void registerCommands(Class<?> commandHandlerClass){
        for(Method m : commandHandlerClass.getMethods()){
            if(!m.isAnnotationPresent(RegisterBeamCommand.class)) continue;
            if(!Modifier.isStatic(m.getModifiers())) continue;
            if(!String.class.isAssignableFrom(m.getReturnType())){
                BeamUtils.getLogger().warning("Command method %s.%s does not return a String".formatted(commandHandlerClass.getName(), m.getName()));
                continue;
            }
            registerCommand(m);
        }
    }

    public List<String> autoComplete(BeamCommandSender sender, String label, String[] args){
        if(args.length == 0) return Collections.emptyList();
        String commandLabel = label.toLowerCase();
        if(!registeredCommands.containsKey(commandLabel)) return Collections.emptyList();
        List<BeamCommand> commands = registeredCommands.get(commandLabel);
        return commands.stream()
                .filter(c -> sender.checkPermission(c.getPermission()))
                .filter(c -> c.getArgumentHandlers().length >= args.length)
                .flatMap(c -> c.tabComplete(sender, args).stream())
                .filter(s -> s.toLowerCase().startsWith(args[args.length-1].toLowerCase()))
                .toList();
    }

    public String execute(BeamCommandSender sender, String label, String[] args) throws CommandExecuteException {
        String commandLabel = label.toLowerCase();
        if(!registeredCommands.containsKey(commandLabel)) return "Unknown command";
        List<BeamCommand> commands = registeredCommands.get(commandLabel).stream().filter(c -> sender.checkPermission(c.getPermission())).toList();
        if(commands.isEmpty()) return "Unknown command";
        for(BeamCommand command : commands){
            if(command.getArgumentHandlers().length != args.length) continue;
            try{
                return command.execute(sender, label, args);
            }catch(CommandExecuteException e){
                BeamUtils.getLogger().log(Level.FINEST, "Failed to execute command %s".formatted(label), e);
            }
        }
        return "Invalid arguments. Try using one of the following formats:\n"
                + commands.stream().map(BeamCommand::getUsage).collect(Collectors.joining("\n"));
    }

}
