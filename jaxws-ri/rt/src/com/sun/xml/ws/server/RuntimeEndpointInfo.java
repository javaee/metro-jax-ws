/*
 * $Id: RuntimeEndpointInfo.java,v 1.52 2005-09-03 02:10:32 jitu Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.server;

import javax.annotation.Resource;
import com.sun.xml.ws.model.RuntimeModel;
import com.sun.xml.ws.modeler.RuntimeModeler;
import com.sun.xml.ws.util.HandlerAnnotationInfo;
import com.sun.xml.ws.util.HandlerAnnotationProcessor;
import com.sun.xml.ws.wsdl.writer.WSDLGenerator;
import com.sun.xml.ws.wsdl.parser.WSDLDocument;
import com.sun.xml.ws.wsdl.parser.RuntimeWSDLParser;
import com.sun.xml.ws.wsdl.parser.Service;
import com.sun.xml.ws.binding.BindingImpl;
import com.sun.xml.ws.binding.soap.SOAPBindingImpl;
import com.sun.xml.ws.server.DocInfo.DOC_TYPE;

import java.util.*;
import javax.xml.namespace.QName;
import com.sun.xml.ws.spi.runtime.Binding;
import javax.xml.ws.Provider;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.transform.Source;
import com.sun.xml.ws.spi.runtime.WebServiceContext;
import com.sun.xml.ws.util.exception.LocalizableExceptionAdapter;
import com.sun.xml.ws.util.localization.LocalizableMessageFactory;
import com.sun.xml.ws.util.localization.Localizer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.logging.Logger;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import javax.xml.ws.BeginService;
import javax.xml.ws.EndService;
import javax.xml.ws.WebServiceProvider;
import javax.xml.stream.XMLStreamException;
import javax.xml.ws.Endpoint;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;




/**
 * modeled after the javax.xml.ws.Endpoint class in API. 
 * Contains all the information about Binding, handler chain, Implementor object, 
 * WSDL & Schema Metadata
 * @author WS Development Team
 */
