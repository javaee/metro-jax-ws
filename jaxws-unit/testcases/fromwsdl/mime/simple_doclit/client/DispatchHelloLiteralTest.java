/*
 * Copyright (c) 2006 Sun Microsystems, Inc.
 * All Rights Reserved.
 */
package fromwsdl.mime.simple_doclit.client;

import junit.framework.TestCase;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import testutil.AttachmentHelper;
import testutil.ClientServerTestUtil;

import javax.activation.DataHandler;
import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.soap.SOAPBinding;
import java.awt.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * @author JAX-RPC RI Development Team
 */
public class DispatchHelloLiteralTest extends TestCase {

    private AttachmentHelper helper = new AttachmentHelper();
    private static final String tns =
        "http://www.ws-i.org/SampleApplications/SupplyChainManagement/2003-07/Catalog.wsdl";
    private static final QName serviceQName = new QName(tns, "CatalogService");
    private static final QName portQName = new QName(tns, "CatalogPort");
    private String bindingIdString = SOAPBinding.SOAP11HTTP_BINDING;

    private String endpointAddress;
    private Service service;
    private Service serviceWithPorts;
    private Dispatch dispatch;

    public DispatchHelloLiteralTest(String name) throws Exception {
        super(name);

        if(ClientServerTestUtil.useLocal())
            endpointAddress = "local://"+new File(System.getProperty("tempdir")).getAbsolutePath().replace('\\','/')+'?'+portQName.getLocalPart();
        else
            endpointAddress = "http://localhost:8080/jaxrpc-fromwsdl_mime_simple_doclit/simple";

    }

    private void createService() {

        try {
            service = Service.create(serviceQName);
        } catch (WebServiceException e) {
            e.printStackTrace();
        }
    }

    private Dispatch createDispatchJAXB() {
        return null;
    }

    protected String setTransport(String endpoint) {
           try {

               if (ClientServerTestUtil.useLocal()) {
                  URI uri = new URI(endpoint);
                  return uri.resolve(new URI("local", uri.getPath(), uri.getFragment())).toString();
               }

           } catch (Exception ex) {
               ex.printStackTrace();
           }
        return endpoint;
       }


    private Dispatch createDispatchSource() {
        try {
            service.addPort(portQName, bindingIdString, setTransport(endpointAddress));
            dispatch = service.createDispatch(portQName, Source.class,
                Service.Mode.PAYLOAD);

        } catch (WebServiceException e) {
            e.printStackTrace();
        }
        return dispatch;
    }

    private Dispatch createDispatchSOAPMessage() {
        try {
            service.addPort(portQName, bindingIdString, setTransport(endpointAddress));
            dispatch = service.createDispatch(portQName, SOAPMessage.class,
                Service.Mode.MESSAGE);

        } catch (WebServiceException e) {
            e.printStackTrace();
        }
        return dispatch;
    }

    private Dispatch createDispatchSOAPMessageSource() {
        try {
            service.addPort(portQName, bindingIdString, setTransport(endpointAddress));
            dispatch = service.createDispatch(portQName, Source.class,
                Service.Mode.MESSAGE);


        } catch (WebServiceException e) {
            e.printStackTrace();
        }
        return dispatch;
    }

    private Dispatch getDispatchJAXB() {
        createService();
        return createDispatchJAXB();
    }

    private Dispatch getDispatchSource() {
        createService();
        return createDispatchSource();
    }

    private Dispatch getDispatchSOAPMessage() {
        createService();
        return createDispatchSOAPMessage();
    }

    private Dispatch getDispatchSOAPMessageSource() {
        createService();
        return createDispatchSOAPMessageSource();
    }

    private Source makeSaxSource(String msg) {

        byte[] bytes = msg.getBytes();
        ByteArrayInputStream saxinputStream = new ByteArrayInputStream(bytes);
        InputSource inputSource = new InputSource(saxinputStream);
        return new SAXSource(inputSource);
    }

    private Source makeStreamSource(String msg) {

        byte[] bytes = msg.getBytes();
        ByteArrayInputStream sinputStream = new ByteArrayInputStream(bytes);
        return new StreamSource(sinputStream);
    }

    private Source makeDOMSource(String msg) {

        byte[] bytes = msg.getBytes();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);

