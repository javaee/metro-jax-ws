/*
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved. 
 */
 
package supplychain.server;

import javax.xml.bind.annotation.XmlElement;

import java.util.List;
import java.util.ArrayList;

public class PurchaseOrder {
    String orderNumber;
    String customerNumber;
    @XmlElement(nillable = true)
    List<Item> itemList;
    
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    
    public String getCustomerNumber() { return customerNumber; }
    public void setCustomerNumber(String customerNumber) { this.customerNumber = customerNumber; }
    
    public List<Item> getItemList() {
        if (itemList == null)
            itemList = new ArrayList<Item>();
            
        return itemList;
    }
}