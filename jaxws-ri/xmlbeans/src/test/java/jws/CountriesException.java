package jws;

import com.bea.wli.sb.transports.ejb.test.xbean.CountriesDocument;

public class CountriesException extends Exception {

    private CountriesDocument faultInfo = null;

    public CountriesException(String msg, CountriesDocument info) {
        super(msg);
        faultInfo = info;
    }
    
    public CountriesException(String msg, CountriesDocument info, Throwable t) {
        super(msg, t);
        faultInfo = info;
    }
    
    public CountriesDocument getFaultInfo() {
        return faultInfo;
    }
}