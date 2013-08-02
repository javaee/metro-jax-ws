package bugs.jaxws236.server;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.ws.WebServiceException;


@WebService
public class TestEndpoint
{
    @WebMethod()
    public String[] testStringArray( String[] s )
    {
        if(!s[0].equals("one") || !s[1].equals("") || (s[2] != null))
            throw new WebServiceException("One of the value was incorrect!");
        return new String[] {"two", "", null};
    }
}
