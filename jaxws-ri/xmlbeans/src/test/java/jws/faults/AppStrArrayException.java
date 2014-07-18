package jws.faults;

public class AppStrArrayException extends Exception{
    private String[] error;
    public AppStrArrayException(){super();}
    public AppStrArrayException(String ... error) {
        super();
        this.error = error;
    }

    public String[] getError() {
        return error;
    }

    public void setError(String[] error) {
        this.error = error;
    }
}
