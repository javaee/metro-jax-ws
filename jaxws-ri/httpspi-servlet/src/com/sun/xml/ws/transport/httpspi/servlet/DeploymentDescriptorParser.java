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

package com.sun.xml.ws.transport.httpspi.servlet;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import static javax.xml.stream.XMLStreamConstants.*;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.soap.MTOMFeature;
import javax.xml.ws.soap.SOAPBinding;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Parses {@code sun-jaxws.xml}
 *
 * @author Jitendra Kotamraju
 */
public class DeploymentDescriptorParser<A> {
    private final ClassLoader classLoader;
    private final ResourceLoader loader;
    private final AdapterFactory<A> adapterFactory;
    private static final XMLInputFactory xif = XMLInputFactory.newInstance();

    /**
     * Endpoint names that are declared.
     * Used to catch double definitions.
     */
    private final Set<String> names = new HashSet<String>();

    /**
     * WSDL/schema documents collected from /WEB-INF/wsdl. Keyed by the system ID.
     */
    private final List<URL> docs = new ArrayList<URL>();

    /**
     *
     * @param cl
     *      Used to load service implementations.
     * @param loader
     *      Used to locate resources, in particular WSDL.
     * @param adapterFactory
     *      Creates {@link EndpointAdapter} (or its derived class.)
     */
    public DeploymentDescriptorParser(ClassLoader cl, ResourceLoader loader, AdapterFactory<A> adapterFactory) throws IOException {
        classLoader = cl;
        this.loader = loader;
        this.adapterFactory = adapterFactory;

        collectDocs("/WEB-INF/wsdl/");
        logger.fine("war metadata="+docs);
    }

