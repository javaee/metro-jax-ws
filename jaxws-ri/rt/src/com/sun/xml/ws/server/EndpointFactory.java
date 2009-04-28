/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.xml.ws.server;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.policy.PolicyResolverFactory;
import com.sun.xml.ws.api.policy.PolicyResolver;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.server.*;
import com.sun.xml.ws.api.wsdl.parser.WSDLParserExtension;
import com.sun.xml.ws.api.wsdl.parser.XMLEntityResolver;
import com.sun.xml.ws.api.wsdl.parser.XMLEntityResolver.Parser;
import com.sun.xml.ws.api.wsdl.writer.WSDLGeneratorExtension;
import com.sun.xml.ws.binding.BindingImpl;
import com.sun.xml.ws.binding.SOAPBindingImpl;
import com.sun.xml.ws.binding.WebServiceFeatureList;
import com.sun.xml.ws.model.AbstractSEIModelImpl;
import com.sun.xml.ws.model.RuntimeModeler;
import com.sun.xml.ws.model.SOAPSEIModel;
import com.sun.xml.ws.model.wsdl.WSDLModelImpl;
import com.sun.xml.ws.model.wsdl.WSDLPortImpl;
import com.sun.xml.ws.model.wsdl.WSDLServiceImpl;
import com.sun.xml.ws.resources.ServerMessages;
import com.sun.xml.ws.server.provider.ProviderInvokerTube;
import com.sun.xml.ws.server.sei.SEIInvokerTube;
import com.sun.xml.ws.util.HandlerAnnotationInfo;
import com.sun.xml.ws.util.HandlerAnnotationProcessor;
import com.sun.xml.ws.util.ServiceConfigurationError;
import com.sun.xml.ws.util.ServiceFinder;
import com.sun.xml.ws.wsdl.parser.RuntimeWSDLParser;
import com.sun.xml.ws.wsdl.writer.WSDLGenerator;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicyUtil;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.ws.Provider;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.soap.SOAPBinding;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Entry point to the JAX-WS RI server-side runtime.
 *
 * @author Kohsuke Kawaguchi
 * @author Jitendra Kotamraju
 */
public class EndpointFactory {

