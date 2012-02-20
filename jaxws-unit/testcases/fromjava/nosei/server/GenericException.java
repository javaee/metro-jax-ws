
package fromjava.nosei.server;

import java.util.List;

public class GenericException extends Exception {
    GenericValue<Integer> age;
    Bar object;
    List<Bar> barList;
    
    public GenericException (String message, GenericValue<Integer> age, Bar object, List<Bar> barList) {
	  super(message);
        this.age = age;
        this.object = object;
        this.barList = barList;
    }

    public GenericValue<Integer> getAge() {
        return age;
    }
    
    public Bar getObject() {
        return object;
    }
    
    public List<Bar> getBarList() {
        return barList;
    }
}
