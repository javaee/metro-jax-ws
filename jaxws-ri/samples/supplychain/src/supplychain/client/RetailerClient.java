/*
* Copyright (c) 2005 Sun Microsystems, Inc.
* All rights reserved. 
*/
package supplychain.client;

import java.util.List;

import javax.xml.ws.ServiceFactory;

import java.rmi.RemoteException;

public class RetailerClient {
    public static void main(String[] args) {
        try {
            ServiceFactory serviceFactory = ServiceFactory.newInstance();
            WarehouseImplService service = (WarehouseImplService)serviceFactory.createService((java.net.URL)null, WarehouseImplService.class);
            WarehouseImpl port = service.getWarehouseImplPort();
			
            int number1 = 10;
            int number2 = 20;

            PurchaseOrder po = new PurchaseOrder();
            po.setCustomerNumber("cust#");
            po.setOrderNumber("order#");
            List<Item> itemList = po.getItemList();
            
            Item item = new Item();
            item.setItemID(1);
            item.setPrice((float)100.00);
            item.setQuantity(10);
            itemList.add(item);
            
            Item item2 = new Item();
            item2.setItemID(2);
            item2.setPrice((float)200.00);
            item2.setQuantity(15);
            itemList.add(item2);

            System.out.printf("Invoking submitPO\n");
            ShipmentNotice sn = port.submitPO(po);
            System.out.printf("Got: %s, %s, %s\n", sn.getCustomerNumber(), sn.getOrderNumber(), sn.getShipmentNumber());
            System.out.printf("Item list\n");
            for (Item responseItem : sn.getItemList()) {
                System.out.printf("\t %d, %f, %d\n", responseItem.getItemID(), responseItem.getPrice(), responseItem.getQuantity());
            }
	} catch (RemoteException ex) {
            ex.printStackTrace();
        } catch (InvalidPOException_Exception ex) {
            System.out.printf("Caught InvalidPOException_Exception: %s\n", ex.getFaultInfo().getDetail());
	}
    }
}
