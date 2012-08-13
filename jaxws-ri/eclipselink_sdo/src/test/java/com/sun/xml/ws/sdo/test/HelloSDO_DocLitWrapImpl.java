package com.sun.xml.ws.sdo.test;

import java.util.List;
import java.util.ArrayList;

import javax.jws.WebService;

import com.sun.xml.ws.sdo.test.helloSDO.MySDO;

@WebService(targetNamespace="http://oracle.j2ee.ws.jaxws.test/")
public class HelloSDO_DocLitWrapImpl {

    public MySDO echoSDO(MySDO coo) {
        coo.setIntPart(coo.getIntPart() + 1);
        coo.setStringPart("Gary");
        return coo;
    }   

    public MySDO partI(MySDO coo, int i) {
        coo.setIntPart(i);
        return coo;
    }
    public String returnS(MySDO coo) { return coo.getStringPart(); }
    
    public int returnI(MySDO coo) { return coo.getIntPart(); }    
    

    public List<String> testList1(List<MySDO> l) {
        ArrayList<String> res = new ArrayList<String>();
        for (MySDO m : l) res.add(m.getStringPart());
        return res;
    }

    public List<String> testList2(List<String> l1, List<MySDO> l2) {
        ArrayList<String> res = new ArrayList<String>();
        for (String s : l1) res.add(s);
        for (MySDO m : l2) res.add(m.getStringPart());
        return res;
    }

    public String[] arrayList01(MySDO[] l) {
        String[] s = new String[l.length];
        for (int i = 0; i < l.length; i++ ) s[i] = l[i].getStringPart();
        return s;
    }
}
