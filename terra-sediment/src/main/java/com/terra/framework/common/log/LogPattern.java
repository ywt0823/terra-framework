package com.terra.framework.common.log;


public class LogPattern {

    private static final String DEFAULT_MESSAGE_KEY = "message";
    private static final String DEFAULT_ARGUMENTS_KEY = "params";
    private static final String COLON = ":";
    private static final String COMMA = ",";

    private String messageKey = DEFAULT_MESSAGE_KEY;

    private String parameterKey = DEFAULT_ARGUMENTS_KEY;

    private TermWrapper messageTermWrapper = MiddleBracketTermWrapper.getInstance();
    private TermWrapper parameterTermWrapper = MiddleBracketTermWrapper.getInstance();

    private boolean alwaysShowParameter = true;

    public LogPattern() {
    }

    public LogPattern(boolean alwaysShowParameter) {
        this.alwaysShowParameter = alwaysShowParameter;
    }

    public LogPattern(String messageKey, String parameterKey) {
        this.messageKey = messageKey;
        this.parameterKey = parameterKey;
    }

    public LogPattern(String messageKey, String parameterKey, boolean alwaysShowParameter) {
        this.messageKey = messageKey;
        this.parameterKey = parameterKey;
        this.alwaysShowParameter = alwaysShowParameter;
    }

    public LogPattern(String messageKey, String parameterKey, TermWrapper messageTermWrapper,
                      TermWrapper parameterTermWrapper) {
        this.messageKey = messageKey;
        this.parameterKey = parameterKey;
        this.messageTermWrapper = messageTermWrapper;
        this.parameterTermWrapper = parameterTermWrapper;
    }

    public LogPattern(String messageKey, String parameterKey, TermWrapper messageTermWrapper,
                      TermWrapper parameterTermWrapper, boolean alwaysShowParameter) {
        this.messageKey = messageKey;
        this.parameterKey = parameterKey;
        this.messageTermWrapper = messageTermWrapper;
        this.parameterTermWrapper = parameterTermWrapper;
        this.alwaysShowParameter = alwaysShowParameter;
    }

    public String formalize(String msg, String... arguments) {
        try {
            if (arguments == null || arguments.length == 0) {
                if (msg == null || "".equals(msg)) {
                    return format(msg, false);
                } else {
                    return String.format(format(msg, false), msg);
                }
            } else {
                StringBuilder sbuf = new StringBuilder();
                int len = arguments.length;
                for (int i = 0; i < len; i++) {
                    sbuf.append(arguments[i]).append("=").append("{}");
                    if (i < len - 1) {
                        sbuf.append(",");
                    }
                }
                return String.format(format(msg, true), msg, sbuf.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return msg;
    }

    public String retainFormalize(String msg) {
        return String.format(format(msg, false), msg);
    }

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getParameterKey() {
        return parameterKey;
    }

    public void setParameterKey(String parameterKey) {
        this.parameterKey = parameterKey;
    }

    public TermWrapper getMessageTermWrapper() {
        return messageTermWrapper;
    }

    public void setMessageTermWrapper(TermWrapper messageTermWrapper) {
        this.messageTermWrapper = messageTermWrapper;
    }

    public TermWrapper getParameterTermWrapper() {
        return parameterTermWrapper;
    }

    public void setParameterTermWrapper(TermWrapper parameterTermWrapper) {
        this.parameterTermWrapper = parameterTermWrapper;
    }

    public boolean isAlwaysShowParameter() {
        return alwaysShowParameter;
    }

    public void setAlwaysShowParameter(boolean alwaysShowParameter) {
        this.alwaysShowParameter = alwaysShowParameter;
    }

    private String format(String msg, boolean hasArgs) {
        StringBuilder sbuf = new StringBuilder().append(this.messageKey)
                .append(COLON)
                .append(this.messageTermWrapper.getLeftTerm())
                .append(msg == null || "".equals(msg.trim()) ? "" : "%s")
                .append(this.messageTermWrapper.getRightTerm());
        if (alwaysShowParameter || hasArgs) {
            sbuf.append(COMMA)
                    .append(this.parameterKey)
                    .append(COLON)
                    .append(this.parameterTermWrapper.getLeftTerm())
                    .append(hasArgs ? "%s" : "")
                    .append(this.parameterTermWrapper.getRightTerm());
        }
        return sbuf.toString();
    }

}
