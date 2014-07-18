package jws;

import java.rmi.RemoteException;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.soap.SOAPBinding;

import com.bea.wli.sb.transports.ejb.test.ejb3.CheckedException;

@WebService(targetNamespace="http://www.openuri.org/")
@SOAPBinding(style=SOAPBinding.Style.DOCUMENT, use=SOAPBinding.Use.LITERAL, parameterStyle=SOAPBinding.ParameterStyle.WRAPPED)
public class WithExceptions {
    @WebMethod(action="http://www.openuri.org/setTxForRollbackOnly")
    public void setTxForRollbackOnly(final boolean arg0) throws Exception
    {
      throw new IllegalStateException("database not connected");
    }

    @WebMethod(action="http://www.openuri.org/throwCheckedException")
    public void throwCheckedException() throws Exception, CheckedException
    {
      throw new CheckedException();
      //throw new Exception("something failed");
    }

    @WebMethod(action="http://www.openuri.org/throwRemote")
    public void throwRemote() throws Exception
    {
        throw new RemoteException("classloader error");
    }

    @WebMethod(action="http://www.openuri.org/throwRuntimeException")
    public void throwRuntimeException() throws Exception
    {
        throw new RuntimeException("system died");
    }

    @WebMethod(action="http://www.openuri.org/txRolledBackWithCheckedException")
    public void txRolledBackWithCheckedException() throws Exception, CheckedException {
       throw new CheckedException();
    }
}
