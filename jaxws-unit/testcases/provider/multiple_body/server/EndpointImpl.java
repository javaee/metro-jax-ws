package provider.multiple_body.server;

import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import javax.xml.transform.Source;
import javax.xml.ws.*;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;

/**
 * @author Jitendra Kotamraju
 */

@WebServiceProvider(
    targetNamespace="urn:test",
    serviceName="Endpoint",
    portName="EndpointPort")

@ServiceMode(Service.Mode.MESSAGE)
public class EndpointImpl implements Provider<SOAPMessage> {
    public SOAPMessage invoke(SOAPMessage request) {
        System.out.println("Received request!");
        try {
            SOAPBody body = request.getSOAPBody();
            NodeList nl = body.getChildNodes();
            if(nl.getLength() != 2){
                throw new WebServiceException("Expected two body got one");
            }

            for(int i=0;i < nl.getLength(); i++){
                Node n = nl.item(i);
                if(i == 0 && (!n.getLocalName().equals("first") || !n.getNamespaceURI().equals("http://first.body"))){
                    throw new WebServiceException("Invalid first body");
                }
                if(i == 1 && (!n.getLocalName().equals("second") || !n.getNamespaceURI().equals("http://second.body"))){
                    throw new WebServiceException("Invalid second body");
                }
            }
        } catch (SOAPException e) {
            throw new WebServiceException(e);
        }
        return request;
    }
}
