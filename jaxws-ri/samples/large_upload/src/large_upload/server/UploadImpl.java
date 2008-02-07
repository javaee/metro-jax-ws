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

package large_upload.server;

import java.io.*;
import javax.jws.WebService;
import javax.activation.*;
import javax.jws.*;
import javax.xml.bind.annotation.*;
import javax.xml.ws.soap.*;
import javax.xml.ws.*;

import com.sun.xml.ws.developer.StreamingDataHandler;

/**
 * @author Jitendra Kotamraju
 */

@MTOM
@WebService
public class UploadImpl {
    
    public void fileUpload(String name, @XmlMimeType("application/octet-stream") DataHandler data) {
        try {
             StreamingDataHandler dh = (StreamingDataHandler)data;
             File file = File.createTempFile(name, "");
             System.out.println("Creating file = "+file);
             dh.moveTo(file);
             dh.close();
             System.out.println("Verifying file = "+file);
             verifyFile(file);
             System.out.println("Verified file = "+file);
             file.delete();
             System.out.println("Deleted file = "+file);
        } catch(Exception e) {
            throw new WebServiceException(e);
        }
    }

    private void verifyFile(File file) throws IOException {
        FileInputStream fin = new FileInputStream(file);
        try {
             byte buf[] = new byte[8192];
             
             for(int i=0; i < 100000; i++) {
                 int len = 0;
                 while(len < buf.length) {
                     int cur = fin.read(buf, len, buf.length-len);
                     if (cur == -1) {
                         throw new WebServiceException("EOF. Didn't receive all the file");
                     }
                     len += cur;
                 }
                 for(int j=0; j < len; j++) {
                     if (buf[j] != (byte)j) {
                         throw new WebServiceException();
                     }
                 }
             }
        } finally {
            fin.close();
        }
    }

}
