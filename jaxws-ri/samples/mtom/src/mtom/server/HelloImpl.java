package mtom.server;

import javax.jws.WebService;
import javax.xml.ws.Holder;
import javax.xml.ws.soap.MTOM;
import javax.activation.DataHandler;
import java.awt.Image;
@MTOM
@WebService (endpointInterface = "mtom.server.Hello")
public class HelloImpl implements Hello {
    public void detail (Holder<byte[]> photo, Holder<Image> image) {
    }
      
    public void echoData (Holder<byte[]> data){
    }
}
