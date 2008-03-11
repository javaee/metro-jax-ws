package async.wsdl_hello_lit_fault.client;

import junit.framework.TestCase;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;
import java.util.concurrent.ExecutionException;

public class HelloCallbackHandler extends TestCase implements AsyncHandler<HelloOutput> {
    public transient int noTimes;
    public transient boolean correctException;

    public void handleResponse(Response<HelloOutput> response) {
        synchronized(this) {
            ++noTimes;
        }
        System.out.println("In asyncHandler noTimes="+noTimes+" thread="+Thread.currentThread());
        if (noTimes != 1) {
            return;
        }
        try {
            HelloOutput output = response.get();
            //assertEquals("foo", output.getArgument());
            //assertEquals("bar", output.getExtra());
        } catch (ExecutionException e) {
            System.out.println("ExecutionException thrown");
            if (e.getCause() instanceof HelloFault) {
                correctException = true;
            }
        } catch (Exception ex) {
            fail("Got InterruptedException");
        }
    }
}
