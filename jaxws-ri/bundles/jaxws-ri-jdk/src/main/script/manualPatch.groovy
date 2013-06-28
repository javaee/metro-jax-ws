#!/home/mkos/java/groovy-2.1.2/bin/groovy
println "Running manual patch ..."

def rootDir = "${project.basedir}" //new File(".").getAbsolutePath()
def ant = new AntBuilder()

dest_dir = new File("${rootDir}/target/generated-sources")

println "Running manual patch in directory: " + dest_dir.toString()

//take care of META-INF/services
//Unknown META-INF/services will be ignored and not inclued in the JDK
// this renaming map is maintained cuz of a bug in package-rename ant task where it doesn't chaneg the file name.
def known_metainf = ["com.sun.tools.xjc.Plugin":"com.sun.tools.internal.xjc.Plugin",
        "com.sun.mirror.apt.AnnotationProcessorFactory":"com.sun.mirror.apt.AnnotationProcessorFactory",
        "com.sun.xml.ws.policy.jaxws.spi.ModelConfiguratorProvider":"com.sun.xml.internal.ws.policy.jaxws.spi.ModelConfiguratorProvider",
        "com.sun.xml.ws.policy.jaxws.spi.PolicyMapUpdateProvider":"com.sun.xml.internal.ws.policy.jaxws.spi.PolicyMapUpdateProvider",
        "com.sun.xml.ws.policy.spi.PolicyAssertionValidator":"com.sun.xml.internal.ws.policy.spi.PolicyAssertionValidator",
        "com.sun.xml.ws.policy.spi.PrefixMapper":"com.sun.xml.internal.ws.policy.spi.PrefixMapper",
        "com.sun.xml.ws.spi.db.BindingContextFactory":"com.sun.xml.internal.ws.spi.db.BindingContextFactory",
        "com.sun.tools.ws.wscompile.Plugin":"com.sun.tools.internal.ws.wscompile.Plugin"]

src_metainf = new File("${dest_dir}/META-INF/services") //output of previous pkg rename action

if (!src_metainf.exists()) {
    println src_metainf.toString() + " does not exist, probably run in a wrong phase? returning ..."
    return
};

dest_metainf = new File("${dest_dir}/com/sun/tools/etc/META-INF/services") // dir for META-INF/services in JDK to go into tools.jar
println "creating dest_metainf: " + dest_metainf
dest_metainf.mkdirs()

src_metainf.eachFile {
    println "processing metainf: " + it.name
    if(known_metainf[it.name] == null){
        println ""
        println "unknown META-INF/services file, Ignoring ${src_metainf}/${it.name}"
        println "Delete ${it.name} in <repo-patch>"
        println "OR"
        println "Update the known_metainf map in ${this.getClass().name}.groovy in <repo-patch>"
        println ""
        //throw new RuntimeException("An Exception in handling META-INF/servcies files")

    } else {
        ant.move(file:it,tofile:"${dest_metainf}/${known_metainf[it.name]}")
    }
}

// change behavior of jdk's MetroConfigLoader
println "Patching MetroConfigLoader ..."
println "   renaming resource name"
File loaderFile = new File("${dest_dir}/com/sun/xml/internal/ws/assembler/MetroConfigLoader.java")
fileText = loaderFile.text;
fileText = fileText.replaceAll("META-INF/", "com/sun/xml/internal/ws/assembler/")
loaderFile.write(fileText);

// tube-assembler - let's look for different location to avoid collissions with standalone JAX-WS
println "   moving jaxws-tubes-default.xml"
ant.move(file:"${dest_dir}/META-INF/jaxws-tubes-default.xml",tofile:"${dest_dir}/com/sun/xml/internal/ws/assembler/jaxws-tubes-default.xml")

println "Deleting META-INF"
ant.delete(dir:"${dest_dir}/META-INF")

println "Manual patch ... successfully finished."
