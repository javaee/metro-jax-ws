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

package large_upload.client;

import java.io.*;
import java.util.*;
import javax.xml.ws.*;
import javax.xml.ws.soap.*;
import javax.activation.*;

import com.sun.xml.ws.developer.JAXWSProperties;
import com.sun.xml.ws.developer.StreamingDataHandler;

/**
 * @author Jitendra Kotamraju
 */
public class UploadClient {

    public static void main (String[] args) throws IOException {
        MTOMFeature feature = new MTOMFeature();
        UploadImplService service = new UploadImplService();
        UploadImpl proxy = service.getUploadImplPort(feature);        
        Map<String, Object> ctxt = ((BindingProvider)proxy).getRequestContext();
        ctxt.put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192); 
        File file = getFile();
        proxy.fileUpload("file.bin", new DataHandler(new FileDataSource(file)));
        file.delete();
    }

    public static File getFile() throws IOException {
        File file = File.createTempFile("jaxws", ".bin");
        OutputStream out = new FileOutputStream(file);
        byte buf[] = new byte[8192];
        for(int i=0; i < buf.length; i++) {
            buf[i] = (byte)i;
        }
        for(int i=0; i < 100000; i++) {
            out.write(buf);
        } 
        out.close();
        return file;
    }

}
