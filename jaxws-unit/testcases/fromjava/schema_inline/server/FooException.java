package fromjava.schema_inline.server;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Jitendra Kotamraju
 */
@XmlType(name = "FooException", namespace = "urn:test:types")
public class FooException {

    @XmlElement(name = "varInt", namespace = "", type = Integer.class)
    protected int varInt;

    @XmlTransient
    public int getVarInt() {
        return varInt;
    }

    public void setVarInt(int value) {
        this.varInt = value;
    }

}
