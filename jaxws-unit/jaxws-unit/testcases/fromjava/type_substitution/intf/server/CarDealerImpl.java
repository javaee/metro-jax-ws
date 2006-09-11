package fromjava.type_substitution.intf.server;

import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * Tests the type substitution.
 */
@WebService(name="CarDealer")
@XmlSeeAlso({Car.class, Toyota.class})
public class CarDealerImpl {

    public Vehicle[] getSedans(){
        Vehicle[] cars = new Toyota[2];
        cars[0] = new Toyota("Camry", "1998", "white");
        cars[1] = new Toyota("Corolla", "1999", "red");
        return cars;
    }
}
