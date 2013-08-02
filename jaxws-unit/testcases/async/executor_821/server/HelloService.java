package async.executor_821.server;

import javax.jws.WebService;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceException;

/**
 * @author Jitendra Kotamraju
 */
@WebService(endpointInterface="async.executor_821.server.Hello")
public class HelloService implements Hello {

    public HelloOutput hello(Hello_Type req){
        // Intentionally adding the sleep so that we can test client
        try {
            Thread.sleep(10000);
        } catch(Exception e) {
            throw new WebServiceException(e);
        }
        HelloOutput resp = new HelloOutput();
        resp.setArgument(req.getArgument());
        resp.setExtra(req.getExtra());
        return resp;
    }
    
    public void hello1(HelloType req, HelloType inHeader, 
            Holder<HelloOutput> resp, Holder<HelloType> outHeader){
        // Intentionally adding the sleep so that we can test client
        try {
            Thread.sleep(10000);
        } catch(Exception e) {
            throw new WebServiceException(e);
        }
        HelloOutput out = new HelloOutput();
        out.setArgument(req.getArgument());
        out.setExtra(req.getExtra());
        resp.value = out; 
        outHeader.value = inHeader;        
    }

    public int hello0(int param_in) {
        // Intentionally adding the sleep so that we can test client
        try {
            Thread.sleep(10000);
        } catch(Exception e) {
            throw new WebServiceException(e);
        }
        return param_in;
    }

}
