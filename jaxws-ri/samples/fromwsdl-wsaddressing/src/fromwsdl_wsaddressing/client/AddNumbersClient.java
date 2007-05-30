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

package fromwsdl_wsaddressing.client;

public class AddNumbersClient {


    int number1 = 10;
    int number2 = 10;
    int negativeNumber1 = -10;

    public static void main(String[] args) {
        System.out.printf("From WSDL Sample application\n");
        AddNumbersClient client = new AddNumbersClient();
        client.addNumbers();
        client.addNumbersFault();
        client.addNumbers2();
        client.addNumbers3();
        client.addNumbers3Fault();
    }


    public void addNumbers() {
        System.out.printf("addNumbers: default input and output names\n");
        try {
            AddNumbersPortType stub = createStub();
            int result = stub.addNumbers(number1, number2);
            assert result == 20;
        } catch (Exception ex) {
            ex.printStackTrace();
            assert false;
        }
        System.out.printf("\n\n");
    }

    public void addNumbersFault() {
        System.out.printf("addNumbers: default fault name, invalid operation input value\n");
        AddNumbersPortType stub = null;
        try {
            stub = createStub();
            int result = stub.addNumbers(-10, 10);
            assert false;
        } catch (AddNumbersFault_Exception ex) {
            assert true;
        } catch (Exception e) {
            e.printStackTrace();
            assert false;
        }
        System.out.printf("\n\n");
    }

    public void addNumbers2() {
        System.out.printf("addNumbers2: custom input and output names\n");
        try {
            AddNumbersPortType stub = createStub();
            int result = stub.addNumbers2(10, 10);
            assert result == 20;
        } catch (Exception ex) {
            ex.printStackTrace();
            assert false;
        }
        System.out.printf("\n\n");
    }

    public void addNumbers3() {
        System.out.printf("addNumbers3: custom input and output names\n");
        try {
            AddNumbersPortType stub = createStub();
            int result = stub.addNumbers3(10, 10);
            assert result == 20;
        } catch (Exception ex) {
            ex.printStackTrace();
            assert false;
        }
        System.out.printf("\n\n");
    }

    public void addNumbers3Fault() {
        System.out.printf("addNumbers3: custom fault and invalid input value\n");
        AddNumbersPortType stub = null;

        try {
            stub = createStub();
            int result = stub.addNumbers3(-10, 10);
            assert false;
        } catch (AddNumbersFault_Exception ex) {
            System.out.printf("Exception value is \"%s\"\n", ex.toString());
        } catch (Exception e) {
            e.printStackTrace();
            assert false;
        }
        System.out.printf("\n\n");
    }


    private AddNumbersPortType createStub() throws Exception {
        AddNumbersService service = new AddNumbersService();
        AddNumbersPortType stub = service.getAddNumbersPort();

        return stub;
    }
}
