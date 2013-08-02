package deployment.partial_webxml_multi.client;

import junit.framework.TestCase;
/**
 * @author Rama Pulavarthi
 */
public class SimpleTest extends TestCase{
    public void test1() {
        String resposne = new HelloImplService().getEchoPort().hello("Duke");
        assertEquals(resposne, "Hello Duke");

        int resp = new CalculatorImplService().getCalculatorPort().add(1,2);
        assertEquals(resp,3);
    }
}
