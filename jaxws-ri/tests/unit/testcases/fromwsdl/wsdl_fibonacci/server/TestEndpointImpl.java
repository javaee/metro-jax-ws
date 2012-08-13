/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Oracle and/or its affiliates. All rights reserved.
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
package fromwsdl.wsdl_fibonacci.server;

import java.util.List;
import javax.jws.WebService;
import javax.xml.ws.BindingProvider;

/**
 * calls itself recursively using its own proxy
 *
 * @author Jitendra Kotamraju
 */
@WebService(endpointInterface = "fromwsdl.wsdl_fibonacci.server.Fib")

public class TestEndpointImpl implements Fib {
    private volatile Fib proxy;
    private volatile int oneway = -100;

    public int getFib(String address, int num) {
        return get(address, num);
    }

    public void getFibOneway(String address, int num) {
        if (num == 0) {		// If it reaches 0, most likely it works
            oneway = num;
        }
        getOneway(address, num);
    }

    public int getFibVerifyOneway() {
        return oneway;		
    }

    private int get(String address, int num) {
        setUpProxy(address);
        System.out.println("num="+num);
    	if (num <= 1) {
            return 1;
        } else {
            return proxy.getFib(address, num-1)+proxy.getFib(address, num-2);
        }
    }

    private void getOneway(String address, int num) {
        setUpProxy(address);
        System.out.println("oneway num="+num);
    	if (num > 1) {
            proxy.getFibOneway(address, num-1);
            proxy.getFibOneway(address, num-2);
        }
    }

    private void setUpProxy(String address) {
        if (proxy == null ) {
            proxy = new Fib_Service().getFibPort();
            ((BindingProvider)proxy).getRequestContext().put(
                BindingProvider.ENDPOINT_ADDRESS_PROPERTY, address);
        }
    }

}
