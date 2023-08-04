package com.github.kokorin.jaffree;

public class StackTraceMatcher {
    private final String messagePart;

    public StackTraceMatcher(String messagePart) {
        this.messagePart = messagePart;
    }

    public boolean matches(Object item) {
        if (!(item instanceof Throwable)) {
            return false;
        }

        Throwable throwable = (Throwable) item;
        while (throwable != null) {
            String message = throwable.getMessage();

            if (message != null && message.contains(messagePart)) {
                return true;
            }

            for (Throwable suppressed : throwable.getSuppressed()) {
                if (matches(suppressed)) {
                    return true;
                }
            }
            throwable = throwable.getCause();
        }

        return false;
    }
}
