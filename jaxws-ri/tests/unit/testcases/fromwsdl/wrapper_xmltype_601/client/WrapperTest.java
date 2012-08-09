package fromwsdl.wrapper_xmltype_601.client;

import junit.framework.TestCase;

import javax.xml.ws.Holder;
import java.math.BigInteger;

/**
 * Test for issue 601
 * xml type is used for deciding whether a wrapper parameter is INOUT
 *
 * @author Jitendra Kotamraju
 */
public class WrapperTest extends TestCase {

    private Hello proxy;

    public WrapperTest(String name) throws Exception{
        super(name);
    }

    protected void setUp() throws Exception {
        proxy = new Hello_Service().getHelloPort();
    }

    public void testMethod() {
        BigInteger inParam1 = new BigInteger("10000");
        Holder<BigInteger> outParam1 = new Holder<BigInteger>();
        int ret = proxy.testApl(inParam1, outParam1);
        assertEquals(10, ret);
        assertEquals(inParam1, outParam1.value);
    }

}
