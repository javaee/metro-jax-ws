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
 * Copyright 2010 Sun Microsystems Inc. All Rights Reserved
 */

package wsa.w3c.fromjava.addressing_required.server;

import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.ws.Action;
import javax.xml.ws.FaultAction;
import javax.xml.ws.soap.Addressing;
import javax.xml.ws.soap.AddressingFeature;

@Addressing(required=true)
@WebService(name = "AddNumbers",
        portName = "AddNumbersPort",
        targetNamespace = "http://foobar.org/",
        serviceName = "AddNumbersService")
public class AddNumbersImpl {

    public int addNumbers(int number1, int number2) throws AddNumbersException {
        if (number1 < 0 || number2 < 0) {
            throw new AddNumbersException("Negative numbers can't be added!",
                    "Numbers: " + number1 + ", " + number2);
        }
        return number1 + number2;
    }


}
