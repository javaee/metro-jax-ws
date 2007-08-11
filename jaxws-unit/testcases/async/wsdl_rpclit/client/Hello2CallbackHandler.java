package async.wsdl_rpclit.client;

import junit.framework.TestCase;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;
import java.util.concurrent.ExecutionException;

public class Hello2CallbackHandler extends TestCase implements AsyncHandler<Hello2Response> {
    /*
    * (non-Javadoc)
    *
    * @see javax.xml.rpc.AsyncHandler#handleResponse(javax.xml.rpc.Response)
    */
    public void handleResponse(Response<Hello2Response> response) {
        try {
            Hello2Response resp = response.get();
            HelloType out = resp.getParam();
            int age = resp.getAge();
            assertEquals("foo", out.getArgument());
            assertEquals("bar", out.getExtra());
            assertEquals(age, 1234);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
