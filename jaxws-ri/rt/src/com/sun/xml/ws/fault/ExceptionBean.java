/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package com.sun.xml.ws.fault;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import com.sun.xml.ws.developer.ServerSideException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * JAXB-bound bean that captures the exception and its call stack.
 *
 * <p>
 * This is used to capture the stack trace of the server side error and
 * send that over to the client.
 *
 * @author Kohsuke Kawaguchi
 */
@XmlRootElement(namespace=ExceptionBean.NS,name=ExceptionBean.LOCAL_NAME)
final class ExceptionBean {
    /**
     * Converts the given {@link Throwable} into an XML representation
     * and put that as a DOM tree under the given node.
     */
    public static void marshal( Throwable t, Node parent ) throws JAXBException {
        Marshaller m = JAXB_CONTEXT.createMarshaller();
        m.setProperty("com.sun.xml.bind.namespacePrefixMapper",nsp);
        m.marshal(new ExceptionBean(t), parent );
    }

    /**
     * Does the reverse operation of {@link #marshal(Throwable, Node)}. Constructs an
     * {@link Exception} object from the XML.
     */
    public static ServerSideException unmarshal( Node xml ) throws JAXBException {
        ExceptionBean e = (ExceptionBean) JAXB_CONTEXT.createUnmarshaller().unmarshal(xml);
        return e.toException();
    }

    @XmlAttribute(name="class")
    public String className;
    @XmlElement
    public String message;
    @XmlElementWrapper(namespace=NS,name="stackTrace")
    @XmlElement(namespace=NS,name="frame")
    public List<StackFrame> stackTrace = new ArrayList<StackFrame>();
    @XmlElement(namespace=NS,name="cause")
    public ExceptionBean cause;

    // so that people noticed this fragment can turn it off
    @XmlAttribute
    public String note = "To disable this feature, set "+SOAPFaultBuilder.CAPTURE_STACK_TRACE_PROPERTY+" system property to false";

    ExceptionBean() {// for JAXB
    }

    /**
     * Creates an {@link ExceptionBean} tree that represents the given {@link Throwable}.
     */
    private ExceptionBean(Throwable t) {
        this.className = t.getClass().getName();
        this.message = t.getMessage();

        for (StackTraceElement f : t.getStackTrace()) {
            stackTrace.add(new StackFrame(f));
        }

        Throwable cause = t.getCause();
        if(t!=cause && cause!=null)
            this.cause = new ExceptionBean(cause);
    }

    private ServerSideException toException() {
        ServerSideException e = new ServerSideException(className,message);
        if(stackTrace!=null) {
            StackTraceElement[] ste = new StackTraceElement[stackTrace.size()];
            for( int i=0; i<stackTrace.size(); i++ )
                ste[i] = stackTrace.get(i).toStackTraceElement();
            e.setStackTrace(ste);
        }
        if(cause!=null)
            e.initCause(cause.toException());
        return e;
    }

    /**
     * Captures one stack frame.
     */
    static final class StackFrame {
        @XmlAttribute(name="class")
        public String declaringClass;
        @XmlAttribute(name="method")
        public String methodName;
        @XmlAttribute(name="file")
        public String fileName;
        @XmlAttribute(name="line")
        public String lineNumber;

        StackFrame() {// for JAXB
        }

        public StackFrame(StackTraceElement ste) {
            this.declaringClass = ste.getClassName();
            this.methodName = ste.getMethodName();
            this.fileName = ste.getFileName();
            this.lineNumber = box(ste.getLineNumber());
        }

        private String box(int i) {
            if(i>=0) return String.valueOf(i);
            if(i==-2)   return "native";
            return "unknown";
        }

        private int unbox(String v) {
            try {
                return Integer.parseInt(v);
            } catch (NumberFormatException e) {
                if(v.equals("native"))  return -2;
                return -1;
            }
        }

        private StackTraceElement toStackTraceElement() {
            return new StackTraceElement(declaringClass,methodName,fileName,unbox(lineNumber));
        }
    }

    /**
     * Checks if the given element is the XML representation of {@link ExceptionBean}.
     */
    public static boolean isStackTraceXml(Element n) {
        return n.getLocalName().equals(LOCAL_NAME) && n.getNamespaceURI().equals(NS);
    }

    private static final JAXBContext JAXB_CONTEXT;

    /**
     * Namespace URI.
     */
    /*package*/ static final String NS = "http://jax-ws.dev.java.net/";

    /*package*/ static final String LOCAL_NAME = "exception";

    static {
        try {
            JAXB_CONTEXT = JAXBContext.newInstance(ExceptionBean.class);
        } catch (JAXBException e) {
            // this must be a bug in our code
            throw new Error(e);
        }
    }

    private static final NamespacePrefixMapper nsp = new NamespacePrefixMapper() {
        public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
            if(namespaceUri.equals(NS)) return "";
            return suggestion;
        }
    };
}
