package jws;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebResult;
import javax.jws.soap.SOAPBinding;


@WebService(targetNamespace="http://www.openuri.org/")
@SOAPBinding(style=SOAPBinding.Style.DOCUMENT, use=SOAPBinding.Use.LITERAL, parameterStyle=SOAPBinding.ParameterStyle.WRAPPED)
public class Arrays {


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
    

    @WebMethod(action="http://www.openuri.org/arrayTest")
    @WebResult(name="cus-return")
    public java.lang.String[] arrayTest(final java.lang.String[] arg0, 
            int[] intA, @WebParam(name="integer-array")Integer[] integerA, 
            double[] d1, Double[] d2,
            float[] fA,   Float[] flA,
            long[] l1, Long[] l2,
            short[] s1, Short[] s2,            
            boolean[] ba, Boolean[] bb,
            byte[] b, Byte[] b2, byte[][] b3,
//            com.bea.xml.XmlObject[] beaXBeans, 
            javax.xml.namespace.QName[] qn,
            java.math.BigInteger[] bi,
            java.math.BigDecimal[] bd,
            java.util.Calendar[] cal,
            org.apache.xmlbeans.XmlObject[] apacheXBeans, org.apache.xmlbeans.XmlObject xb,
//            com.bea.wli.sb.transports.ejb.test.xbean.MyAddress address, com.bea.wli.sb.transports.ejb.test.xbean.MyAddress[] aa,
            com.bea.wli.sb.transports.ejb.test.xbean.CountriesDocument[] countries, com.bea.wli.sb.transports.ejb.test.xbean.CountryInfoType[] info) throws Exception
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