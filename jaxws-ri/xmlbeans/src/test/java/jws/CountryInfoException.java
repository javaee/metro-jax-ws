package jws;

import com.bea.wli.sb.transports.ejb.test.xbean.CountryInfoType;

public class CountryInfoException extends Exception {

    private CountryInfoType faultInfo = null;

    public CountryInfoException(String msg, CountryInfoType info) {
        super(msg);
        faultInfo = info;
    }
    
    public CountryInfoException(String msg, CountryInfoType info, Throwable t) {
        super(msg, t);
        faultInfo = info;
    }
    
    public CountryInfoType getFaultInfo() {
        return faultInfo;
    }
}