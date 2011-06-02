package com.sun.xml.ws.sdo.test;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import com.sun.xml.ws.sdo.test.helloSDO.MySDO;

@WebService(targetNamespace="http://oracle.j2ee.ws.jaxws.test/")

public class HelloSDO_ProxyInterfaceImpl implements HelloSDO_ProxyInterface {

    @SOAPBinding(parameterStyle= SOAPBinding.ParameterStyle.BARE)
    public MySDO echoSDO(MySDO coo) {
        coo.setIntPart(coo.getIntPart() + 1);
        coo.setStringPart("Gary");
        return coo;

    }
}
