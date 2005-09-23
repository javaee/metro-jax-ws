/*
 * $Id: Processor.java,v 1.6 2005-09-23 22:05:45 kohsuke Exp $
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

package com.sun.tools.ws.processor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import com.sun.tools.ws.processor.config.Configuration;
import com.sun.tools.ws.processor.config.ModelInfo;
import com.sun.tools.ws.processor.model.Model;
import com.sun.tools.ws.processor.util.ProcessorEnvironment;
import com.sun.xml.ws.util.exception.JAXWSExceptionBase;
import com.sun.xml.ws.util.localization.NullLocalizable;

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
        _configuration = configuration;
        _options = options;
        _actions = new ArrayList();

        // find the value of the "print stack traces" property
        _printStackTrace = Boolean.valueOf(_options.getProperty(
            ProcessorOptions.PRINT_STACK_TRACE_PROPERTY)).booleanValue();
        _env = (ProcessorEnvironment)_configuration.getEnvironment();
        _model = model;
    }

    public Processor(Configuration configuration, Properties options) {
        _configuration = configuration;
        _options = options;
        _actions = new ArrayList();

        // find the value of the "print stack traces" property
        _printStackTrace = Boolean.valueOf(_options.getProperty(
            ProcessorOptions.PRINT_STACK_TRACE_PROPERTY)).booleanValue();
        _env = (ProcessorEnvironment)_configuration.getEnvironment();
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
            ModelInfo modelInfo = (ModelInfo)_configuration.getModelInfo();
            if (modelInfo == null) {
                throw new ProcessorException("processor.missing.model");
            }

            _model = modelInfo.buildModel(_options);

        } catch (JAXWSExceptionBase e) {
            if (_printStackTrace) {
                _env.printStackTrace(e);
            }
            _env.error(e);
        } catch (Exception e) {
            if (_printStackTrace) {
                _env.printStackTrace(e);
            }
            _env.error(new NullLocalizable(e.getMessage()));
        }
    }

    public void runActions() {
        try {
            if (_model == null) {

                // avoid reporting yet another error here
                return;
            }

            for (Iterator iter = _actions.iterator(); iter.hasNext();) {
                ProcessorAction action = (ProcessorAction) iter.next();
                action.perform(_model, _configuration, _options);
            }
        } catch (JAXWSExceptionBase e) {
            if (_printStackTrace || _env.verbose()) {
                _env.printStackTrace(e);
            }
            _env.error(e);
        } catch (Exception e) {
            if (_printStackTrace || _env.verbose()) {
                _env.printStackTrace(e);
            }
            _env.error(new NullLocalizable(e.getMessage()));
        }
    }

    private Properties _options;
    private Configuration _configuration;
    private List _actions;
    private Model _model;
    private boolean _printStackTrace;
    private ProcessorEnvironment _env;
}
