/*
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved. 
 */
package supplychain.server;
 
public class Item {
    String name;
    int itemID;
    int quantity;
    float price;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public int getItemID() { return itemID; }
    public void setItemID(int itemID) { this.itemID = itemID; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public float getPrice() { return price; }
    public void setPrice(float price) { this.price = price; }
}
