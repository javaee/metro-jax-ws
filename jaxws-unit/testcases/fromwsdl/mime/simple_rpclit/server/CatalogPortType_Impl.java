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

package fromwsdl.mime.simple_rpclit.server;

import javax.activation.DataHandler;
import javax.jws.WebService;
import javax.xml.transform.Source;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceException;
import java.awt.*;
import java.math.BigDecimal;

/**
 * @author Jitendra Kotamraju
 */
@WebService(endpointInterface = "fromwsdl.mime.simple_rpclit.server.CatalogPortType")
public class CatalogPortType_Impl {

    public OutputResponseAll echoAllAttachmentTypes(VoidRequest request,
                                                    Holder<Source> attach10) {

        OutputResponseAll theResponse = new OutputResponseAll();
        theResponse.setResult("ok");
        theResponse.setReason("ok");

        if (attach10 == null || attach10.value == null) {
            if (theResponse.getReason().equals("ok"))
                theResponse.setReason("attach10.value is null (unexpected)");
            else
                theResponse.setReason(theResponse.getReason() +
                        "\nattach10.value is null (unexpected)");
            theResponse.setResult("not ok");
        }
        return theResponse;
    }

    public FooType getFooWithMimeDisabled(boolean param, FooType foo) {
        return foo;
    }

    public String testNormalWithMimeBinding(String inBody, Holder<Integer> header) {
        return inBody;
    }

    public void defaultTypeTest(String order, ProductDetailsType pdt,
            Holder<Integer> prodNum, Holder<String> status) {

        if (!order.equals("tv"))
            throw new WebServiceException("Wrong order! expected \"tv\", got: " + order);

        DimensionsType dt = pdt.getDimensions();
        if ((dt.getHeight() != 36) || (dt.getWidth() != 24) || (dt.getDepth() != 36))
            throw new WebServiceException("Wrong Dimension! height: " + dt.getHeight() + ", width: " + dt.getWidth() + ", depth: " + dt.getDepth());

        if (!pdt.getDimensionsUnit().equals("Inch") || (pdt.getWeight() != 125) || !pdt.getWeightUnit().equals("LB"))
            throw new WebServiceException("Wrong ProductDetailsType! DimensionType: " + pdt.getDimensionsUnit() + ", wwight: " +
                    pdt.getWeight() + ", weightUnit: " + pdt.getWeightUnit());

        prodNum.value = 12345;
        status.value = "ok";
    }

    public DataHandler testDataHandler(String body, DataHandler attachIn) {
        return attachIn;
    }

    public String echoString(String input) {
        return input;
    }

    public byte[] voidTest(byte[] attach1) {
        return attach1;
    }

    public ProductCatalogType getCatalogWithImages(GetCatalogWithImagesType request) {
        DataHandler tn = request.getThumbnail();
        ProductCatalogType pct = new ProductCatalogType();
        java.util.List<ProductType> pts = pct.getProduct();
        ProductType pt = new ProductType();
        pt.setName("JAXWS2.0");
        pt.setBrand("SUN");
        pt.setCategory("Web Services");
        pt.setDescription("WEB SERVICE Development");
        pt.setPrice(new BigDecimal("2000"));
        pt.setProductNumber(1);
        pt.setThumbnail(tn);
        pts.add(pt);
        return pct;
    }

    // Not invoked by client since it doesn't populate image, and xml
    public void getProductDetails(GetProductDetailsType request,
                                  Holder<ProductDetailsType> body,
                                  Holder<Image> pic, Holder<Source> specs) {
        //pic.value = getImage();
        //specs.value = getSampleXML();
        if (request.getProductNumber() == 1) {
            DimensionsType dt = new DimensionsType();
            dt.setHeight(36);
            dt.setWidth(24);
            dt.setDepth(36);
            ProductDetailsType pdt = new ProductDetailsType();
            pdt.setDimensions(dt);
            pdt.setDimensionsUnit("Inch");
            pdt.setWeight(125);
            pdt.setWeightUnit("LB");
            body.value = pdt;
        }
    }

}
