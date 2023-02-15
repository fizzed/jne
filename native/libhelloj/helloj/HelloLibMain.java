package helloj;

import java.nio.file.Path;
import java.nio.file.Paths;

public class HelloLibMain {

    static public void main(String[] args) throws Exception {
        Path libFile = Paths.get("./libhelloj.so");
        System.load(libFile.toAbsolutePath().toString());

        HelloLib lib = new HelloLib();
        System.out.println("Response from lib: " + lib.hi());
    }

}