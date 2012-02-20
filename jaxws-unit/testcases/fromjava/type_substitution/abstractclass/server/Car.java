package fromjava.type_substitution.abstractclass.server;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public abstract class Car implements Vehicle{
    private String model;
    private String year;
    private String make;

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public abstract String getMake();

    static class Adapter extends XmlAdapter<Car, Vehicle> {
        public Vehicle unmarshal(Car v) throws Exception {
            return v;
        }

        public Car marshal(Vehicle v) throws Exception {
            return (Car)v;
        }
    }
}
