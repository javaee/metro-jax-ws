/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package server.endpoint.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Endpoint;

import testutil.PortAllocator;



import junit.framework.TestCase;

public class EmptyNamespaceTest extends TestCase {
	   public void testEmptyNamespace() throws Exception {
	        int port = PortAllocator.getFreePort();
	        String address = "http://localhost:" + port + "/hello";
	        Endpoint endpoint = Endpoint.create(new GetTotalPrice());
	        List<Source> metadata = new ArrayList<Source>();
	        ClassLoader cl = Thread.currentThread().getContextClassLoader();
	      
	        endpoint.publish(address);
	        URL pubUrl = new URL(address + "?wsdl");
            boolean gen = isGenerated(pubUrl.openStream());
            assertTrue(gen);
          
          
	        URL xsdUrl = new URL(address + "?xsd=2");
	        String contents = getContent(xsdUrl.openStream());
	        
	        assertTrue(contents, contents.indexOf(":complexType name=\"shoppingCart\"" )>-1);
     
	        	
	        endpoint.stop();
	     
	    }	
	    // read the whole document. Otherwise, endpoint.stop() would be
	    // done by this test and the server runtime throws an exception
	    // when it tries to send the rest of the doc
	    public boolean isGenerated(InputStream in) throws IOException {
	        BufferedReader rdr = new BufferedReader(new InputStreamReader(in));
	        boolean generated = true;
	        try {
	            String str;
	            while ((str = rdr.readLine()) != null) {
	                if (str.indexOf("NOT_GENERATED") != -1) {
	                    generated = false;
	                }
	            }
	        } finally {
	            rdr.close();
	        }
	        return generated;
	    }	   
	    
	    public String getContent(InputStream in) throws IOException {
	        BufferedReader rdr = new BufferedReader(new InputStreamReader(in));
	        StringBuffer buffer = new StringBuffer();
	        try {
	            String str;
	            while ((str = rdr.readLine()) != null) {
	            	buffer.append(str);
	               
	            }
	        } finally {
	            rdr.close();
	        }
	        return buffer.toString();
	    }	   
	 
	   @WebService (serviceName="GetPriceRef_WS_Name", portName="GetPriceRef_WS_Port", targetNamespace="http://jsca.oracle.com")
	   public class GetTotalPrice {

	   	@WebMethod
	   	public double getTotalPrice(ShoppingCart sCart) {
	   		double retPrice = 0.0;
	   		List<ShoppingCartItem> items = sCart.getItems();
	   		for ( int i=0; i<items.size(); i++ ) {
	   			ShoppingCartItem item = items.get(i);
	   			retPrice += item.getPrice() * item.getQuantity();
	   		}
	   		return retPrice;
	   	}

	   	
	   }
	   @XmlAccessorType(XmlAccessType.FIELD)
	   @XmlType(name = "shoppingCart", namespace = "", propOrder = {
	       "items"
	   })
	   static public class ShoppingCart implements Serializable {

	   	private static final long serialVersionUID = 1;

	       protected List<ShoppingCartItem> items;

	       /**
	        * Gets the value of the items property.
	        *
	        * <p>
	        * This accessor method returns a reference to the live list,
	        * not a snapshot. Therefore any modification you make to the
	        * returned list will be present inside the JAXB object.
	        * This is why there is not a <CODE>set</CODE> method for the items property.
	        *
	        * <p>
	        * For example, to add a new item, do as follows:
	        * <pre>
	        *    getItems().add(newItem);
	        * </pre>
	        *
	        *
	        * <p>
	        * Objects of the following type(s) are allowed in the list
	        * {@link ShoppingCartItem }
	        *
	        *
	        */
	       public List<ShoppingCartItem> getItems() {
	           if (items == null) {
	               items = new ArrayList<ShoppingCartItem>();
	           }
	           return this.items;
	       }

	   }
	   @XmlAccessorType(XmlAccessType.FIELD)
	   @XmlType(name = "shoppingCartItem", namespace = "", propOrder = {
	       "id",
	       "price",
	       "quantity"
	   })
	   static public class ShoppingCartItem implements Serializable {

	   	private static final long serialVersionUID = 1;

	       protected int id;
	       protected double price;
	       protected int quantity;

	       /**
	        * Gets the value of the id property.
	        *
	        */
	       public int getId() {
	           return id;
	       }

	       /**
	        * Sets the value of the id property.
	        *
	        */
	       public void setId(int value) {
	           this.id = value;
	       }

	       /**
	        * Gets the value of the price property.
	        *
	        */
	       public double getPrice() {
	           return price;
	       }

	       /**
	        * Sets the value of the price property.
	        *
	        */
	       public void setPrice(double value) {
	           this.price = value;
	       }

	       /**
	        * Gets the value of the quantity property.
	        *
	        */
	       public int getQuantity() {
	           return quantity;
	       }

	       /**
	        * Sets the value of the quantity property.
	        *
	        */
	       public void setQuantity(int value) {
	           this.quantity = value;
	       }
	   }
}
