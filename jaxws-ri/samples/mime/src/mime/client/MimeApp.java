/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package mime.client;

import javax.xml.ws.Holder;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.Source;
import javax.activation.DataHandler;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.awt.*;
import java.util.Arrays;

public class MimeApp {
    public static void main (String[] args){
        try {
            Object port = new HelloService().getHelloPort ();
            if(port == null){
                System.out.println ("Mime TEST FAILURE: Couldnt get port!");
                System.exit (-1);
            }

            //test wsdl mime without enableMIMEContent(default)
            testEchoData ((Hello)port);

            //test wsdl mime with enableMIMEContent
            testEchoDataWithEnableMIMEContent((Hello)port);

            testDetail((Hello)port);

            //test swaref
            testSwaref ((Hello)port);
        } catch (Exception ex) {
            ex.printStackTrace ();
        }
    }

    private static void testEchoData(Hello port) throws Exception{
        Holder<byte[]> photo = new Holder<byte[]>();
        byte[] bytes = AttachmentHelper.getImageBytes(getImage("java.jpg"), "image/jpeg");
        photo.value = bytes;
        port.echoData("echoData", photo);
        if(Arrays.equals(photo.value, bytes))
            System.out.println ("testEchoData() PASSED!");
        else
            System.out.println ("testEchoData() FAILED!");
    }

    private static void testEchoDataWithEnableMIMEContent(Hello port) throws Exception{
        Holder<Image> photo = new Holder<Image>();
        photo.value =  getImage("java.jpg");
        port.echoDataWithEnableMIMEContent("echoDataWithEnableMIMEContent", photo);
        if(AttachmentHelper.compareImages(getImage("java.jpg"), photo.value))
            System.out.println ("testEchoDataWithEnableMIMEContent() PASSED!");
        else
            System.out.println ("testEchoDataWithEnableMIMEContent() FAILED!");
    }

    private static void testDetail(Hello port) throws Exception{
        DetailType req = new DetailType();
        req.setName("XYZ corp");
        req.setAddress("1234 Some street");
        Source resp = port.detail(req);

        if(AttachmentHelper.compareStreamSource(new StreamSource(new ByteArrayInputStream(sampleXML.getBytes())), (StreamSource)resp))
            System.out.println ("testDetail() PASSED!");
        else
            System.out.println ("testDetail() FAILED!");
    }

    private static void testSwaref (Hello port) throws Exception{
        DataHandler claimForm = new DataHandler (new StreamSource(new ByteArrayInputStream(sampleXML.getBytes())), "text/xml");
        ClaimFormTypeRequest req = new ClaimFormTypeRequest();
        req.setRequest(claimForm);
        ClaimFormTypeResponse resp = port.claimForm (req);
        DataHandler out = resp.getResponse();
        if(out != null && AttachmentHelper.compareStreamSource (new StreamSource(new ByteArrayInputStream(sampleXML.getBytes())), (StreamSource)out.getContent ()))
            System.out.println ("testSwaref() PASSED!");
        else
            System.out.println ("testSwaref() FAILED!");
    }

    private static Image getImage (String imageName) throws Exception {
        String location = getDataDir () + imageName;
        return javax.imageio.ImageIO.read (new File (location));
    }

    private static String getDataDir () {
        String userDir = System.getProperty ("user.dir");
        String sepChar = System.getProperty ("file.separator");
        return userDir+sepChar+ "common_resources/";
    }

    private static final String sampleXML = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?> \n" +
            "<NMEAstd>\n" +
            "<DevIdSentenceId>$GPRMC</DevIdSentenceId>\n" +
            "<Time>212949</Time>\n" +
            "<Navigation>A</Navigation>\n" +
            "<NorthOrSouth>4915.61N</NorthOrSouth>\n" +
            "<WestOrEast>12310.55W</WestOrEast>\n" +
            "<SpeedOnGround>000.0</SpeedOnGround>\n" +
            "<Course>360.0</Course>\n" +
            "<Date>030904</Date>\n" +
            "<MagneticVariation>020.3</MagneticVariation>\n" +
            "<MagneticPoleEastOrWest>E</MagneticPoleEastOrWest>\n" +
            "<ChecksumInHex>*6B</ChecksumInHex>\n" +
            "</NMEAstd>";
}
