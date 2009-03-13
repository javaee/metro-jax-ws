
package fromjava.xml_java_type_adapter.server;

import javax.jws.*;

import javax.xml.ws.Holder;
import javax.xml.ws.*;
import javax.xml.soap.*;
import javax.xml.ws.soap.SOAPFaultException;
import javax.xml.namespace.QName;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.rmi.RemoteException;

@WebService(name="Echo", serviceName="echoService", targetNamespace="http://echo.org/")
public class EchoImpl {

    @XmlJavaTypeAdapter(HexBinaryAdapter.class)
    public byte[] echo(String str,
                       @XmlJavaTypeAdapter(HexBinaryAdapter.class)
                       byte[] bin) {
        return bin;
    }
    
}
