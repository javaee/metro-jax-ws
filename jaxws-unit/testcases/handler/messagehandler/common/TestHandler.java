package handler.messagehandler.common;

import com.sun.xml.ws.api.handler.MessageHandler;
import com.sun.xml.ws.api.handler.MessageHandlerContext;
import com.sun.xml.ws.api.message.Message;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import java.util.Set;

public class TestHandler implements MessageHandler<MessageHandlerContext> {

    public Set<QName> getHeaders() {
        return null;
    }

    public boolean handleMessage(MessageHandlerContext context) {
        JAXBContext jc = ((com.sun.xml.ws.model.AbstractSEIModelImpl)(context.getSEIModel())).getBindingContext().getJAXBContext();
        Message in_message = context.getMessage();
       try {
            JAXBElement obj = in_message.readPayloadAsJAXB(jc.createUnmarshaller());
            //handler.messagehandler.client.Hello hello= obj.getValue();
            //System.out.println(hello); 
       } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public boolean handleFault(MessageHandlerContext context) {
        return true;
    }

    public void close(MessageContext context) {}

}
