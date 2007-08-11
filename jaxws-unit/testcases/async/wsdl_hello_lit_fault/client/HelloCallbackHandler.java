package async.wsdl_hello_lit_fault.client;

import junit.framework.TestCase;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;
import java.util.concurrent.ExecutionException;

public class HelloCallbackHandler extends TestCase implements AsyncHandler<HelloOutput> {
    public void handleResponse(Response<HelloOutput> response) {
        System.out.println("In asyncHandler");
        try {
            HelloOutput output = response.get();
            //assertEquals("foo", output.getArgument());
            //assertEquals("bar", output.getExtra());
        } catch (ExecutionException e) {
            System.out.println("ExecutionException thrown");
            assertTrue(e.getCause() instanceof HelloFault);
            assertTrue(true);
            //e.printStackTrace();
        } catch (InterruptedException e) {
            assertTrue(false);
            // e.printStackTrace();
        } catch (Exception ex) {
            System.out.println("e is " + ex.getClass().getName());
            ex.printStackTrace();
        }
    }
}
