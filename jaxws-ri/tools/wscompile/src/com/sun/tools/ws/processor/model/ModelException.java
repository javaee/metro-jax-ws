/*
 * $Id: ModelException.java,v 1.4 2005-09-10 19:49:37 kohsuke Exp $
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

package com.sun.tools.ws.processor.model;

import com.sun.tools.ws.processor.ProcessorException;
import com.sun.xml.ws.util.localization.Localizable;

/**
 * ModelException represents an exception that occurred while
 * visiting service model.
 *
 * @see ProcessorException
 *
 * @author WS Development Team
 */
public class ModelException extends ProcessorException {

    public ModelException(String key) {
        super(key);
    }

    public ModelException(String key, String arg) {
        super(key, arg);
    }

    public ModelException(String key, Object[] args) {
        super(key, args);
    }

    public ModelException(String key, Localizable arg) {
        super(key, arg);
    }

    public ModelException(Localizable arg) {
        super("model.nestedModelError", arg);
    }

    public String getResourceBundleName() {
        return "com.sun.tools.ws.resources.model";
    }
}
