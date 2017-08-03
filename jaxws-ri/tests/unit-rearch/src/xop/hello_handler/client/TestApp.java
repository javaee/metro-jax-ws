/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004-2017 Oracle and/or its affiliates. All rights reserved.
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

package xop.hello_handler.client;

import com.sun.xml.ws.developer.JAXWSProperties;
import junit.framework.TestCase;
import testutil.AttachmentHelper;
import testutil.ClientServerTestUtil;

import javax.activation.DataHandler;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;
import javax.xml.ws.soap.SOAPBinding;
import java.awt.*;
import java.io.InputStream;
import java.util.Arrays;


public class TestApp extends TestCase{
    private static Hello port;
    public TestApp(String name) throws Exception{
        super(name);

        HelloService helloService = new HelloService();
        Object obj = helloService.getHelloPort();
        assertTrue(obj != null);
        ClientServerTestUtil.setTransport(obj);

        //set Mtom optimization.
        SOAPBinding binding = (SOAPBinding)((BindingProvider)obj).getBinding();
        binding.setMTOMEnabled(true);
        if(ClientServerTestUtil.useLocal()){
            ((BindingProvider)obj).getRequestContext().put("LocalTransport", true);    
        }
        port = (Hello)obj;
    }

    public void testMtom() throws Exception{
        ((BindingProvider)port).getRequestContext().put(JAXWSProperties.MTOM_THRESHOLOD_VALUE, 0);
        String name="Duke";
        Holder<byte[]> photo = new Holder<byte[]>(name.getBytes());
        Holder<Image> image = new Holder<Image>(getImage("java.jpg"));
        port.detail(photo, image);
        assertTrue(new String(photo.value).equals(name));
        assertTrue(AttachmentHelper.compareImages(getImage("java.jpg"), image.value));
    }

    public void testEcho() throws Exception{
        ((BindingProvider)port).getRequestContext().put(JAXWSProperties.MTOM_THRESHOLOD_VALUE, 2000);
        byte[] bytes = AttachmentHelper.getImageBytes(getImage("java.jpg"), "image/jpeg");
        Holder<byte[]> image = new Holder<byte[]>(bytes);
        port.echoData(image);
        assertTrue(Arrays.equals(bytes, image.value));
    }

    public void testSwarefSource() throws Exception{
        DataHandler claimForm = new DataHandler(getFileAsStreamSource("gpsXml.xml"), "text/xml");
        DataHandler out = port.claimForm(claimForm);
        assertTrue(AttachmentHelper.compareSource(getFileAsStreamSource("gpsXml.xml"), (StreamSource)out.getContent()));
    }

    public void testSwarefImage() throws Exception{
        DataHandler claimForm = new DataHandler(getImage("java.jpg"), "image/jpeg");
        DataHandler out = port.claimForm(claimForm);
        assertTrue(AttachmentHelper.compareImages(getImage("java.jpg"), (Image)out.getContent()));
    }

    private Image getImage(String imageName) throws Exception {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(imageName);
        return javax.imageio.ImageIO.read(is);
    }

    private StreamSource getFileAsStreamSource(String fileName) throws Exception {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(fileName);
        return new StreamSource(is);
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("uselocal", "true");
        TestApp testor = new TestApp("TestApp");
        testor.testMtom();
    }

}
