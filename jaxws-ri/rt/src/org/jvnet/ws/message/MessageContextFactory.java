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

    @Deprecated
    public abstract MessageContext doCreate();

    @Deprecated
    public abstract MessageContext doCreate(SOAPMessage m);

    //public abstract MessageContext doCreate(InputStream x);

    @Deprecated
    public abstract MessageContext doCreate(Source x, SOAPVersion soapVersion);

    @Deprecated
    public static MessageContext create(final ClassLoader... classLoader) {
        return serviceFinder(classLoader,
                             new Creator() {
                                 public MessageContext create(final MessageContextFactory f) {
                                     return f.doCreate();
                                 }
                             });
    }

    @Deprecated
    public static MessageContext create(final SOAPMessage m, final ClassLoader... classLoader) {
        return serviceFinder(classLoader,
                             new Creator() {
                                 public MessageContext create(final MessageContextFactory f) {
                                     return f.doCreate(m);
                                 }
                             });
    }

    @Deprecated
    public static MessageContext create(final Source m, final SOAPVersion v, final ClassLoader... classLoader) {
        return serviceFinder(classLoader,
                             new Creator() {
                                 public MessageContext create(final MessageContextFactory f) {
                                     return f.doCreate(m, v);
                                 }
                             });
    }

    @Deprecated
    private static MessageContext serviceFinder(final ClassLoader[] classLoader, final Creator creator) {
        final ClassLoader cl = classLoader.length == 0 ? null : classLoader[0];
        for (MessageContextFactory factory : ServiceFinder.find(MessageContextFactory.class, cl)) {
            final MessageContext messageContext = creator.create(factory);
            if (messageContext != null)
                return messageContext;
        }
        return creator.create(DEFAULT);
    }

    @Deprecated
    private static interface Creator {
        public MessageContext create(MessageContextFactory f);
    }
}

