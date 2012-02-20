package handler.messagehandler.client;

import com.sun.xml.ws.api.handler.MessageHandlerContext;
import com.sun.xml.ws.api.handler.MessageHandler;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.message.jaxb.JAXBMessage;

import javax.xml.namespace.QName;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
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
            if((Boolean)context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)) {
                handler.messagehandler.client.Hello hello= (handler.messagehandler.client.Hello) obj.getValue();
                hello.setX(hello.getX()+1);
                obj.setValue(hello);
                System.out.println(hello);
            } else {
                handler.messagehandler.client.HelloResponse helloResponse= (handler.messagehandler.client.HelloResponse) obj.getValue();
                helloResponse.setReturn(helloResponse.getReturn()+1);
                obj.setValue(helloResponse);
                System.out.println(helloResponse);
            }
           //Message newMessage = Messages.create(jc.createMarshaller(),obj,context.getWSBinding().getSOAPVersion());
           Message newMessage = Messages.create(jc,obj,context.getWSBinding().getSOAPVersion());
           context.setMessage(newMessage);
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
