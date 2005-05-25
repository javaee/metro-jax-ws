/**
 * $Id: MessageInfoUtil.java,v 1.2 2005-05-25 18:22:13 kohlert Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.util;

import com.sun.pept.ept.MessageInfo;
import com.sun.xml.ws.server.RuntimeContext;
import com.sun.xml.ws.client.BindingProviderProperties;

/**
 * @author JAX-RPC RI Development Team
 */
public class MessageInfoUtil {

    public static void setRuntimeContext(MessageInfo messageInfo,
        RuntimeContext runtimeContext) {
        messageInfo.setMetaData(BindingProviderProperties.JAXWS_RUNTIME_CONTEXT,  runtimeContext);
    }

    public static RuntimeContext getRuntimeContext(MessageInfo messageInfo) {
        return (RuntimeContext)messageInfo.getMetaData(BindingProviderProperties.JAXWS_RUNTIME_CONTEXT);
    }

}
