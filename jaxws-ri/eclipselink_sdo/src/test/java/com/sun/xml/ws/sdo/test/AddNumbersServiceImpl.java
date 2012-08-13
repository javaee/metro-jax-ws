package com.sun.xml.ws.sdo.test;

import javax.jws.WebService;

@WebService(endpointInterface = "com.sun.xml.ws.sdo.test.AddNumbersPortType")

public class AddNumbersServiceImpl implements AddNumbersPortType {


    public AddNumbersServiceImpl() {
    }

    public int addNumbers(int arg0, int arg1) {
        return arg1 + arg0;
    }
}