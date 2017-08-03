/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package fromjava.custom_server_jaxbcontext.server;

import javax.jws.*;

import javax.xml.namespace.QName;
import javax.xml.bind.JAXBException;
import java.util.ArrayList;
import java.util.List;

import com.sun.xml.ws.developer.JAXBContextFactory;
import com.sun.xml.ws.developer.UsesJAXBContext;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.xml.bind.api.TypeReference;


/**
 * JAX-WS RI runtime uses the custom JAXBRIContext provided by the
 * applicaiton. This endpoint plugs-in a custom JAXBRIContext to
 * achieve type substitution
 *
 * @author Jitendra Kotamraju
 */
// DO NOT put @XmlSeeAlso({Totyota.class})
@WebService
@UsesJAXBContext(value= EchoImpl.ServerJAXBContextFactory.class)
public class EchoImpl {
    
    public Car echo(Car car) {
        return car;
    }

    public static class ServerJAXBContextFactory implements JAXBContextFactory {

        public JAXBRIContext createJAXBContext(SEIModel sei,
            List<Class> classesToBind, List<TypeReference> typeReferences)
                throws JAXBException {
            System.out.println("Using server's custom JAXBContext");

            // Adding Toyota.class to our JAXBRIContext
            List<Class> classList = new ArrayList<Class>();
            classList.addAll(classesToBind);
            classList.add(Toyota.class);

            List<TypeReference> refList = new ArrayList<TypeReference>();
            refList.addAll(typeReferences);
            refList.add(new TypeReference(new QName("","arg0"),Toyota.class));

            return JAXBRIContext.newInstance(classList.toArray
                    (new Class[classList.size()]),
                    refList, null, sei.getTargetNamespace(), false, null);
        }
    }

}
