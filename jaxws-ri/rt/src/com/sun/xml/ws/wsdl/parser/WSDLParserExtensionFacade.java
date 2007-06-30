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

package com.sun.xml.ws.wsdl.parser;

import com.sun.xml.ws.api.wsdl.parser.WSDLParserExtension;
import com.sun.xml.ws.api.wsdl.parser.WSDLParserExtensionContext;
import com.sun.xml.ws.api.model.wsdl.*;
import com.sun.xml.ws.streaming.XMLStreamReaderUtil;
import com.sun.xml.ws.model.wsdl.WSDLPortImpl;
import com.sun.xml.ws.model.wsdl.WSDLBoundPortTypeImpl;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.Location;

import org.xml.sax.Locator;
import org.xml.sax.helpers.LocatorImpl;

/**
 * {@link WSDLParserExtension} that delegates to
 * multiple {@link WSDLParserExtension}s.
 *
 * <p>
 * This simplifies {@link RuntimeWSDLParser} since it now
 * only needs to work with one {@link WSDLParserExtension}.
 *
 * <p>
 * This class is guaranteed to return true from
 * all the extension callback methods.
 *
 * @author Kohsuke Kawaguchi
 */
final class WSDLParserExtensionFacade extends WSDLParserExtension {
    private final WSDLParserExtension[] extensions;

    WSDLParserExtensionFacade(WSDLParserExtension... extensions) {
        assert extensions!=null;
        this.extensions = extensions;
    }

    public void start(WSDLParserExtensionContext context) {
        for (WSDLParserExtension e : extensions) {
            e.start(context);
        }
    }

    public boolean serviceElements(WSDLService service, XMLStreamReader reader) {
        for (WSDLParserExtension e : extensions) {
            if(e.serviceElements(service,reader))
                return true;
        }
        XMLStreamReaderUtil.skipElement(reader);
        return true;
    }

    public void serviceAttributes(WSDLService service, XMLStreamReader reader) {
        for (WSDLParserExtension e : extensions)
            e.serviceAttributes(service,reader);
    }

    public boolean portElements(WSDLPort port, XMLStreamReader reader) {
        for (WSDLParserExtension e : extensions) {
            if(e.portElements(port,reader))
                return true;
        }
        //extension is not understood by any WSDlParserExtension
        //Check if it must be understood.
        if(isRequiredExtension(reader)) {
            ((WSDLPortImpl)port).addNotUnderstoodExtension(reader.getName(),getLocator(reader));
        }
        XMLStreamReaderUtil.skipElement(reader);
        return true;
    }

    public boolean portTypeOperationInput(WSDLOperation op, XMLStreamReader reader) {
        for (WSDLParserExtension e : extensions)
            e.portTypeOperationInput(op,reader);

        return false;
    }

    public boolean portTypeOperationOutput(WSDLOperation op, XMLStreamReader reader) {
        for (WSDLParserExtension e : extensions)
            e.portTypeOperationOutput(op,reader);

        return false;
    }

    public boolean portTypeOperationFault(WSDLOperation op, XMLStreamReader reader) {
        for (WSDLParserExtension e : extensions)
            e.portTypeOperationFault(op,reader);

        return false;
    }

    public void portAttributes(WSDLPort port, XMLStreamReader reader) {
        for (WSDLParserExtension e : extensions)
            e.portAttributes(port,reader);
    }

    public boolean definitionsElements(XMLStreamReader reader){
        for (WSDLParserExtension e : extensions) {
            if (e.definitionsElements(reader)) {
                return true;
            }
        }
        XMLStreamReaderUtil.skipElement(reader);
        return true;
    }

    public boolean bindingElements(WSDLBoundPortType binding, XMLStreamReader reader){
        for (WSDLParserExtension e : extensions) {
            if (e.bindingElements(binding, reader)) {
                return true;
            }
        }
        //extension is not understood by any WSDlParserExtension
        //Check if it must be understood.
        if (isRequiredExtension(reader)) {
            ((WSDLBoundPortTypeImpl) binding).addNotUnderstoodExtension(
                    reader.getName(), getLocator(reader));
        }
        XMLStreamReaderUtil.skipElement(reader);
        return true;
    }

    public void bindingAttributes(WSDLBoundPortType binding, XMLStreamReader reader){
        for (WSDLParserExtension e : extensions) {
            e.bindingAttributes(binding, reader);
        }
    }

    public boolean portTypeElements(WSDLPortType portType, XMLStreamReader reader) {
        for (WSDLParserExtension e : extensions) {
            if (e.portTypeElements(portType, reader)) {
                return true;
            }
        }
        XMLStreamReaderUtil.skipElement(reader);
        return true;
    }

    public void portTypeAttributes(WSDLPortType portType, XMLStreamReader reader) {
        for (WSDLParserExtension e : extensions) {
            e.portTypeAttributes(portType, reader);
        }
    }

    public boolean portTypeOperationElements(WSDLOperation operation, XMLStreamReader reader) {
        for (WSDLParserExtension e : extensions) {
            if (e.portTypeOperationElements(operation, reader)) {
                return true;
            }
        }
        XMLStreamReaderUtil.skipElement(reader);
        return true;
    }

    public void portTypeOperationAttributes(WSDLOperation operation, XMLStreamReader reader) {
        for (WSDLParserExtension e : extensions) {
            e.portTypeOperationAttributes(operation, reader);
        }
    }

