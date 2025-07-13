package com.terra.framework.nova.prompt.proxy;

import com.terra.framework.nova.prompt.annotation.Param;
import com.terra.framework.nova.prompt.config.PromptConfig;
import com.terra.framework.nova.prompt.exception.PromptException;
import com.terra.framework.nova.prompt.template.PromptTemplate;
import com.terra.framework.nova.prompt.template.PromptTemplateRegistry;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The dynamic proxy that handles invocations of prompt mapper interface methods.
 * <p>
 * This is the core component that connects a mapper interface to the AI model.
 * It intercepts method calls, finds the corresponding prompt template,
 * populates it with method arguments, and executes it against the configured chat model.
 *
 * @author DeavyJones
 */
public class PromptMapperProxy implements InvocationHandler {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^}]+)}");

    private final ApplicationContext applicationContext;
    private final PromptTemplateRegistry registry;
    private final Class<?> mapperInterface;
    private final ChatModel defaultChatModel;
    private final Map<Method, Function<Object[], Object>> methodCache = new ConcurrentHashMap<>();

    public PromptMapperProxy(ApplicationContext applicationContext,
                           PromptTemplateRegistry registry,
                           Class<?> mapperInterface,
                           ChatModel defaultChatModel) {
        this.applicationContext = applicationContext;
        this.registry = registry;
        this.mapperInterface = mapperInterface;
        this.defaultChatModel = defaultChatModel;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (Object.class.equals(method.getDeclaringClass())) {
            return method.invoke(this, args);
        }
        return methodCache.computeIfAbsent(method, this::createMethodExecutor).apply(args);
    }

    private Function<Object[], Object> createMethodExecutor(Method method) {
        String fqId = mapperInterface.getName() + "." + method.getName();
        PromptTemplate template = registry.getTemplate(fqId);

        if (template == null) {
            throw new PromptException("No prompt template found for method " + fqId);
        }

        // Currently only supports String return type
        if (method.getReturnType() != String.class) {
            throw new PromptException("Prompt mapper method " + fqId + " must have a String return type.");
        }

        return (params) -> {
            Map<String, Object> paramMap = getParamMap(method, params);
            String renderedPrompt = renderPrompt(template.getTemplate(), paramMap);

            // Resolve the chat model based on configuration
            ChatModel chatModel = resolveChatModel(template.getConfig());

            // Execute with configuration
            return executeWithConfig(chatModel, renderedPrompt, template.getConfig());
        };
    }

    /**
     * Resolves the appropriate ChatModel based on the prompt configuration.
     */
    private ChatModel resolveChatModel(PromptConfig config) {
        String modelName = config.getModel();

        if (modelName == null || modelName.trim().isEmpty()) {
            return defaultChatModel;
        }

        try {
            // Try to get the model by bean name first
            if (applicationContext.containsBean(modelName)) {
                Object bean = applicationContext.getBean(modelName);
                if (bean instanceof ChatModel) {
                    return (ChatModel) bean;
                }
            }

            // Try to get the model by class name
            Class<?> modelClass = Class.forName(modelName);
            Map<String, ?> beans = applicationContext.getBeansOfType(modelClass);
            if (!beans.isEmpty()) {
                Object bean = beans.values().iterator().next();
                if (bean instanceof ChatModel) {
                    return (ChatModel) bean;
                }
            }

            // Fallback to default model
            return defaultChatModel;

        } catch (Exception e) {
            throw new PromptException("Failed to resolve chat model: " + modelName +
                    ". Available models: " + getAvailableModels(), e);
        }
    }

    /**
     * Executes the prompt with the given configuration.
     */
    private String executeWithConfig(ChatModel chatModel, String promptText, PromptConfig config) {
        try {
            // Build chat options from configuration
            ChatOptions.Builder optionsBuilder = ChatOptions.builder();

            if (config.getTemperature() != null) {
                optionsBuilder.temperature(config.getTemperature());
            }

            if (config.getMaxTokens() != null) {
                optionsBuilder.maxTokens(config.getMaxTokens());
            }

            if (config.getTopP() != null) {
                optionsBuilder.topP(config.getTopP());
            }

            ChatOptions options = optionsBuilder.build();

            // Create prompt with options
            Prompt prompt = new Prompt(promptText, options);

            // Execute the prompt
            ChatResponse response = chatModel.call(prompt);


            // Return the content
            return response.getResult().getOutput().getText();

        } catch (Exception e) {
            throw new PromptException("Failed to execute prompt with model " +
                    chatModel.getClass().getSimpleName(), e);
        }
    }

    private String renderPrompt(String template, Map<String, Object> params) {
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String placeholder = matcher.group(1);
            Object value = params.get(placeholder);
            if (value == null) {
                throw new PromptException("Missing value for placeholder '" + placeholder + "' in prompt template.");
            }
            matcher.appendReplacement(sb, String.valueOf(value));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private Map<String, Object> getParamMap(Method method, Object[] args) {
        Map<String, Object> paramMap = new HashMap<>();
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Param paramAnnotation = parameters[i].getAnnotation(Param.class);
            if (paramAnnotation == null) {
                throw new PromptException("Method parameter at index " + i + " of " + method.getName() +
                        " is missing @Param annotation.");
            }
            String name = paramAnnotation.value();
            paramMap.put(name, args[i]);
        }
        return paramMap;
    }

    /**
     * Gets a list of available chat models for error reporting.
     */
    private String getAvailableModels() {
        try {
            Map<String, ChatModel> models = applicationContext.getBeansOfType(ChatModel.class);
            return String.join(", ", models.keySet());
        } catch (Exception e) {
            return "Unable to retrieve available models";
        }
    }
}
