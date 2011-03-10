/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
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

package fromwsdl.mime.simple_rpclit.client;

import junit.framework.TestCase;
import testutil.AttachmentHelper;

import javax.activation.DataHandler;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceException;
import java.awt.*;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;


/**
 * @author Jitendra Kotamraju
 */
public class HelloLiteralTest extends TestCase {

    private CatalogPortType port;
    private AttachmentHelper helper = new AttachmentHelper();

    public HelloLiteralTest(String name) throws Exception {
        super(name);
        CatalogService service = new CatalogService();
        port = service.getCatalogPort();
    }

    public void testHello() throws Exception {
        String ret = port.echoString("Hello");
        assertTrue(ret.equals("Hello"));
    }

    public void testEchoAllAttachmentTypesTest() throws Exception {
        Holder<Source> attach10 = new Holder<Source>();
        attach10.value = getSource("sample.xml");
        VoidRequest request = new VoidRequest();
        OutputResponseAll response = port.echoAllAttachmentTypes(request, attach10);
    }

    public void testGetFooWithMimeDisabled() {
        FooType ft = new FooType();
        ft.setName("SUNW");
        ft.setValue(100);
        FooType resp = port.getFooWithMimeDisabled(true, ft);
        assertTrue(resp.getName().equals("SUNW") && (resp.getValue() == 100));
    }

    public void testDataHandler() throws Exception {
        DataHandler dh = new DataHandler(getSource("sample.xml"), "text/xml");
        DataHandler resp = port.testDataHandler("test", dh);
        assertTrue(AttachmentHelper.compareSource(getSource("sample.xml"), (Source) resp.getContent()));
    }

    public void testNormalWithMimeBinding() {
        Holder<Integer> hdr = new Holder<Integer>(100);
        String resp = port.testNormalWithMimeBinding("Hello World!", hdr);
        assertTrue(resp.equals("Hello World!") && (hdr.value == 100));
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
        assertTrue(status.value.equals("ok"));
        assertTrue(prodNum.value == 12345);
    }

    public void testVoidTest() throws Exception  {
        //java.awt.Image ret = port.voidTest(getImage());
        byte[] req = AttachmentHelper.getImageBytes(getImage("vivek.jpg"), "image/jpeg");
        byte[] ret = port.voidTest(req);
        assertTrue(ret != null);
        assertTrue(Arrays.equals(ret, req));
    }

    public void testGetCatalogWithImages() throws Exception  {
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
            //assertTrue(AttachmentHelper.compareStreamSource(getSampleXML(), thumbnail));
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
            throw new WebServiceException("Cannot load " + image);
        }
        return javax.imageio.ImageIO.read(is);
    }

    private StreamSource getSource(String file) throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream(file);
        if (is == null) {
            throw new WebServiceException("Cannot load " + file);
        }
        return new StreamSource(is);
    }
}
