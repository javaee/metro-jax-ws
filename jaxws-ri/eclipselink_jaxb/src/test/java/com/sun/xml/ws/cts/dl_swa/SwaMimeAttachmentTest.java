/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
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

package com.sun.xml.ws.cts.dl_swa;

import java.awt.Image;
import java.awt.Rectangle;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.handler.MessageContext;

import com.oracle.webservices.api.databinding.DatabindingModeFeature;
import com.oracle.webservices.api.databinding.JavaCallInfo;
import org.xml.sax.EntityResolver;

import com.oracle.webservices.api.message.ContentType;

import com.sun.xml.ws.InVmWSDLResolver;
import com.sun.xml.ws.WsDatabindingTestBase;
import com.sun.xml.ws.api.databinding.Databinding;
import com.sun.xml.ws.api.databinding.DatabindingConfig;
import com.sun.xml.ws.api.databinding.WSDLGenInfo;
import com.sun.xml.ws.api.message.Attachment;
import com.sun.xml.ws.api.message.AttachmentSet;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.wsdl.parser.WSDLParserExtension;
import com.sun.xml.ws.api.wsdl.writer.WSDLGeneratorExtension;
import com.sun.xml.ws.handler.SOAPMessageContextImpl;
import com.sun.xml.ws.message.DataHandlerAttachment;
import com.sun.xml.ws.model.wsdl.WSDLModelImpl;
import com.sun.xml.ws.util.ServiceFinder;
import com.sun.xml.ws.util.xml.XmlUtil;
import com.sun.xml.ws.wsdl.parser.RuntimeWSDLParser;

public class SwaMimeAttachmentTest extends WsDatabindingTestBase  {
    static public final String ECLIPSELINK_JAXB = "eclipselink.jaxb";
//    static public final String ECLIPSELINK_JAXB = "glassfish.jaxb";
    
    protected DatabindingModeFeature databindingMode() {
        return new DatabindingModeFeature(ECLIPSELINK_JAXB); 
    }

    public void testAttachmentContentId() throws Exception {
        WSDLPort wsdlPort = getWSDLPort(getResource("WSW2JDLSwaTestService.wsdl"));
        Class proxySEIClass = SwaTest1.class;
        WebServiceFeature[] f = { databindingMode() };
        
        DatabindingConfig cliConfig = new DatabindingConfig();
        cliConfig.setContractClass(proxySEIClass);
        cliConfig.setFeatures(f);
        cliConfig.setWsdlPort(wsdlPort);

        cliConfig.setWsdlPort(wsdlPort);
        cliConfig.getMappingInfo().setServiceName(new QName("http://SwaTestService.org/wsdl", "WSIDLSwaTestService"));
        Databinding cli = (Databinding) factory.createRuntime(cliConfig); 

        URL url1 = getResource("attach.text");
        URL url2 = getResource("attach.html");
        URL url3 = getResource("attach.xml");
        URL url4 = getResource("attach.jpeg1");
        URL url5 = getResource("attach.jpeg2");
        DataHandler dh1 = new DataHandler(url1);
        DataHandler dh2 = new DataHandler(url2);
        DataHandler dh3 = new DataHandler(url3);
//        DataHandler dh4 = new DataHandler(url4);
//        DataHandler dh5 = new DataHandler(url5);
        javax.xml.ws.Holder<javax.activation.DataHandler> attach1 = new javax.xml.ws.Holder<javax.activation.DataHandler>();
        attach1.value = dh1;
        javax.xml.ws.Holder<javax.activation.DataHandler> attach2 = new javax.xml.ws.Holder<javax.activation.DataHandler>();
        attach2.value = dh2;
        javax.xml.ws.Holder<javax.xml.transform.Source> attach3 = new javax.xml.ws.Holder<javax.xml.transform.Source>();
        attach3.value = new StreamSource(dh3.getInputStream());
        javax.xml.ws.Holder<java.awt.Image> attach4 = new javax.xml.ws.Holder<java.awt.Image>();
        javax.xml.ws.Holder<java.awt.Image> attach5 = new javax.xml.ws.Holder<java.awt.Image>();
        attach4.value = javax.imageio.ImageIO.read(url4);
        attach5.value = javax.imageio.ImageIO.read(url5);
        VoidRequest request = new VoidRequest();
        Object[] args = { request, attach1, attach2, attach3, attach4, attach5 };
        Method method = findMethod(proxySEIClass, "echoAllAttachmentTypes");
        JavaCallInfo cliCall = cli.createJavaCallInfo(method, args);
        Packet cliSoapReq = (Packet)cli.serializeRequest(cliCall);

        SOAPMessageContextImpl smc = new SOAPMessageContextImpl(null, cliSoapReq, null);
        Map<String, DataHandler> smcAtts1 = (Map<String, DataHandler>) smc.get(MessageContext.OUTBOUND_MESSAGE_ATTACHMENTS);
        smc.put(MessageContext.OUTBOUND_MESSAGE_ATTACHMENTS, smcAtts1);
        assertEquals( 5, smcAtts1.size() );
        for (String cid : smcAtts1.keySet()) 
            assertTrue(cid.charAt(0)!='<');
        
        for (com.sun.xml.ws.api.message.Attachment a : cliSoapReq.getMessage().getAttachments()) 
            assertTrue(a.getContentId().charAt(0)!='<');
        
        Object s1 = cliSoapReq.getAsSOAPMessage();
        Object s2 = smc.getMessage();
        assertTrue(s1 == s2);
        
        for (com.sun.xml.ws.api.message.Attachment a : cliSoapReq.getMessage().getAttachments()) 
            assertTrue(a.getContentId().charAt(0)!='<');
//        {
//        Map<String, DataHandler> atts = (Map<String, DataHandler>) smc.get(MessageContext.OUTBOUND_MESSAGE_ATTACHMENTS);
//        AttachmentSet attSet = cliSoapReq.getMessage().getAttachments();
//        for(String cid : atts.keySet()){
//            if (attSet.get(cid) == null) {  // Otherwise we would be adding attachments twice
//                Attachment att = new DataHandlerAttachment(cid, atts.get(cid));
//                attSet.add(att);
//            }
//        }
//        }


        Map<String, DataHandler> smcAtts2 = (Map<String, DataHandler>) smc.get(MessageContext.OUTBOUND_MESSAGE_ATTACHMENTS);
        assertEquals( 5, smcAtts1.size() );
        for (String cid : smcAtts2.keySet()) assertTrue(cid.charAt(0)!='<');
    }


