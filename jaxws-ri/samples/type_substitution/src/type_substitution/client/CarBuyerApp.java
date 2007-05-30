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

package type_substitution.client;


import java.util.List;

public class CarBuyerApp {
    public static void main(String[] args) {
        CarDealerService service = new CarDealerService();
        CarDealer dealer = service.getCarDealerPort();

        /**
         * CarDealer.getSedans() returns List<Car>
         * Car is abstract class. The code below shows
         * that the client is expecting a Toyota which extends
         * Car.
         *
         * It shows a doc wrapper style operation.
        */
        List<Car> r = dealer.getSedans("toyota");
        Toyota car = (Toyota)r.get(0);
        if(car != null && car.getMake().equals("Toyota") &&
                car.getModel().equals("Camry") &&
                car.getYear().equals("1998") &&
                car.getColor().equals("white")){
            System.out.println("Received Right car!");
        }else{
            System.out.println("Failed to get Car!");
        }

        /**
         * CarDealer.tradeIn(Car) takes an abstract class Car and returns the same.
         * We will send a sub-class instead and expect to get the same.
         *
         */
        Toyota oldCar = new Toyota();
        oldCar.setMake("Toyota");
        oldCar.setModel("Avalon");
        oldCar.setYear("1999");
        oldCar.setColor("white");

        Toyota newCar = (Toyota)dealer.tradeIn(oldCar);

        if(newCar != null &&
                newCar.getMake().equals("Toyota") &&
                newCar.getModel().equals("Avalon") &&
                newCar.getYear().equals("2007") &&
                newCar.getColor().equals("black")){
            System.out.println("Traded in Right Car!");
        }else{
            System.out.println("Failed to tradeIn!");
        }
    }
}
