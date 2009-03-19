
package fromjava.holder_508.server;

import javax.jws.*;

import javax.xml.ws.Holder;
import javax.xml.ws.*;
import javax.xml.soap.*;
import javax.xml.ws.soap.SOAPFaultException;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.rmi.RemoteException;

@WebService(endpointInterface="fromjava.holder_508.server.AddNumbersPortType")
public class EchoImpl {

    @WebMethod
    public int addNumbers(Holder<Integer> holder, int val) {
        if (holder.value == null) {
            return val;
        }
        holder.value = holder.value+val;
        return holder.value;
    }
}
