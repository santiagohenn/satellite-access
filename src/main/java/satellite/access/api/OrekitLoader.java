package satellite.access.api;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class OrekitLoader {

    public File loadOrekitDataFromClasspath() {

        Resource resource = new ClassPathResource("orekit-data");

        try {
            File file = resource.getFile();
            System.out.println("Orekit-data path loaded");
            return file;
        } catch (IOException e) {
            System.out.println("ERROR");
        }
        return null;
    }

}
