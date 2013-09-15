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

