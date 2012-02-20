package fromjava.type_substitution.tcktest;

import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests the type substitution.
 */
@WebService(
    name="CarDealer",
    portName="CarDealerPort",
    serviceName="CarDealerService",
    targetNamespace="http://typesubstitution/wsdl"
)
@XmlSeeAlso({Toyota.class, Ford.class})
public class CarDealerImpl {
    public List<Car> getSedans(){
        List<Car> cars = new ArrayList<Car>();
        Toyota camry = new Toyota();

        camry.setMake("Toyota");
        camry.setModel("Camry");
        camry.setYear("1998");
        camry.setColor("white");

        cars.add(camry);

        Ford mustang = new Ford();

        mustang.setMake("Ford");
        mustang.setModel("Mustang");
        mustang.setYear("1999");
        mustang.setColor("red");
        cars.add(mustang);
        return cars;
    }
}
