/*
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved. 
 */

package supplychain.server;

import java.util.List;

public class ShipmentNotice {
    String shipmentNumber;
    String orderNumber;
    String customerNumber;
    Item[] itemList;
    
    public String getShipmentNumber() { return shipmentNumber; }
    public void setShipmentNumber(String shipmentNumber) { this.shipmentNumber = shipmentNumber; }
    
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    
    public String getCustomerNumber() { return customerNumber; }
    public void setCustomerNumber(String customerNumber) { this.customerNumber = customerNumber; }
    
    public Item[] getItemList() { return itemList; }
    public void setItemList(Item[] itemList) { this.itemList = itemList; }
}
