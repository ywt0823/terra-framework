package com.terra.framework.common.log;

public class EmptyTermWrapper implements TermWrapper {

    private final static String LEFT_MIDDLE_BRACKET = "";
    private final static String RIGHT_MIDDLE_BRACKET = "";

    private static final EmptyTermWrapper instance = new EmptyTermWrapper();

    private EmptyTermWrapper() {
    }

    public static EmptyTermWrapper getInstance() {
        return instance;
    }

    @Override
    public String getLeftTerm() {
        return LEFT_MIDDLE_BRACKET;
    }

    @Override
    public String getRightTerm() {
        return RIGHT_MIDDLE_BRACKET;
    }
}