    public void testCustomAttachmentContentId() throws Exception {
        WSDLPort wsdlPort = getWSDLPort(getResource("WSW2JDLSwaTestService.wsdl"));
        Class proxySEIClass = SwaTest1.class;
        WebServiceFeature[] f = { databindingMode() };
        
        DatabindingConfig cliConfig = new DatabindingConfig();
        cliConfig.setContractClass(proxySEIClass);
        cliConfig.setFeatures(f);
        cliConfig.setWsdlPort(wsdlPort);

        cliConfig.setWsdlPort(wsdlPort);
        cliConfig.getMappingInfo().setServiceName(new QName("http://SwaTestService.org/wsdl", "WSIDLSwaTestService"));
        Databinding cli = (Databinding) factory.createRuntime(cliConfig); 

        URL url1 = getResource("attach.text");
        URL url2 = getResource("attach.html");
        URL url3 = getResource("attach.xml");
        URL url4 = getResource("attach.jpeg1");
        URL url5 = getResource("attach.jpeg2");
        DataHandler dh1 = new DataHandler(url1);
        DataHandler dh2 = new DataHandler(url2);
        DataHandler dh3 = new DataHandler(url3);
        DataHandler dh4 = new DataHandler(url4);
//        DataHandler dh5 = new DataHandler(url5);
        javax.xml.ws.Holder<javax.activation.DataHandler> attach1 = new javax.xml.ws.Holder<javax.activation.DataHandler>();
        attach1.value = dh1;
        javax.xml.ws.Holder<javax.activation.DataHandler> attach2 = new javax.xml.ws.Holder<javax.activation.DataHandler>();
        attach2.value = dh2;
        javax.xml.ws.Holder<javax.xml.transform.Source> attach3 = new javax.xml.ws.Holder<javax.xml.transform.Source>();
        attach3.value = new StreamSource(dh3.getInputStream());
        javax.xml.ws.Holder<java.awt.Image> attach4 = new javax.xml.ws.Holder<java.awt.Image>();
        javax.xml.ws.Holder<java.awt.Image> attach5 = new javax.xml.ws.Holder<java.awt.Image>();
        attach4.value = javax.imageio.ImageIO.read(url4);
        attach5.value = javax.imageio.ImageIO.read(url5);
        VoidRequest request = new VoidRequest();
        Object[] args = { request, attach1, attach2, attach3, attach4, attach5 };
        Method method = findMethod(proxySEIClass, "echoAllAttachmentTypes");
        JavaCallInfo cliCall = cli.createJavaCallInfo(method, args);
        Packet cliSoapReq = (Packet)cli.serializeRequest(cliCall);

        String customContentId = "<abcd@example.org>";
        Map<String, DataHandler> attMap = new HashMap<String, DataHandler>();
        attMap.put(customContentId, dh4);
        cliSoapReq.invocationProperties.put(MessageContext.OUTBOUND_MESSAGE_ATTACHMENTS, attMap);

        SOAPMessageContextImpl smc = new SOAPMessageContextImpl(null, cliSoapReq, null);
        Map<String, DataHandler> smcAtts1 = (Map<String, DataHandler>) smc.get(MessageContext.OUTBOUND_MESSAGE_ATTACHMENTS);

        assertEquals( 6, smcAtts1.size() );
        assertNotNull(smcAtts1.get(customContentId));
        
        {//ClientSOAPHandlerTube.callHandlersOnRequest
            Map<String, DataHandler> atts = (Map<String, DataHandler>) smc.get(MessageContext.OUTBOUND_MESSAGE_ATTACHMENTS);
            AttachmentSet attSet = cliSoapReq.getMessage().getAttachments();
            for(String cid : atts.keySet()){
                if (attSet.get(cid) == null) {  // Otherwise we would be adding attachments twice
                    Attachment att = new DataHandlerAttachment(cid, atts.get(cid));
                    attSet.add(att);
                }
            }
        }

        int attCount = 0;
        for (com.sun.xml.ws.api.message.Attachment a : cliSoapReq.getMessage().getAttachments()) {
//            assertTrue(a.getContentId().charAt(0)!='<'); 
            attCount++;
        }
        assertEquals( 6, attCount);
        Object s1 = cliSoapReq.getAsSOAPMessage();
        Object s2 = smc.getMessage();
        assertTrue(s1 == s2);

        int attCountSaaj = 0;
        for (com.sun.xml.ws.api.message.Attachment a : cliSoapReq.getMessage().getAttachments()) {
            assertTrue(a.getContentId().charAt(0)!='<');
            attCountSaaj++;
        }
        assertEquals( 6, attCountSaaj);
        Map<String, DataHandler> smcAtts2 = (Map<String, DataHandler>) smc.get(MessageContext.OUTBOUND_MESSAGE_ATTACHMENTS);
        assertEquals( 6, smcAtts2.size() );
//        System.out.println(smcAtts2.size() + " " + smcAtts2);
        assertNotNull(smcAtts2.get(customContentId));
    }
    
