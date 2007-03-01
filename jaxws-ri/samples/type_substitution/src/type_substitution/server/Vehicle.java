package type_substitution.server;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlJavaTypeAdapter(Car.Adapter.class)
public interface Vehicle {
    String getYear();
    String getModel();
}
