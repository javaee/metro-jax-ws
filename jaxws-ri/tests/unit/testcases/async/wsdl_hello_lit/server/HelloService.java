/*
 * $Id: HelloService.java,v 1.1 2007-08-10 17:23:00 kohsuke Exp $
 * 
 * Copyright 2004 Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package async.wsdl_hello_lit.server;

import javax.jws.WebService;
import javax.xml.ws.Holder;

@WebService(endpointInterface="async.wsdl_hello_lit.server.Hello")
public class HelloService implements Hello {
    public HelloOutput hello(Hello_Type req){
        System.out.println("Hello_PortType_Impl received: " + req.getArgument() +
            ", " + req.getExtra());
        HelloOutput resp = new HelloOutput();
        resp.setArgument(req.getArgument());
        resp.setExtra(req.getExtra());
        return resp;
    }
    
    public void hello1(HelloType req, HelloType inHeader, 
            Holder<HelloOutput> resp, Holder<HelloType> outHeader){
        HelloOutput out = new HelloOutput();
        out.setArgument(req.getArgument());
        out.setExtra(req.getExtra());
        resp.value = out; 
        outHeader.value = inHeader;        
    }

    public int hello0(int param_in){
        return param_in;
    }

}
