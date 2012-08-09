package fromjava.jaxb_annotation;

import javax.jws.WebService;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;


@WebService(name = "Echo", serviceName = "EchoService", targetNamespace = "http://echo.org/")
public class EchoImpl {
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    public String normalize(@XmlJavaTypeAdapter(NormalizedStringAdapter.class) String value) {
        return value;
    }

    public NormalizedString echo(NormalizedString str) {
        return str;
    }
}