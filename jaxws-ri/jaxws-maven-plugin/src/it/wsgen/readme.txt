
This example:

   - Compiles a service end point(SEI) initialize phase
   
   - Wsgens web service classes from the compile SEI
   
   - Packages all classes and related files  in a war
   
   
Invoke mvn jetty:run and hit http://localhost:9090/helloworld for manually test.

However, The web app is not functional until Sun adds httpd.jar and resolver.jar
in the jaxws-ri bundle as dependencies of jaxws-rt.jar.  The work around is to 
deploy those missing jars to your local repository and add them to this example's
pom.

Enjoy


