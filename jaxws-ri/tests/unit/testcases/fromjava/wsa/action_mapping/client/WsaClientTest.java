/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Oracle and/or its affiliates. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package fromjava.wsa.action_mapping.client;

import testutil.XMLTestCase;

import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPath;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;

/**
 * @author Rama Pulavarthi
 */
public class WsaClientTest extends XMLTestCase {
    private Document wsdlDoc;

    public WsaClientTest(String s) throws ParserConfigurationException, IOException, SAXException {
        super(s);
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        String wsdl = System.getProperty("addNumbersPortAddress") + "?wsdl";
        wsdlDoc = builder.parse(wsdl);


    }

    private String evaluateXpath(String expr) throws XPathExpressionException, IOException {
        XPathFactory xpf = XPathFactory.newInstance();
        XPath xpath = xpf.newXPath();
        xpath.setNamespaceContext(new NSContextImpl());
        //return xpath.evaluate(expr, new InputSource(new URL(wsdl).openStream()));
        return xpath.evaluate(expr, wsdlDoc);
    }

    public void testOnewayDefaultActionMapping() throws IOException, XPathExpressionException {
        String inputExpr = getInputExpression("notify");
        Object result = evaluateXpath(inputExpr);
        assertNotNull(result);
        assertEquals(TestConstants.NOTIFY_IN_NOACTION, result);
    }

    public void testDefaultActions() throws Exception {
        String inputExpr = getInputExpression("addNumbersNoAction");
        Object result = evaluateXpath(inputExpr);
        assertNotNull(result);
        assertEquals(TestConstants.ADD_NUMBERS_IN_NOACTION, result);

        String outputExpr = getOutputExpression("addNumbersNoAction");
        result = evaluateXpath(outputExpr);
        assertNotNull(result);
        assertEquals(TestConstants.ADD_NUMBERS_OUT_NOACTION, result);

        String faultExpr = getFaultExpression("addNumbersNoAction");
        result = evaluateXpath(faultExpr);
        assertNotNull(result);
        assertEquals(TestConstants.ADD_NUMBERS_FAULT_NOACTION, result);

    }

     public void testEmptyActions() throws Exception {
        String inputExpr = getInputExpression("addNumbersEmptyAction");
        Object result = evaluateXpath(inputExpr);
        assertNotNull(result);
        assertEquals(TestConstants.ADD_NUMBERS_IN_EMPTYACTION, result);

        String outputExpr = getOutputExpression("addNumbersEmptyAction");
        result = evaluateXpath(outputExpr);
        assertNotNull(result);
        assertEquals(TestConstants.ADD_NUMBERS_OUT_EMPTYACTION, result);

    }

    public void testNonEmptySOAPAction() throws Exception {
        String inputExpr = getInputExpression("addNumbersSOAPAction");
        Object result = evaluateXpath(inputExpr);
        assertNotNull(result);
        assertEquals(TestConstants.ADD_NUMBERS_IN_SOAPACTION, result);

        String outputExpr = getOutputExpression("addNumbersSOAPAction");
        result = evaluateXpath(outputExpr);
        assertNotNull(result);
        assertEquals(TestConstants.ADD_NUMBERS_OUT_SOAPACTION, result);

    }

    public void testExplicitInputOutputAction1() throws Exception {
        String inputExpr = getInputExpression("addNumbers");
        Object result = evaluateXpath(inputExpr);
        assertNotNull(result);
        assertEquals(TestConstants.ADD_NUMBERS_IN_ACTION, result);

        String outputExpr = getOutputExpression("addNumbers");
        result = evaluateXpath(outputExpr);
        assertNotNull(result);
        assertEquals(TestConstants.ADD_NUMBERS_OUT_ACTION, result);

    }

