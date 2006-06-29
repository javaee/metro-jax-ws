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

package provider.server;

import java.io.ByteArrayInputStream;
import java.rmi.RemoteException;
import java.util.Map;

import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.ServiceMode;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;
import org.w3c.dom.Node;
import javax.xml.ws.WebServiceProvider;

@ServiceMode(value=Service.Mode.PAYLOAD)
@WebServiceProvider()
public class AddNumbersImpl implements Provider<Source> {
    public Source invoke(Source source) {
        try {
            DOMResult dom = new DOMResult();
            Transformer trans = TransformerFactory.newInstance().newTransformer();
            trans.transform(source, dom);
            Node node = dom.getNode();
            Node root = node.getFirstChild();
            Node first = root.getFirstChild();
            int number1 = Integer.decode(first.getFirstChild().getNodeValue());
            Node second = first.getNextSibling();
            int number2 = Integer.decode(second.getFirstChild().getNodeValue());
            return sendSource(number1, number2);
        } catch(Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error in provider endpoint", e);
        }
    }
    
    private Source sendSource(int number1, int number2) {
        int sum = number1+number2;
        String body =
            "<ns:addNumbersResponse xmlns:ns=\"http://duke.example.org\"><ns:return>"
            +sum
            +"</ns:return></ns:addNumbersResponse>";
        Source source = new StreamSource(
            new ByteArrayInputStream(body.getBytes()));
        return source;
    }
    
}
