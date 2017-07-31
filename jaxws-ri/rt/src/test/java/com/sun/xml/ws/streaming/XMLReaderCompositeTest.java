/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.xml.ws.streaming;

//import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

//import org.junit.Test;

import com.sun.xml.ws.encoding.TagInfoset;
import com.sun.xml.ws.util.xml.XMLReaderComposite;
import com.sun.xml.ws.util.xml.XMLReaderComposite.ElemInfo;

public class XMLReaderCompositeTest extends TestCase {
    static XMLInputFactory xifac;
//    @Test
    public void testComposite() throws Exception {
        XMLStreamReader r = r("<root xmlns='ns0' xmlns:p1='ns1' att1='11' xmlns:p2='ns2' xmlns:p3='ns3'/>");
        XMLStreamReader x = r("<p1:x xmlns:p2='ns2' xmlns:p1='ns1' att2='22' p2:att2='11'/>");
        XMLStreamReader a = r("<a xmlns='ns0' att1='11'/>");
        XMLStreamReader b = r("<p1:b xmlns='ns0' att1='11' xmlns:p3='ns3' xmlns:p1='ns1'/>");
        TagInfoset rTag = new TagInfoset(r);
        ElemInfo rElem =  new ElemInfo(rTag, null);
        TagInfoset xTag = new TagInfoset(x);
        ElemInfo xElem =  new ElemInfo(xTag, rElem);
        XMLStreamReader[] kids = {a,b};
        XMLReaderComposite xrc = new XMLReaderComposite(xElem,kids);
        assertTrue(xrc.isStartElement());
        assertEquals(new QName("ns1","x"),xrc.getName());
        assertEquals(2,xrc.getAttributeCount());
        assertEquals(2,xrc.getNamespaceCount());
        assertEquals("ns0", xrc.getNamespaceURI(""));
        assertEquals("ns3", xrc.getNamespaceURI("p3"));
        
        xrc.next();
        assertTrue(xrc.isStartElement());
        assertEquals(new QName("ns0","a"),xrc.getName());
        assertEquals(1,xrc.getAttributeCount());
        xrc.next();
        assertTrue(xrc.isEndElement());
        
        xrc.next();
        assertTrue(xrc.isStartElement());
        assertEquals(new QName("ns1","b"),xrc.getName());
        assertEquals(1,xrc.getAttributeCount());
        xrc.next();
        assertTrue(xrc.isEndElement());

        xrc.next();
        assertTrue(xrc.isEndElement());
    }

    private XMLStreamReader r(String msg) throws XMLStreamException {
//      if (xifac == null) xifac = XMLInputFactory.newFactory("com.ctc.wstx.stax.WstxInputFactory", getClass().getClassLoader());
        if (xifac == null) xifac = XMLInputFactory.newFactory();
        ByteArrayInputStream in = new ByteArrayInputStream(msg.getBytes());
        XMLStreamReader r = xifac.createXMLStreamReader(in);
        if (r.getEventType() == XMLStreamReader.START_DOCUMENT) r.next();
        return r;
    }
}
