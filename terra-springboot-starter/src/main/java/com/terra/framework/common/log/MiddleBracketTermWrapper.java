package com.terra.framework.common.log;

public class MiddleBracketTermWrapper implements TermWrapper {

    private final static String LEFT_MIDDLE_BRACKET = "[";
    private final static String RIGHT_MIDDLE_BRACKET = "]";

    private static final MiddleBracketTermWrapper instance = new MiddleBracketTermWrapper();

    private MiddleBracketTermWrapper() {
    }

    public static MiddleBracketTermWrapper getInstance() {
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
