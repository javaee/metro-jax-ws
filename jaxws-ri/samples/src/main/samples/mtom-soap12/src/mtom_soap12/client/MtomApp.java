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

package mtom_soap12.client;

import com.sun.xml.ws.developer.StreamingDataHandler;

import javax.activation.DataHandler;
import javax.xml.ws.soap.MTOMFeature;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class MtomApp {

    public static void main (String[] args){
        try {
            MtomSample port = new MtomService().getMtomPort(new MTOMFeature());
            if(port == null){
                System.out.println ("FAILURE: Couldnt get port!");
                System.exit (-1);
            }

            testUpload(port);

            testDownload(port);
        } catch (Exception ex) {
            System.out.println ("SOAP 1.2 MtomApp FAILED!");
            ex.printStackTrace ();
        }
    }

    /**
     * Uploads an Image to the endpoint using MTOM
     */
    public static void testUpload (MtomSample port) throws Exception{
        Image image = getImage ("java.jpg");
        port.upload (image);
        if(image != null)
            System.out.println ("SOAP 1.2 testUpdate() PASSED!");
        else
            System.out.println ("SOAP 1.2 testUpdate() FAILED!");
    }

    /**
     * Downloads 20MB binary data using MTOM in streaming fashion
     */
    public static void testDownload(MtomSample port) throws Exception{
        int size = 20000000;//20MB

        DataHandler dh = port.download(size);
        validateDataHandler(size, dh);
    }

    private static void validateDataHandler(int expTotal, DataHandler dh)
		throws IOException {

        // readOnce() doesn't store attachment on the disk in some cases
        // for e.g when only one attachment is in the message
        StreamingDataHandler sdh = (StreamingDataHandler)dh;
        InputStream in = sdh.readOnce();
        byte[] buf = new byte[8192];
        int total = 0;
        int len;
        while((len=in.read(buf, 0, buf.length)) != -1) {
            for(int i=0; i < len; i++) {
                if ((byte)('A'+(total+i)%26) != buf[i]) {
                    System.out.println("FAIL: DataHandler data is different");
                }
            }
            total += len;
            if (total%(8192*250) == 0) {
            	System.out.println("Total so far="+total);
            }
        }
        System.out.println("Total Received="+total);
        if (total != expTotal) {
           System.out.println("FAIL: DataHandler data size is different. Expected="+expTotal+" Got="+total);
        }
        in.close();
        sdh.close();
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
}
