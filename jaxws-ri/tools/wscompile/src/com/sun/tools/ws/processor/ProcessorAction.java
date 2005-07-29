/*
 * $Id: ProcessorAction.java,v 1.3 2005-07-29 19:54:48 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.processor;

import java.util.Properties;

import com.sun.tools.ws.processor.model.Model;
import com.sun.tools.ws.processor.config.Configuration;

/**
 * A ProcessorAction is used to perform some operation on a
 * {@link com.sun.tools.ws.processor.model.Model Model} such as
 * generating a Java source file.
 *
 * @author WS Development Team
 */
public interface ProcessorAction {
    public void perform(Model model, Configuration config, Properties options);
}
