package async.wsdl_rpclit.client;

import junit.framework.TestCase;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;
import java.util.concurrent.ExecutionException;

public class Hello0CallbackHandler extends TestCase implements AsyncHandler<Integer> {

    /*
     * (non-Javadoc)
     *
     * @see javax.xml.rpc.AsyncHandler#handleResponse(javax.xml.rpc.Response)
     */
    public void handleResponse(Response<Integer> response) {
        try {
            Integer output = response.get();
            assertEquals(output.intValue(), 54321);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
