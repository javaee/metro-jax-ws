/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */

/**
 * $Id: MimeApp.java,v 1.7 2006-06-29 22:30:03 ofung Exp $
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
import java.lang.reflect.Proxy;

import com.sun.xml.ws.client.EndpointIFBase;
import com.sun.xml.ws.transport.http.client.HttpClientTransportFactory;

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
            System.out.println ("MimeApp FAILED!");
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
        DataHandler out = port.claimForm (claimForm);
        if(AttachmentHelper.compareStreamSource (new StreamSource(new ByteArrayInputStream(sampleXML.getBytes())), (StreamSource)out.getContent ()))
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

    private static final String sampleXML = "?xml version=\"1.0\" encoding=\"UTF-8\" ?> \n" +
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