    public void testCTS_WsiDocLitSwaTest() throws Exception {
        WSDLPort wsdlPort = getWSDLPort(getResource("WSW2JDLSwaTestService.wsdl"));


        Class endpointClass = SwaTestImpl1.class;
        Class proxySEIClass = SwaTest1.class;
        DatabindingConfig srvConfig = new DatabindingConfig();
        srvConfig.setEndpointClass(endpointClass);
//        srvConfig.setMetadataReader(new DummyAnnotations());
        WebServiceFeature[] f = { databindingMode() };
        srvConfig.setFeatures(f);
        srvConfig.setWsdlPort(wsdlPort);
        
        DatabindingConfig cliConfig = new DatabindingConfig();
//        cliConfig.setMetadataReader(new DummyAnnotations());
        cliConfig.setContractClass(proxySEIClass);
        cliConfig.setFeatures(f);
        cliConfig.setWsdlPort(wsdlPort);

        SwaTest1 port = createProxy(SwaTest1.class, srvConfig, cliConfig, false);        
        {
            URL url1 = getResource("attach.text");
            URL url2 = getResource("attach.html");
            URL url3 = getResource("attach.xml");
            URL url4 = getResource("attach.jpeg1");
            URL url5 = getResource("attach.jpeg2");
            DataHandler dh1 = new DataHandler(url1);
            DataHandler dh2 = new DataHandler(url2);
            DataHandler dh3 = new DataHandler(url3);
            DataHandler dh4 = new DataHandler(url4);
            DataHandler dh5 = new DataHandler(url5);
            javax.xml.ws.Holder<javax.activation.DataHandler> attach1 = new javax.xml.ws.Holder<javax.activation.DataHandler>();
            attach1.value = dh1;
            javax.xml.ws.Holder<javax.activation.DataHandler> attach2 = new javax.xml.ws.Holder<javax.activation.DataHandler>();
            attach2.value = dh2;
            javax.xml.ws.Holder<javax.xml.transform.Source> attach3 = new javax.xml.ws.Holder<javax.xml.transform.Source>();
            attach3.value = new StreamSource(dh3.getInputStream());
            javax.xml.ws.Holder<java.awt.Image> attach4 = new javax.xml.ws.Holder<java.awt.Image>();
            javax.xml.ws.Holder<java.awt.Image> attach5 = new javax.xml.ws.Holder<java.awt.Image>();
            attach4.value = javax.imageio.ImageIO.read(url4);
            attach5.value = javax.imageio.ImageIO.read(url5);
            VoidRequest request = new VoidRequest();
            OutputResponseAll response = port.echoAllAttachmentTypes(request,
                    attach1, attach2, attach3, attach4, attach5);
            assertTrue(ValidateRequestResponseAttachmentsEchoAllTestCase(
                    request, response, attach1, attach2, attach3, attach4,
                    attach5));
        }
        {
            InputRequestGet request = new InputRequestGet();
            URL url1 = getResource("attach.text");
            URL url2 = getResource("attach.html");
            request.setMimeType1("text/plain");
            request.setMimeType2("text/html");
            request.setUrl1(url1.toString());
            request.setUrl2(url2.toString());
            javax.xml.ws.Holder<DataHandler> attach1 = new javax.xml.ws.Holder<DataHandler>();
            javax.xml.ws.Holder<DataHandler> attach2 = new javax.xml.ws.Holder<DataHandler>();
            javax.xml.ws.Holder<OutputResponse> response = new javax.xml.ws.Holder<OutputResponse>();
            port.getMultipleAttachments(request, response, attach1, attach2);
            assertTrue(ValidateRequestResponseAttachmentsGetTestCase(request,
                    response.value, attach1, attach2));
        }
        {
            javax.xml.ws.Holder<byte[]> data = new javax.xml.ws.Holder<byte[]>();
//            InputStream in = getSwaAttachmentURL("attach.jpeg1").openStream();
//            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
//            byte[] bytes = new byte[4096];
//            int read = in.read(bytes);
//            while (read != -1) {
//                baos.write(bytes, 0, read);
//                read = in.read(bytes);
//            }

            java.awt.Image image =  javax.imageio.ImageIO.read(getResource("attach.jpeg1"));
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            javax.imageio.ImageIO.write((java.awt.image.RenderedImage) image, "jpeg", baos);
            data.value = baos.toByteArray();
            byte[] bytes = baos.toByteArray();
            port.echoData("EnableMIMEContent = false", data);   
//            for ( int i = 0; i < data.value.length; i++ ) {
//                assertTrue(bytes[i] == data.value[i]);
//            }
        }
    }

