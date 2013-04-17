/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004-2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package xop.hello_soap12.client;

import junit.framework.TestCase;
import testutil.AttachmentHelper;
import testutil.ClientServerTestUtil;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;
import javax.xml.ws.soap.SOAPBinding;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.w3c.dom.Node;



public class TestApp extends TestCase{
    private static Hello port;
    public TestApp(String name) throws Exception{
        super(name);
//        Object obj = ClientServerTestUtil.getPort(HelloService.class, Hello.class, new QName("http://example.org/mtom", "HelloPort"));
//        assertTrue(obj != null);
        HelloService helloService = new HelloService();
        Object obj = helloService.getHelloPort();
        assertTrue(obj != null);
        ClientServerTestUtil.setTransport(obj);

        //set Mtom optimization.
        //set Mtom optimization.
        SOAPBinding binding = (SOAPBinding)((BindingProvider)obj).getBinding();
        binding.setMTOMEnabled(true);
        port = (Hello)obj;
    }

    public void testMtom() throws Exception{
        String name="Duke";
        Holder<byte[]> photo = new Holder<byte[]>(name.getBytes());
        Holder<Image> image = new Holder<Image>(getImage("java.jpg"));
        port.detail(photo, image);
        assertTrue(new String(photo.value).equals(name));
        assertTrue(AttachmentHelper.compareImages(getImage("java.jpg"), image.value));
    }

    public void testSwaref() throws Exception{
        DataHandler claimForm = new DataHandler(getFileAsStreamSource("gpsXml.xml"), "text/xml");
        DataHandler out = port.claimForm(claimForm);
        assertTrue(AttachmentHelper.compareSource(getFileAsStreamSource("gpsXml.xml"), (Source)out.getContent()));
    }

    private Image getImage(String imageName) throws Exception {
        String location = getDataDir() + imageName;
        return javax.imageio.ImageIO.read(new File(location));
    }

    private String getDataDir() {
        String userDir = System.getProperty("user.dir");
        String sepChar = System.getProperty("file.separator");
        return userDir+sepChar+ "src/xop/hello/common_resources/WEB-INF/";
    }

    private StreamSource getFileAsStreamSource(String fileName)
        throws Exception {
        InputStream is = null;
        String location = getDataDir() + fileName;
        File f = new File(location);
        FileInputStream fis = new FileInputStream(f);
        return new StreamSource(fis);
    }





}
