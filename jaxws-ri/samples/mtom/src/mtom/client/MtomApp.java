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
package mtom.client;

import com.sun.xml.ws.developer.JAXWSProperties;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.transform.stream.StreamSource;
import javax.activation.DataHandler;
import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.awt.*;
import java.util.Arrays;

public class MtomApp {

    public static void main (String[] args){
        try {
            Object port = new HelloService().getHelloPort ();
            if(port == null){
                System.out.println ("TEST FAILURE: Couldnt get port!");
                System.exit (-1);
            }

            //get the binding and enable mtom
            SOAPBinding binding = (SOAPBinding)((BindingProvider)port).getBinding ();
            binding.setMTOMEnabled (true);

            //test mtom
            testMtom ((Hello)port);

            //test echo
            testEcho((Hello)port);
        } catch (Exception ex) {
            System.out.println ("SOAP 1.1 MtomApp FAILED!");
            ex.printStackTrace ();
        }
    }

    /**
     * Demonstrates xmime:expectedContentTypes annotation
     */
    public static void testMtom (Hello port) throws Exception{
        String name="Duke";
        Holder<byte[]> photo = new Holder<byte[]>(name.getBytes ());
        Holder<Image> image = new Holder<Image>(getImage ("java.jpg"));
        port.detail (photo, image);
        if(new String (photo.value).equals (name) && (image.value != null))
            System.out.println ("SOAP 1.1 testMtom() PASSED!");
        else
            System.out.println ("SOAP 1.1 testMtom() FAILED!");
    }

    /**
     * Demonstrates a basic xs:base64Binary optimization
     */
    public static void testEcho(Hello port) throws Exception{
        byte[] bytes = AttachmentHelper.getImageBytes(getImage("java.jpg"), "image/jpeg");
        Holder<byte[]> image = new Holder<byte[]>(bytes);
        port.echoData(image);
        if(image.value != null)
            System.out.println ("SOAP 1.1 testEcho() PASSED!");
        else
            System.out.println ("SOAP 1.1 testEcho() FAILED!");
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

    private static StreamSource getFileAsStreamSource (String fileName)
    throws Exception {
        InputStream is = null;
        String location = getDataDir () + fileName;
        File f = new File (location);
        FileInputStream fis = new FileInputStream (f);
        return new StreamSource (fis);
    }
}
