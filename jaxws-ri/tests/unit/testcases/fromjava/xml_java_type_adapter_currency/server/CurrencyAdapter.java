package fromjava.xml_java_type_adapter_currency.server;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.Currency;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * @author Jitendra Kotamraju
 */
public class CurrencyAdapter extends XmlAdapter<String,Currency> {

    public Currency unmarshal(String val) throws Exception {
        return Currency.getInstance(val);
    }

    public String marshal(Currency val) throws Exception {
        return val.toString();
    }

}
