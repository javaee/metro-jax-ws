package com.sun.xml.ws.api.message;

import com.sun.xml.ws.util.exception.JAXWSExceptionBase;
import com.sun.xml.ws.protocol.soap.VersionMismatchException;

/**
 * This class represents an Exception that needs to be marshalled
 * with a specific protocol wire format. For example, the SOAP's
 * VersionMismatchFault needs to be written with a correct fault code.
 * In that case, decoder could throw {@link VersionMismatchException},
 * and the correspoinding fault {@link Message} from {@link ExceptionHasMessage::getFaultMessage}
 * is sent on the wire.
 *
 * @author Jitendra Kotamraju
 */
public abstract class ExceptionHasMessage extends JAXWSExceptionBase {

    public ExceptionHasMessage(String key, Object... args) {
        super(key, args);
    }

    /**
     * Returns the exception into a fault Message
     *
     * @return Message for this exception
     */
    public abstract Message getFaultMessage();
}
