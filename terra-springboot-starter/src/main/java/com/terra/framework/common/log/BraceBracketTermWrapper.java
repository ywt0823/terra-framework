package com.terra.framework.common.log;

public class BraceBracketTermWrapper implements TermWrapper {

    private final static String LEFT_MIDDLE_BRACKET = "{";
    private final static String RIGHT_MIDDLE_BRACKET = "}";

    private static final BraceBracketTermWrapper instance = new BraceBracketTermWrapper();

    private BraceBracketTermWrapper() {
    }

    public static BraceBracketTermWrapper getInstance() {
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