public class RuntimeEndpointInfo extends Endpoint
    implements com.sun.xml.ws.spi.runtime.RuntimeEndpointInfo {
    
    private String name;
    private Exception exception;
    private QName portName;
    private QName serviceName;
    private String wsdlFileName;
    private boolean deployed;
    private String urlPattern;
    private List<Source> metadata;
    private Binding binding;
    private RuntimeModel runtimeModel;
    private Object implementor;
    private Class implementorClass;
    private Map<String, DocInfo> docs;      // /WEB-INF/wsdl/xxx.wsdl -> DocInfo
    private Map<String, DocInfo> query2Doc;     // (wsdl=a) --> DocInfo
    private WebServiceContext wsContext;
    private boolean beginServiceDone;
    private boolean endServiceDone;
    private boolean injectedContext;
    private boolean publishingDone;
    private URL wsdlUrl;
    private EntityResolver wsdlResolver;
    private QName portTypeName;
    private Map<String, Object> properties;
    private static final Logger logger = Logger.getLogger(
        com.sun.xml.ws.util.Constants.LoggingDomain + ".server.endpoint");
    private static final Localizer localizer = new Localizer();
    private static final LocalizableMessageFactory messageFactory =
        new LocalizableMessageFactory("com.sun.xml.ws.resources.server");


    public void setException(Exception e) {
        exception = e;
    }

    public String getName() {
        return name;
    }

    public void setName(String s) {
        name = s;
    }

    public String getWSDLFileName() {
        return wsdlFileName;
    }

    public void setWSDLFileName(String s) {
        wsdlFileName = s;
    }
    
    /**
     * set the URL for primary WSDL, and an EntityResolver to resolve all
     * imports/references
     */
    public void setWsdlInfo(URL wsdlUrl, EntityResolver wsdlResolver) {
        this.wsdlUrl = wsdlUrl;
        this.wsdlResolver = wsdlResolver;
    }
    
    public EntityResolver getWsdlResolver() {
        return wsdlResolver;
    }
    
    public URL getWsdLUrl() {
        return wsdlUrl;
    }

    public boolean isDeployed() {
        return deployed;
    }
    
    public boolean isPublished() {
        return deployed;
    }
    
    public void stop() {
        
    }
    
    public void publish(Object obj) {
        
    }
    
    public void publish(String address) {
        
    }
    
    
    
    public void createModel() {
        // Create runtime model for non Provider endpoints

        // wsdlURL will be null, means we will generate WSDL. Hence no need to apply
        // bindings or need to look in the WSDL
        if(wsdlUrl == null){
            RuntimeModeler rap = new RuntimeModeler(getImplementorClass(),
                getImplementor(), getServiceName(), ((BindingImpl)binding).getBindingId());
            if (getPortName() != null) {
                rap.setPortName(getPortName());
            }
            runtimeModel = rap.buildRuntimeModel();
        }else {
            try {
                WSDLDocument wsdlDoc = RuntimeWSDLParser.parse(getWsdLUrl(), getWsdlResolver());
                com.sun.xml.ws.wsdl.parser.Binding wsdlBinding = null;
                if(serviceName == null)
                    serviceName = RuntimeModeler.getServiceName(getImplementorClass());
                if(getPortName() != null){
                    wsdlBinding = wsdlDoc.getBinding(getServiceName(), getPortName());
                    if(wsdlBinding == null)
                        throw new ServerRtException("runtime.parser.wsdl.incorrectserviceport", new Object[]{serviceName, portName, getWsdLUrl()});
                }else{
                    Service service = wsdlDoc.getService(serviceName);
                    if(service == null)
                        throw new ServerRtException("runtime.parser.wsdl.noservice", new Object[]{serviceName, getWsdLUrl()});

                    String bindingId = ((BindingImpl)binding).getBindingId();
                    List<com.sun.xml.ws.wsdl.parser.Binding> bindings = wsdlDoc.getBindings(service, bindingId);
                    if(bindings.size() == 0)
                        throw new ServerRtException("runtime.parser.wsdl.nobinding", new Object[]{bindingId, serviceName, getWsdLUrl()});

                    if(bindings.size() > 1)
                        throw new ServerRtException("runtime.parser.wsdl.multiplebinding", new Object[]{bindingId, serviceName, getWsdLUrl()});
                }
                //now we got the Binding so lets build the model
                RuntimeModeler rap = new RuntimeModeler(getImplementorClass(), getImplementor(), getServiceName(), wsdlBinding);
                if (getPortName() != null) {
                    rap.setPortName(getPortName());
                }
                runtimeModel = rap.buildRuntimeModel();
            } catch (IOException e) {
                throw new ServerRtException("runtime.parser.wsdl", getWsdLUrl().toString());
            } catch (XMLStreamException e) {
                throw new ServerRtException("runtime.saxparser.exception",
                        new Object[]{e.getMessage(), e.getLocation()});
            } catch (SAXException e) {
                throw new ServerRtException("runtime.parser.wsdl", getWsdLUrl().toString());
            }
        }
    }
    
    
    public boolean isProviderEndpoint() {
        Annotation ann = getImplementorClass().getAnnotation(
            WebServiceProvider.class);
        return (ann != null);
    }
    
    /*
     * If serviceName is not already set via DD or programmatically, it uses
     * annotations on implementorClass to set ServiceName.
     */
    public void doServiceNameProcessing() {
        if (getServiceName() == null) {
            if (isProviderEndpoint()) {
                WebServiceProvider wsProvider =
                    (WebServiceProvider)getImplementorClass().getAnnotation(
                        WebServiceProvider.class);
                String tns = wsProvider.targetNamespace();
                String local = wsProvider.serviceName();
                if (local.length() > 0) {
                    setServiceName(new QName(tns, local));
                }
            } else {
                setServiceName(RuntimeModeler.getServiceName(getImplementorClass()));
            }
        }
    }
    
    /*
     * Sets PortType QName 
     */
    public void doPortTypeNameProcessing() {
        if (getPortTypeName() == null) {
            if (!isProviderEndpoint()) {
                setPortTypeName(RuntimeModeler.getPortTypeName(getImplementorClass()));
            }
        }
    }
    
    
    /**
     * creates a RuntimeModel using @link com.sun.xml.ws.modeler.RuntimeModeler. 
     * The modeler creates the model by reading annotations on ImplementorClassobject. 
     * RuntimeModel is read only and is accessed from multiple threads afterwards.

     */
    public void deploy() {
        if (implementor == null) {
            throw new ServerRtException("null.implementor");
        }
        if (implementorClass == null) {
            setImplementorClass(getImplementor().getClass());
        }
        
        // verify if implementor class has @WebService or @WebServiceProvider
        
        // ServiceName processing
        doServiceNameProcessing();
        
        // PortType Name processing
        //doPortTypeNameProcessing();
        
        // setting a default binding
        if (binding == null) {
            String bindingId = RuntimeModeler.getBindingId(getImplementorClass());
            setBinding(new SOAPBindingImpl(SOAPBinding.SOAP11HTTP_BINDING));
        }
        
        if (isProviderEndpoint()) {
            deployProvider();
        } else {
            // Create runtime model for non Provider endpoints    
            createModel();
            if (getServiceName() == null) {
                setServiceName(runtimeModel.getServiceQName());
            }
            if (getPortName() == null) {
                setPortName(runtimeModel.getPortName());
            }
            if (getBinding().getHandlerChain() == null) {
                HandlerAnnotationInfo chainInfo =
                    HandlerAnnotationProcessor.buildHandlerInfo(
                        implementorClass);
                if (chainInfo != null) {
                    getBinding().setHandlerChain(chainInfo.getHandlers());
                    if (getBinding() instanceof SOAPBinding) {
                        ((SOAPBinding) getBinding()).setRoles(
                            chainInfo.getRoles());
                    }
                }
            }
            //set momt processing
            if(binding instanceof SOAPBindingImpl){
                runtimeModel.enableMtom(((SOAPBinding)binding).isMTOMEnabled());
            }
        }
        deployed = true;
    }
    
    public boolean needWSDLGeneration() {
        return (getWsdLUrl() == null);
    }
    
    public boolean isPublishingDone() {
        return publishingDone;
    }
    
    public void setPublishingDone(boolean publishingDone) {
        this.publishingDone = publishingDone;
    }
    
    /*
     * Generates the WSDL and XML Schema for the endpoint if necessary
     * It generates WSDL only for SOAP1.1, and for XSOAP1.2 bindings
     */
    public void generateWSDL() {
        BindingImpl bindingImpl = (BindingImpl)getBinding();
        String bindingId = bindingImpl.getActualBindingId();
        if (!bindingId.equals(SOAPBinding.SOAP11HTTP_BINDING) &&
            !bindingId.equals(SOAPBindingImpl.X_SOAP12HTTP_BINDING)) {
            throw new ServerRtException("can.not.generate.wsdl", bindingId);
        }
        
        if (bindingId.equals(SOAPBindingImpl.X_SOAP12HTTP_BINDING)) {  
            String msg = localizer.localize(
                messageFactory.getMessage("generate.non.standard.wsdl"));
            logger.warning(msg);
        }
         
        // Generate WSDL and schema documents using runtime model
        if (getDocMetadata() == null) {
            setMetadata(new HashMap<String, DocInfo>());
        }
        WSDLGenResolver wsdlResolver = new WSDLGenResolver(getDocMetadata());
        WSDLGenerator wsdlGen = new WSDLGenerator(runtimeModel, wsdlResolver,
                ((BindingImpl)binding).getBindingId());
        try {
            wsdlGen.doGeneration();
        } catch(Exception e) {
            throw new ServerRtException("server.rt.err",
                    new LocalizableExceptionAdapter(e));
        }
        //setMetadata(wsdlResolver.getDocs());
        setWSDLFileName(wsdlResolver.getWSDLFile());
        setPublishingDone(true);
    }
    
    /*
     * Provider endpoint validation
     */
    private void deployProvider() {
        if (!Provider.class.isAssignableFrom(getImplementorClass())) {
            throw new ServerRtException("not.implement.provider",
                new Object[] {getImplementorClass()});
        }
        WebServiceProvider wsProvider =
            (WebServiceProvider)getImplementorClass().getAnnotation(
                WebServiceProvider.class);
        if (getWSDLFileName() == null) {
            if (!wsProvider.wsdlLocation().equals("")) {
                setWSDLFileName(wsProvider.wsdlLocation());
            }
        }
        if (getWSDLFileName() == null) {
            throw new ServerRtException("wsdl.required");
        }
        if (getServiceName() == null) {
            throw new ServerRtException("service.name.required");
        }
        if (getPortName() == null) {
            throw new ServerRtException("port.name.required");
        }
    }

    public QName getPortName() {
        return portName;
    }

    public void setPortName(QName n) {
        portName = n;
    }
    
    public QName getPortTypeName() {
        return portTypeName;
    }

    public void setPortTypeName(QName n) {
        portTypeName = n;
    }

    public QName getServiceName() {
        return serviceName;
    }

    public void setServiceName(QName n) {
        serviceName = n;
    }

    public String getUrlPattern() {
        return urlPattern;
    }

    public void setUrlPattern(String s) {
        urlPattern = s;
    }

    public void setBinding(Binding binding){
        this.binding = binding;
    }

    public Binding getBinding() {
        return binding;
    }
    
    public java.util.List<Source> getMetadata() {
        return metadata;
    }
        
    public void setMetadata(java.util.List<Source> metadata) {

        this.metadata = metadata;
    }

    public RuntimeModel getRuntimeModel() {
        return runtimeModel;
    }
    
    public Object getImplementor() {
        return implementor;
    }
    
    public void setImplementor(Object implementor) {
        this.implementor = implementor;
    }
    
    public Class getImplementorClass() {
        if (implementorClass == null) {
            implementorClass = implementor.getClass();
        }
        return implementorClass;
    }
    
    public void setImplementorClass(Class implementorClass) {
        this.implementorClass = implementorClass;
    }
    
    public void setMetadata(Map<String, DocInfo> docs) {
        this.docs = docs;
        // update uri-->DocInfo map
        if (query2Doc != null) {
            query2Doc.clear();
        } else {
            query2Doc = new HashMap<String, DocInfo>();
        }
        Set<Map.Entry<String, DocInfo>> entries = docs.entrySet();
        for(Map.Entry<String, DocInfo> entry : entries) {
            DocInfo docInfo = entry.getValue();
            query2Doc.put(docInfo.getQueryString(), docInfo);
        }
    }
    
    public void updateQuery2DocInfo() {
        // update uri-->DocInfo map
        if (query2Doc != null) {
            query2Doc.clear();
        } else {
            query2Doc = new HashMap<String, DocInfo>();
        }
        Set<Map.Entry<String, DocInfo>> entries = docs.entrySet();        
        for(Map.Entry<String, DocInfo> entry : entries) {
            DocInfo docInfo = entry.getValue();          
            query2Doc.put(docInfo.getQueryString(), docInfo);
        }        
    }
    
    public WebServiceContext getWebServiceContext() {
        return wsContext;
    }
    
    public void setWebServiceContext(WebServiceContext wsContext) {
        this.wsContext = wsContext;
    }
     
    
    /*
     * key - /WEB-INF/wsdl/xxx.wsdl
     */
    public Map<String, DocInfo> getDocMetadata() {
        return docs;
    }
    
    /*
     * path - /WEB-INF/wsdl/xxx.wsdl
     * return - xsd=a | wsdl | wsdl=b etc
     */
    public String getQueryString(URL url) {
        Set<Entry<String, DocInfo>> entries = getDocMetadata().entrySet();
        for(Entry<String, DocInfo> entry : entries) {
            // URLs are not matching. servlet container bug ?            
            if (entry.getValue().getUrl().toString().equals(url.toString())) {       
                return entry.getValue().getQueryString();
            }
        }
        return null;
        /*
        DocInfo docInfo = docs.get(path);
        return (docInfo == null) ? null : docInfo.getQueryString();
         */
    }
    
    /*
     * queryString - xsd=a | wsdl | wsdl=b etc
     * return - /WEB-INF/wsdl/xxx.wsdl
     */
    public String getPath(String queryString) {
        DocInfo docInfo = query2Doc.get(queryString);
        return (docInfo == null) ? null : docInfo.getUrl().toString();
    }
    
    /*
     * Injects the WebServiceContext. Called from Servlet.init(), or
     * Endpoint.publish(). Used synchronized because multiple servlet
     * instances may call this in their init()
     */
    public synchronized void injectContext()
    throws IllegalAccessException, InvocationTargetException {
        if (injectedContext) {
            return;
        }
        try {
            doFieldsInjection();
            doMethodsInjection();
        } finally {
            injectedContext = true;
        }
    }
    
    private void doFieldsInjection() {
        Class c = getImplementorClass();
        Field[] fields = c.getDeclaredFields();
        for(final Field field: fields) {
            Resource resource = field.getAnnotation(Resource.class);
            if (resource != null) {
                Class resourceType = resource.type();
                Class fieldType = field.getType();                  
                if (resourceType.equals(Object.class)) {                    
                    if (fieldType.equals(javax.xml.ws.WebServiceContext.class)) {      
                        injectField(field);
                    }
                } else if (resourceType.equals(javax.xml.ws.WebServiceContext.class)) {
                    if (fieldType.isAssignableFrom(javax.xml.ws.WebServiceContext.class)) {
                        injectField(field);
                    } else {
                        throw new ServerRtException("wrong.field.type",
                            field.getName());
                    }
                }
            }
        }
    }
    
    private void doMethodsInjection() {
        Class c = getImplementorClass();
        Method[] methods = c.getDeclaredMethods();
        for(final Method method : methods) {
            Resource resource = method.getAnnotation(Resource.class);
            if (resource != null) {
                Class[] paramTypes = method.getParameterTypes();
                if (paramTypes.length != 1) {
                    throw new ServerRtException("wrong.no.parameters",
                        method.getName());
                }
                Class resourceType = resource.type();
                Class argType = paramTypes[0];
                if (resourceType.equals(Object.class)
                    && argType.equals(javax.xml.ws.WebServiceContext.class)) {
                    invokeMethod(method, new Object[] { wsContext });
                } else if (resourceType.equals(javax.xml.ws.WebServiceContext.class)) {
                    if (argType.isAssignableFrom(javax.xml.ws.WebServiceContext.class)) {
                        invokeMethod(method, new Object[] { wsContext });
                    } else {
                        throw new ServerRtException("wrong.parameter.type",
                            method.getName());
                    }
                }
            }
        }
    }
    
    /*
     * injects a resource into a Field
     */
    private void injectField(final Field field) {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction() {
                public Object run() throws IllegalAccessException,
                    InvocationTargetException {
                    if (!field.isAccessible()) {                        
                        field.setAccessible(true);
                    }
                    field.set(implementor, wsContext);
                    return null;
                }
            });
        } catch(PrivilegedActionException e) {
            throw new ServerRtException("server.rt.err",
                new LocalizableExceptionAdapter(e.getException()));
        }
    }
    
    /*
     * Helper method to invoke a Method
     */
    private void invokeMethod(final Method method, final Object[] args) {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction() {
                public Object run() throws IllegalAccessException,
                InvocationTargetException {
                    if (!method.isAccessible()) {
                        method.setAccessible(true);
                    }
                    method.invoke(implementor, args);
                    return null;
                }
            });
        } catch(PrivilegedActionException e) {
            throw new ServerRtException("server.rt.err",
                new LocalizableExceptionAdapter(e.getException()));
        }
    }
    
    /*
     * Calls the first method in the implementor object that has @BeginService
     * annotation. Servlet.init(), or Endpoint.publish() may call this. Used
     * synchronized because multiple servlet instances may call this in their
     * init()
     */
    public synchronized void beginService() {
        if (beginServiceDone) {
            return;                 // Already called for this endpoint object
        }
        try {
            invokeLifeCycleMethod(BeginService.class);
        } finally {
            beginServiceDone = true;
        }
    }
    
    /*
     * Calls the first method in the implementor object that has @EndService
     * annotation. Servlet.destory(), or Endpoint.stop() may call this. Used
     * synchronized because multiple servlet instances may call this in their
     * destroy()
     */
    public synchronized void endService() {
        if (endServiceDone) {
            return;                 // Already called for this endpoint object
        }
        try {
            invokeLifeCycleMethod(EndService.class);
        } finally {
            endServiceDone = true;
        }
    }
    
    /*
     * Helper method to invoke any lifecycle method
     */
    private void invokeLifeCycleMethod(Class annType) {
        Class c = getImplementorClass();
        Method[] methods = c.getDeclaredMethods();
        boolean once = false;
        for(final Method method : methods) {
            if (method.getAnnotation(annType) != null) {
                if (once) {
                    // Err: Multiple methods have @xxxService annotation
                    throw new ServerRtException("more.begin.or.end.service");
                }
                if (method.getParameterTypes().length != 0) {              
                    throw new ServerRtException("not.zero.parameters",
                        method.getName());
                }
                invokeMethod(method, new Object[]{ });
                once = true;
            } 
        }
    }
    
    public Executor getExecutor() {
        return null;
    }

    public void setExecutor(Executor executor) {
       
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
    
    // Fill DocInfo with document info : WSDL or Schema, targetNS etc.
    public static void fillDocInfo(RuntimeEndpointInfo endpointInfo)
    throws XMLStreamException {
        Map<String, DocInfo> metadata = endpointInfo.getDocMetadata();
        if (metadata != null) {
            for(Entry<String, DocInfo> entry: metadata.entrySet()) {
                RuntimeWSDLParser.fillDocInfo(entry.getValue(), 
                    endpointInfo.getServiceName(),
                    endpointInfo.getPortTypeName());
            }
        }
    }
    
    public static void publishWSDLDocs(RuntimeEndpointInfo endpointInfo) {     
        // Set queryString for the documents
        Map<String, DocInfo> docs = endpointInfo.getDocMetadata();
		if (docs == null) {
			return;
		}
        Set<Entry<String, DocInfo>> entries = docs.entrySet();
        List<String> wsdlSystemIds = new ArrayList<String>();
        List<String> schemaSystemIds = new ArrayList<String>();
        for(Entry<String, DocInfo> entry : entries) {
            DocInfo docInfo = (DocInfo)entry.getValue();
            DOC_TYPE docType = docInfo.getDocType();
            String query = docInfo.getQueryString();
            if (query == null && docType != null) {
                switch(docType) {
                    case WSDL :                   
                        wsdlSystemIds.add(entry.getKey());
                        break;
                    case SCHEMA : 
                        schemaSystemIds.add(entry.getKey());
                        break;
                    case OTHER :
                        //(docInfo.getUrl()+" is not a WSDL or Schema file.");
                }
            }
        }
        
        Collections.sort(wsdlSystemIds);
        int wsdlnum = 1;
        for(String wsdlSystemId : wsdlSystemIds) {
            DocInfo docInfo = docs.get(wsdlSystemId);
            docInfo.setQueryString("wsdl="+(wsdlnum++));
        }
        Collections.sort(schemaSystemIds);
        int xsdnum = 1;
        for(String schemaSystemId : schemaSystemIds) {
            DocInfo docInfo = docs.get(schemaSystemId);
            docInfo.setQueryString("xsd="+(xsdnum++));
        }
        endpointInfo.updateQuery2DocInfo();
    }

}
