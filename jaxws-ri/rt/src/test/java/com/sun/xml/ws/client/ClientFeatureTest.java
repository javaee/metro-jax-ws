package com.sun.xml.ws.client;

import java.net.URL;

import javax.activation.DataSource;
import javax.xml.namespace.QName;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.http.HTTPBinding;

import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.WSFeatureList;
import com.sun.xml.ws.developer.SerializationFeature;

import junit.framework.TestCase;

public class ClientFeatureTest extends TestCase {
    private static final QName SERVICE_NAME = new QName("http://performance.bea.com", "DocService");
    private static final QName PORT_NAME = new QName("http://performance.bea.com", "DocServicePortTypePort");
    private static final String ENDPOINT = "http://localhost:7001/DocService/DocService";
   
    private DocServicePortType createProxy(DocService service, String encoding) throws Exception {
        if (encoding==null) {
            return service.getDocServicePortTypePort();
        } else {
            return service.getDocServicePortTypePort(new SerializationFeature(encoding));
        }
    }
    
    private Dispatch<DataSource> createDispatch(Service service, String encoding) throws Exception {
        if (encoding==null) {
            return service.createDispatch(PORT_NAME, DataSource.class, Mode.MESSAGE);
        } else {
            return service.createDispatch(PORT_NAME, DataSource.class, Mode.MESSAGE, new SerializationFeature(encoding));
        }
    }
    
    public void testSameServiceDifferentDispatch() throws Exception {
        Service service = Service.create(SERVICE_NAME);
        service.addPort(PORT_NAME, HTTPBinding.HTTP_BINDING, ENDPOINT);
        
        String encoding = "UTF-16";
        Dispatch<DataSource> dispatch1 = createDispatch(service, encoding);
        validateFeatureList(dispatch1, encoding);
        
        encoding = null;
        Dispatch<DataSource> dispatch2 = createDispatch(service, encoding);
        validateFeatureList(dispatch2, encoding);
    }
    
    public void testSameServiceDifferentPort() throws Exception {
        URL wsdlUrl = getClass().getResource("service.wsdl");
        DocService service = new DocService(wsdlUrl, SERVICE_NAME);
        
        String encoding = "UTF-16";
        DocServicePortType proxy1 = createProxy(service, encoding);
        validateFeatureList(proxy1, encoding);
        
        encoding = null;
        DocServicePortType proxy2 = createProxy(service, encoding);
        validateFeatureList(proxy2, encoding);
    }
    
    private void validateFeatureList(Object bindingProvider, String expectedEncoding) throws Exception {
        Binding binding = ((BindingProvider)bindingProvider).getBinding();
        WSFeatureList list = (((WSBinding)binding).getFeatures());
        //System.out.println(list);
        SerializationFeature encoding = list.get(SerializationFeature.class);
        if (expectedEncoding==null) {
            assertNull("There should not be a SerializationFeature", encoding);
        } else {
            assertEquals("Mismatched encoding in SerializationFeature", expectedEncoding, encoding.getEncoding());
        }
    }
}
