package com.sun.xml.ws.server.sei;

import com.sun.xml.ws.api.message.Message;

/**
 * {@link Exception} that demands a specific fault message to be sent back.
 *
 * TODO: think about a way to generalize it, as it seems to be useful
 * in other places.
 *
 * @author Kohsuke Kawaguchi
 */
final class DispatchException extends Exception {
    final Message fault;

    public DispatchException(Message fault) {
        this.fault = fault;
    }
}
