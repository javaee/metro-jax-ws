/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */

/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
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
    public ShipmentNotice submitPO (PurchaseOrder po) throws InvalidPOException {
        if (po.getItemList () == null) {
            throw new InvalidPOException ("Invalid Item List");
        }
        
        // warehouse processes the PurchaseOrder
        // and prepares a list of items that are shipped back
        
        ShipmentNotice shipmentNotice = new ShipmentNotice ();
        shipmentNotice.setOrderNumber (po.getOrderNumber ());
        shipmentNotice.setCustomerNumber (po.getCustomerNumber ());
        shipmentNotice.setShipmentNumber ("ABC-12345-XYZ");
        List<Item> itemList = shipmentNotice.getItemList ();
        
        for (Item item : po.getItemList ()) {
            itemList.add (item);
        }
        
        return shipmentNotice;
    }
}


