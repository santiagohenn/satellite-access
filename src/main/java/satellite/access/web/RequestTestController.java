package satellite.access.web;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

/**
 * Defines a controller to handle HTTP requests
 */
@Controller
public final class RequestTestController {

    private static String project;
    private static final Logger logger = LoggerFactory.getLogger(RequestTestController.class);

    @Autowired
    private ResourceLoader resourceLoader;

    public File accessStaticResource() throws IOException {
        Resource resource = resourceLoader.getResource("classpath:static/orekit-data/");
        //InputStream inputStream = resource.getInputStream();
        System.out.println("Exists?: " + resource.exists());
        return resource.getFile();        
        // Read the content of the file from the input stream and perform computations
    }

    @GetMapping("/testAccess")
    public String tryToGetPath() {
        
        File[] contents = null;
        ArrayList<String> fileNames = new ArrayList<>();
        try {
            File orekitFile = accessStaticResource();
            if (orekitFile.isDirectory()) {
                contents = orekitFile.listFiles();
                if (contents != null) {
                    for (File file : contents) {
                        System.out.println(file.getName());
                        fileNames.add(file.getName());
                    }
                }
            }
        } catch (Exception e) {
            return e.getMessage();
        }
        
        return fileNames.toString();
    }

    @Value("${NAME:Santi}")
    String name;

    @RestController
    class TestController {
        @GetMapping("/hello")
        String hello() {

            File[] contents = null;
            ArrayList<String> fileNames = new ArrayList<>();
            try {
                File orekitFile = new File("img");
                if (orekitFile.isDirectory()) {
                    contents = orekitFile.listFiles();
                    if (contents != null) {
                        for (File file : contents) {
                            System.out.println(file.getName());
                            fileNames.add(file.getName());
                        }
                    }
                }
            } catch (Exception e) {
                return e.getMessage();
            }
            
            return fileNames.toString();
            //return "Service running";
        }
    }

    /**
     * Create an endpoint for the landing page
     *
     * @return the index view template
     */
    @GetMapping("/")
    public String helloWorld(Model model) {

        // Get Cloud Run environment variables.
        String revision = System.getenv("K_REVISION") == null ? "???" : System.getenv("K_REVISION");
        String service = System.getenv("K_SERVICE") == null ? "???" : System.getenv("K_SERVICE");

        // Set variables in html template.
        model.addAttribute("revision", revision);
        model.addAttribute("service", service);
        return "index";
    }

    @Configuration
    public class WebConfiguration extends WebMvcConfigurationSupport {
    
        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) { 
                registry.addResourceHandler("/**")
                     .addResourceLocations("classpath:/static/");
        }
    }

}
