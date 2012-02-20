package async.wsdl_rpclit.client;

import junit.framework.TestCase;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;
import java.util.concurrent.ExecutionException;

public class Hello1CallbackHandler extends TestCase implements AsyncHandler<Hello1Response> {

    /*
    * (non-Javadoc)
    *
    * @see javax.xml.rpc.AsyncHandler#handleResponse(javax.xml.rpc.Response)
    */
    public void handleResponse(Response<Hello1Response> response) {
        try {
            Hello1Response resp = response.get();
            HelloType out = resp.getRes();
            HelloType outH = resp.getHeader();
            assertEquals("foo", out.getArgument());
            assertEquals("bar", out.getExtra());
            assertEquals("header arg", outH.getArgument());
            assertEquals("header extra", outH.getExtra());
//            assertEquals("Hello World!", resp.getExtraHeader());
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
