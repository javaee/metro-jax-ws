/*
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All Rights Reserved.
 */
package restful.client;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;

import org.w3c.dom.Node;

import java.net.URL;
import java.net.URI;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.Dispatch;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.http.HTTPBinding;
import java.io.ByteArrayOutputStream;
import java.util.Properties;
import java.util.Map;

public class DispatchAddNumbersClient {

    private static final QName serviceQName = new QName("http://duke.org", "AddNumbersService");
    private static final QName portQName = new QName("http://duke.org", "AddNumbersPort");

    private static String endpointAddress =
        "http://localhost:8080/jaxws-restful/addnumbers";
    private static String queryString = "?num1=30&num2=20";
    private static String pathInfo = "/num1/10/num2/20";

    public static void main(String[] args) throws Exception {

        DispatchAddNumbersClient client = new DispatchAddNumbersClient();

        Service service = client.createService();
        URI endpointURI = new URI(endpointAddress.toString());

        String path = null;
        String query = null;
        if (endpointURI != null){
            path = endpointURI.getPath();
            query = endpointURI.getQuery();
        }

        service.addPort(portQName, HTTPBinding.HTTP_BINDING, endpointAddress.toString());

        Dispatch<Source> d = service.createDispatch(portQName, Source.class, Service.Mode.MESSAGE);

        Map<String, Object> requestContext = d.getRequestContext();
        requestContext.put(MessageContext.HTTP_REQUEST_METHOD, new String("GET"));

        requestContext.put(MessageContext.QUERY_STRING, queryString);
        //this is the original path part of uri
        requestContext.put(MessageContext.PATH_INFO, path);
        System.out.println ("Invoking Restful GET Request with path info and query string " + path + queryString);

        Source result = d.invoke(null);
        printSource(result);

        //for this request the query string is not needed
        requestContext.remove(MessageContext.QUERY_STRING);
        //add path information - in this case additional pathinfo is added to original path
        requestContext.put(MessageContext.PATH_INFO, path + pathInfo);

        System.out.println ("Invoking Restful GET Request with path info " + path + pathInfo);

        result = d.invoke(null);
        printSource(result);
    }


    private Service createService() {
        Service service = Service.create(serviceQName);
        return service;
    }

    private static void printSource(Source source) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            StreamResult sr = new StreamResult(bos);
            Transformer trans = TransformerFactory.newInstance().newTransformer();
            Properties oprops = new Properties();
            oprops.put(OutputKeys.OMIT_XML_DECLARATION, "yes");
            trans.setOutputProperties(oprops);
            trans.transform(source, sr);
            System.out.println("**** Response ******" + bos.toString());
            System.out.println("");
            bos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
