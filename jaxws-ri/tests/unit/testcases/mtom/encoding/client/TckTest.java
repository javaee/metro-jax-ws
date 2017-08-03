/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
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

package mtom.encoding.client;

import junit.framework.TestCase;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.soap.MTOMFeature;
import javax.xml.ws.Holder;
import java.awt.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Closeable;

import com.sun.xml.ws.developer.SerializationFeature;

/**
 * @author Jitendra Kotamraju
 */
public class TckTest extends TestCase {

    private Hello proxy;

    public TckTest(String name) throws Exception{
        super(name);
    }

    protected void setUp() throws Exception {
        proxy = new HelloService().getHelloPort(new MTOMFeature(),
            new SerializationFeature("utf-16"));
    }

    public void testMtomIn() throws Exception {
        DataType dt = new DataType();
        dt.setDoc1(getSource("gpsXml.xml"));
        dt.setDoc2(getSource("gpsXml.xml"));
        // This not working since DCH is not registerd by JAX-WS
        //dt.setDoc3(new DataHandler(getSource("gpsXml.xml"), "text/xml"));
        dt.setDoc3(getDataHandler("gpsXml.xml", "text/xml"));
        dt.setDoc4(getImage("java.jpg"));

        String works = proxy.mtomIn(dt);
        assertEquals("works", works);
    }

    public void testMtomInOut() throws Exception {
        Holder<DataHandler> doc1 = new Holder<DataHandler>();
	    doc1.value = getDataHandler("attach.txt", "text/plain");
        Holder<DataHandler> doc2 = new Holder<DataHandler>();
        doc2.value = getDataHandler("attach.html", "text/html");
        Holder<DataHandler> doc3 = new Holder<DataHandler>();
        doc3.value = getDataHandler("attach.xml", "application/xml");
        Holder<Image> doc4 = new Holder<Image>();
        doc4.value = getImage("attach.jpeg");
        Holder<Image> doc5 = new Holder<Image>();
        doc5.value = getImage("attach2.jpeg");

    	proxy.mtomInOut(doc1, doc2, doc3, doc4, doc5);
        validate(getDataHandler("attach.txt", "text/plain"), doc1.value);
        validate(getDataHandler("attach.html", "text/html"), doc2.value);
        validate(getDataHandler("attach.xml", "application/xml"), doc3.value);
    }

    public void testMtomInOut1() throws Exception {
        Holder<DataHandler> doc1 = new Holder<DataHandler>();
	    doc1.value = getDataHandler("some.bin", "application/octet-stream");
        Holder<DataHandler> doc2 = new Holder<DataHandler>();
        doc2.value = getDataHandler("some.bin", "application/octet-stream");
        Holder<DataHandler> doc3 = new Holder<DataHandler>();
        doc3.value = getDataHandler("some.bin", "application/octet-stream");
        Holder<Image> doc4 = new Holder<Image>();
        doc4.value = getImage("attach.jpeg");
        Holder<Image> doc5 = new Holder<Image>();
        doc5.value = getImage("attach2.jpeg");

    	proxy.mtomInOut(doc1, doc2, doc3, doc4, doc5);
        validate(getDataHandler("some.bin", "application/octet-stream"), doc1.value);
        closeDataHandler(doc1.value);
        validate(getDataHandler("some.bin", "application/octet-stream"), doc2.value);
        closeDataHandler(doc2.value);
        validate(getDataHandler("some.bin", "application/octet-stream"), doc3.value);
        closeDataHandler(doc3.value);
    }

    private void closeDataHandler(DataHandler dh) throws Exception {
        if (dh instanceof Closeable) {
            ((Closeable)dh).close();
        }
    }

    private void validate(DataHandler exp, DataHandler got) throws Exception {
        InputStream inExp = exp.getInputStream();
        InputStream inGot = got.getInputStream();
        int ch;
        while((ch=inExp.read()) != -1) {
	        assertEquals(ch, inGot.read());	
        }
        assertEquals(-1, inGot.read());
        inExp.close();
        inGot.close();
    }

    private Image getImage(String image) throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream(image);
        return javax.imageio.ImageIO.read(is);
    }

    private StreamSource getSource(String file) throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream(file);
        return new StreamSource(is);
    }

    private DataHandler getDataHandler(final String file, final String ct) throws Exception {
        return new DataHandler(new DataSource() {
            public String getContentType() {
                return ct;
            }

            public InputStream getInputStream() {
                return getClass().getClassLoader().getResourceAsStream(file);
            }

            public String getName() {
                return null;
            }

            public OutputStream getOutputStream() {
                throw new UnsupportedOperationException();
            }
        });
    }

}
