package provider.attachment_595.client;

import junit.framework.TestCase;

/**
 * Client SOAP handler adds an attachment
 *
 * @author Jitendra Kotamraju
 */
public class AttachmentTest extends TestCase {

    public AttachmentTest(String name) throws Exception {
        super(name);
    }

    public void testProxy() {
        Hello hello = new Hello_Service().getHelloPort();
        hello.voidTest(new VoidType());
    }

}