    public boolean bindingOperationElements(WSDLBoundOperation operation, XMLStreamReader reader) {
        for (WSDLParserExtension e : extensions) {
            if (e.bindingOperationElements(operation, reader)) {
                return true;
            }
        }
        XMLStreamReaderUtil.skipElement(reader);
        return true;
    }

    public void bindingOperationAttributes(WSDLBoundOperation operation, XMLStreamReader reader) {
        for (WSDLParserExtension e : extensions) {
            e.bindingOperationAttributes(operation, reader);
        }
    }

    public boolean messageElements(WSDLMessage msg, XMLStreamReader reader) {
        for (WSDLParserExtension e : extensions) {
            if (e.messageElements(msg, reader)) {
                return true;
            }
        }
        XMLStreamReaderUtil.skipElement(reader);
        return true;
    }

    public void messageAttributes(WSDLMessage msg, XMLStreamReader reader) {
        for (WSDLParserExtension e : extensions) {
            e.messageAttributes(msg, reader);
        }
    }

    public boolean portTypeOperationInputElements(WSDLInput input, XMLStreamReader reader) {
        for (WSDLParserExtension e : extensions) {
            if (e.portTypeOperationInputElements(input, reader)) {
                return true;
            }
        }
        XMLStreamReaderUtil.skipElement(reader);
        return true;
    }

    public void portTypeOperationInputAttributes(WSDLInput input, XMLStreamReader reader) {
        for (WSDLParserExtension e : extensions) {
            e.portTypeOperationInputAttributes(input, reader);
        }
    }

    public boolean portTypeOperationOutputElements(WSDLOutput output, XMLStreamReader reader) {
        for (WSDLParserExtension e : extensions) {
            if (e.portTypeOperationOutputElements(output, reader)) {
                return true;
            }
        }
        XMLStreamReaderUtil.skipElement(reader);
        return true;
    }

    public void portTypeOperationOutputAttributes(WSDLOutput output, XMLStreamReader reader) {
        for (WSDLParserExtension e : extensions) {
            e.portTypeOperationOutputAttributes(output, reader);
        }
    }

    public boolean portTypeOperationFaultElements(WSDLFault fault, XMLStreamReader reader) {
        for (WSDLParserExtension e : extensions) {
            if (e.portTypeOperationFaultElements(fault, reader)) {
                return true;
            }
        }
        XMLStreamReaderUtil.skipElement(reader);
        return true;
    }

    public void portTypeOperationFaultAttributes(WSDLFault fault, XMLStreamReader reader) {
        for (WSDLParserExtension e : extensions) {
            e.portTypeOperationFaultAttributes(fault, reader);
        }
    }

    public boolean bindingOperationInputElements(WSDLBoundOperation operation, XMLStreamReader reader) {
        for (WSDLParserExtension e : extensions) {
            if (e.bindingOperationInputElements(operation, reader)) {
                return true;
            }
        }
        XMLStreamReaderUtil.skipElement(reader);
        return true;
    }

    public void bindingOperationInputAttributes(WSDLBoundOperation operation, XMLStreamReader reader) {
        for (WSDLParserExtension e : extensions) {
            e.bindingOperationInputAttributes(operation, reader);
        }
    }

    public boolean bindingOperationOutputElements(WSDLBoundOperation operation, XMLStreamReader reader) {
        for (WSDLParserExtension e : extensions) {
            if (e.bindingOperationOutputElements(operation, reader)) {
                return true;
            }
        }
        XMLStreamReaderUtil.skipElement(reader);
        return true;
    }

    public void bindingOperationOutputAttributes(WSDLBoundOperation operation, XMLStreamReader reader) {
        for (WSDLParserExtension e : extensions) {
            e.bindingOperationOutputAttributes(operation, reader);
        }
    }

    public boolean bindingOperationFaultElements(WSDLBoundFault fault, XMLStreamReader reader) {
        for (WSDLParserExtension e : extensions) {
            if (e.bindingOperationFaultElements(fault, reader)) {
                return true;
            }
        }
        XMLStreamReaderUtil.skipElement(reader);
        return true;
    }

    public void bindingOperationFaultAttributes(WSDLBoundFault fault, XMLStreamReader reader) {
        for (WSDLParserExtension e : extensions) {
            e.bindingOperationFaultAttributes(fault, reader);
        }
    }

    public void finished(WSDLParserExtensionContext context) {
        for (WSDLParserExtension e : extensions) {
            e.finished(context);
        }
    }

    public void postFinished(WSDLParserExtensionContext context) {
        for (WSDLParserExtension e : extensions) {
            e.postFinished(context);
        }
    }
    /**
     *
     * @param reader
     * @return If the element has wsdl:required attribute set to true
     */

    private boolean isRequiredExtension(XMLStreamReader reader) {
        String required = reader.getAttributeValue(WSDLConstants.NS_WSDL, "required");
        if(required != null)
            return Boolean.parseBoolean(required);
        return false;
    }

    private Locator getLocator(XMLStreamReader reader) {
        Location location = reader.getLocation();
            LocatorImpl loc = new LocatorImpl();
            loc.setSystemId(location.getSystemId());
            loc.setLineNumber(location.getLineNumber());
        return loc;
    }

}
