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
import com.sun.tools.ws.util.xml.NullEntityResolver;
import com.sun.tools.ws.wsdl.framework.ParseException;

/**
 * @author Vivek Pandey
 *
 * External jaxws:bindings parser
 */
public class JAXWSBindingInfoParser {

    private ProcessorEnvironment env;

    /**
     * @param env
     */
    public JAXWSBindingInfoParser(ProcessorEnvironment env) {
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
            throw new ParseException("parsing.parserConfigException",e);
        } catch (FactoryConfigurationError e) {
            throw new ParseException("parsing.factoryConfigException",e);
        }catch(SAXException e){
            throw new ParseException("parsing.saxException",e);
        }catch(IOException e){
            throw new ParseException("parsing.saxException",e);
        }
    }

    public final Set<Element> outerBindings = new HashSet<Element>();
}
