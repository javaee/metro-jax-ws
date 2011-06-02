package com.sun.xml.ws.sdo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

public class ResourceHelper {
//  static String resources = "../../../../buildout/modules/ws-databinding/test/resources" + File.separator;
    static String runtime_files = "." + File.separator;  
    static {
        File curDir = new File(".");
        if (new File(curDir, "runtime_files").exists()) {
            runtime_files = "runtime_files" + File.separator;
        }
    }
    
    static public File file(String ... path) {
        String f = runtime_files;
        for (String item : path) {
            f += item;
            if (item != path[path.length -1]) {
                f += File.separator;
            }
        }
        return new File(f);
    }

  static public Source fileAsSource(String ... path) throws FileNotFoundException, MalformedURLException {
    File f = file(path);
    Source src = new StreamSource(new FileInputStream(f));
    src.setSystemId(f.toURI().toURL().toExternalForm());
    return src;
  }
}
