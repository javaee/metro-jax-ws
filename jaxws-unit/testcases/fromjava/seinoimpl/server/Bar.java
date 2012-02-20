
package fromjava.seinoimpl.server;

import java.util.Date;

public class Bar {
   private int age; 
   private Date dob;
   
   public Bar() {}

   public int getAge() {
       return age;
   }

   public void setAge(int age) {
       this.age = age;
   }


   public Date getDob() {
       return dob;
   }

   public void setDob(Date dob) {
       this.dob = dob;
   }
}