    /***************************************************************************
     * Validate request, response and attachments (echoAllAttachmentTypes)
     **************************************************************************/
    private boolean ValidateRequestResponseAttachmentsEchoAllTestCase(
            VoidRequest request, OutputResponseAll response,
            javax.xml.ws.Holder<DataHandler> attach1,
            javax.xml.ws.Holder<DataHandler> attach2,
            javax.xml.ws.Holder<Source> attach3,
            javax.xml.ws.Holder<Image> attach4,
            javax.xml.ws.Holder<Image> attach5) throws Exception {
        boolean result = true;
        URL url1 = getResource("attach.text");
        URL url2 = getResource("attach.html");
        URL url3 = getResource("attach.xml");
        URL url4 = getResource("attach.jpeg1");
        URL url5 = getResource("attach.jpeg2");
        DataHandler dh1 = new DataHandler(url1);
        byte data1[] = new byte[4096];
        byte data2[] = new byte[4096];
        int count1 = dh1.getInputStream().read(data1, 0, 4096);
        int count2 = attach1.value.getInputStream().read(data2, 0, 4096);
        if (!ValidateAttachmentData(count1, data1, count2, data2, "Attachment1"))
            result = false;

        data2 = new byte[4096];
        dh1 = new DataHandler(url2);
        count1 = dh1.getInputStream().read(data1, 0, 4096);
        count2 = attach2.value.getInputStream().read(data2, 0, 4096);
        if (!ValidateAttachmentData(count1, data1, count2, data2, "Attachment2"))
            result = false;

        data2 = new byte[4096];
        dh1 = new DataHandler(url3);
        count1 = dh1.getInputStream().read(data1, 0, 4096);
        count2 = ((StreamSource) attach3.value).getInputStream().read(data2, 0,
                4096);
//        System.out.println("------------------------------ " + count1);
//        System.out.println(new String(data1, 0, count1));
//        System.out.println("------------------------------ " + count2);
//        System.out.println(new String(data2, 0, count2));
////        if (!ValidateAttachmentData(count1, data1, count2, data2, "Attachment3"))
////            result = false;

        dh1 = new DataHandler(url4);
        count1 = dh1.getInputStream().read(data1, 0, 4096);
        Image image1 = javax.imageio.ImageIO.read(url4);
        Image image2 = attach4.value;
        AttachmentHelper helper = new AttachmentHelper();
        if (!helper.compareImages(image1, image2,
                new Rectangle(0, 0, 100, 120), "Attachment4"))
            result = false;

        dh1 = new DataHandler(url5);
        count1 = dh1.getInputStream().read(data1, 0, 4096);
        image1 = javax.imageio.ImageIO.read(url5);
        image2 = attach5.value;
        helper = new AttachmentHelper();
        if (!helper.compareImages(image1, image2,
                new Rectangle(0, 0, 100, 120), "Attachment5"))
            result = false;
        return result;
    }

