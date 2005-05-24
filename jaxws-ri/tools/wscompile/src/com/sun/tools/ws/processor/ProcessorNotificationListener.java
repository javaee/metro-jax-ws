/*
 * $Id: ProcessorNotificationListener.java,v 1.1 2005-05-24 13:43:49 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.processor;

import com.sun.xml.ws.util.localization.Localizable;

/**
 * A ProcessorNotificationListener is registered with a Processor and receives
 * notifications of errors, warnings and informational messages.
 *
 * @author JAX-RPC Development Team
 */
public interface ProcessorNotificationListener {
    public void onError(Localizable msg);
    public void onWarning(Localizable msg);
    public void onInfo(Localizable msg);
}
