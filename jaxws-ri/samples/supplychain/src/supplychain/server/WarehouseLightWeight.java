/*
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */
package supplychain.server;

import javax.xml.ws.Endpoint;

public class WarehouseLightWeight {
    
    public static void main (String[] args) throws Exception {
        Endpoint.publish(
            "http://localhost:8080/jaxws-supplychain/submitpo",
            new WarehouseImpl ());
    }
}