    /**
     * Implements {@link WSEndpoint#create}.
     *
     * No need to take WebServiceContext implementation. When InvokerPipe is
     * instantiated, it calls InstanceResolver to set up a WebServiceContext.
     * We shall only take delegate to getUserPrincipal and isUserInRole from adapter.
     *
     * <p>
     * Nobody else should be calling this method.
     */
    public static <T> WSEndpoint<T> createEndpoint(
        Class<T> implType, boolean processHandlerAnnotation, @Nullable Invoker invoker,
        @Nullable QName serviceName, @Nullable QName portName,
        @Nullable Container container, @Nullable WSBinding binding,
        @Nullable SDDocumentSource primaryWsdl,
        @Nullable Collection<? extends SDDocumentSource> metadata,
        EntityResolver resolver, boolean isTransportSynchronous) {

        if(implType ==null)
            throw new IllegalArgumentException();

        verifyImplementorClass(implType);

        if (invoker == null) {
            invoker = InstanceResolver.createDefault(implType).createInvoker();
        }

        List<SDDocumentSource> md = new ArrayList<SDDocumentSource>();
        if(metadata!=null)
            md.addAll(metadata);

        if(primaryWsdl!=null && !md.contains(primaryWsdl))
            md.add(primaryWsdl);

        if(container==null)
            container = ContainerResolver.getInstance().getContainer();

        if(serviceName==null)
            serviceName = getDefaultServiceName(implType);

        if(portName==null)
            portName = getDefaultPortName(serviceName,implType);

        {// error check
            String serviceNS = serviceName.getNamespaceURI();
            String portNS = portName.getNamespaceURI();
            if (!serviceNS.equals(portNS)) {
                throw new ServerRtException("wrong.tns.for.port",portNS, serviceNS);
            }
        }

        // setting a default binding
        if (binding == null)
            binding = BindingImpl.create(BindingID.parse(implType));

        if (primaryWsdl != null) {
            verifyPrimaryWSDL(primaryWsdl, serviceName);
        }

        QName portTypeName = null;
        if (implType.getAnnotation(WebServiceProvider.class)==null) {
            portTypeName = RuntimeModeler.getPortTypeName(implType);
        }

        // Categorises the documents as WSDL, Schema etc
        List<SDDocumentImpl> docList = categoriseMetadata(md, serviceName, portTypeName);
        // Finds the primary WSDL and makes sure that metadata doesn't have
        // two concrete or abstract WSDLs
        SDDocumentImpl primaryDoc = findPrimary(docList);

        InvokerTube terminal;
        WSDLPortImpl wsdlPort = null;
        AbstractSEIModelImpl seiModel = null;
        // create WSDL model
        if (primaryDoc != null) {
            wsdlPort = getWSDLPort(primaryDoc, docList, serviceName, portName, container);
        }

        WebServiceFeatureList features=((BindingImpl)binding).getFeatures();
        features.parseAnnotations(implType);
        PolicyMap policyMap = null;
        // create terminal pipe that invokes the application
        if (implType.getAnnotation(WebServiceProvider.class)!=null) {
            //Provider case: Enable Addressing from WSDL only if it has RespectBindingFeature enabled
            if(wsdlPort != null) {
                 policyMap = wsdlPort.getOwner().getParent().getPolicyMap();
                 //Merge features from WSDL and other policy configuration
                 features.mergeFeatures(wsdlPort,true,true);
            } else {
                //No WSDL, so try to merge features from Policy configuration
                policyMap = PolicyResolverFactory.create().resolve(
                        new PolicyResolver.ServerContext(null, container, implType, false));
                for(WebServiceFeature f: PolicyUtil.getPortScopedFeatures(policyMap,serviceName,portName)){
                    features.add(f);
                }
            }
            terminal = ProviderInvokerTube.create(implType,binding,invoker);
        } else {
            // Create runtime model for non Provider endpoints
            seiModel = createSEIModel(wsdlPort, implType, serviceName, portName, binding);
            if(binding instanceof SOAPBindingImpl){
                //set portKnownHeaders on Binding, so that they can be used for MU processing
                ((SOAPBindingImpl)binding).setPortKnownHeaders(
                        ((SOAPSEIModel)seiModel).getKnownHeaders());
            }
            // Generate WSDL for SEI endpoints(not for Provider endpoints)
            if (primaryDoc == null) {
                primaryDoc = generateWSDL(binding, seiModel, docList, container, implType);
                // create WSDL model
                wsdlPort = getWSDLPort(primaryDoc, docList, serviceName, portName, container);
                seiModel.freeze(wsdlPort);
            }
            policyMap = wsdlPort.getOwner().getParent().getPolicyMap();
            // New Features might have been added in WSDL through Policy.
            //Merge features from WSDL and other policy configuration
            // This sets only the wsdl features that are not already set(enabled/disabled)
            features.mergeFeatures(wsdlPort, false, true);
            terminal= new SEIInvokerTube(seiModel,invoker,binding);
        }

        // Process @HandlerChain, if handler-chain is not set via Deployment Descriptor
        if (processHandlerAnnotation) {
            processHandlerAnnotation(binding, implType, serviceName, portName);
        }
        // Selects only required metadata for this endpoint from the passed-in metadata
        if (primaryDoc != null) {
            docList = findMetadataClosure(primaryDoc, docList);
        }
        ServiceDefinitionImpl serviceDefiniton = (primaryDoc != null) ? new ServiceDefinitionImpl(docList, primaryDoc) : null;

        return new WSEndpointImpl<T>(serviceName, portName, binding,container,seiModel,wsdlPort,implType, serviceDefiniton,terminal, isTransportSynchronous, policyMap);
    }


    /**
     * Goes through the original metadata documents and collects the required ones.
     * This done traversing from primary WSDL and its imports until it builds a
     * complete set of documents(transitive closure) for the endpoint.
     *
     * @param primaryDoc primary WSDL doc
     * @param docList complete metadata
     * @return new metadata that doesn't contain extraneous documnets.
     */
    private static List<SDDocumentImpl> findMetadataClosure(SDDocumentImpl primaryDoc, List<SDDocumentImpl> docList) {
        // create a map for old metadata
        Map<String, SDDocumentImpl> oldMap = new HashMap<String, SDDocumentImpl>();
        for(SDDocumentImpl doc : docList) {
            oldMap.put(doc.getSystemId().toString(), doc);
        }
        // create a map for new metadata
        Map<String, SDDocumentImpl> newMap = new HashMap<String, SDDocumentImpl>();
        newMap.put(primaryDoc.getSystemId().toString(), primaryDoc);

        List<String> remaining = new ArrayList<String>();
        remaining.addAll(primaryDoc.getImports());
        while(!remaining.isEmpty()) {
            String url = remaining.remove(0);
            SDDocumentImpl doc = oldMap.get(url);
            if (doc == null) {
                // old metadata doesn't have this imported doc, may be external
                continue;
            }
            // Check if new metadata already contains this doc
            if (!newMap.containsKey(url)) {
                newMap.put(url, doc);
                remaining.addAll(doc.getImports());
            }
        }
        List<SDDocumentImpl> newMetadata = new ArrayList<SDDocumentImpl>();
        newMetadata.addAll(newMap.values());
        return newMetadata;
    }

