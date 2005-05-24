/*
 * $Id: ProcessorAction.java,v 1.1 2005-05-24 13:43:48 bbissett Exp $
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
 * @author JAX-RPC Development Team
 */
public interface ProcessorAction {
    public void perform(Model model, Configuration config, Properties options);
}
