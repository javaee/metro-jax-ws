/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

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
