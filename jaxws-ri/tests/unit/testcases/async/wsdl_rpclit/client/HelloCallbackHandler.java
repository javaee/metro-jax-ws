package async.wsdl_rpclit.client;

import junit.framework.TestCase;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;
import java.util.concurrent.ExecutionException;

public class HelloCallbackHandler extends TestCase implements AsyncHandler<HelloType> {

    /*
     * (non-Javadoc)
     *
     * @see javax.xml.rpc.AsyncHandler#handleResponse(javax.xml.rpc.Response)
     */
    public void handleResponse(Response<HelloType> response) {
        try {
            HelloType output = response.get();
            assertEquals("foo", output.getArgument());
            assertEquals("bar", output.getExtra());
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
