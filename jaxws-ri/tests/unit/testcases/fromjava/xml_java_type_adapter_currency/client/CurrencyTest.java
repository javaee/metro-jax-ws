package fromjava.xml_java_type_adapter_currency.client;

import junit.framework.TestCase;

/**
 * @author Jitendra Kotamraju
 */
public class CurrencyTest extends TestCase {

    public CurrencyTest(String name) throws Exception{
        super(name);
    }

    public void testCurrency() throws Exception {
        Echo echoPort = new EchoService().getEchoPort(); 
        String currency = echoPort.getCurrency();
        assertEquals("USD", currency);
    }

}
