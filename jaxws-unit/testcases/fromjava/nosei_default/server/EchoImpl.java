
package fromjava.nosei_default.server;

import javax.jws.*;

@WebService
public class EchoImpl {

    private void foo(Param p) {
    }

    public Bar echoBar(Bar bar) {
        return bar;
    }

    public String echoString(String str) {
        return str;
    }
    
    public String[] echoStringArray(String[] str) {
        return str;
    }
    
    public Bar[] echoBarArray(Bar[] bar) {
        return bar;
    }

    public Bar[] echoTwoBar(Bar bar, Bar bar2) {
        return new Bar[] { bar, bar2 };
    }
    
    public CLASS2 echoClass2(CLASS2 cls) {
        return cls;
    }
    
    @Oneway
    public void oneway() {
    }

    @WebMethod(exclude=true)
    public void test() {}
}
