/*
 * $Id: ProcessorAction.java,v 1.2 2005-07-18 18:13:54 kohlert Exp $
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
 *
 * @author WS Development Team
 */
public interface ProcessorAction {
    public void perform(Model model, Configuration config, Properties options);
}
