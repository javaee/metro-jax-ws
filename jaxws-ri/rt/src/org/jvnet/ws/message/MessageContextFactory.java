package org.jvnet.ws.message;

import com.sun.istack.Nullable;
import com.sun.xml.ws.api.SOAPVersion; // TODO leaking RI APIs
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.util.ServiceFinder;

//import java.io.InputStream;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;

import org.jvnet.ws.message.MessageContext;

public abstract class MessageContextFactory
{
    private static final MessageContextFactory DEFAULT = new MessageContextFactory() {
        @Override
        public MessageContext doCreate() {
            return new Packet();
        }
        public MessageContext doCreate(final SOAPMessage m) {
            // null soap message means no message should be on packet so set it to null
            return p(m == null ? null : Messages.create(m));
        }
        public MessageContext doCreate(final Source m, SOAPVersion v) {
            return p(Messages.create(m, v));
        }
        private MessageContext p(final Message m) {
            final Packet p = new Packet();
            p.setMessage(m);
            return p;
        }
    };

    public abstract MessageContext doCreate();
    public abstract MessageContext doCreate(SOAPMessage m);
    //public abstract MessageContext doCreate(InputStream x);
    public abstract MessageContext doCreate(Source x, SOAPVersion soapVersion);

    public static MessageContext create(final ClassLoader... classLoader) {
        return serviceFinder(classLoader,
                             new Creator() {
                                 public MessageContext create(final MessageContextFactory f) {
                                     return f.doCreate();
                                 }
                             });
    }

    public static MessageContext create(final SOAPMessage m, final ClassLoader... classLoader) {
        return serviceFinder(classLoader,
                             new Creator() {
                                 public MessageContext create(final MessageContextFactory f) {
                                     return f.doCreate(m);
                                 }
                             });
    }

    public static MessageContext create(final Source m, final SOAPVersion v, final ClassLoader... classLoader) {
        return serviceFinder(classLoader,
                             new Creator() {
                                 public MessageContext create(final MessageContextFactory f) {
                                     return f.doCreate(m, v);
                                 }
                             });
    }

    private static MessageContext serviceFinder(final ClassLoader[] classLoader, final Creator creator) {
        final ClassLoader cl = classLoader.length == 0 ? null : classLoader[0];
        for (MessageContextFactory factory : ServiceFinder.find(MessageContextFactory.class, cl)) {
            final MessageContext messageContext = creator.create(factory);
            if (messageContext != null)
                return messageContext;
        }
        return creator.create(DEFAULT);
    }

    private static interface Creator {
        public MessageContext create(MessageContextFactory f);
    }
}

