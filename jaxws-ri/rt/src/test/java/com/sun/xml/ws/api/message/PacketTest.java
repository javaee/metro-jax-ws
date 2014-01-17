package com.sun.xml.ws.api.message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.soap.MTOMFeature;

import org.w3c.dom.Node;

import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.binding.BindingImpl;

import junit.framework.TestCase;

public class PacketTest extends TestCase {
	/**
	 * Tests that a server response Packet with MTOM feature, but
	 * decoded from an InputStream with a user specified non-MTOM
	 * content type, does NOT use MTOM when re-encoded
	 * @throws Exception
	 */
	public void testEncodeDecodedPacketMtom() throws Exception {
		String msg = "<?xml version='1.0' encoding='UTF-8'?>" +
	"<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
	"<soapenv:Body><soapenv:Fault>" +
	"<faultcode>soapenv:Server</faultcode>" +
	"<faultstring>ABC-380001:Internal Server Error</faultstring>" +
	"<detail><con:fault xmlns:con=\"http://www.bea.com/wli/sb/context\">" +
	"<con:errorCode>ABC-380001</con:errorCode>" +
	"<con:reason>Internal Server Error</con:reason>" + 
	"<con:location><con:node>RouteNode1</con:node><con:path>response-pipeline</con:path></con:location>" +
	"</con:fault></detail>" +
	"</soapenv:Fault></soapenv:Body></soapenv:Envelope>";
		WebServiceFeature[] features = {new MTOMFeature(true, 0)};
		MessageContextFactory mcf = new MessageContextFactory(features);
		Packet fakeRequest = (Packet) mcf.createContext();
		Packet p = (Packet) mcf.createContext(new ByteArrayInputStream(msg.getBytes()), 
				"text/xml");
		
		fakeRequest.relateServerResponse(p, null, null, BindingImpl.create(BindingID.SOAP11_HTTP, features));

		ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
		p.writeTo(bos);
		String writtenMsg = new String(bos.toByteArray());
		System.out.println(writtenMsg);
		assertEquals("text/xml", p.getContentType().getContentType());

		//try reading the message as a soap message with text/xml - this should succeed
		//in parsing the message
		Packet reReadPacket = (Packet) mcf.createContext(new ByteArrayInputStream(writtenMsg.getBytes()), "text/xml");
		SOAPMessage soap = reReadPacket.getAsSOAPMessage();
		Node bodyChild = soap.getSOAPBody().getFirstChild();
		assertEquals("Fault", bodyChild.getLocalName());
	}
}
