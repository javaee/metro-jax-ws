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

package fromjava.wsa.custom_action.server;

import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.ws.Action;
import javax.xml.ws.FaultAction;

import javax.xml.ws.soap.Addressing;

/**
 * @author Rama Pulavarthi
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
