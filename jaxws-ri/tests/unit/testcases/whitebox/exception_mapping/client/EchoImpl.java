
package whitebox.exception_mapping.client;


import javax.jws.WebService;
import javax.jws.Oneway;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.Holder;
import java.rmi.RemoteException;
import java.util.List;

/**
 * @author Rama Pulavarthi
 */
@WebService
public class EchoImpl {
   
    public Bar echoBar(Bar bar) throws EchoException, EchoRuntimeException {
        if(bar.getAge() < 0) {
            throw new EchoException("Age cannot be less than 0","Age cannot be less than 0");
        } else if(bar.getAge() >1000) {
            throw new EchoRuntimeException("Too much for me to process","Too much for me to process");
        }
        return bar;
    }

    public String echoString(String str) throws RuntimeException, RemoteException {
        if(str.contains("fault")) {
            throw new RuntimeException("You asked it, you got it");
        } else if(str.contains("remote")) {
            throw new RemoteException("As asked here is the Remote Exception");
        }
        return str;
    }

    public String echoStringHolder(Holder<String> str) throws WebServiceException, Error  {
        if(str.value.contains("fault")) {
            throw new WebServiceException("You asked it, you got it");
        } else if(str.value.contains("error")) {
            throw new Error("You asked it, you got it");
        }
        return str.value;
    }

    public List<Bar> echoBarList(List<Bar> list) throws Throwable {
        if(list.size() == 0) {
            throw new Throwable("List cannot be empty");
        }
        return list;
    }

    @Oneway
    public void notify(String str) throws WebServiceException, RemoteException {
        if(str.contains("fault")) {
            throw new RuntimeException("You asked it, you got it");
        } else if(str.contains("remote")) {
            throw new RemoteException("As asked here is the Remote Exception");
        }
        //do nothing
    }

}