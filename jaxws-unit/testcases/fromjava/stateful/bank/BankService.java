package fromjava.stateful.bank;

import javax.jws.WebService;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

/**
 * @author Kohsuke Kawaguchi
 */
@WebService(portName = "BankServicePort")
public class BankService {
    public W3CEndpointReference getAccount(int id) {
        Account a = new Account(id);
        return Account.manager.export(a);
    }
}