    private static <T> void processHandlerAnnotation(WSBinding binding, Class<T> implType, QName serviceName, QName portName) {
        HandlerAnnotationInfo chainInfo =
                HandlerAnnotationProcessor.buildHandlerInfo(
                        implType, serviceName, portName, binding);
        if (chainInfo != null) {
            binding.setHandlerChain(chainInfo.getHandlers());
            if (binding instanceof SOAPBinding) {
                ((SOAPBinding) binding).setRoles(chainInfo.getRoles());
            }
        }

    }

    /**
     * Verifies if the endpoint implementor class has @WebService or @WebServiceProvider
     * annotation
     *
     * @return
     *       true if it is a Provider or AsyncProvider endpoint
     *       false otherwise
     * @throws java.lang.IllegalArgumentException
     *      If it doesn't have any one of @WebService or @WebServiceProvider
     *      If it has both @WebService and @WebServiceProvider annotations
     */
    public static boolean verifyImplementorClass(Class<?> clz) {
        WebServiceProvider wsProvider = clz.getAnnotation(WebServiceProvider.class);
        WebService ws = clz.getAnnotation(WebService.class);
        if (wsProvider == null && ws == null) {
            throw new IllegalArgumentException(clz +" has neither @WebService nor @WebServiceProvider annotation");
        }
        if (wsProvider != null && ws != null) {
            throw new IllegalArgumentException(clz +" has both @WebService and @WebServiceProvider annotations");
        }
        if (wsProvider != null) {
            if (Provider.class.isAssignableFrom(clz) || AsyncProvider.class.isAssignableFrom(clz)) {
                return true;
            }
            throw new IllegalArgumentException(clz +" doesn't implement Provider or AsyncProvider interface");
        }
        return false;
    }


    private static AbstractSEIModelImpl createSEIModel(WSDLPort wsdlPort,
                                                       Class<?> implType, @NotNull QName serviceName, @NotNull QName portName, WSBinding binding) {

        RuntimeModeler rap;
        // Create runtime model for non Provider endpoints

        // wsdlPort will be null, means we will generate WSDL. Hence no need to apply
        // bindings or need to look in the WSDL
        if(wsdlPort == null){
            rap = new RuntimeModeler(implType,serviceName, binding.getBindingId(), binding.getFeatures().toArray());
        } else {
            /*
            This not needed anymore as wsdlFeatures are merged later anyway
            and so is the MTOMFeature.
            applyEffectiveMtomSetting(wsdlPort.getBinding(), binding);
            */
            //now we got the Binding so lets build the model
            rap = new RuntimeModeler(implType, serviceName, (WSDLPortImpl)wsdlPort, binding.getFeatures().toArray());
        }
        rap.setPortName(portName);
        return rap.buildRuntimeModel();
    }

    /**
     *Set the mtom enable setting from wsdl model (mtom policy assertion) on to @link WSBinding} if DD has
     * not already set it on BindingID. Also check conflicts.
     */
    /*
    private static void applyEffectiveMtomSetting(WSDLBoundPortType wsdlBinding, WSBinding binding){
        if(wsdlBinding.isMTOMEnabled()){
            BindingID bindingId = binding.getBindingId();
            if(bindingId.isMTOMEnabled() == null){
                binding.setMTOMEnabled(true);
            }else if (bindingId.isMTOMEnabled() != null && bindingId.isMTOMEnabled() == Boolean.FALSE){
                //TODO: i18N
                throw new ServerRtException("Deployment failed! Mtom policy assertion in WSDL is enabled whereas the deplyment descriptor setting wants to disable it!");
            }
        }
    }
    */
    /**
     * If service name is not already set via DD or programmatically, it uses
     * annotations {@link WebServiceProvider}, {@link WebService} on implementorClass to get PortName.
     *
     * @return non-null service name
     */
    public static @NotNull QName getDefaultServiceName(Class<?> implType) {
        QName serviceName;
        WebServiceProvider wsProvider = implType.getAnnotation(WebServiceProvider.class);
        if (wsProvider!=null) {
            String tns = wsProvider.targetNamespace();
            String local = wsProvider.serviceName();
            serviceName = new QName(tns, local);
        } else {
            serviceName = RuntimeModeler.getServiceName(implType);
        }
        assert serviceName != null;
        return serviceName;
    }

