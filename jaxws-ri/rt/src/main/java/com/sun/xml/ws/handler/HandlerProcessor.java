/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.xml.ws.handler;

import com.sun.xml.ws.api.WSBinding;

import javax.xml.ws.ProtocolException;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author WS Development Team
 */
abstract class HandlerProcessor<C extends MessageUpdatableContext> {

    boolean isClient;
    static final Logger logger = Logger.getLogger(
            com.sun.xml.ws.util.Constants.LoggingDomain + ".handler");

    // need request or response for Handle interface
    public enum RequestOrResponse {
        REQUEST, RESPONSE }

    public enum Direction {
        OUTBOUND, INBOUND }

    private List<? extends Handler> handlers; // may be logical/soap mixed

    WSBinding binding;
    private int index = -1;
    private HandlerTube owner;

    /**
     * The handlers that are passed in will be sorted into
     * logical and soap handlers. During this sorting, the
     * understood headers are also obtained from any soap
     * handlers.
     *
     * @param chain A list of handler objects, which can
     *              be protocol or logical handlers.
     */
    protected HandlerProcessor(HandlerTube owner, WSBinding binding, List<? extends Handler> chain) {
        this.owner = owner;
        if (chain == null) { // should only happen in testing
            chain = new ArrayList<Handler>();
        }
        handlers = chain;
        this.binding = binding;
    }

    /**
     * Gives index of the handler in the chain to know what handlers in the chain
     * are invoked
     */
    int getIndex() {
        return index;
    }

    /**
     * This is called when a handler returns false or throws a RuntimeException
     */
    void setIndex(int i) {
        index = i;
    }

    /**
     * TODO: Just putting thoughts,
     * Current contract: This is Called during Request Processing.
     * return true, if all handlers in the chain return true
     * Current Pipe can call nextPipe.process();
     * return false, One of the handlers has returned false or thrown a
     * RuntimeException. Remedy Actions taken:
     * 1) In this case, The processor will setIndex()to track what
     * handlers are invoked until that point.
     * 2) Previously invoked handlers are again invoked (handleMessage()
     * or handleFault()) to take remedy action.
     * CurrentPipe should NOT call nextPipe.process()
     * While closing handlers, check getIndex() to get the invoked
     * handlers.
     * @throws RuntimeException this happens when a RuntimeException occurs during
     * handleMessage during Request processing or
     * during remedy action 2)
     * CurrentPipe should NOT call nextPipe.process() and throw the
     * exception to the previous Pipe
     * While closing handlers, check getIndex() to get the invoked
     * handlers.
     */
    public boolean callHandlersRequest(Direction direction,
                                       C context,
                                       boolean responseExpected) {
        setDirection(direction, context);
        boolean result;
        // call handlers
        try {
            if (direction == Direction.OUTBOUND) {
                result = callHandleMessage(context, 0, handlers.size() - 1);
            } else {
                result = callHandleMessage(context, handlers.size() - 1, 0);
            }
        } catch (ProtocolException pe) {
            logger.log(Level.FINER, "exception in handler chain", pe);
            if (responseExpected) {
                //insert fault message if its not a fault message
                insertFaultMessage(context, pe);
                // reverse direction
                reverseDirection(direction, context);
                //Set handleFault so that cousinTube is aware of fault
                setHandleFaultProperty();
                // call handle fault                
                if (direction == Direction.OUTBOUND) {
                    callHandleFault(context, getIndex() - 1, 0);
                } else {
                    callHandleFault(context, getIndex() + 1, handlers.size() - 1);
                }
                return false;
            }
            throw pe;
        } catch (RuntimeException re) {
            logger.log(Level.FINER, "exception in handler chain", re);
            throw re;
        }

        if (!result) {
            if (responseExpected) {
                // reverse direction
                reverseDirection(direction, context);
                // call handle message
                if (direction == Direction.OUTBOUND) {
                    callHandleMessageReverse(context, getIndex() - 1, 0);
                } else {
                    callHandleMessageReverse(context, getIndex() + 1, handlers.size() - 1);
                }
            } else {
                // Set handleFalse so that cousinTube is aware of false processing
                // Oneway, dispatch the message
                // cousinTube should n't call handleMessage() anymore.
                setHandleFalseProperty();
            }
            return false;
        }

        return result;
    }


    /**
     * TODO: Just putting thoughts,
     * Current contract: This is Called during Response Processing.
     * Runs all handlers until handle returns false or throws a RuntimeException
     * CurrentPipe should close all the handlers in the chain.
     * throw RuntimeException, this happens when a RuntimeException occurs during
     * normal Response processing or remedy action 2) taken
     * during callHandlersRequest().
     * CurrentPipe should close all the handlers in the chain.     * 
     */
    public void callHandlersResponse(Direction direction,
                                     C context, boolean isFault) {
        setDirection(direction, context);
        try {
            if (isFault) {
                // call handleFault on handlers
                if (direction == Direction.OUTBOUND) {
                    callHandleFault(context, 0, handlers.size() - 1);
                } else {
                    callHandleFault(context, handlers.size() - 1, 0);
                }
            } else {
                // call handleMessage on handlers                
                if (direction == Direction.OUTBOUND) {
                    callHandleMessageReverse(context, 0, handlers.size() - 1);
                } else {
                    callHandleMessageReverse(context, handlers.size() - 1, 0);
                }
            }
        } catch (RuntimeException re) {
            logger.log(Level.FINER, "exception in handler chain", re);
            throw re;
        }
    }


