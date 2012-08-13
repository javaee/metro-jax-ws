package com.sun.xml.ws.test;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.ws.Holder;

@WebService
public class WebParamHolderSEB {
    public String withoutWebParam(Holder<String> arg0) {return null;}
    public String withWebParam(@WebParam(name="arg0") Holder<String> arg0)  {return null;}
}
