
package fromjava.nosei_apt.server;

public class Bar {
    private int age; 

    public Bar() {}

    public Bar(int age) {
       this.age = age;
    }

    public int getAge() {
       return age;
    }

    public void setAge(int age) {
       this.age = age;
    }
    
    public static class InnerBar {
        private String name;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
    }
}
