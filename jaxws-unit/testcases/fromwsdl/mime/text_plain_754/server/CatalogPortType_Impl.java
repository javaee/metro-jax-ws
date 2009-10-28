/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package fromwsdl.mime.text_plain_754.server;

import javax.jws.WebService;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceException;


/**
 * Test case for issue: 754 - tests text/plain in mime binding
 *
 * @author Jitendra Kotamraju
 */
@WebService(endpointInterface = "fromwsdl.mime.text_plain_754.server.CatalogPortType")
public class CatalogPortType_Impl {

    public void echoString(String input, String attInput, Holder<String> output, Holder<String> att) {
        if (!input.equals("input")) {
            throw new WebServiceException("Expected input=input, got="+input);
        }
        if (!attInput.equals("attInput")) {
            throw new WebServiceException("Expected attInput=attInput, got="+attInput);
        }
        output.value = "output";
        att.value = "att";
    }

}
