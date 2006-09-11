package fromwsdl.type_substitution.server;

import java.util.ArrayList;
import java.util.List;

@javax.jws.WebService (endpointInterface="fromwsdl.type_substitution.server.CarDealer")
public class CarDealerImpl {
    public List<Car> getSedans(){
        List<Car> cars = new ArrayList<Car>();
        Toyota camry = new Toyota();

        camry.setMake("Toyota");
        camry.setModel("Camry");
        camry.setYear("1998");
        camry.setColor("white");

        cars.add(camry);

        Toyota corolla = new Toyota();

        corolla.setMake("Toyota");
        corolla.setModel("Corolla");
        corolla.setYear("1999");
        corolla.setColor("red");
        cars.add(corolla);
        return cars;
    }
}
