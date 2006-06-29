/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.tools.ws.processor;

import com.sun.tools.ws.processor.config.Configuration;
import com.sun.tools.ws.processor.config.ModelInfo;
import com.sun.tools.ws.processor.model.Model;
import com.sun.tools.ws.processor.util.ProcessorEnvironment;
import com.sun.xml.ws.util.exception.JAXWSExceptionBase;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * This abstract class contains methods for getting a Modeler and creating a model
 * from that Modeler given a particular configuration. ProcessorActions can also
 * be registered and run with instances of this class.
 *
 * @author WS Development Team
 *
 */
public class Processor {

    public Processor(Configuration configuration, Properties options, Model model) {
        this(configuration,options);
        _model = model;
    }

    public Processor(Configuration configuration, Properties options) {
        _configuration = configuration;
        _options = options;

        // find the value of the "print stack traces" property
        _printStackTrace = Boolean.valueOf(_options.getProperty(ProcessorOptions.PRINT_STACK_TRACE_PROPERTY));
        _env = _configuration.getEnvironment();
    }

    public void add(ProcessorAction action) {
        _actions.add(action);
    }

    public Model getModel() {
        return _model;
    }

    public void run() {
        runModeler();
        if (_model != null) {
            runActions();
        }
    }

    public void runModeler() {
        try {
            ModelInfo modelInfo = _configuration.getModelInfo();
            if (modelInfo == null) {
                throw new ProcessorException("processor.missing.model");
            }

            _model = modelInfo.buildModel(_options);

        } catch (JAXWSExceptionBase e) {
            if (_printStackTrace) {
                _env.printStackTrace(e);
            }
            _env.error(e);
        }
    }

    public void runActions() {
        try {
            if (_model == null) {
                // avoid reporting yet another error here
                return;
            }

            for (ProcessorAction action : _actions) {
                action.perform(_model, _configuration, _options);
            }
        } catch (JAXWSExceptionBase e) {
            if (_printStackTrace || _env.verbose()) {
                _env.printStackTrace(e);
            }
            _env.error(e);
        }
    }

    private final Properties _options;
    private final Configuration _configuration;
    private final List<ProcessorAction> _actions = new ArrayList<ProcessorAction>();
    private Model _model;
    private final boolean _printStackTrace;
    private final ProcessorEnvironment _env;
}
