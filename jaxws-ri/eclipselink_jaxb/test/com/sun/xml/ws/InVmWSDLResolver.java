package com.sun.xml.ws;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.Holder;

import org.jvnet.ws.databinding.WSDLGenerator;
import org.xml.sax.SAXException;

import com.sun.xml.ws.api.streaming.XMLStreamReaderFactory;
import com.sun.xml.ws.api.wsdl.parser.XMLEntityResolver;
import com.sun.xml.ws.api.wsdl.parser.XMLEntityResolver.Parser;
import com.sun.xml.ws.streaming.TidyXMLStreamReader;
import com.sun.xml.ws.wsdl.writer.WSDLResolver;

public class InVmWSDLResolver implements WSDLResolver, org.jvnet.ws.databinding.WSDLResolver {
	String wsdlID = null;
	ByteArrayOutputStream wsdlIO = null;
	HashMap<String, ByteArrayOutputStream> files = new HashMap<String, ByteArrayOutputStream>();
	public Result getAbstractWSDL(Holder<String> h) {
//		System.out.println("---- getAbstractWSDL " + h.value);
		return result(h.value, null);
	}

	public Result getSchemaOutput(String v, Holder<String> h) {
//		System.out.println("---- getSchemaOutput " + v + " " + h.value);
		return result(h.value, null);
	}

	public Result getWSDL(String v) {
//		System.out.println("---- getWSDL " + v);
		wsdlID = v;
		wsdlIO = new ByteArrayOutputStream();
		return result(v, wsdlIO);
	}
    private Result result(String s, ByteArrayOutputStream bio) {
    	if (bio == null) bio = new ByteArrayOutputStream();
    	files.put(s, bio);
        Result result = new StreamResult(bio);
        result.setSystemId(s.replace('\\', '/'));
        return result;
    }
    
    public void print() {
    	for(String s : files.keySet()) {
//			System.out.println("---- file name: " + s);
			System.out.println(new String(files.get(s).toByteArray()));    			
    	}
    }
    public void printXSD() {
    	for(String s : files.keySet()) {
    		if (s.endsWith("xsd")) {
//				System.out.println("---- file name: " + s);
				System.out.println(new String(files.get(s).toByteArray()));    	
    		}
    	}
    }
    
    
    public Parser getWsdlSource() throws Exception  {
//    	return new StreamSource(new ByteArrayInputStream(wsdlIO.toByteArray()), wsdlID);
    	ByteArrayInputStream bi = new ByteArrayInputStream(wsdlIO.toByteArray()); 
        return new Parser(new URL("file://"+wsdlID), new TidyXMLStreamReader(XMLStreamReaderFactory.create(wsdlID, bi, true), bi));
    }
    
    public XMLEntityResolver getEntityResolver() {
    	return new XMLEntityResolver() {
    	    public Parser resolveEntity(String publicId, String systemId) throws SAXException, IOException {
//    			System.out.println("---- publicId: " + publicId);
//    			System.out.println("---- systemId: " + systemId);
    			ByteArrayOutputStream b = files.get(systemId);
    			if (b!= null) {
    				ByteArrayInputStream bi = new ByteArrayInputStream(b.toByteArray()); 
    	            return new Parser(null, new TidyXMLStreamReader(XMLStreamReaderFactory.create(systemId, bi, true), bi));
    			}
				return null;
			}
    		
    	};
    }
    
    public HashMap<String, ByteArrayOutputStream> getAll() { 
        return files;
    }	
}