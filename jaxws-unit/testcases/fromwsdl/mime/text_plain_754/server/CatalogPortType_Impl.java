/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package fromwsdl.mime.text_plain_754.server;

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


@WebService(endpointInterface = "fromwsdl.mime.text_plain_754.server.CatalogPortType")
public class CatalogPortType_Impl {

    public void echoString(String input, Holder<String> output, Holder<DataHandler>att) {
        output.value = "testing";
        //att.value = "value";
    }

}
