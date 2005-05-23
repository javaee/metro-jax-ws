/**
 * $Id: MessageInfoUtil.java,v 1.1 2005-05-23 23:06:26 bbissett Exp $
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
        messageInfo.setMetaData(BindingProviderProperties.JAXRPC_RUNTIME_CONTEXT,  runtimeContext);
    }

    public static RuntimeContext getRuntimeContext(MessageInfo messageInfo) {
        return (RuntimeContext)messageInfo.getMetaData(BindingProviderProperties.JAXRPC_RUNTIME_CONTEXT);
    }

}
