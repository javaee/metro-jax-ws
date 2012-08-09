/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package fromwsdl.mime.simple_doclit.server;

import javax.activation.DataHandler;
import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.transform.Source;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import java.math.BigDecimal;
import java.awt.Image;
import java.util.List;
import java.util.Map;
import javax.xml.ws.handler.MessageContext;


@WebService(endpointInterface = "fromwsdl.mime.simple_doclit.server.CatalogPortType")
public class CatalogPortType_Impl {
    @Resource
    protected WebServiceContext wsContext;

    public void echoImageWithInfo(String inImageType, Image image,
                                  Holder<String> outImageType,
                                  Holder<Image> out_image) {

        outImageType.value = inImageType;
        out_image.value = image;

       MessageContext mc = wsContext.getMessageContext();
       Map<String, DataHandler> attachments = (Map<String, DataHandler>)mc.get(MessageContext.INBOUND_MESSAGE_ATTACHMENTS);

       if(attachments.size() != 2)
           throw new WebServiceException("Expected 2 attachments in MessageContext, received: "+attachments.size()+"!");

       //now copy the received attachemnts to outbound attachemnt property
       mc.put(MessageContext.OUTBOUND_MESSAGE_ATTACHMENTS, attachments);

    }

    public FooType getFooWithMimeDisabled(boolean param, FooType foo) {
        return foo;
    }

    public String testNormalWithMimeBinding(String inBody, Holder<Integer> header) {
        return inBody;
    }

    public void defaultTypeTest(String order, ProductDetailsType pdt,
                                Holder<Integer> prodNum, Holder<String> status) {

        if (!order.equals("tv")) {
            throw new WebServiceException("Wrong order! expected \"tv\", got: " + order);
        }
        DimensionsType dt = pdt.getDimensions();
        if ((dt.getHeight() != 36) || (dt.getWidth() != 24) || (dt.getDepth() != 36)) {
            throw new WebServiceException("Wrong Dimension! height: " + dt.getHeight() + ", width: " + dt.getWidth() + ", depth: " + dt.getDepth());
        }
        if (!pdt.getDimensionsUnit().equals("Inch") || (pdt.getWeight() != 125) || !pdt.getWeightUnit().equals("LB")) {
            throw new WebServiceException("Wrong ProductDetailsType! DimensionType: " + pdt.getDimensionsUnit() + ", wwight: " +
                    pdt.getWeight() + ", weightUnit: " + pdt.getWeightUnit());
        }
        prodNum.value = 12345;
        status.value = "ok";
    }

    public DataHandler testDataHandler(String body, DataHandler attachIn) {
        if (attachIn == null) {
            throw new WebServiceException("Received DataHandler should not be null!");
        }
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
        List<ProductType> pts = pct.getProduct();
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

    public void getProductDetails(GetProductDetailsType request, Holder<ProductDetailsType> body,
                                  Holder<Image> pic, Holder<Source> specs) {
        // Don't know how to get a resource
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
