package fromjava.custom_server_jaxbcontext.server;

public class Toyota extends Car {

    private String color;
    private final String make="Toyota";

    public Toyota() {
        setMake("Toyota");
    }

    public String getMake() {
        return make;
    }

    public Toyota(String model, String year, String color) {
        setModel(model);
        setYear(year);
        this.color = color;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String toString(){
        return getMake()+":"+getModel()+":"+getYear()+":"+color;
    }

}
