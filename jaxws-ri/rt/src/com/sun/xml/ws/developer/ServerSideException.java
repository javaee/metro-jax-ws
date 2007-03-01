package com.sun.xml.ws.developer;

/**
 * Represents the exception that has occurred on the server side.
 *
 * <p>
 * When an exception occurs on the server, JAX-WS RI sends the stack
 * trace of that exception to the client. On the client side,
 * instances of this class are used to represent such stack trace.
 *
 * @author Kohsuke Kawaguchi
 * @since 2.1
 */
public class ServerSideException extends Exception {
    private final String className;

    public ServerSideException(String className, String message) {
        super(message);
        this.className = className;
    }

    public String toString() {
        String s = className;
        String message = getLocalizedMessage();
        return (message != null) ? (s + ": " + message) : s;
    }
}
