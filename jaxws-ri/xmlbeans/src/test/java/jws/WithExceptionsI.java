package jws;


import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.soap.SOAPBinding;

import com.bea.wli.sb.transports.ejb.test.ejb3.CheckedException;

@WebService(targetNamespace="http://www.openuri.org/")
@SOAPBinding(style=SOAPBinding.Style.DOCUMENT, use=SOAPBinding.Use.LITERAL, parameterStyle=SOAPBinding.ParameterStyle.WRAPPED)
public interface WithExceptionsI {
    @WebMethod(action="http://www.openuri.org/setTxForRollbackOnly")
    public void setTxForRollbackOnly(final boolean arg0) throws Exception;

    @WebMethod(action="http://www.openuri.org/throwCheckedException")
    public void throwCheckedException() throws Exception, CheckedException;

    @WebMethod(action="http://www.openuri.org/throwRemote")
    public void throwRemote() throws Exception;

    @WebMethod(action="http://www.openuri.org/throwRuntimeException")
    public void throwRuntimeException() throws Exception;

    @WebMethod(action="http://www.openuri.org/txRolledBackWithCheckedException")
    public void txRolledBackWithCheckedException() throws Exception, CheckedException;
}
