* These tests are based on ws-test-harness (https://ws-test-harness.dev.java.net/).

* All the tests are stored under 'testcases' directory.

* Here are the test directory structure:

fromjava - All the starting from Java tests go here
fromwsdl - All the starting from WSDL tests go here
<any feature> - A feature level test. May have any number of subdirectories.

* To run all the tests in one-go, simply type:
  - 'ant run' -> This gets the latest JAXWS 2.1 RI build form hudson and run all the tests against them using local transport.
  - 'ant run -Djaxwsdir=<path to jaxws wspace>' -> Runs all the tests against the jaxws wspace referenced using jaxwsdir property using local transport.
  - Use -Dtransport=tomcat to run the tests using embedded-tomcat.
  - Use -Dlog=true to enable logging.
  - Use -Dtest=<path to individual test or any test subdirectory> to run any individual or any sub-dir test.
  - Use -Dargs=-skip to only run the tests without generating any code

* Refer to docs/index.html on how to run the tests 
  individually during development/debugging.

* For those who are lazy to read the above doc, here instructions to run tests against the workspace !

1. Unset JAXWS_HOME
2. set JAVA_HOME to JDK 5
3. Checkout RI & test workspaces such that jaxws-ri and jaxws-unit are in the same directory.
    $ svn co https://svn.java.net/svn/jax-ws~sources/trunk/jaxws-unit
    $ svn co https://svn.java.net/svn/jax-ws~sources/branches/jaxws22/jaxws-ri
4. Build RI
    $ cd jaxws-ri
    $ ./build.sh
5. Run a test using in-vm transport
    $ cd jaxws-unit
    $ java -jar lib/harness.jar testcases/fromwsdl/wsdl_hello_lit
6. Run a test using lwhs
    $cd jaxws-unit
    $ java -jar lib/harness.jar -lwhs testcases/fromwsdl/wsdl_hello_lit
