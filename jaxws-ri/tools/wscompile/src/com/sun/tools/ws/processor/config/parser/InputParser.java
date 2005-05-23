/*
 * $Id: InputParser.java,v 1.1 2005-05-23 23:13:21 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.ws.processor.config.parser;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.namespace.QName;

import org.xml.sax.InputSource;

import com.sun.tools.ws.processor.util.ProcessorEnvironment;
import com.sun.tools.ws.processor.config.Configuration;
import com.sun.xml.ws.util.localization.LocalizableMessageFactory;

/**
 * @author Vivek Pandey
 *
 *
 */
public abstract class InputParser{
    protected LocalizableMessageFactory _messageFactory =
        new LocalizableMessageFactory(
            "com.sun.tools.ws.resources.configuration");

    public InputParser(ProcessorEnvironment env, Properties options) {
        this._env = env;
        this._options = options;
        _modelInfoParsers = new HashMap<QName, Object>();

//        /*
//         * Load modelinfo parsers from the plugins which want to extend
//         * this functionality
//         */
//        Iterator i = ToolPluginFactory.getInstance().getExtensions(
//            ToolPluginConstants.WSCOMPILE_PLUGIN,
//            ToolPluginConstants.WSCOMPILE_MODEL_INFO_EXT_POINT);
//        while(i != null && i.hasNext()) {
//            ModelInfoPlugin plugin = (ModelInfoPlugin)i.next();
//            _modelInfoParsers.put(plugin.getModelInfoName(),
//                plugin.createModelInfoParser(env));
//        }
    }

    protected Configuration parse(InputStream is) throws Exception{
        //TODO: Not implemented exception
        return null;
    }

    protected Configuration parse(InputSource is) throws Exception{
        //TODO: Not implemented exception
        return null;
    }

    protected Configuration parse(List<String> inputSources) throws Exception{
        //TODO: Not implemented exception
        return null;
    }

    /**
     * @return Returns the _env.
     */
    public  ProcessorEnvironment getEnv(){
        return _env;
    }

    /**
     * @param _env The _env to set.
     */
    public void setEnv(ProcessorEnvironment env){
        this._env = env;
    }

    protected void warn(String key) {
        _env.warn(_messageFactory.getMessage(key));
    }

    protected void warn(String key, String arg) {
        _env.warn(_messageFactory.getMessage(key, arg));
    }

    protected void warn(String key, Object[] args) {
        _env.warn(_messageFactory.getMessage(key, args));
    }

    protected void info(String key) {
        _env.info(_messageFactory.getMessage(key));
    }

    protected void info(String key, String arg) {
        _env.info(_messageFactory.getMessage(key, arg));
    }

    protected ProcessorEnvironment _env;
    protected Properties _options;
    protected Map<QName, Object> _modelInfoParsers;
}