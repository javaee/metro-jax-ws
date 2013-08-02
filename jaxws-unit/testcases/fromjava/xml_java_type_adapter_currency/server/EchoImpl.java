
package fromjava.xml_java_type_adapter_currency.server;

import javax.jws.WebService;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.ws.WebServiceException;
import java.util.Currency;
import java.util.Locale;

/**
 * @author Jitendra Kotamraju
 */
@WebService(name="Echo", serviceName="echoService", targetNamespace="http://echo.org/")
public class EchoImpl {

    public Currency getCurrency() {
        return Currency.getInstance(Locale.US);
    }
    
}
