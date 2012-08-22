/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004-2012 Oracle and/or its affiliates. All rights reserved.
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

package fromwsdl.mime.simple_doclit.client;

import junit.framework.TestCase;
import testutil.AttachmentHelper;

import javax.activation.DataHandler;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Holder;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import java.awt.Image;
import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.io.InputStream;


public class HelloLiteralTest extends TestCase {

    private static CatalogPortType port;
    private AttachmentHelper helper = new AttachmentHelper();

    public HelloLiteralTest(String name) throws Exception {
        super(name);
        CatalogService service = new CatalogService();
        port = service.getCatalogPort();
    }

    public void testEchoImageWithInfo() throws Exception {
        String imageType = "image/jpeg";
        Holder<String> outImageType = new Holder<String>();
        Holder<Image> outImage = new Holder<Image>();
        Map<String, DataHandler> att = new HashMap<String, DataHandler>();
        att.put("<abcd@example.org>", new DataHandler(getSource("sample.xml"), "text/xml"));
        ((BindingProvider) port).getRequestContext().put(MessageContext.OUTBOUND_MESSAGE_ATTACHMENTS, att);
        port.echoImageWithInfo(imageType, getImage("vivek.jpg"), outImageType, outImage);
        assertEquals(outImageType.value, imageType);
        assertNotNull(outImage.value);
        assertTrue(AttachmentHelper.compareImages(getImage("vivek.jpg"), outImage.value));
    }

    public void testGetFooWithMimeDisabled() {
        FooType ft = new FooType();
        ft.setName("ORACLEW");
        ft.setValue(100);
        FooType resp = port.getFooWithMimeDisabled(true, ft);
        assertEquals(resp.getName(), "ORACLEW");
        assertEquals(resp.getValue(), 100);
    }

    public void testDataHandler() throws Exception {
        DataHandler dh = new DataHandler(getSource("sample.xml"), "text/xml");
        DataHandler resp = port.testDataHandler("test", dh);
        assertTrue(AttachmentHelper.compareSource(getSource("sample.xml"), (Source) resp.getContent()));
    }

    public void testNormalWithMimeBinding() {
        Holder<Integer> hdr = new Holder<Integer>(100);
        String resp = port.testNormalWithMimeBinding("Hello World!", hdr);
        assertEquals(resp, "Hello World!" );
        assertEquals((int)hdr.value, 100);
    }

    public void testDefaultTypeTest() {
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
        port.defaultTypeTest("tv", pdt, prodNum, status);
        assertEquals(status.value, "ok");
        assertEquals((int)prodNum.value, 12345);
    }

    public void testVoidTest() throws Exception {
        byte[] req = AttachmentHelper.getImageBytes(getImage("vivek.jpg"), "image/jpeg");
        byte[] ret = port.voidTest(req);
        assertNotNull(ret);
        assertTrue(Arrays.equals(ret, req));
    }


    public void testGetCatalogWithImages() throws Exception {
        GetCatalogWithImagesType cit = new GetCatalogWithImagesType();
        DataHandler dh = new DataHandler(getSource("sample.xml"), "text/xml");
        cit.setThumbnail(dh);
        ProductCatalogType out1 = port.getCatalogWithImages(cit);
        List<ProductType> out = out1.getProduct();

        assertTrue((out != null) && (out.size() > 0));

        for (ProductType pt : out) {
            //System.out.println("\nProductType ["+i+"]:");

            //System.out.println("Name: "+ pt.getName()+"\n"+
            //  "Description: "+ pt.getDescription()+"\n"+
            // "ProductNumber: "+ pt.getProductNumber()+"\n"+
            // "Category: "+ pt.getCategory()+"\n"+
            // "Brand: "+ pt.getBrand()+"\n"+
            // "Price: "+ pt.getPrice()+"\n"+
            // "Thumbnail: "+pt.getThumbnail());
            DataHandler tn = pt.getThumbnail();
            assertTrue(tn != null);
            assertTrue(tn.getContent() instanceof StreamSource);
            StreamSource thumbnail = (StreamSource) tn.getContent();
            assertTrue(AttachmentHelper.compareSource(getSource("sample.xml"), thumbnail));
        }
    }

    // SERVER SIDE is not populating imageHolder, sourceHolder
    // so not running this test
    public void xtestGetAProductDetailsType() throws Exception {
        GetProductDetailsType input = new GetProductDetailsType();
        input.setProductNumber(1);
        Holder<Image> imageHolder = new Holder<Image>();
        Holder<Source> sourceHolder = new Holder<Source>();
        Holder<ProductDetailsType> bodyHolder = new Holder<ProductDetailsType>();
        port.getProductDetails(input, bodyHolder, imageHolder, sourceHolder);
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


        assertTrue(imageHolder.value != null);
        assertTrue(AttachmentHelper.compareImages(getImage("vivek.jpg"), imageHolder.value));

        assertTrue(sourceHolder.value != null);
        assertTrue(AttachmentHelper.compareSource(getSource("sample.xml"), sourceHolder.value));
    }

    private Image getImage(String image) throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream(image);
        if (is == null) {
            throw new WebServiceException("Cannot load "+image);
        }
        return javax.imageio.ImageIO.read(is);
    }

    private StreamSource getSource(String file) throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream(file);
        if (is == null) {
            throw new WebServiceException("Cannot load "+file);
        }
        return new StreamSource(is);
    }

}
