package async.executor_821.client;

import junit.framework.TestCase;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;
import java.util.concurrent.ExecutionException;

/*
 * @author Jitendra Kotamraju
 */
public class Hello0CallbackHandler extends TestCase implements AsyncHandler<Integer> {
    public void handleResponse(Response<Integer> response) {
        try {
            Integer output = response.get();
            assertEquals(54321, output.intValue());
            System.out.println("Callback Handler Completed-Test pass");
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
