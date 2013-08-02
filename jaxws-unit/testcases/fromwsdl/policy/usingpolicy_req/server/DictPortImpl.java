package fromwsdl.policy.usingpolicy_req.server;

import javax.jws.WebService;


@WebService(endpointInterface = "fromwsdl.policy.usingpolicy_req.server.DictPortType")
public class DictPortImpl {
    public String translateOperation(String translateIt) throws DictFault {
        return null;
    }                                                        

}