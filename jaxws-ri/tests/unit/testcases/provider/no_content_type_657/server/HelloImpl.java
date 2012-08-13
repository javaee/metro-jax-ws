/*
 * Copyright 2004 Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * Hello_Impl.java
 *
 * Created on July 25, 2003, 10:37 AM
 */

package provider.no_content_type_657.server;

import javax.activation.DataSource;
import javax.annotation.Resource;
import javax.xml.ws.*;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.handler.MessageContext;

/**
 * Issue 657
 *
 * HTTP PUT operation sends a response with no content-type
 *
 * @author Jitendra Kotamraju
 */
@WebServiceProvider
@ServiceMode(value=Service.Mode.MESSAGE)
@BindingType(HTTPBinding.HTTP_BINDING)
public class HelloImpl implements Provider<DataSource> {

    @Resource
    WebServiceContext wsCtxt;

    public DataSource invoke(DataSource msg) {
        MessageContext msgCtxt = wsCtxt.getMessageContext();        
        msgCtxt.put(MessageContext.HTTP_RESPONSE_CODE, 200);
        return null;
    }
}