    /**
     * If portName is not already set via DD or programmatically, it uses
     * annotations on implementorClass to get PortName.
     *
     * @return non-null port name
     */
    public static @NotNull QName getDefaultPortName(QName serviceName, Class<?> implType) {
        QName portName;
        WebServiceProvider wsProvider = implType.getAnnotation(WebServiceProvider.class);
        if (wsProvider!=null) {
            String tns = wsProvider.targetNamespace();
            String local = wsProvider.portName();
            portName = new QName(tns, local);
        } else {
            portName = RuntimeModeler.getPortName(implType, serviceName.getNamespaceURI());
        }
        assert portName != null;
        return portName;
    }

    /**
     * Returns the wsdl from @WebService, or @WebServiceProvider annotation using
     * wsdlLocation element.
     *
     * @param implType
     *      endpoint implementation class
     *      make sure that you called {@link #verifyImplementorClass} on it.
     * @return wsdl if there is wsdlLocation, else null
     */
    public static @Nullable String getWsdlLocation(Class<?> implType) {
        String wsdl;
        WebService ws = implType.getAnnotation(WebService.class);
        if (ws != null) {
            wsdl = ws.wsdlLocation();
        } else {
            WebServiceProvider wsProvider = implType.getAnnotation(WebServiceProvider.class);
            assert wsProvider != null;
            wsdl = wsProvider.wsdlLocation();
        }
        if (wsdl.length() < 1) {
            wsdl = null;
        }
        return wsdl;
    }

    /**
     * Generates the WSDL and XML Schema for the endpoint if necessary
     * It generates WSDL only for SOAP1.1, and for XSOAP1.2 bindings
     */
    private static SDDocumentImpl generateWSDL(WSBinding binding, AbstractSEIModelImpl seiModel, List<SDDocumentImpl> docs,
                                               Container container, Class implType) {
        BindingID bindingId = binding.getBindingId();
        if (!bindingId.canGenerateWSDL()) {
            throw new ServerRtException("can.not.generate.wsdl", bindingId);
        }

        if (bindingId.toString().equals(SOAPBindingImpl.X_SOAP12HTTP_BINDING)) {
            String msg = ServerMessages.GENERATE_NON_STANDARD_WSDL();
            logger.warning(msg);
        }

        // Generate WSDL and schema documents using runtime model
        WSDLGenResolver wsdlResolver = new WSDLGenResolver(docs,seiModel.getServiceQName(),seiModel.getPortTypeName());
        WSDLGenerator wsdlGen = new WSDLGenerator(seiModel, wsdlResolver, binding, container, implType,
                ServiceFinder.find(WSDLGeneratorExtension.class).toArray());
        wsdlGen.doGeneration();
        return wsdlResolver.updateDocs();
    }

    /**
     * Builds {@link SDDocumentImpl} from {@link SDDocumentSource}.
     */
    private static List<SDDocumentImpl> categoriseMetadata(
        List<SDDocumentSource> src, QName serviceName, QName portTypeName) {

        List<SDDocumentImpl> r = new ArrayList<SDDocumentImpl>(src.size());
        for (SDDocumentSource doc : src) {
            r.add(SDDocumentImpl.create(doc,serviceName,portTypeName));
        }
        return r;
    }

    /**
     * Verifies whether the given primaryWsdl contains the given serviceName.
     * If the WSDL doesn't have the service, it throws an WebServiceException.
     */
    private static void verifyPrimaryWSDL(@NotNull SDDocumentSource primaryWsdl, @NotNull QName serviceName) {
        SDDocumentImpl primaryDoc = SDDocumentImpl.create(primaryWsdl,serviceName,null);
        if (!(primaryDoc instanceof SDDocument.WSDL)) {
            throw new WebServiceException("Not a primary WSDL="+primaryWsdl.getSystemId());
        }
        SDDocument.WSDL wsdlDoc = (SDDocument.WSDL)primaryDoc;
        if (!wsdlDoc.hasService()) {
            if(wsdlDoc.getAllServices().isEmpty())
                throw new WebServiceException("Not a primary WSDL="+primaryWsdl.getSystemId()+
                        " since it doesn't have Service "+serviceName);
            else
                throw new WebServiceException("WSDL "+primaryDoc.getSystemId()+" has the following services "+wsdlDoc.getAllServices()+" but not "+serviceName+". Maybe you forgot to specify a service name in @WebService/@WebServiceProvider?");
        }
    }

