package fromwsdl.handler_simple_rpclit.client;

import org.w3c.dom.Node;

import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.LogicalMessage;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

/*
 * This handler needs to be in the same package as the
 * client code so that it is compiled by the test harness
 * at the same time as the client code (so that it can
 * pick up the generated beans).
 */
public class LogicalTestHandler implements LogicalHandler<LogicalMessageContext> {

    public static enum HandleMode { SOURCE, JAXB, SOURCE_NO_CHANGE }

    private HandleMode mode = HandleMode.SOURCE;

    public void setHandleMode(HandleMode mode) {
        this.mode = mode;
    }

    public boolean handleMessage(LogicalMessageContext context) {
        try {
            if (mode == HandleMode.SOURCE) {
                return handleMessageSource(context);
            }else if (mode == HandleMode.SOURCE_NO_CHANGE) {
                return handleMessageSourceNoChange(context);
            } else {
                throw new Exception("unknown command");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // makes sure we can get the source without breaking something downstream
    private boolean handleMessageSourceNoChange(LogicalMessageContext context) {
        LogicalMessage logMsg = context.getMessage();
        Source msgSource = logMsg.getPayload();
        return true;
    }

//    private boolean handleMessageJAXB(LogicalMessageContext context)
//        throws Exception {
//
//        JAXBContext jaxbContext =
//            JAXBContext.newInstance(ObjectFactory.class);
//        LogicalMessage msg = context.getMessage();
//        if (context.get(context.MESSAGE_OUTBOUND_PROPERTY).equals(
//            Boolean.TRUE)) {
//
//            Hello_Type hello = (Hello_Type) msg.getPayload(jaxbContext);
//            int num = hello.getIntin();
//            hello.setIntin(++num);
//            msg.setPayload(hello, jaxbContext);
//        } else {
//            HelloResponse hello = (HelloResponse)
//                msg.getPayload(jaxbContext);
//            int num = hello.getIntout();
//            hello.setIntout(++num);
//            msg.setPayload(hello, jaxbContext);
//        }
//        return true;
//    }

    private boolean handleMessageSource(LogicalMessageContext context)
        throws Exception {

        LogicalMessage msg = context.getMessage();
        DOMResult dResult = createDOMResult(msg.getPayload());

        Node documentNode = dResult.getNode();
        Node requestResponseNode = documentNode.getFirstChild();
        Node textNode = requestResponseNode.getFirstChild().getFirstChild();
        int orig = Integer.parseInt(textNode.getNodeValue());
        textNode.setNodeValue(String.valueOf(++orig));
        DOMSource source = new DOMSource(documentNode);
        msg.setPayload(source);

        int removeme = 0;
        return true;
    }

    private DOMResult createDOMResult(Source source) throws Exception {
        if (source == null) {
            throw new RuntimeException("source is null in logical message");
        }
        Transformer xFormer =
            TransformerFactory.newInstance().newTransformer();
        xFormer.setOutputProperty("omit-xml-declaration", "yes");
        DOMResult result = new DOMResult();
        xFormer.transform(source, result);
        return result;
    }

    /**** empty methods below ****/
    public void close(MessageContext context) {}

    public boolean handleFault(LogicalMessageContext context) {
        return true;
    }

}
