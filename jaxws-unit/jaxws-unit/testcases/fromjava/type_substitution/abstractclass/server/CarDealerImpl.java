package fromjava.type_substitution.abstractclass.server;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.WebServiceException;

/**
 * Tests the type substitution.
 */
@WebService(name="CarDealer")
@XmlSeeAlso({Car.class, Toyota.class})
public class CarDealerImpl {

    public Car[] getSedans(){
        Car[] cars = new Toyota[2];
        cars[0] = new Toyota("Camry", "1998", "white");
        cars[1] = new Toyota("Corolla", "1999", "red");
        return cars;
    }

    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    public Car tradeIn(Car oldCar){
        if(!(oldCar instanceof Toyota))
            throw new WebServiceException("Expected Toyota, received, "+oldCar.getClass().getName());

        Toyota toyota = (Toyota)oldCar;
        if(toyota.getMake().equals("Toyota") && toyota.getModel().equals("Avalon") && toyota.getYear().equals("1999") && toyota.getColor().equals("white")){
            return new Toyota("Avalon", "2007", "black");
        }
        throw new WebServiceException("Invalid Toyota Car");
    }

    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    public Vehicle tradeInVehicle(Vehicle oldCar){
        if(!(oldCar instanceof Toyota))
            throw new WebServiceException("Expected Toyota, received, "+oldCar.getClass().getName());

        Toyota toyota = (Toyota)oldCar;
        if(toyota.getMake().equals("Toyota") && toyota.getModel().equals("Celica") && toyota.getYear().equals("1999") && toyota.getColor().equals("red")){
            return new Toyota("Prius", "2007", "white");
        }
        throw new WebServiceException("Invalid Toyota Car");
    }

}
