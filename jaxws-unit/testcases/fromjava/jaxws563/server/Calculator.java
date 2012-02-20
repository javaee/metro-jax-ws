package fromjava.jaxws563.server;

import fromjava.jaxws563.server.types.Query;
import fromjava.jaxws563.server.types.Result;
import java.math.BigInteger;
import javax.jws.WebService;


@WebService
public class Calculator {

    public Result add(Query query) {
        BigInteger sum = query.getFoo().add(query.getBar());
        Result result = new Result();

        result.setSum(sum);
        return result;
    }
    
}