    /**
     * Parses the {@code sun-jaxws.xml} file and configures
     * a set of {@link EndpointAdapter}s.
     */
    public List<A> parse(String systemId, InputStream is) {
        XMLStreamReader reader = null;
        try {
            synchronized(xif) {
                reader = xif.createXMLStreamReader(systemId, is);
            }
            nextElementContent(reader);
            return parseAdapters(reader);
        } catch(IOException e) {
            throw new WebServiceException(e);
        } catch(XMLStreamException xe) {
            throw new WebServiceException(xe);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (XMLStreamException e) {
                    // ignore
                }
            }
            try {
                is.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    private static int nextElementContent(XMLStreamReader reader) throws XMLStreamException {
        do {
            int state = reader.next();
            if (state == START_ELEMENT || state == END_ELEMENT || state == END_DOCUMENT) {
                return state;
            }
        } while(true);
    }

    /**
     * Parses the {@code sun-jaxws.xml} file and configures
     * a set of {@link EndpointAdapter}s.
     */
    public List<A> parse(File f) throws IOException {
        FileInputStream in = new FileInputStream(f);
        try {
            return parse(f.getPath(), in);
        } finally {
            in.close();
        }
    }

    /**
     * Get all the WSDL & schema documents recursively.
     */
    private void collectDocs(String dirPath) throws IOException {
        Set<String> paths = loader.getResourcePaths(dirPath);
        if (paths != null) {
            for (String path : paths) {
                if (path.endsWith("/")) {
                    if(path.endsWith("/CVS/") || path.endsWith("/.svn/"))
                        continue;
                    collectDocs(path);
                } else {
                    URL res = loader.getResource(path);
                    docs.add(res);
                }
            }
        }
    }

    private List<A> parseAdapters(XMLStreamReader reader) throws IOException, XMLStreamException {
        if (!reader.getName().equals(QNAME_ENDPOINTS)) {
            failWithFullName("runtime.parser.invalidElement", reader);
        }

        List<A> adapters = new ArrayList<A>();

        String version = getMandatoryNonEmptyAttribute(reader, ATTR_VERSION);
        if (!version.equals(ATTRVALUE_VERSION_1_0)) {
            failWithLocalName("sun-jaxws.xml's version attribut runtime.parser.invalidVersionNumber",
                    reader, version);
        }

        while (nextElementContent(reader) != XMLStreamConstants.END_ELEMENT) {
            if (reader.getName().equals(QNAME_ENDPOINT)) {

                String name = getMandatoryNonEmptyAttribute(reader, ATTR_NAME);
                if (!names.add(name)) {
                    logger.warning("sun-jaxws.xml contains duplicate endpoint names. "+
                            "The first duplicate name is = "+name);
                }

                String implementationName =
                    getMandatoryNonEmptyAttribute(reader, ATTR_IMPLEMENTATION);
                Class<?> implementorClass = getImplementorClass(implementationName, reader);

                QName serviceName = getQNameAttribute(reader, ATTR_SERVICE);
                QName portName = getQNameAttribute(reader, ATTR_PORT);

                //get enable-mtom attribute value
                String enable_mtom = getAttribute(reader, ATTR_ENABLE_MTOM);
                String mtomThreshold = getAttribute(reader, ATTR_MTOM_THRESHOLD_VALUE);
                String bindingId = getAttribute(reader, ATTR_BINDING);
                if (bindingId != null) {
                    // Convert short-form tokens to API's binding ids
                    bindingId = getBindingIdForToken(bindingId);
                }
                String urlPattern =
                        getMandatoryNonEmptyAttribute(reader, ATTR_URL_PATTERN);

                //boolean handlersSetInDD = setHandlersAndRoles(binding, reader, serviceName, portName);

                nextElementContent(reader);
                ensureNoContent(reader);

                List<Source> metadata = new ArrayList<Source>();
                for(URL url : docs) {
                    Source source = new StreamSource(url.openStream(), url.toExternalForm());
                    metadata.add(source);
                }

                adapters.add(adapterFactory.createAdapter(name, urlPattern,
                        implementorClass, serviceName, portName, bindingId,
                        metadata));

            } else {
                failWithLocalName("runtime.parser.invalidElement", reader);
            }
        }
        return adapters;
    }

    /*
     * @param ddBindingId
     *      binding id explicitlyspecified in the DeploymentDescriptor or parameter
     * @param implClass
     *      Endpoint Implementation class
     * @param mtomEnabled
     *      represents mtom-enabled attribute in DD
     * @param mtomThreshold
     *      threshold value specified in DD
     * @return
     *      is returned with only MTOMFeature set resolving the various precendece rules
     *
    private static WSBinding createBinding(String ddBindingId,Class implClass,
                                          String mtomEnabled, String mtomThreshold) {
        // Features specified through DD
        WebServiceFeatureList features;

        MTOMFeature mtomfeature = null;
        if (mtomEnabled != null) {
            if (mtomThreshold != null)
                mtomfeature = new MTOMFeature(Boolean.valueOf(mtomEnabled),
                        Integer.valueOf(mtomThreshold));
            else
                mtomfeature = new MTOMFeature(Boolean.valueOf(mtomEnabled));
        }


        BindingID bindingID;
        if (ddBindingId != null) {
            bindingID = BindingID.parse(ddBindingId);
            features = bindingID.createBuiltinFeatureList();

            if(checkMtomConflict(features.get(MTOMFeature.class),mtomfeature)) {
                throw new ServerRtException(ServerMessages.DD_MTOM_CONFLICT(ddBindingId, mtomEnabled));
            }
        } else {
            bindingID = BindingID.parse(implClass);
            // Since bindingID is coming from implclass,
            // mtom through Feature annotation or DD takes precendece

            features = new WebServiceFeatureList();
            if(mtomfeature != null)
                features.add(mtomfeature); // this wins over MTOM setting in bindingID
            features.addAll(bindingID.createBuiltinFeatureList());
        }

        return bindingID.createBinding(features.toArray());
    }
    */

    private static boolean checkMtomConflict(MTOMFeature lhs, MTOMFeature rhs) {
        if(lhs==null || rhs==null)  return false;
        return lhs.isEnabled() ^ rhs.isEnabled();
    }

    /**
     * JSR-109 defines short-form tokens for standard binding Ids. These are
     * used only in DD. So stand alone deployment descirptor should also honor
     * these tokens. This method converts the tokens to API's standard
     * binding ids
     *
     * @param lexical binding attribute value from DD. Always not null
     *
     * @return returns corresponding API's binding ID or the same lexical
     */
    private static String getBindingIdForToken(String lexical) {
        if (lexical.equals("##SOAP11_HTTP")) {
            return SOAPBinding.SOAP11HTTP_BINDING;
        } else if (lexical.equals("##SOAP11_HTTP_MTOM")) {
            return SOAPBinding.SOAP11HTTP_MTOM_BINDING;
        } else if (lexical.equals("##SOAP12_HTTP")) {
            return SOAPBinding.SOAP12HTTP_BINDING;
        } else if (lexical.equals("##SOAP12_HTTP_MTOM")) {
            return SOAPBinding.SOAP12HTTP_MTOM_BINDING;
        } else if (lexical.equals("##XML_HTTP")) {
            return HTTPBinding.HTTP_BINDING;
        }
        return lexical;
    }

    /**
     * Creates a new "Adapter".
     *
     * <P>
     * Normally 'A' would be {@link EndpointAdapter} or some derived class.
     * But the parser doesn't require that to be of any particular type.
     */
    public static interface AdapterFactory<A> {
        A createAdapter(String name, String urlPattern, Class implType,
            QName serviceName, QName portName, String bindingId,
            List<Source> metadata, WebServiceFeature... features);
    }

    /*
     * Checks the deployment descriptor or {@link @WebServiceProvider} annotation
     * to see if it points to any WSDL. If so, returns the {@link SDDocumentSource}.
     *
     * @return
     *      The pointed WSDL, if any. Otherwise null.
     *
    private SDDocumentSource getPrimaryWSDL(XMLStreamReader xsr, Class<?> implementorClass) {

        String wsdlFile = getAttribute(xsr, ATTR_WSDL);
        if (wsdlFile == null) {
            wsdlFile = EndpointFactory.getWsdlLocation(implementorClass);
        }

        if (wsdlFile!=null) {
            if (!wsdlFile.startsWith(JAXWS_WSDL_DD_DIR)) {
                logger.warning("Ignoring wrong wsdl="+wsdlFile+". It should start with "
                        +JAXWS_WSDL_DD_DIR
                        +". Going to generate and publish a new WSDL.");
                return null;
            }

            URL wsdl;
            try {
                wsdl = loader.getResource('/'+wsdlFile);
            } catch (MalformedURLException e) {
                throw new LocatableWebServiceException(
                    ServerMessages.RUNTIME_PARSER_WSDL_NOT_FOUND(wsdlFile), e, xsr );
            }
            if (wsdl == null) {
                throw new LocatableWebServiceException(
                    ServerMessages.RUNTIME_PARSER_WSDL_NOT_FOUND(wsdlFile), xsr );
            }
            SDDocumentSource docInfo = docs.get(wsdl.toExternalForm());
            assert docInfo != null;
            return docInfo;
        }

        return null;
    }
    */

    /*
     * Creates an {@link org.xml.sax.EntityResolver} that consults {@code /WEB-INF/jax-ws-catalog.xml}.
     *
    private EntityResolver createEntityResolver() {
        try {
            return XmlUtil.createEntityResolver(loader.getCatalogFile());
        } catch(MalformedURLException e) {
            throw new WebServiceException(e);
        }
    }
    */

    protected String getAttribute(XMLStreamReader reader, String name) {
        String value = reader.getAttributeValue(null, name);
        if (value != null) {
            value = value.trim();
        }
        return value;
    }

    protected QName getQNameAttribute(XMLStreamReader reader, String name) {
        String value = reader.getAttributeValue(null, name);
        if (value == null || value.equals("")) {
            return null;
        } else {
            return QName.valueOf(value);
        }
    }

    protected String getNonEmptyAttribute(XMLStreamReader reader, String name) {
        String value = reader.getAttributeValue(null, name);
        if (value != null && value.equals("")) {
            failWithLocalName(
                "runtime.parser.invalidAttributeValue",
                reader,
                name);
        }
        return value;
    }

    protected String getMandatoryAttribute(XMLStreamReader reader, String name) {
        String value = reader.getAttributeValue(null, name);
        if (value == null) {
            failWithLocalName("runtime.parser.missing.attribute", reader, name);
        }
        return value;
    }

    protected String getMandatoryNonEmptyAttribute(XMLStreamReader reader, String name) {
        String value = reader.getAttributeValue(null, name);
        if (value == null) {
            failWithLocalName("Missing attribute", reader, name);
        } else if (value.equals("")) {
            failWithLocalName("Invalid attribute value",
                reader,
                name);
        }
        return value;
    }

    /*
     * Parses the handler and role information and sets it
     * on the {@link WSBinding}.
     * @return true if <handler-chains> element present in DD
     *         false otherwise.
     *
    protected boolean setHandlersAndRoles(WSBinding binding, XMLStreamReader reader, QName serviceName, QName portName) {

        if (XMLStreamReaderUtil.nextElementContent(reader) ==
            XMLStreamConstants.END_ELEMENT ||
            !reader.getName().equals(
            HandlerChainsModel.QNAME_HANDLER_CHAINS)) {

            return false;
        }

        HandlerAnnotationInfo handlerInfo = HandlerChainsModel.parseHandlerFile(
            reader, classLoader,serviceName, portName, binding);

        binding.setHandlerChain(handlerInfo.getHandlers());
        if (binding instanceof SOAPBinding) {
            ((SOAPBinding)binding).setRoles(handlerInfo.getRoles());
        }

        // move past </handler-chains>
        XMLStreamReaderUtil.nextContent(reader);
        return true;
    }
    */

    protected static void ensureNoContent(XMLStreamReader reader) {
        if (reader.getEventType() != XMLStreamConstants.END_ELEMENT) {
            fail("While parsing sun-jaxws.xml, found unexpected content at line=", reader);
        }
    }

    protected static void fail(String key, XMLStreamReader reader) {
        String msg = key + reader.getLocation().getLineNumber();
        logger.log(Level.SEVERE, msg);
        throw new WebServiceException(msg);
    }

    protected static void failWithFullName(String key, XMLStreamReader reader) {
        String msg = key + reader.getLocation().getLineNumber() +
                reader.getName();
        throw new WebServiceException(msg);
    }

    protected static void failWithLocalName(String key, XMLStreamReader reader) {
        String msg = key + reader.getLocation().getLineNumber() +
                reader.getLocalName();
        throw new WebServiceException(msg);
    }

    protected static void failWithLocalName(String key, XMLStreamReader reader, String arg) {
        String msg = key + reader.getLocation().getLineNumber() +
                reader.getLocalName() + arg;
        throw new WebServiceException(msg);
    }

    protected Class loadClass(String name) {
        try {
            return Class.forName(name, true, classLoader);
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new WebServiceException(e);
        }
    }


    /**
     * Loads the class of the given name.
     *
     * @param xsr
     *      Used to report the source location information if there's any error.
     */
    private Class getImplementorClass(String name, XMLStreamReader xsr) {
        try {
            return Class.forName(name, true, classLoader);
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new WebServiceException("Class at "+xsr.getLocation().getLineNumber()+" is not found",e);
        }
    }

    public static final String NS_RUNTIME =
        "http://java.sun.com/xml/ns/jax-ws/ri/runtime";

    public static final String JAXWS_WSDL_DD_DIR = "WEB-INF/wsdl";

    public static final QName QNAME_ENDPOINTS =
        new QName(NS_RUNTIME, "endpoints");
    public static final QName QNAME_ENDPOINT =
        new QName(NS_RUNTIME, "endpoint");

    public static final String ATTR_VERSION = "version";
    public static final String ATTR_NAME = "name";
    public static final String ATTR_IMPLEMENTATION = "implementation";
    public static final String ATTR_WSDL = "wsdl";
    public static final String ATTR_SERVICE = "service";
    public static final String ATTR_PORT = "port";
    public static final String ATTR_URL_PATTERN = "url-pattern";
    public static final String ATTR_ENABLE_MTOM = "enable-mtom";
    public static final String ATTR_MTOM_THRESHOLD_VALUE = "mtom-threshold-value";
    public static final String ATTR_BINDING = "binding";

    public static final String ATTRVALUE_VERSION_1_0 = "2.0";
    private static final Logger logger =
        Logger.getLogger(DeploymentDescriptorParser.class.getName());

}
