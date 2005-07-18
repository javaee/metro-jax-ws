/*
 * $Id: ClassModelParser.java,v 1.2 2005-07-18 18:13:56 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.processor.config.parser;


import java.io.File;
import java.util.List;
import java.util.Properties;

import com.sun.tools.ws.processor.config.ClassModelInfo;
import com.sun.tools.ws.processor.config.Configuration;
import com.sun.tools.ws.processor.util.ProcessorEnvironment;


/**
 *
 * @author WS Development Team
 */
public class ClassModelParser extends InputParser {

    public ClassModelParser(ProcessorEnvironment env, Properties options) {
        super(env, options);
    }

    public Configuration parse(List<String> inputClasses) {
        return parse(new File(inputClasses.get(0)));
    }

    public Configuration parse(File file) {
        Configuration config = new Configuration(getEnv());

        config.setModelInfo(new ClassModelInfo(file.getName()));
        return config;
    }
}
