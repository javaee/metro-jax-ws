/*
 * $Id: JAXRPCBindingInfoParser.java,v 1.1 2005-05-23 23:13:22 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.ws.processor.config.parser;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.sun.tools.ws.processor.util.ProcessorEnvironment;
import com.sun.xml.ws.util.exception.LocalizableExceptionAdapter;
import com.sun.tools.ws.util.xml.NullEntityResolver;
import com.sun.tools.ws.wsdl.framework.ParseException;

/**
 * @author Vivek Pandey
 *
 * External jaxrpc:bindings parser
 */
public class JAXRPCBindingInfoParser {

    private ProcessorEnvironment env;

    /**
     * @param env
     */
    public JAXRPCBindingInfoParser(ProcessorEnvironment env) {
        this.env = env;
    }

    public Element parse(InputSource source) {
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setNamespaceAware(true);
            builderFactory.setValidating(false);
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            builder.setErrorHandler(new ErrorHandler() {
                public void error(SAXParseException e)
                    throws SAXParseException {
                    throw e;
                }

                public void fatalError(SAXParseException e)
                    throws SAXParseException {
                    throw e;
                }

                public void warning(SAXParseException err)
                    throws SAXParseException {
                    // do nothing
                }
            });

            builder.setEntityResolver(new NullEntityResolver());
            Document dom = builder.parse(source);
            Element root = dom.getDocumentElement();
            return root;
        } catch (ParserConfigurationException e) {
            throw new ParseException(
                "parsing.parserConfigException",
                new LocalizableExceptionAdapter(e));
        } catch (FactoryConfigurationError e) {
            throw new ParseException(
                "parsing.factoryConfigException",
                new LocalizableExceptionAdapter(e));
        }catch(SAXException e){
            throw new ParseException(
                    "parsing.saxException",
                    new LocalizableExceptionAdapter(e));
        }catch(IOException e){
            throw new ParseException(
                    "parsing.saxException",
                    new LocalizableExceptionAdapter(e));
        }
    }

    public final Set<Element> outerBindings = new HashSet<Element>();
}
