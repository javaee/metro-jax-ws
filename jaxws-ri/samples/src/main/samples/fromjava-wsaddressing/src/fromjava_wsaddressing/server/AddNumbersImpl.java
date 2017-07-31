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

package fromjava_wsaddressing.server;

import javax.jws.WebService;
import javax.xml.ws.*;
import javax.xml.ws.soap.Addressing;

@Addressing
@WebService
public class AddNumbersImpl {

    @Action(
            input = "http://example.com/input",
            output = "http://example.com/output")
    public int addNumbers(int number1, int number2) throws AddNumbersException {
        return impl(number1, number2);
    }

    public int addNumbers2(int number1, int number2) throws AddNumbersException {
        return impl(number1, number2);
    }

    @Action(
            input = "http://example.com/input3",
            output = "http://example.com/output3",
            fault = {
            @FaultAction(className = AddNumbersException.class, value = "http://example.com/fault3")
                    }
    )
    public int addNumbers3(int number1, int number2) throws AddNumbersException {
        return impl(number1, number2);
    }

    int impl(int number1, int number2) throws AddNumbersException {
        if (number1 < 0 || number2 < 0) {
            throw new AddNumbersException("Negative numbers can't be added!",
                                          "Numbers: " + number1 + ", " + number2);
        }
        return number1 + number2;
    }
}

