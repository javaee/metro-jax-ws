package org.jvnet.ws.message;

import java.io.IOException;
import java.io.InputStream;

import com.sun.xml.ws.api.SOAPVersion; // TODO leaking RI APIs
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.util.ServiceFinder;

//import java.io.InputStream;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.ws.WebServiceFeature;

import org.jvnet.ws.EnvelopeStyle;
import org.jvnet.ws.message.MessageContext;

public abstract class MessageContextFactory
{   
    private static final MessageContextFactory DEFAULT = new com.sun.xml.ws.api.message.MessageContextFactory(new WebServiceFeature[0]);

    protected abstract MessageContextFactory newFactory(WebServiceFeature ... f);
    
    public abstract MessageContext createContext();

    public abstract MessageContext createContext(SOAPMessage m);
    
    public abstract MessageContext createContext(Source m);
    
    public abstract MessageContext createContext(Source m, EnvelopeStyle.Style envelopeStyle);
    
    public abstract MessageContext createContext(InputStream in, String contentType) throws IOException;
    
    static public MessageContextFactory createFactory(WebServiceFeature ... f) {
        return createFactory(null, f);
    }
    
    static public MessageContextFactory createFactory(ClassLoader cl, WebServiceFeature ...f) {
        for (MessageContextFactory factory : ServiceFinder.find(MessageContextFactory.class, cl)) {
            MessageContextFactory newfac = factory.newFactory(f);
            if (newfac != null) return newfac;
        }
        return new com.sun.xml.ws.api.message.MessageContextFactory(f);
    }  
}