    /**
     * Reverses the Message Direction.
     * MessageContext.MESSAGE_OUTBOUND_PROPERTY is changed.
     */
    private void reverseDirection(Direction origDirection, C context) {
        if (origDirection == Direction.OUTBOUND) {
            context.put(MessageContext.MESSAGE_OUTBOUND_PROPERTY, false);
        } else {
            context.put(MessageContext.MESSAGE_OUTBOUND_PROPERTY, true);
        }
    }

    /**
     * Sets the Message Direction.
     * MessageContext.MESSAGE_OUTBOUND_PROPERTY is changed.
     */
    private void setDirection(Direction direction, C context) {
        if (direction == Direction.OUTBOUND) {
            context.put(MessageContext.MESSAGE_OUTBOUND_PROPERTY, true);
        } else {
            context.put(MessageContext.MESSAGE_OUTBOUND_PROPERTY, false);
        }
    }

    /**
     * When this property is set HandlerPipes can call handleFault() on the
     * message
     */
    private void setHandleFaultProperty() {
        owner.setHandleFault();
    }

    /**
     * When this property is set HandlerPipes will not call
     * handleMessage() during Response processing.
     */
    private void setHandleFalseProperty() {
        owner.setHandleFalse();
    }

    /**
     * When a ProtocolException is thrown, this is called.
     * If it's XML/HTTP Binding, clear the the message
     * If its SOAP/HTTP Binding, put right SOAP Fault version
     */
    abstract void insertFaultMessage(C context,
                                     ProtocolException exception);

    /*
    * Calls handleMessage on the handlers. Indices are
    * inclusive. Exceptions get passed up the chain, and an
    * exception or return of 'false' ends processing.
    */
    private boolean callHandleMessage(C context, int start, int end) {
        /* Do we need this check?
        if (handlers.isEmpty() ||
                start == -1 ||
                start == handlers.size()) {
            return false;
        }
         */
        int i = start;
        try {
            if (start > end) {
                while (i >= end) {
                    if (!handlers.get(i).handleMessage(context)) {
                        setIndex(i);
                        return false;
                    }
                    i--;
                }
            } else {
                while (i <= end) {
                    if (!handlers.get(i).handleMessage(context)) {
                        setIndex(i);
                        return false;
                    }
                    i++;
                }
            }
        } catch (RuntimeException e) {
            setIndex(i);
            throw e;
        }
        return true;
    }

    /*
    * Calls handleMessage on the handlers. Indices are
    * inclusive. Exceptions get passed up the chain, and an
    * exception (or)
    * return of 'false' calls addHandleFalseProperty(context) and
    * ends processing.
    * setIndex() is not called.
    *
    */
    private boolean callHandleMessageReverse(C context, int start, int end) {

        if (handlers.isEmpty() ||
                start == -1 ||
                start == handlers.size()) {
            return false;
        }

        int i = start;

        if (start > end) {
            while (i >= end) {
                if (!handlers.get(i).handleMessage(context)) {
                    // Set handleFalse so that cousinTube is aware of false processing
                    setHandleFalseProperty();
                    return false;
                }
                i--;
            }
        } else {
            while (i <= end) {
                if (!handlers.get(i).handleMessage(context)) {
                    // Set handleFalse so that cousinTube is aware of false processing
                    setHandleFalseProperty();
                    return false;
                }
                i++;
            }
        }
        return true;
    }

    /*
    * Calls handleFault on the handlers. Indices are
    * inclusive. Exceptions get passed up the chain, and an
    * exception or return of 'false' ends processing.
    */

    private boolean callHandleFault(C context, int start, int end) {

        if (handlers.isEmpty() ||
                start == -1 ||
                start == handlers.size()) {
            return false;
        }

        int i = start;
        if (start > end) {
            try {
                while (i >= end) {
                    if (!handlers.get(i).handleFault(context)) {
                        return false;
                    }
                    i--;
                }
            } catch (RuntimeException re) {
                logger.log(Level.FINER,
                        "exception in handler chain", re);
                throw re;
            }
        } else {
            try {
                while (i <= end) {
                    if (!handlers.get(i).handleFault(context)) {
                        return false;
                    }
                    i++;
                }
            } catch (RuntimeException re) {
                logger.log(Level.FINER,
                        "exception in handler chain", re);
                throw re;
            }
        }
        return true;
    }

    /**
     * Calls close on the handlers from the starting
     * index through the ending index (inclusive). Made indices
     * inclusive to allow both directions more easily.
     */
    void closeHandlers(MessageContext context, int start, int end) {
        if (handlers.isEmpty() ||
                start == -1) {
            return;
        }
        if (start > end) {
            for (int i = start; i >= end; i--) {
                try {
                    handlers.get(i).close(context);
                } catch (RuntimeException re) {
                    logger.log(Level.INFO,
                            "Exception ignored during close", re);
                }
            }
        } else {
            for (int i = start; i <= end; i++) {
                try {
                    handlers.get(i).close(context);
                } catch (RuntimeException re) {
                    logger.log(Level.INFO,
                            "Exception ignored during close", re);
                }
            }
        }
    }
}
