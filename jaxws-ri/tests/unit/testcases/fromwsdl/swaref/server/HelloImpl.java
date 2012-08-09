package fromwsdl.swaref.server;

import javax.jws.WebService;

@WebService(endpointInterface = "fromwsdl.swaref.server.Hello")
public class HelloImpl {
    public ClaimFormTypeResponse claimForm(ClaimFormTypeRequest data){
        ClaimFormTypeResponse resp = new ClaimFormTypeResponse();
        resp.setResponse(data.getRequest());
        return resp;
    }
}
