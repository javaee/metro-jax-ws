package async.wsdl_hello_lit.client;

import junit.framework.TestCase;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;
import java.util.concurrent.ExecutionException;

public class HelloCallbackHandler extends TestCase implements AsyncHandler<HelloOutput> {
    public void handleResponse(Response<HelloOutput> response) {
        try {
            HelloOutput output = response.get();
            assertEquals("foo", output.getArgument());
            assertEquals("bar", output.getExtra());
            System.out.println("Callback Handler Completed-Test pass");
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
