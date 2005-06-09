/*
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved. 
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

@ServiceMode(value=Service.Mode.PAYLOAD)
public class AddNumbersImpl implements Provider<Source> {

    public Source invoke(Source source, Map<String, Object> context)
        throws RemoteException {
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
            throw new RemoteException("Error in provider endpoint");
        }
    }

    private Source sendSource(int number1, int number2) {
        int sum = number1+number2;
        String body =
            "<ns:addNumbersResponse xmlns:ns=\"http://duke.org\"><return>"
            +sum
            +"</return></ns:addNumbersResponse>";
        Source source = new StreamSource(
            new ByteArrayInputStream(body.getBytes()));
        return source;
    }

}
