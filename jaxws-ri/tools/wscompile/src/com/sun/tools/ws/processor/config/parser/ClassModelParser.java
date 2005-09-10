/*
 * $Id: ClassModelParser.java,v 1.3 2005-09-10 19:49:33 kohsuke Exp $
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
