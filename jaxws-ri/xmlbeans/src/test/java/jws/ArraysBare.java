package jws;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebResult;
import javax.jws.soap.SOAPBinding;

@WebService(targetNamespace="http://www.openuri.org/")
@SOAPBinding(style=SOAPBinding.Style.DOCUMENT, use=SOAPBinding.Use.LITERAL, parameterStyle=SOAPBinding.ParameterStyle.BARE)
public class ArraysBare {

    @WebMethod(action="http://www.openuri.org/sort1")
    @WebResult(name="return")
    public java.lang.String[] sort1(final java.lang.String[] arg0) throws Exception
    {
      if (arg0 == null)
        return null;
      List<String> ls = new ArrayList<String>();
      for (String s : arg0)
        ls.add(s);
      Collections.sort(ls);
        return ls.toArray(new String[arg0.length]);
    }
}