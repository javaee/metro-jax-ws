/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2013 Oracle and/or its affiliates. All rights reserved.
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

package fromjava.wsa.action.server;

import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.ws.Action;
import javax.xml.ws.FaultAction;
import javax.xml.ws.soap.Addressing;

/**
 * @author Arun Gupta
 */
@HandlerChain(
    name="",
    file="handlers.xml"
)
@Addressing
@WebService(name="AddNumbers",
    portName="AddNumbersPort",
    targetNamespace="http://foobar.org/",
    serviceName="AddNumbersService")
public class AddNumbersImpl {

    public int addNumbersNoAction(int number1, int number2) throws AddNumbersException {
        return doStuff(number1, number2);
    }

    @Action(
        input="",
        output="")
    public int addNumbersEmptyAction(int number1, int number2) throws AddNumbersException {
        return doStuff(number1, number2);
    }

    @Action(
        input="http://example.com/input",
        output="http://example.com/output")
    public int addNumbers(int number1, int number2) throws AddNumbersException {
        return doStuff(number1, number2);
    }

    @Action(
        input="http://example.com/input2",
        output="http://example.com/output2")
    public int addNumbers2(int number1, int number2) throws AddNumbersException {
        return doStuff(number1, number2);
    }

    @Action(input="http://example.com/input3")
    public int addNumbers3(int number1, int number2) throws AddNumbersException {
        return doStuff(number1, number2);
    }

    @Action(input="finput1",
            output="foutput1",
            fault={
                @FaultAction(className=AddNumbersException.class, value="http://fault1")
            })
    public int addNumbersFault1(int number1, int number2) throws AddNumbersException {
        return doStuff(number1, number2);
    }

    @Action(input="finput2",
            output="foutput2",
            fault={
                @FaultAction(className=AddNumbersException.class, value="http://fault2/addnumbers"),
                @FaultAction(className=TooBigNumbersException.class, value="http://fault2/toobignumbers")
            })
    public int addNumbersFault2(int number1, int number2) throws AddNumbersException, TooBigNumbersException {
        throwTooBigException(number1, number2);

        return doStuff(number1, number2);
    }

    @Action(input="finput3",
            output="foutput3",
            fault={
                @FaultAction(className=AddNumbersException.class, value="http://fault3/addnumbers")
            })
    public int addNumbersFault3(int number1, int number2) throws AddNumbersException, TooBigNumbersException {
        throwTooBigException(number1, number2);

        return doStuff(number1, number2);
    }

    @Action(
        fault={
            @FaultAction(className=AddNumbersException.class, value="http://fault4/addnumbers")
        })
    public int addNumbersFault4(int number1, int number2) throws AddNumbersException, TooBigNumbersException {
        throwTooBigException(number1, number2);

        return doStuff(number1, number2);
    }

    @Action(
        fault={
            @FaultAction(className=TooBigNumbersException.class, value="http://fault5/toobignumbers")
        })
    public int addNumbersFault5(int number1, int number2) throws AddNumbersException, TooBigNumbersException {
        throwTooBigException(number1, number2);

        return doStuff(number1, number2);
    }

    @Action(
        fault={
            @FaultAction(className=AddNumbersException.class, value="http://fault6/addnumbers"),
            @FaultAction(className=TooBigNumbersException.class, value="http://fault6/toobignumbers")
        })
    public int addNumbersFault6(int number1, int number2) throws AddNumbersException, TooBigNumbersException {
        throwTooBigException(number1, number2);

        return doStuff(number1, number2);
    }

    @Action(
        fault={
            @FaultAction(className=AddNumbersException.class, value=""),
            @FaultAction(className=TooBigNumbersException.class, value="")
        })
    public int addNumbersFault7(int number1, int number2) throws AddNumbersException, TooBigNumbersException {
        throwTooBigException(number1, number2);

        return doStuff(number1, number2);
    }

    int doStuff(int number1, int number2) throws AddNumbersException {
        if (number1 < 0 || number2 < 0) {
            throw new AddNumbersException("Negative numbers can't be added!",
                                          "Numbers: " + number1 + ", " + number2);
                }
        return number1 + number2;
    }

    void throwTooBigException(int number1, int number2) throws TooBigNumbersException {
        if (number1 > 10 || number2 > 10)
            throw new TooBigNumbersException("Too bug numbers can't be added!",
                                             "Numbers: " + number1 + ", " + number2);
    }

}
