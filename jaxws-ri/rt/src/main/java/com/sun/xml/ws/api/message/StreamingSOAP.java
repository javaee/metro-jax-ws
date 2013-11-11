package com.sun.xml.ws.api.message;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.sun.xml.ws.encoding.TagInfoset;

public interface StreamingSOAP {
    public XMLStreamReader readEnvelope();
    public QName getPayloadQName();   
    public XMLStreamReader readToBodyStarTag() throws XMLStreamException;
    public XMLStreamReader readPayload() throws XMLStreamException;
    public void writeToBodyStart(XMLStreamWriter w) throws XMLStreamException;
    public void writePayloadTo(XMLStreamWriter writer)throws XMLStreamException;
    public boolean isPayloadStreamReader();
}
