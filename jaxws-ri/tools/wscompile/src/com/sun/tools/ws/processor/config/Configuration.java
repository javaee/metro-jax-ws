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

import com.sun.tools.ws.processor.util.ProcessorEnvironment;

/**
 *
 * @author WS Development Team
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
