/*
 * Copyright 2004 Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package provider.xmlbind_ds.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.soap.*;
import org.w3c.dom.Node;
import javax.xml.transform.dom.DOMSource;
import javax.activation.DataSource;
import javax.xml.ws.ServiceMode;
import com.sun.xml.messaging.saaj.packaging.mime.internet.MimeBodyPart;
import com.sun.xml.messaging.saaj.packaging.mime.internet.MimeMultipart;
import com.sun.xml.messaging.saaj.packaging.mime.internet.InternetHeaders;
import java.io.*;
import junit.framework.*;
import javax.xml.ws.BindingType;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.WebServiceProvider;

/**
 * @author Jitendra Kotamraju
 */
@WebServiceProvider
@ServiceMode(value=Service.Mode.MESSAGE)
@BindingType(value=HTTPBinding.HTTP_BINDING)
public class HelloImpl implements Provider<DataSource> {

    private static final JAXBContext jaxbContext = createJAXBContext();
    private int bodyIndex;

    public javax.xml.bind.JAXBContext getJAXBContext(){
        return jaxbContext;
    }
    
    private static javax.xml.bind.JAXBContext createJAXBContext(){
        try{
            return javax.xml.bind.JAXBContext.newInstance(ObjectFactory.class);
        }catch(javax.xml.bind.JAXBException e){
            throw new WebServiceException(e.getMessage(), e);
        }
    }

    private byte[] sendSource() {
        System.out.println("**** sendSource ******");
        String begin = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Body>";
		String end = "</soapenv:Body></soapenv:Envelope>";

        String[] body  = {
            "<HelloResponse xmlns=\"urn:test:types\"><argument xmlns=\"\">foo</argument><extra xmlns=\"\">bar</extra></HelloResponse>",
            "<ans1:HelloResponse xmlns:ans1=\"urn:test:types\"><argument>foo</argument><extra>bar</extra></ans1:HelloResponse>"
        };
        int i = (++bodyIndex)%body.length;
		String content = begin+body[i]+end;
		return content.getBytes();
    }

    private void recvBean(Source source) throws Exception {
        System.out.println("**** recvBean ******");
        Hello_Type hello = (Hello_Type)jaxbContext.createUnmarshaller().unmarshal(source);
        if ("Dispatch".equals(hello.getArgument())) {
            throw new WebServiceException("hello.getArgument() got ="+
				hello.getArgument());
		}
        if ("test".equals(hello.getExtra())) {
            throw new WebServiceException("hello.getExtra() got ="+
				hello.getExtra());
		}
    }

    public DataSource invoke(DataSource dataSource) {

        System.out.println("**** Received in Provider Impl ******");

        try {
			final MimeMultipart multipart = new MimeMultipart(dataSource, null);
			MimeBodyPart bodyPart = (MimeBodyPart)multipart.getBodyPart(0);
			InputStream is = bodyPart.getInputStream();
			Source source = bodyPart.getContentType().indexOf("xml") > 0 ? 
                            new StreamSource(is) : new org.jvnet.fastinfoset.FastInfosetSource(is);
			SOAPMessage msg = MessageFactory.newInstance().createMessage();
			msg.getSOAPPart().setContent(source);
			Node node = msg.getSOAPBody().getFirstChild();
			recvBean(new DOMSource(node));

			final MimeMultipart resp = new MimeMultipart("related");
                        resp.getContentType().setParameter("type", "text/xml");
			InternetHeaders hdrs = new InternetHeaders();
			hdrs.setHeader("Content-Type", "text/xml");
            byte[] bytes = sendSource();
			MimeBodyPart body = new MimeBodyPart(hdrs, bytes, bytes.length);
			resp.addBodyPart(body);

            return new DataSource() {
                public InputStream getInputStream() {
                    try {
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        resp.writeTo(bos);
                        bos.close();
                        return new ByteArrayInputStream(bos.toByteArray());
                    } catch(Exception ioe) {
                        throw new WebServiceException("Cannot give DataSource", ioe);
                    }
                }

                public OutputStream getOutputStream() {
                    return null;
                }

                public String getContentType() {
                    return resp.getContentType().toString();
                }

                public String getName() {
                    return "";
                }
            };

        } catch(Exception e) {
            e.printStackTrace();
            throw new WebServiceException("Provider endpoint failed", e);
        }
    }
}
