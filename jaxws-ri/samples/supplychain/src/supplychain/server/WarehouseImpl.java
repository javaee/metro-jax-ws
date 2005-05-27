/*
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved. 
 */
package supplychain.server;

import java.util.List;
import javax.jws.WebService;

@WebService
public class WarehouseImpl {
	
    /**
     * @param PurchaseOrder
     * @throws InvalidPOException
     *             if the item list in the PurchaseOrder is null
     */
    public ShipmentNotice submitPO(PurchaseOrder po) throws InvalidPOException {
	if (po.getItemList() == null) {
            throw new InvalidPOException("Invalid Item List");
	}

        // warehouse processes the PurchaseOrder
        // and prepares a list of items that are shipped back
                
        ShipmentNotice shipmentNotice = new ShipmentNotice();
        shipmentNotice.setOrderNumber(po.getOrderNumber());
        shipmentNotice.setCustomerNumber(po.getCustomerNumber());
        shipmentNotice.setShipmentNumber("shipment#");
        List<Item> itemList = shipmentNotice.getItemList();
//        for (int i=0; i< po.getItemList().length; i++) {
//            itemList[i] = po.getItemList()[i];
//            System.out.printf("Getting %d item: %d, %f, %d\n", i, po.getItemList()[i].getItemID(), po.getItemList()[i].getPrice(), po.getItemList()[i].getQuantity());
//            System.out.printf("Setting %d item: %d, %f, %d\n", i, itemList[i].getItemID(), itemList[i].getPrice(), itemList[i].getQuantity());
//        }
//        shipmentNotice.setItemList(itemList);
        
        for (Item item : po.getItemList()) {
            itemList.add(item);
        }
        
	return shipmentNotice;
    }
}


