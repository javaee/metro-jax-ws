package mtom.charset.server;

import javax.xml.ws.WebServiceException;
import javax.activation.DataHandler;
import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import java.awt.*;
import java.io.*;
import java.util.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.ws.soap.MTOM;

/**
 * @author Jitendra Kotamraju
 */
@MTOM
@WebService
public class HelloImpl {

    public String echoBinaryAsString(byte[] buf) {
        try {
            return new String(buf, "UTF-8");
        } catch(Exception e) {
            throw new WebServiceException(e);
        }
    }

    public String echoBinaryAsString16(byte[] buf) {
        try {
            return new String(buf, "UTF-16");
        } catch(Exception e) {
            throw new WebServiceException(e);
        }
    }

    public String echoString(String str) {
        return str;
    }

}
