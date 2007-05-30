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

package mtom_soap12.client;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.soap.MTOMFeature;
import javax.xml.transform.stream.StreamSource;
import javax.activation.DataHandler;
import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.awt.*;

public class MtomApp {
    
    public static void main (String[] args){
        try {
            Hello port = new HelloService().getHelloPort (new MTOMFeature());
            if(port == null){
                System.out.println ("TEST FAILURE: Couldnt get port!");
                System.exit (-1);
            }

            //test mtom
            testMtom (port);

            //test echo
            testEcho(port);

        } catch (Exception ex) {
            System.out.println ("SOAP 1.2 MtomApp FAILED!");
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
            System.out.println ("SOAP 1.2 testMtom() PASSED!");
        else
            System.out.println ("SOAP 1.2 testMtom() FAILED!");
    }

    /**
     * Demonstrates a basic xs:base64Binary optimization
     */
    public static void testEcho(Hello port) throws Exception {
        byte[] bytes = AttachmentHelper.getImageBytes(getImage("java.jpg"), "image/jpeg");
        Holder<byte[]> image = new Holder<byte[]>(bytes);
        port.echoData(image);
        if (image.value != null)
            System.out.println("SOAP 1.1 testEcho() PASSED!");
        else
            System.out.println("SOAP 1.1 testEcho() FAILED!");
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
