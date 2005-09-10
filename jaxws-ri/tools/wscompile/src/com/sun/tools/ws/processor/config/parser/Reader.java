/*
 * $Id: Reader.java,v 1.8 2005-09-10 19:49:35 kohsuke Exp $
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


import java.net.URL;
import java.util.List;
import java.util.Properties;

import com.sun.tools.ws.processor.config.Configuration;
import com.sun.tools.ws.processor.util.ProcessorEnvironment;
import com.sun.tools.ws.util.JAXWSUtils;
import com.sun.tools.ws.wsdl.document.WSDLConstants;
import com.sun.xml.ws.streaming.XMLStreamReaderFactory;
import com.sun.xml.ws.streaming.XMLStreamReaderUtil;

import javax.xml.stream.XMLStreamReader;

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

        InputParser parser = null;
        //now its just the first file. do we expect more than one input files?
        validateInput(inputSources.get(0));

        if(isClassFile){
            parser = new ClassModelParser(_env, _options);
        } else {
            parser = new CustomizationParser(_env, _options);
        } 
        return parser.parse(inputSources);
    }

    protected void validateInput(String file) throws Exception{
        if(isClass(file)){
            isClassFile = true;
            return;
        }

//        JAXWSUtils.checkAbsoluteness(file);
//        URL url = new URL(file);
//
//        XMLStreamReader reader =
//                XMLStreamReaderFactory.createXMLStreamReader(url.openStream(), true);
//
//        XMLStreamReaderUtil.nextElementContent(reader);
//        if(!reader.getName().equals(WSDLConstants.QNAME_DEFINITIONS)){
//            //we are here, means invalid element
//            ParserUtil.failWithFullName("configuration.invalidElement", file, reader);
//        }
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

    protected ProcessorEnvironment _env;

    protected Properties _options;
}