    /**
     * Finds the primary WSDL document from the list of metadata documents. If
     * there are two metadata documents that qualify for primary, it throws an
     * exception. If there are two metadata documents that qualify for porttype,
     * it throws an exception.
     *
     * @return primay wsdl document, null if is not there in the docList
     *
     */
    private static @Nullable SDDocumentImpl findPrimary(@NotNull List<SDDocumentImpl> docList) {
        SDDocumentImpl primaryDoc = null;
        boolean foundConcrete = false;
        boolean foundAbstract = false;
        for(SDDocumentImpl doc : docList) {
            if (doc instanceof SDDocument.WSDL) {
                SDDocument.WSDL wsdlDoc = (SDDocument.WSDL)doc;
                if (wsdlDoc.hasService()) {
                    primaryDoc = doc;
                    if (foundConcrete) {
                        throw new ServerRtException("duplicate.primary.wsdl", doc.getSystemId() );
                    }
                    foundConcrete = true;
                }
                if (wsdlDoc.hasPortType()) {
                    if (foundAbstract) {
                        throw new ServerRtException("duplicate.abstract.wsdl", doc.getSystemId());
                    }
                    foundAbstract = true;
                }
            }
        }
        return primaryDoc;
    }

    /**
     * Parses the primary WSDL and returns the {@link WSDLPort} for the given service and port names
     *
     * @param primaryWsdl Primary WSDL
     * @param metadata it may contain imported WSDL and schema documents
     * @param serviceName service name in wsdl
     * @param portName port name in WSDL
     * @param container container in which this service is running
     * @return non-null wsdl port object
     */
    private static @NotNull WSDLPortImpl getWSDLPort(SDDocumentSource primaryWsdl, List<? extends SDDocumentSource> metadata,
                                                     @NotNull QName serviceName, @NotNull QName portName, Container container) {
        URL wsdlUrl = primaryWsdl.getSystemId();
        try {
            // TODO: delegate to another entity resolver
            WSDLModelImpl wsdlDoc = RuntimeWSDLParser.parse(
                new Parser(primaryWsdl), new EntityResolverImpl(metadata),
                    false, container, ServiceFinder.find(WSDLParserExtension.class).toArray());
            if(wsdlDoc.getServices().size() == 0) {
                throw new ServerRtException(ServerMessages.localizableRUNTIME_PARSER_WSDL_NOSERVICE_IN_WSDLMODEL(wsdlUrl));
            }
            WSDLServiceImpl wsdlService = wsdlDoc.getService(serviceName);
            if (wsdlService == null) {
                throw new ServerRtException(ServerMessages.localizableRUNTIME_PARSER_WSDL_INCORRECTSERVICE(serviceName,wsdlUrl));
            }
            WSDLPortImpl wsdlPort = wsdlService.get(portName);
            if (wsdlPort == null) {
                throw new ServerRtException(ServerMessages.localizableRUNTIME_PARSER_WSDL_INCORRECTSERVICEPORT(serviceName, portName, wsdlUrl));
            }
            return wsdlPort;
        } catch (IOException e) {
            throw new ServerRtException("runtime.parser.wsdl", wsdlUrl,e);
        } catch (XMLStreamException e) {
            throw new ServerRtException("runtime.saxparser.exception", e.getMessage(), e.getLocation(), e);
        } catch (SAXException e) {
            throw new ServerRtException("runtime.parser.wsdl", wsdlUrl,e);
        } catch (ServiceConfigurationError e) {
            throw new ServerRtException("runtime.parser.wsdl", wsdlUrl,e);
        }
    }

    /**
     * {@link XMLEntityResolver} that can resolve to {@link SDDocumentSource}s.
     */
    private static final class EntityResolverImpl implements XMLEntityResolver {
        private Map<String,SDDocumentSource> metadata = new HashMap<String,SDDocumentSource>();

        public EntityResolverImpl(List<? extends SDDocumentSource> metadata) {
            for (SDDocumentSource doc : metadata) {
                this.metadata.put(doc.getSystemId().toExternalForm(),doc);
            }
        }

        public Parser resolveEntity (String publicId, String systemId) throws IOException, XMLStreamException {
            if (systemId != null) {
                SDDocumentSource doc = metadata.get(systemId);
                if (doc != null)
                    return new Parser(doc);
            }
            return null;
        }

    }

    private static final Logger logger = Logger.getLogger(
        com.sun.xml.ws.util.Constants.LoggingDomain + ".server.endpoint");
}
