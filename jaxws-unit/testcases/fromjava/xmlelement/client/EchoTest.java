package fromjava.xmlelement.client;

import junit.framework.TestCase;

import javax.xml.bind.annotation.XmlElement;
import java.lang.reflect.Field;

/**
 * @author Jitendra Kotamraju
 */
public class EchoTest extends TestCase {

    public void testEchoInt() throws Exception {
        Field f = EchoInt.class.getDeclaredField("arg0");
        assertTrue(getNillable(f));
    }

    public void testEchoIntResponse() throws Exception {
        Field f = EchoIntResponse.class.getDeclaredField("_return");
        assertFalse(getNillable(f));
    }

    public void testEchoStringResponse() throws Exception {
        Field f = EchoStringResponse.class.getDeclaredField("_return");
        assertFalse(getNillable(f));
        assertTrue(getRequired(f));
    }

    private boolean getNillable(Field f) {
        XmlElement elem = f.getAnnotation(XmlElement.class);
        return elem.nillable();
    }

    private boolean getRequired(Field f) {
        XmlElement elem = f.getAnnotation(XmlElement.class);
        return elem.required();
    }

}