    private boolean ValidateAttachmentData(int count1, byte[] data1,
            int count2, byte[] data2, String attach) {
        int max = 0;
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        java.io.PrintStream ps = new java.io.PrintStream(baos);
        if (count2 > count1) {
            for (int i = count1; i < count2; i++) {
                if ((char) data2[i] != '\r')
                    break;
            }
            count2 = count1;
        }
        if (count1 != count2) {
            if (count2 > count1)
                max = count1;
            else
                max = count2;
            ps.printf("data1[%d]=0x%x  data2[%d]=0x%x", max - 1,
                    data1[max - 1], max - 1, data2[max - 1]);
            baos.reset();
            if (count2 > count1) {
                for (int i = count1; i < count2; i++) {
                    ps.printf("Extra data was: data2[%d]=0x%x|0%o", i,
                            data2[i], data2[i]);
                    baos.reset();
                }
            } else {
                for (int i = count2; i < count1; i++) {
                    ps.printf("Extra data was: data1[%d]=0x%x|0%o", i,
                            data1[i], data1[i]);
                    baos.reset();
                }
            }
            return false;
        }
        for (int i = 0; i < count1; i++) {
            if (data1[i] != data2[i]) {
                return false;
            }
        }
        return true;
    }

    /***************************************************************************
     * Validate request, response and attachments (getMultipleAttachments)
     **************************************************************************/
    private boolean ValidateRequestResponseAttachmentsGetTestCase(
            InputRequestGet request, OutputResponse response,
            javax.xml.ws.Holder<DataHandler> attach1,
            javax.xml.ws.Holder<DataHandler> attach2) {
        boolean result = true;
        if (!response.getMimeType1().equals(request.getMimeType1())) {
            result = false;
        }
        if (!response.getMimeType2().equals(request.getMimeType2())) {
            result = false;
        } else {
        }
        if (!response.getResult().equals("ok")) {
            result = false;
        } else {
        }
        try {
            DataHandler dh1 = new DataHandler(new URL(request.getUrl1()));
            DataHandler dh2 = new DataHandler(new URL(request.getUrl2()));
            byte data1[] = new byte[4096];
            byte data2[] = new byte[4096];
            int count1 = dh1.getInputStream().read(data1, 0, 4096);
            int count2 = attach1.value.getInputStream().read(data2, 0, 4096);
            if (!ValidateAttachmentData(count1, data1, count2, data2,
                    "Attachment1"))
                result = false;
            count1 = dh2.getInputStream().read(data1, 0, 4096);
            data2 = new byte[4096];
            count2 = attach2.value.getInputStream().read(data2, 0, 4096);
            if (!ValidateAttachmentData(count1, data1, count2, data2,
                    "Attachment2"))
                result = false;
        } catch (Exception e) {
            result = false;
        }
        return result;
    }
    
    static WSDLPort getWSDLPort(URL wsdlLoc) throws Exception {
        EntityResolver er = XmlUtil.createDefaultCatalogResolver();
        WSDLModelImpl wsdl = RuntimeWSDLParser.parse(wsdlLoc, new StreamSource(wsdlLoc.toExternalForm()), er,
                true, null, ServiceFinder.find(WSDLParserExtension.class).toArray());
        QName serviceName = wsdl.getFirstServiceName();
        return wsdl.getService(serviceName).getFirstPort();
    }
    private URL getResource(String str) throws Exception {
//        return new File("D:/oc4j/webservices/devtest/data/cts15/DLSwaTest/" + str).toURL();
        return Thread.currentThread().getContextClassLoader().getResource("etc/"+str);
    }
    
}
