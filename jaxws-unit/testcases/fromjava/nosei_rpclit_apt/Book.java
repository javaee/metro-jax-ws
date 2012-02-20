
package fromjava.nosei_rpclit_apt;


public enum Book {
    HTPJ("How to Program Java", "2005"),
    JWSDP("Java Web Services Developers Pack", "2005");

    private final String title;
    private final String yearPublished;

    Book(String title, String year) {
        this.title = title;
        this.yearPublished = year;
    }

    public String getTitle() {
        return title;
    }
  
    public String getYearPublished() {
        return yearPublished;
    }
}