    public void testExplicitInputOutputAction2() throws Exception {
        String inputExpr = getInputExpression("addNumbers2");
        Object result = evaluateXpath(inputExpr);
        assertNotNull(result);
        assertEquals(TestConstants.ADD_NUMBERS2_IN_ACTION, result);

        String outputExpr = getOutputExpression("addNumbers2");
        result = evaluateXpath(outputExpr);
        assertNotNull(result);
        assertEquals(TestConstants.ADD_NUMBERS2_OUT_ACTION, result);

    }

    public void testExplicitInputOutputFaultAction1() throws Exception {
        String inputExpr = getInputExpression("addNumbersFault1");
        Object result = evaluateXpath(inputExpr);
        assertNotNull(result);
        assertEquals(TestConstants.ADD_NUMBERS_FAULT1_IN_ACTION, result);

        String outputExpr = getOutputExpression("addNumbersFault1");
        result = evaluateXpath(outputExpr);
        assertNotNull(result);
        assertEquals(TestConstants.ADD_NUMBERS_FAULT1_OUT_ACTION, result);

        String faultExpr = getFaultExpression("addNumbersFault1");
        result = evaluateXpath(faultExpr);
        assertNotNull(result);
        assertEquals(TestConstants.ADD_NUMBERS_FAULT1_ADDNUMBERS_ACTION, result);

    }

    public void testExplicitInputOutputFaultAction2() throws Exception {
        String inputExpr = getInputExpression("addNumbersFault2");
        Object result = evaluateXpath(inputExpr);
        assertNotNull(result);
        assertEquals(TestConstants.ADD_NUMBERS_FAULT2_IN_ACTION, result);

        String outputExpr = getOutputExpression("addNumbersFault2");
        result = evaluateXpath(outputExpr);
        assertNotNull(result);
        assertEquals(TestConstants.ADD_NUMBERS_FAULT2_OUT_ACTION, result);

        String faultExpr1 = getFaultExpression("addNumbersFault2","AddNumbersException");
        result = evaluateXpath(faultExpr1);
        assertNotNull(result);
        assertEquals(TestConstants.ADD_NUMBERS_FAULT2_ADDNUMBERS_ACTION, result);

        String faultExpr2 = getFaultExpression("addNumbersFault2","TooBigNumbersException");
        result = evaluateXpath(faultExpr2);
        assertNotNull(result);
        assertEquals(TestConstants.ADD_NUMBERS_FAULT2_TOOBIGNUMBERS_ACTION, result);

    }

    public void testEmptyFaultAction() throws Exception {
        String faultExpr1 = getFaultExpression("addNumbersFault7","AddNumbersException");
        Object result = evaluateXpath(faultExpr1);
        assertNotNull(result);
        assertEquals(TestConstants.ADD_NUMBERS_FAULT7_ADDNUMBERS_ACTION, result);

        String faultExpr2 = getFaultExpression("addNumbersFault7","TooBigNumbersException");
        result = evaluateXpath(faultExpr2);
        assertNotNull(result);
        assertEquals(TestConstants.ADD_NUMBERS_FAULT7_TOOBIGNUMBERS_ACTION, result);

    }

    private String getInputExpression(String operation) {
        String expr = "/*[name()='definitions']/*[name()='portType']/*[name()='operation'][@name='" + operation + "']/*[name()='input']/@wsam:Action";
        return expr;
    }

    private String getOutputExpression(String operation) {
        String expr = "/*[name()='definitions']/*[name()='portType']/*[name()='operation'][@name='" + operation + "']/*[name()='output']/@wsam:Action";
        return expr;
    }

    private String getFaultExpression(String operation) {
        String expr = "/*[name()='definitions']/*[name()='portType']/*[name()='operation'][@name='" + operation + "']/*[name()='fault']/@wsam:Action";
        return expr;
    }

    private String getFaultExpression(String operation, String exception) {
        String expr = "/*[name()='definitions']/*[name()='portType']/*[name()='operation'][@name='" + operation + "']/*[name()='fault'][@name='"+exception+"']/@wsam:Action";
        return expr;
    }
}
