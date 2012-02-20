package fromjava.innerclass;

import javax.jws.WebService;

public class EchoImpl {
    @WebService
    public static class EchoInner
        implements EchoIF {
        public Bar echoBar(Bar bar) {
            return bar;
        }

        public String echoString(String str) {
            return str;
        }
    }
}
