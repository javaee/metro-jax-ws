/*
 * $Id: ProcessorNotificationListener.java,v 1.2 2005-07-18 18:13:54 kohlert Exp $
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
 * @author WS Development Team
 */
public interface ProcessorNotificationListener {
    public void onError(Localizable msg);
    public void onWarning(Localizable msg);
    public void onInfo(Localizable msg);
}
