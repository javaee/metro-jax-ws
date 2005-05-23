/*
 * $Id: Configuration.java,v 1.1 2005-05-23 23:13:24 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.processor.config;

import com.sun.tools.ws.processor.util.ProcessorEnvironment;

/**
 *
 * @author JAX-RPC Development Team
 */
public class Configuration {

    public Configuration(ProcessorEnvironment env) {
        _env = (ProcessorEnvironment)env;
    }

    public ModelInfo getModelInfo() {
        return _modelInfo;
    }

    public void setModelInfo(ModelInfo i) {
        _modelInfo = (ModelInfo)i;
        _modelInfo.setParent(this);
    }

    public ProcessorEnvironment getEnvironment() {
        return _env;
    }

    private ProcessorEnvironment _env;
    private ModelInfo _modelInfo;
}
