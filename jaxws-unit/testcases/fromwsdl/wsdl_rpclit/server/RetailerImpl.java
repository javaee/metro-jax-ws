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
package fromwsdl.wsdl_rpclit.server;

import javax.xml.ws.Holder;
import java.math.BigDecimal;

@javax.jws.WebService(endpointInterface="fromwsdl.wsdl_rpclit.server.RetailerPortType")
public class RetailerImpl implements RetailerPortType {
    public CatalogType getCatalog() {
             try{
                 ObjectFactory of = new ObjectFactory();
                 CatalogType ret = of.createCatalogType();
                 CatalogItem ci = of.createCatalogItem();
                 ci.setName("JAXWS RI 2.1.1");
                 ci.setBrand("java.net");
                 ci.setCategory("Web Services");
                 ci.setProductNumber(1234);
                 ci.setDescription("Coolest Webservice Product");
                 ci.setPrice(new BigDecimal("100"));
                 ret.getItem().add(ci);

                 ci = of.createCatalogItem();
                 ci.setName("JAXWS RI 2.0");
                 ci.setBrand("java.net");
                 ci.setCategory("Web Services");
                 ci.setProductNumber(5678);
                 ci.setDescription("Coolest Webservice Product");
                 ci.setPrice(new BigDecimal("200"));
                 ret.getItem().add(ci);
                return ret;
             }catch(Exception e){ e.printStackTrace();}
             return null;
    }

    public void getGCBug(String fn, String ln, String mn,int age,Holder<NameType> nameHolder,
            javax.xml.ws.Holder<PersonalDetailsType> personalHolder,
            javax.xml.ws.Holder<AddressType> addressHolder){
        try{
            ObjectFactory of = new ObjectFactory();
            NameType name = of.createNameType();
            AddressType address = of.createAddressType();
            PersonalDetailsType personal = of.createPersonalDetailsType();
            name.setFn(fn);
            name.setLn(ln);
            address.setStreet(" 1234 network circle");
            address.setZipcode(100001);
            personal.setSsn("123456789");
            personal.setDob("03-18-2005");
            nameHolder.value = name;
            addressHolder.value=address;
            personalHolder.value=personal;

        }catch(Exception e){ e.printStackTrace();}

}

    public CatalogType echoCatalog(CatalogType input)
    {
         return input;
    }

    public CatalogType testCatalog(Holder<String> name, int index)
    {
        name.value += ".net";
        try{
            ObjectFactory of = new ObjectFactory();
            CatalogType ret = of.createCatalogType();
            CatalogItem ci = of.createCatalogItem();
            ci.setName("JAXWS RI 2.1.1");
            ci.setBrand("java.net");
            ci.setCategory("Web Services");
            ci.setProductNumber(index);
            ci.setDescription("Coolest Webservice Product");
            ci.setPrice(new BigDecimal("100"));
            ret.getItem().add(ci);
            return ret;
        }catch(Exception e){e.printStackTrace();}
        return null;
    }

    public void helloOneWay(String parameters){
        if(!parameters.equals("JAXWS RI 2.1.1"))
             System.out.println("HelloOneWay FAILED: received \""+parameters+"\", expected \"JAXRPC 2.0\"");
    }

     public double testHolders(String name,Holder<Integer> inout){
            inout.value = 2;
            return 1.0;
     }

     public ShortArrayResponseType shortArrayTest(ShortArrayType parameters){
        try{
        ShortArrayResponseType resp = new ObjectFactory().createShortArrayResponseType();
        resp.getShortArray().addAll(parameters.getShortArray());
        return resp;
        }catch(Exception e){e.printStackTrace();}
        return null;
     }

     public int testParameterOrder(int bar, String foo, Holder<String> foo1){
         foo1.value = foo;
         return bar;
     }

     public int testUnboundedParts(String foo, String foo1, Holder<Integer> bar, Holder<Integer> bar1) throws UnboundedPartsException{
         if(foo1 != null)
             throw new UnboundedPartsException("foo1 MUST be null!", "foo1 MUST be null!");
         bar.value = Integer.valueOf(foo);
         bar1.value = bar.value;
         return bar.value;
     }
}
