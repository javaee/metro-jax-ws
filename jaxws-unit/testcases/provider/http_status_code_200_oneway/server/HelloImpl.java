package provider.http_status_code_200_oneway.server;

import javax.activation.DataSource;
import javax.annotation.Resource;
import javax.xml.ws.*;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.handler.MessageContext;
import javax.xml.transform.Source;

/**
 * Issue WSIT:1354
 *
 * Oneway returns 200 status code
 *
 * @author Jitendra Kotamraju
 */
@WebServiceProvider
@ServiceMode(value=Service.Mode.MESSAGE)
public class HelloImpl implements Provider<Source> {

    @Resource
    WebServiceContext wsCtxt;

    public Source invoke(Source msg) {
        MessageContext msgCtxt = wsCtxt.getMessageContext();        
        msgCtxt.put(MessageContext.HTTP_RESPONSE_CODE, 200);
        return null;
    }
}
