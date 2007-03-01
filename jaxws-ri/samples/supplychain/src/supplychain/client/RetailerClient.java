package supplychain.client;

import java.util.List;

public class RetailerClient {
    public static void main (String[] args) {
        try {
            WarehouseImplService service = new  WarehouseImplService();
            WarehouseImpl port = service.getWarehouseImplPort();
            
            int number1 = 10;
            int number2 = 20;
            
            PurchaseOrder po = new PurchaseOrder ();
            po.setCustomerNumber ("Duke");
            po.setOrderNumber ("1001");
            List<Item> itemList = po.getItemList ();
            
            Item item = new Item ();
            item.setName ("SunFire V40Z");
            item.setItemID (1);
            item.setPrice ((float)6995.50);
            item.setQuantity (10);
            itemList.add (item);
            
            Item item2 = new Item ();
            item2.setName ("Solaris 10 Support Plan");
            item2.setItemID (2);
            item2.setPrice ((float)120.50);
            item2.setQuantity (15);
            itemList.add (item2);
            
            System.out.printf ("Invoking submitPO\n");
            ShipmentNotice sn = port.submitPO (po);
            System.out.printf ("Got: %s, %s, %s\n", sn.getCustomerNumber (), sn.getOrderNumber (), sn.getShipmentNumber ());
            System.out.printf ("Item list\n");
            for (Item responseItem : sn.getItemList ()) {
                System.out.printf ("\t %s, %d, %f, %d\n", responseItem.getName (), responseItem.getItemID (), responseItem.getPrice (), responseItem.getQuantity ());
            }
        } catch (InvalidPOException_Exception ex) {
            System.out.printf ("Caught InvalidPOException_Exception: %s\n", ex.getFaultInfo ().getDetail ());
        }
    }
}
