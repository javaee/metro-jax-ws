/*
 * $Id: ClassModelInfo.java,v 1.3 2005-09-10 19:49:31 kohsuke Exp $
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

package com.sun.tools.ws.processor.config;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import com.sun.tools.ws.processor.modeler.Modeler;
import com.sun.tools.ws.processor.ProcessorOptions;
import com.sun.xml.ws.util.VersionUtil;

/**
 *
 * @author WS Development Team
 */
public class ClassModelInfo extends ModelInfo {

    public ClassModelInfo(String className) {
        this.className = className;
    }


    public Modeler getModeler(Properties properties) {
        return null;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    private String className;
}
