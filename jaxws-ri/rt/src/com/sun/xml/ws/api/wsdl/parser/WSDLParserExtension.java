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

package com.sun.xml.ws.api.wsdl.parser;

import com.sun.xml.ws.api.WSService;
import com.sun.xml.ws.api.model.wsdl.*;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.wsdl.parser.RuntimeWSDLParser;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.WebServiceException;

/**
 * Extends the WSDL parsing process.
 *
 * <p>
 * This interface is implemented by components that build on top of the JAX-WS RI,
 * to participate in the WSDL parsing process that happens in the runtime.
 * This allows such components to retrieve information from WSDL extension elements,
 * and use that later to, for example, configure {@link Tube}s.
 *
 *
 *
 * <h2>How it works?</h2>
 * <p>
 * Each method on this interface denotes one extension point in WSDL
 * (the place where foreign elements/attributes can be added.) A {@link RuntimeWSDLParser}
 * starts parsing WSDL with a fixed set of {@link WSDLParserExtension}s, and
 * as it finds extension elements/attributes, it calls appropriate callback methods
 * to provide a chance for {@link WSDLParserExtension} to parse such
 * an extension element.
 *
 * <p>
 * There are two kinds of callbacks.
 *
 * <h3>Attribute callbacks</h3>
 * <p>
 * One is for attributes, which ends with the name {@code Attributes}.
 * This callback is invoked with {@link XMLStreamReader} that points
 * to the start tag of the WSDL element.
 *
 * <p>
 * The callback method can read interesting attributes on it.
 * The method must return without advancing the parser to the next token.
 *
 * <h3>Element callbacks</h3>
 * <p>
 * The other callback is for extension elements, which ends with the name
 * {@code Elements}.
 * When a callback is invoked, {@link XMLStreamReader} points to the
 * start tag of the extension element. The callback method can do
 * one of the following:
 *
 * <ol>
 *  <li>Return {@code false} without moving {@link XMLStreamReader},
 *      to indicate that the extension element isn't recognized.
 *      This allows the next {@link WSDLParserExtension} to see this
 *      extension element.
 *  <li>Parse the whole subtree rooted at the element,
 *      move the cursor to the {@link XMLStreamConstants#END_ELEMENT} state,
 *      and return {@code true}, indicating that the extension
 *      element is consumed.
 *      No other {@link WSDLParserExtension}s are notified of this extension.
 * </ol>
 *
 * <h3>Parsing in callback</h3>
 * <p>
 * For each callback, the corresponding WSDL model object is passed in,
 * so that {@link WSDLParserExtension} can relate what it's parsing
 * to the {@link WSDLModel}. Most likely, extensions can parse
 * their data into an {@link WSDLExtension}-derived classes, then
 * use {@link WSDLExtensible} interface to hook them into {@link WSDLModel}.
 *
 * <p>
 * Note that since the {@link WSDLModel} itself
 * is being built, {@link WSDLParserExtension} may not invoke any of
 * the query methods on the WSDL model. Those references are passed just so that
 * {@link WSDLParserExtension} can hold on to those references, or put
 * {@link WSDLExtensible} objects into the model, not to query it.
 *
 * <p>
 * If {@link WSDLParserExtension} needs to query {@link WSDLModel},
 * defer that processing until {@link #finished(WSDLParserExtensionContext)}, when it's
 * safe to use {@link WSDLModel} can be used safely.
 *
 * <p>
 * Also note that {@link WSDLParserExtension}s are called in no particular order.
 * This interface is not designed for having multiple {@link WSDLParserExtension}s
 * parse the same extension element.
 *
 *
 * <h2>Error Handling</h2>
 * <p>
 * For usability, {@link WSDLParserExtension}s are expected to check possible
 * errors in the extension elements that it parses. When an error is found,
 * it may throw a {@link WebServiceException} to abort the parsing of the WSDL.
 * This exception will be propagated to the user, so it should have
 * detailed error messages pointing at the problem.
 *
 * <h2>Discovery</h2>
 * <p>
 * The JAX-WS RI locates the implementation of {@link WSDLParserExtension}s
 * by using the standard service look up mechanism, in particular looking for
 * <tt>META-INF/services/com.sun.xml.ws.api.wsdl.parser.WSDLParserExtension</tt>
 *
 *
 * <h2>TODO</h2>
 * <p>
 * As it's designed today, extensions cannot access to any of the environmental
 * information before the parsing begins (such as what {@link WSService} this
 * WSDL is being parsed for, etc.) We might need to reconsider this aspect.
 * The JAX-WS team waits for feedback on this topic.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class WSDLParserExtension {
    public void start(WSDLParserExtensionContext context){
        // noop
    }
    public void serviceAttributes(WSDLService service, XMLStreamReader reader) {
        // noop
    }

    public boolean serviceElements(WSDLService service, XMLStreamReader reader) {
        return false;
    }

    public void portAttributes(WSDLPort port, XMLStreamReader reader) {
        // noop
    }

    public boolean portElements(WSDLPort port, XMLStreamReader reader) {
        return false;
    }

    public boolean portTypeOperationInput(WSDLOperation op, XMLStreamReader reader) {
        return false;
    }

    public boolean portTypeOperationOutput(WSDLOperation op, XMLStreamReader reader) {
        return false;
    }

    public boolean portTypeOperationFault(WSDLOperation op, XMLStreamReader reader) {
        return false;
    }
    
    public boolean definitionsElements(XMLStreamReader reader) {
        return false;
    }
    
    public boolean bindingElements(WSDLBoundPortType binding, XMLStreamReader reader) {
        return false;
    }
    
    public void bindingAttributes(WSDLBoundPortType binding, XMLStreamReader reader) {
    }
    
    public boolean portTypeElements(WSDLPortType portType, XMLStreamReader reader) {
        return false;
    }
    
    public void portTypeAttributes(WSDLPortType portType, XMLStreamReader reader) {
    }

    public boolean portTypeOperationElements(WSDLOperation operation, XMLStreamReader reader) {
        return false;
    }

    public void portTypeOperationAttributes(WSDLOperation operation, XMLStreamReader reader) {
    }

    public boolean bindingOperationElements(WSDLBoundOperation operation, XMLStreamReader reader) {
        return false;
    }

    public void bindingOperationAttributes(WSDLBoundOperation operation, XMLStreamReader reader) {
    }

    public boolean messageElements(WSDLMessage msg, XMLStreamReader reader) {
        return false;
    }

    public void messageAttributes(WSDLMessage msg, XMLStreamReader reader) {
    }

    public boolean portTypeOperationInputElements(WSDLInput input, XMLStreamReader reader) {
        return false;
    }

    public void portTypeOperationInputAttributes(WSDLInput input, XMLStreamReader reader) {
    }

    public boolean portTypeOperationOutputElements(WSDLOutput output, XMLStreamReader reader) {
        return false;
    }

    public void portTypeOperationOutputAttributes(WSDLOutput output, XMLStreamReader reader) {
    }

    public boolean portTypeOperationFaultElements(WSDLFault fault, XMLStreamReader reader) {
        return false;
    }

    public void portTypeOperationFaultAttributes(WSDLFault fault, XMLStreamReader reader) {
    }

    public boolean bindingOperationInputElements(WSDLBoundOperation operation, XMLStreamReader reader) {
        return false;
    }

    public void bindingOperationInputAttributes(WSDLBoundOperation operation, XMLStreamReader reader) {
    }

    public boolean bindingOperationOutputElements(WSDLBoundOperation operation, XMLStreamReader reader) {
        return false;
    }

    public void bindingOperationOutputAttributes(WSDLBoundOperation operation, XMLStreamReader reader) {
    }

    public boolean bindingOperationFaultElements(WSDLBoundFault fault, XMLStreamReader reader) {
        return false;
    }

    public void bindingOperationFaultAttributes(WSDLBoundFault fault, XMLStreamReader reader) {
    }

    // TODO: complete the rest of the callback

    /**
     * Called when the parsing of a set of WSDL documents are all done.
     * <p>
     * This is the opportunity to do any post-processing of the parsing
     * you've done.
     *
     * @param context  {@link WSDLParserExtensionContext} gives fully parsed {@link WSDLModel}.
     */
    public void finished(WSDLParserExtensionContext context) {
        // noop
    }

    public void postFinished(WSDLParserExtensionContext context) {
        // noop
    }
}
