package supplychain.server;

import java.util.List;
import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;

public class ShipmentNotice {
    String shipmentNumber;
    String orderNumber;
    String customerNumber;
    @XmlElement (nillable = true)
    List<Item> itemList;
    
    public String getShipmentNumber () { return shipmentNumber; }
    public void setShipmentNumber (String shipmentNumber) { this.shipmentNumber = shipmentNumber; }
    
    public String getOrderNumber () { return orderNumber; }
    public void setOrderNumber (String orderNumber) { this.orderNumber = orderNumber; }
    
    public String getCustomerNumber () { return customerNumber; }
    public void setCustomerNumber (String customerNumber) { this.customerNumber = customerNumber; }
    
    public List<Item> getItemList () {
        if (itemList == null)
            itemList = new ArrayList<Item>();
        
        return itemList;
    }
}
