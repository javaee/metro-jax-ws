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

package restful.client;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import org.w3c.dom.Node;
import java.net.URL;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.util.Properties;

public class AddNumbersClient {

    public static void main (String[] args) throws Exception {
        String endpointAddress = 
            "http://localhost:8080/jaxws-restful/addnumbers";
        URL url = new URL(endpointAddress+"?num1=10&num2=20");
        System.out.println ("Invoking URL="+url);
        process(url);

        url = new URL(endpointAddress+"/num1/10/num2/20");
        System.out.println ("Invoking URL="+url);
        process(url);
    }

    private static void process(URL url) throws Exception {
        InputStream in = url.openStream();
        StreamSource source = new StreamSource(in);
        printSource(source);
    }

    private static void printSource(Source source) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            StreamResult sr = new StreamResult(bos );
            Transformer trans = TransformerFactory.newInstance().newTransformer();
            Properties oprops = new Properties();
            oprops.put(OutputKeys.OMIT_XML_DECLARATION, "yes");
            trans.setOutputProperties(oprops);
            trans.transform(source, sr);
            System.out.println("**** Response ******"+bos.toString());
            bos.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}