        return new DOMSource(createDOMNode(inputStream));
    }

    public Node createDOMNode(InputStream inputStream) {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setValidating(false);
        try {
            DocumentBuilder builder = dbf.newDocumentBuilder();
            try {
                return builder.parse(inputStream);
            } catch (SAXException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        } catch (ParserConfigurationException pce) {
            IllegalArgumentException iae = new IllegalArgumentException(pce.getMessage());
            iae.initCause(pce);
            throw iae;
        }
        return null;
    }

    /*private static javax.xml.bind.JAXBContext createJAXBContext() {
        try {
            return javax.xml.bind.JAXBContext.newInstance(client.dispatch.wsdl_hello_lit.client.ObjectFactory.class);
        } catch (javax.xml.bind.JAXBException e) {
            throw new WebServiceException(e.getMessage(), e);
        }
    }
    */
    private SOAPMessage getSOAPMessage(Source msg) throws Exception {
        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage message = factory.createMessage();
        message.getSOAPPart().setContent((Source) msg);
        message.saveChanges();
        return message;
    }

    private String sourceToXMLString(Source result) {

        String xmlResult = null;
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            OutputStream out = new ByteArrayOutputStream();
            StreamResult streamResult = new StreamResult();
            streamResult.setOutputStream(out);
            transformer.transform(result, streamResult);
            xmlResult = streamResult.getOutputStream().toString();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return xmlResult;
    }

//    public void testHello() throws Exception {
//        try {
//            String ret = port.echoString("Hello");
//            assertTrue(ret.equals("Hello"));
//        } catch (Exception e) {
//            e.printStackTrace();
//            assertTrue(false);
//        }
//    }

    /* public void testEchoImageWithInfo(){
        try{
            String imageType = "image/jpeg";
            Holder<String> outImageType = new Holder<String>();
            Holder<Image> outImage = new Holder<Image>();
            DispatchHelloLiteralTest.port.echoImageWithInfo(imageType, getImage(), outImageType, outImage);
            assertTrue(outImageType.value.equals(imageType));
            assertTrue(outImage.value != null);
            assertTrue(AttachmentHelper.compareImages(getImage(), outImage.value));
        }catch(Exception e){
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public void testGetFooWithMimeDisabled(){
        try{
            FooType ft = new FooType();
            ft.setName("SUNW");
            ft.setValue(100);
            FooType resp = DispatchHelloLiteralTest.port.getFooWithMimeDisabled(true, ft);
            assertTrue(resp.getName().equals("SUNW") && (resp.getValue() == 100));
        } catch(Exception e){
            e.printStackTrace();
            assertTrue(false);
        }
    }
    */

    public void testDataHandler() {

        Dispatch dispatch = getDispatchSource();
        Source src = makeStreamSource(testDHBodyPayload);

        DataHandler dh = new DataHandler(getSampleXML(), "text/xml");
        Map<String, DataHandler> attMap = new HashMap();
        //make content id the wsdl part name
        attMap.put("<attachIn=1234@example.org>", dh);
        Map<String, Object> requestContext = dispatch.getRequestContext();
        requestContext.put(MessageContext.OUTBOUND_MESSAGE_ATTACHMENTS, attMap);

        Object result = dispatch.invoke(src);
        assertTrue(result == null);

        HashMap<String, DataHandler> attresult = (HashMap<String, DataHandler>)
                dispatch.getResponseContext().get(MessageContext.INBOUND_MESSAGE_ATTACHMENTS);
        Object content = null;
        if (attresult != null) {
            for (Map.Entry<String, DataHandler> att : attresult.entrySet()) {
                System.out.println("the content id " + att.getKey());
                try {
                    content = att.getValue().getContent();
                    System.out.println("Content is " + content.getClass());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                assertTrue(AttachmentHelper.compareSource(getSampleXML(), (Source) content));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else
           fail("mime test with dispatch failed");
    }

    /* public void testNormalWithMimeBinding(){
         try{
             Holder<Integer> hdr = new Holder<Integer>(100);
             String resp = DispatchHelloLiteralTest.port.testNormalWithMimeBinding("Hello World!", hdr);
             assertTrue(resp.equals("Hello World!") && (hdr.value == 100));
         }catch(Exception e){
             e.printStackTrace();
             assertTrue(false);
         }
     }

     public void testDefaultTypeTest(){
         try {
             DimensionsType dt = new DimensionsType();
             dt.setHeight(36);
             dt.setWidth(24);
             dt.setDepth(36);
             ProductDetailsType pdt = new ProductDetailsType();
             pdt.setDimensions(dt);
             pdt.setDimensionsUnit("Inch");
             pdt.setWeight(125);
             pdt.setWeightUnit("LB");
             Holder<String> status = new Holder<String>();
             Holder<Integer> prodNum = new Holder<Integer>();
             DispatchHelloLiteralTest.port.defaultTypeTest("tv", pdt, prodNum, status);
             assertTrue(status.value.equals("ok"));
             assertTrue(prodNum.value == 12345);
         } catch (Exception e) {
             e.printStackTrace();
             assertTrue(false);
         }
     }


     public void testVoidTest() {
         try {
             //java.awt.Image ret = port.voidTest(getImage());
             byte[] req = AttachmentHelper.getImageBytes(getImage(), "image/jpeg");
             byte[] ret = DispatchHelloLiteralTest.port.voidTest(req);
             assertTrue(ret != null);
             assertTrue(Arrays.equals(ret, req));
         } catch (Exception e) {
             e.printStackTrace();
             assertTrue(false);
         }
     }


     public void testGetCatalogWithImages() {
         try{
             //invoke the method
             GetCatalogWithImagesType cit = new GetCatalogWithImagesType();
             DataHandler dh = new DataHandler(getSampleXML(), "text/xml");
             cit.setThumbnail(dh);
             ProductCatalogType out1 = DispatchHelloLiteralTest.port.getCatalogWithImages(cit);
             java.util.List<ProductType> out = out1.getProduct();

             assertTrue((out != null) && (out.size() > 0));

             for(ProductType pt : out) {
                 //System.out.println("\nProductType ["+i+"]:");

                 //System.out.println("Name: "+ pt.getName()+"\n"+
                   //  "Description: "+ pt.getDescription()+"\n"+
                    // "ProductNumber: "+ pt.getProductNumber()+"\n"+
                    // "Category: "+ pt.getCategory()+"\n"+
                    // "Brand: "+ pt.getBrand()+"\n"+
                    // "Price: "+ pt.getPrice()+"\n"+
                    // "Thumbnail: "+pt.getThumbnail());
                 DataHandler tn = pt.getThumbnail();
                 assertTrue(tn!=null);
                 assertTrue(tn.getContent() instanceof StreamSource);
                 StreamSource thumbnail =  (StreamSource)tn.getContent();
                 assertTrue(AttachmentHelper.compareSource(getSampleXML(), thumbnail));
                 //assertTrue(AttachmentHelper.compareStreamSource(getSampleXML(), thumbnail));
             }
         }catch (Exception e){
             e.printStackTrace();
             assertTrue(false);
         }
     }

     public void testGetAProductDetailsType() {
        invokeGetProductDetails(1);
     }
    */
    /*private void invokeGetProductDetails(int productNumber) {
        try {
            GetProductDetailsType input = new GetProductDetailsType();
            input.setProductNumber(productNumber);
            Holder<Image> imageHolder = new Holder<Image>();
            Holder<Source> sourceHolder = new Holder<Source>();
            Holder<ProductDetailsType> bodyHolder = new Holder<ProductDetailsType>();
            DispatchHelloLiteralTest.port.getProductDetails(input, bodyHolder, imageHolder, sourceHolder);
            ProductDetailsType output = bodyHolder.value;
            assertTrue(output != null);
            /*
            System.out.println("--------------getProductDetails() response...");
            if(output == null)
                System.out.println("No response!");
            else {
                System.out.println("ProductDetailsType:");
                System.out.println("Weight: "+output.getWeight() +"\n"+
                                    "Weight Unit: "+output.getWeightUnit() + "\n" +
                                    "Dimension Unit: "+output.getDimensionsUnit() +"\n"+
                                    "Dimension:\n"+
                                    "\t"+"Height: "+output.getDimensions().getHeight() +"\n"+
                                    "\t"+"Weight: "+output.getDimensions().getWidth() +"\n"+
                                    "\t"+"Depth: "+output.getDimensions().getDepth());
            }
            */

    /*      assertTrue(imageHolder.value != null);
            assertTrue(AttachmentHelper.compareImages(getImage(), imageHolder.value));

            assertTrue(sourceHolder.value != null);
            assertTrue(AttachmentHelper.compareStreamSource(getSampleXML(), sourceHolder.value));
        }catch(Exception e){
            e.printStackTrace();
            assertTrue(false);
        }
    }
    */

    private Image getImage() throws Exception {
        String image = getDataDir() + "vivek.jpg";
        return javax.imageio.ImageIO.read(new File(image));
    }

    private String getDataDir() {
        String userDir = System.getProperty("user.dir");
        String sepChar = System.getProperty("file.separator");
        return userDir + sepChar + "testcases/fromwsdl/mime/simple_doclit/resources/";
         //return userDir + sepChar + "../WEB-INF/";

    }

/*
    private static String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"+
            "<START><A>Hello</A><B>World</B></START>";

    private StreamSource getSpec(){
        StringReader reader = new StringReader(xml);
        return new StreamSource(reader);
    }
  */

    private StreamSource getSampleXML() {
        try {
            String location = getDataDir() + "sample.xml";
            File f = new File(location);
            FileInputStream is = new FileInputStream(f);
            return new StreamSource(is);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private int getSampleXMLLength() {
        String location = getDataDir() + "sample.xml";
        File f = new File(location);
        return (int) f.length();
    }


    private static final String testDHBodyPayload = "<ns1:testDataHandlerBody xmlns:ns1=\"http://www.ws-i.org/SampleApplications/SupplyChainManagement/2003-07/Catalog.xsd\" xmlns:ns2=\"http://www.ws-i.org/SampleApplications/SupplyChainManagement/2003-07/Catalog.wsdl\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">test</ns1:testDataHandlerBody>";

}
