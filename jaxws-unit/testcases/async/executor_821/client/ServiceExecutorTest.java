package async.executor_821.client;

import junit.framework.TestCase;
import testutil.AttachmentHelper;
import testutil.ClientServerTestUtil;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.soap.MTOMFeature;
import javax.xml.ws.Holder;
import java.awt.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.*;
import javax.xml.ws.*;

/**
 * @author Jitendra Kotamraju
 */
public class ServiceExecutorTest extends TestCase {

    private Hello proxy;
    private MyExecutor executor;
    private ExecutorService delegateExecutor;

    public ServiceExecutorTest(String name) throws Exception {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        Hello_Service service = new Hello_Service();
        delegateExecutor = Executors.newFixedThreadPool(5);
        executor = new MyExecutor(delegateExecutor);
        service.setExecutor(executor);
        proxy = service.getHelloPort();
    }

    private static class MyExecutor implements Executor {
        private volatile boolean executed;
        private final ExecutorService delegateExecutor;

        MyExecutor(ExecutorService delegateExecutor) {
            this.delegateExecutor = delegateExecutor;
        }

        public boolean isExecuted() {
            return executed;
        }

        public void execute(Runnable command) {
            executed = true;
            delegateExecutor.execute(command);
        }
    }

    public void testHello0Poll() throws Exception {
        long begin = System.currentTimeMillis();
        Response<Integer> response1 = proxy.hello0Async(54321);
        long end = System.currentTimeMillis();
        if (end-begin > 5000) {
            fail("Invoking thread doesn't seem to spawn a new thread for async processing");
        }

        begin = System.currentTimeMillis();
        Response<Integer> response2 = proxy.hello0Async(43210);
        end = System.currentTimeMillis();
        if (end-begin > 5000) {
            fail("Invoking thread doesn't seem to spawn a new thread for async processing");
        }

        begin = System.currentTimeMillis();
        Response<Integer> response3 = proxy.hello0Async(32104);
        if (end-begin > 5000) {
            fail("Invoking thread doesn't seem to spawn a new thread for async processing");
        }

        Integer output1 = response1.get(15, TimeUnit.SECONDS);
        assertEquals(54321, (int)output1);
        Integer output2 = response2.get(15, TimeUnit.SECONDS);
        assertEquals(43210, (int)output2);
        Integer output3 = response3.get(15, TimeUnit.SECONDS);
        assertEquals(32104, (int)output3);

        assertTrue(executor.isExecuted());
    }

    public void testHello1Callback() throws Exception {
        long begin = System.currentTimeMillis();
        String arg = "foo";
        String extra = "bar";
        HelloType req = new HelloType();
        req.setArgument(arg);
        req.setExtra(extra);

        HelloType reqH = new HelloType();
        reqH.setArgument("header arg");
        reqH.setExtra("header extra");

        Exchanger<Hello1Response> exchanger = new Exchanger<Hello1Response>();

        proxy.hello1Async(req, reqH, new Hello1CallbackHandler(exchanger));
        long end = System.currentTimeMillis();
        if (end-begin > 5000) {
            fail("Invoking thread doesn't seem to spawn a new thread for async processing");
        }

        Hello1Response resp = exchanger.exchange(null, 15, TimeUnit.SECONDS);
        HelloOutput out = resp.getHelloOutput();
        HelloType outH = resp.getHeader();
        assertEquals("foo", out.getArgument());
        assertEquals("bar", out.getExtra());
        assertEquals("header arg", outH.getArgument());
        assertEquals("header extra", outH.getExtra());

        assertTrue(executor.isExecuted());
    }

    @Override
    protected void tearDown() {
        delegateExecutor.shutdown();
    }

}
