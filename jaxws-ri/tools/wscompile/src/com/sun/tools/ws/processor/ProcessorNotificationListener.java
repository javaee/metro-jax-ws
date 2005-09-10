/*
 * $Id: ProcessorNotificationListener.java,v 1.3 2005-09-10 19:49:31 kohsuke Exp $
 */

/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
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
