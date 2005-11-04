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

import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.transform.stream.StreamSource;
import javax.activation.DataHandler;
import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.awt.*;

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
            
            //test swaref
            testSwaref ((Hello)port);
        } catch (Exception ex) {
            System.out.println ("SOAP 1.1 MtomApp FAILED!");
            ex.printStackTrace ();
        }
    }
    
    public static void testMtom (Hello port) throws Exception{
        String name="Duke";
        Holder<byte[]> photo = new Holder<byte[]>(name.getBytes ());
        Holder<Image> image = new Holder<Image>(getImage ("java.jpg"));
        port.detail (photo, image);
        if(new String (photo.value).equals (name) && (AttachmentHelper.compareImages (getImage ("java.jpg"), image.value)))
            System.out.println ("SOAP 1.1 testMtom() PASSED!");
        else
            System.out.println ("SOAP 1.1 testMtom() FAILED!");
    }
    
    public static void testSwaref (Hello port) throws Exception{
        DataHandler claimForm = new DataHandler (getFileAsStreamSource ("gpsXml.xml"), "text/xml");
        DataHandler out = port.claimForm (claimForm);
        if(AttachmentHelper.compareStreamSource (getFileAsStreamSource ("gpsXml.xml"), (StreamSource)out.getContent ()))
            System.out.println ("SOAP 1.1 testSwaref() PASSED!");
        else
            System.out.println ("SOAP 1.1 testSwaref() FAILED!");
    }
//
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
