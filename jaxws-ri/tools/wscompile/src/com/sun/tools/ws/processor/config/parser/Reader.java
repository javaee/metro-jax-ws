/*
 * $Id: Reader.java,v 1.1 2005-05-23 23:13:23 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.ws.processor.config.parser;


import java.net.URL;
import java.util.List;
import java.util.Properties;

import com.sun.tools.ws.processor.ProcessorOptions;
import com.sun.tools.ws.processor.config.ConfigurationException;
import com.sun.tools.ws.processor.config.Configuration;
import com.sun.tools.ws.processor.util.ProcessorEnvironment;
import com.sun.tools.ws.util.JAXRPCUtils;
import com.sun.tools.ws.wsdl.document.WSDLConstants;
import com.sun.xml.ws.streaming.XMLReader;
import com.sun.xml.ws.streaming.XMLReaderFactory;

/**
 * @author Vivek Pandey
 *
 * Main entry point from CompileTool
 */
public class Reader {

    /**
     *
     */
    public Reader(ProcessorEnvironment env, Properties options) {
        this._env = env;
        this._options = options;
    }

    public Configuration parse(List<String> inputSources)
            throws Exception {
        //reset the input type flags before parsing
        isClassFile = false;
        isWSDLFile = false;;
        isConfigFile = false;;

        InputParser parser = null;
        //now its just the first file. do we expect more than one input files?
        validateInput(inputSources.get(0));

        if(isClassFile){
            parser = new ClassModelParser(_env, _options);
        }else if(isWSDLFile){
            parser = new CustomizationParser(_env, _options);
        }else if(isConfigFile){
            throw new ConfigurationException("configuration.configNotSupport", new Object[] {inputSources.get(0)});
        }
        return parser.parse(inputSources);
    }

    protected void validateInput(String file) throws Exception{
        if(isClass(file)){
            isClassFile = true;
            return;
        }

        JAXRPCUtils.checkAbsoluteness(file);
        URL url = new URL(file);

        XMLReader reader = XMLReaderFactory.newInstance().createXMLReader(url.openStream());

        reader.next();
        if (reader.getName().equals(Constants.QNAME_CONFIGURATION)){
            while(reader.next() != XMLReader.EOF){
                if(reader.getState() != XMLReader.START)
                    continue;
            }
            isConfigFile = true;
        }else if(reader.getName().equals(WSDLConstants.QNAME_DEFINITIONS)){
            isWSDLFile = true;
        }else{
            //we are here, means invalid element
            ParserUtil.failWithFullName("configuration.invalidElement", reader);
        }
    }

    public boolean isClass(String className) {
        try {
            _env.getClassLoader().loadClass(className);
        } catch (ClassNotFoundException e) {
            return false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private boolean isClassFile;
    private boolean isWSDLFile;
    private boolean isConfigFile;

    protected ProcessorEnvironment _env;

    protected Properties _options;
}
