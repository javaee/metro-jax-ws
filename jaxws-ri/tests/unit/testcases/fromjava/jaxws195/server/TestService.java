package fromjava.jaxws195.server;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import java.util.Date;


@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class TestService {

    public Date echoDate(Date dateArg) {
        return dateArg;
    }
}
