/*
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */
package mtom_soap12.server;

import javax.jws.WebService;
import javax.xml.ws.Holder;
import javax.activation.DataHandler;
import java.rmi.RemoteException;
import java.awt.*;

@WebService(endpointInterface = "mtom_soap12.server.Hello")

public class HelloImpl implements Hello {
    public void detail(Holder<byte[]> photo, Holder<Image> image) throws RemoteException {
    }

    public DataHandler claimForm(DataHandler data){
        return data;
    }

    public void echoData(Holder<byte[]> data){

    }
}
