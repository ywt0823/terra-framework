package com.terra.framework.nova.prompt.exception;

/**
 * A custom exception for errors related to the Prompt Mapper feature.
 * <p>
 * This exception is thrown for issues such as missing prompt templates,
 * parsing errors, or problems during proxy invocation.
 *
 * @author DeavyJones
 */
public class PromptException extends RuntimeException {

    public PromptException(String message) {
        super(message);
    }

    public PromptException(String message, Throwable cause) {
        super(message, cause);
    }
} 