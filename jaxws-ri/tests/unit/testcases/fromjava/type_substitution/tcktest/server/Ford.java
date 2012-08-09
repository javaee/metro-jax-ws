package fromjava.type_substitution.tcktest;

public class Ford extends Car {

    private String color;
    private final String make="Ford";

    public Ford() {
        setMake("Ford");
    }

    public String getMake() {
        return make;
    }

    public Ford(String model, String year, String color) {
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
