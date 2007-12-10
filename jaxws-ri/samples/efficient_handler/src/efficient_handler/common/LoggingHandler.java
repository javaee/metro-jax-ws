/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

package efficient_handler.common;


import com.sun.xml.ws.api.handler.MessageHandlerContext;
import com.sun.xml.ws.api.handler.MessageHandler;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.streaming.XMLStreamWriterFactory;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamException;
import java.io.PrintStream;
import java.util.Set;
import java.lang.reflect.Constructor;

/*
 * This simple LoggingHandler will log the contents of incoming
 * and outgoing messages. This is implemented as a MessageHandler
 * for better performance over SOAPHandler.
 *
 * @author Rama Pulavarthi
 */
public class LoggingHandler implements MessageHandler<MessageHandlerContext> {

    // change this to redirect output if desired
    private static PrintStream out = System.out;

    public Set<QName> getHeaders() {
        return null;
    }

    public boolean handleMessage(MessageHandlerContext mhc) {
        logToSystemOut(mhc);
        return true;
    }

    public boolean handleFault(MessageHandlerContext mhc) {
        logToSystemOut(mhc);
        return true;
    }

    // nothing to clean up
    public void close(MessageContext messageContext) {
    }

    /**
     * Check the MESSAGE_OUTBOUND_PROPERTY in the context
     * to see if this is an outgoing or incoming message.
     * Writes the message to the OutputStream.
     */
    private void logToSystemOut(MessageHandlerContext mhc) {
        Boolean outboundProperty = (Boolean)
                mhc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

        if (outboundProperty.booleanValue()) {
            out.println("\nOutbound message:");
        } else {
            out.println("\nInbound message:");
        }

        Message m = mhc.getMessage().copy();
        XMLStreamWriter writer = XMLStreamWriterFactory.create(System.out);
        try {
            m.writeTo(createIndenter(writer));
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        out.println("");

    }
    /**
     * Wraps {@link XMLStreamWriter} by an indentation engine if possible.
     *
     * <p>
     * We can do this only when we have <tt>stax-utils.jar</tt> in the classpath.
     */
    private XMLStreamWriter createIndenter(XMLStreamWriter writer) {
        try {
            Class clazz = getClass().getClassLoader().loadClass("javanet.staxutils.IndentingXMLStreamWriter");
            Constructor c = clazz.getConstructor(XMLStreamWriter.class);
            writer = (XMLStreamWriter)c.newInstance(writer);
        } catch (Exception e) {
            // if stax-utils.jar is not in the classpath, this will fail
            // so, we'll just have to do without indentation
            out.println("WARNING: put stax-utils.jar to the classpath to indent the dump output");

        }
        return writer;
    }
}
