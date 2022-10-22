package de.mark225.beam.command.resolver.arguments;

import de.mark225.beam.util.BeamUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ArgumentRegistry {

    private ConcurrentMap<String, ArgumentHandlerData> argumentHandlers = new ConcurrentHashMap<>();

    private void registerArgumentHandler(Method annotatedMethod) {
        RegisterArgumentHandler annotation = annotatedMethod.getAnnotation(RegisterArgumentHandler.class);
        String id = annotation.value().toLowerCase();
        Class<?> returnType = annotatedMethod.getReturnType();
        Class<?>[] parameters = annotatedMethod.getParameterTypes();
        Class<?>[] contextTypes = Arrays.stream(parameters).skip(2).toArray(Class<?>[]::new);
        String[] usageIndicators = annotation.usageIndicators();

        if(usageIndicators.length != 2)
            throw new IllegalArgumentException("Usage indicators must be an array of length 2");

        AutocompleteHandler autocompleteHandler = null;
        try {
            Method autocompleteMethod = annotatedMethod.getClass().getMethod(annotatedMethod.getName() + "Autocomplete", parameters);
            if(autocompleteMethod.getReturnType() == List.class && Modifier.isStatic(autocompleteMethod.getModifiers())){
                autocompleteHandler = (argumentDefinition, argument, context) -> {
                    if (context == null) context = new Object[0];
                    try {
                        Object[] callParameters = new Object[context.length + 2];
                        callParameters[0] = argumentDefinition;
                        callParameters[1] = argument;
                        System.arraycopy(context, 0, callParameters, 2, context.length);
                        return (List<String>) autocompleteMethod.invoke(null, callParameters);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                };
            }
        }catch(NoSuchMethodException e) {
            //No autocomplete method found, ignore
        }

        ArgumentHandler argumentHandler = (argumentDefinition, argument, context) -> {
            if(context == null) context = new Object[0];
            try {
                Object[] callParameters = new Object[context.length + 2];
                callParameters[0] = argumentDefinition;
                callParameters[1] = argument;
                System.arraycopy(context, 0, callParameters, 2, context.length);
                return annotatedMethod.invoke(null, callParameters);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        };

        argumentHandlers.put(id, new ArgumentHandlerData(argumentHandler, autocompleteHandler, id, returnType, contextTypes, usageIndicators));
    }

    public Optional<ArgumentHandlerData> getArgumentHandlerData(String dataType) {
        return Optional.ofNullable(argumentHandlers.get(dataType.toLowerCase()));
    }

    public void registerArgumentHandlers(Class<?> handlerClass){
        for(Method method : handlerClass.getMethods()){
            if(!Modifier.isStatic(method.getModifiers()) || !method.isAnnotationPresent(RegisterArgumentHandler.class)) continue;
            if(method.getParameterCount() < 2 || !String.class.isAssignableFrom(method.getParameterTypes()[0]) || !String.class.isAssignableFrom(method.getParameterTypes()[1])){
                BeamUtils.getLogger().warning("Invalid argument handler method signature: "+method.getName());
                continue;
            }
            registerArgumentHandler( method);
        }
    }



}
