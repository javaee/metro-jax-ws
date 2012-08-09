package async.async_service.client;

import junit.framework.TestCase;
import java.lang.Exception;
import java.util.concurrent.*;
import java.util.Date;
import java.util.Random;



/**
 * @author Rama Pulavarthi
 */
public class AsyncServiceTest extends TestCase {
    private static final int NO_THREADS = 20;
    private static final int NO_OF_REQUESTS = 1000;
    private final Hello proxy;
    private int noReqs = 0;
    private int noResps = 0;

    public AsyncServiceTest(String name) throws Exception {
        super(name);
        Hello_Service service = new Hello_Service();
		proxy = service.getHelloPort();
    }

    public void testAsync() throws Exception {
      invoke("Hello","Duke");  
    }
    
    public void testAsyncWithCachedThreadPool() throws Exception {
        ExecutorService service = new ThreadPoolExecutor(NO_THREADS/2, NO_THREADS,
            30L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        synchronized(this) {
            noReqs = NO_OF_REQUESTS; noResps = 0;
        }
        doTestWithThreadPool(service, noReqs);
        service.shutdown();
        while(!service.awaitTermination(5L, TimeUnit.SECONDS));
        synchronized(this) {
            if(noReqs!= noResps) {
                throw new Exception("No. of requests and responses did not match");
            }
        }
    }

    private void doTestWithThreadPool(ExecutorService service, int noReqs) throws Exception {
        for(int i=0; i < noReqs; i++) {
            final int j = i;
            service.execute(new Runnable() {
                public void run() {
                    try {
                        invoke("Hello", "Duke"+j);
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

	private void invoke(String arg, String extra) throws Exception {
    	Hello_Type req = new Hello_Type();
        req.setArgument(arg);
        req.setExtra(extra);
        System.out.println("Invoking Web Service with = " + arg + "," + extra);
        HelloResponse response = proxy.hello(req, req);
        System.out.println("arg=" + response.getArgument() + " extra=" + response.getExtra());

        if(!arg.equals(response.getArgument()) || !extra.equals(response.getExtra())) {
            throw new Exception("Mismatch in comparison");
        }

        synchronized (this) {
            ++noResps;
        }
	}
}