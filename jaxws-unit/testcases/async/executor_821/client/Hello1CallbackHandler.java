package async.executor_821.client;

import junit.framework.TestCase;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.*;

/*
 * @author Jitendra Kotamraju
 */
public class Hello1CallbackHandler extends TestCase implements AsyncHandler<Hello1Response> {
    final Exchanger<Hello1Response> exchanger;

    Hello1CallbackHandler(Exchanger<Hello1Response> exchanger) {
        this.exchanger = exchanger;
    }
    public void handleResponse(Response<Hello1Response> response) {
        Hello1Response resp = null;
        try {
            resp = response.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            exchanger.exchange(resp);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }
}
