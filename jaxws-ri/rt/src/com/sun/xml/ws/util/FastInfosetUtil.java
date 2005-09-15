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

package com.sun.xml.ws.util;

import java.io.OutputStream;
import java.io.StringReader;
import java.util.StringTokenizer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;

import com.sun.xml.ws.util.xml.XmlUtil;

public class FastInfosetUtil {
    
    public static boolean isFastInfosetAccepted(String[] accepts) {
        for (String accept : accepts) {
            if (isFastInfosetAccepted(accept)) {
                return true;
            }
        }
        
        return false;
    }

    public static boolean isFastInfosetAccepted(String accept) {
        StringTokenizer st = new StringTokenizer(accept, ",");
        while (st.hasMoreTokens()) {
            final String token = st.nextToken().trim();
            if (token.equalsIgnoreCase("application/fastinfoset")) {
                return true;
            }
        }
        
        return false;
    }
    
    public static void transcodeXMLStringToFI(String xml, OutputStream out) {
        try {
            XmlUtil.newTransformer().transform(
                new StreamSource(new java.io.StringReader(xml)),
                FastInfosetReflection.FastInfosetResult_new(out));
        }
        catch (Exception e) {
            // Ignore
        }
    }

}
