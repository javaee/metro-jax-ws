/*
 * $Id: DOMUtil.java,v 1.5 2005-10-17 21:47:30 kohsuke Exp $
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

package com.sun.xml.ws.util;

import com.sun.xml.ws.util.xml.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMResult;

/**
 * $author: JAXWS Development Team
 */
public class DOMUtil {

    private static DocumentBuilder db;

    /**
     * Creates a new DOM document.
     */
    public static Document createDom() {
        synchronized (DOMUtil.class) {
            if (db == null) {
                try {
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    dbf.setNamespaceAware(true);
                    db = dbf.newDocumentBuilder();
                } catch (ParserConfigurationException e) {
                    throw new FactoryConfigurationError(e);
                }
            }
            return db.newDocument();
        }
    }
}
