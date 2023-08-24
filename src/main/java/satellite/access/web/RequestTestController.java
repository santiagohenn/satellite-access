package satellite.access.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Defines a controller to handle HTTP requests
 */
@Controller
public final class RequestTestController {

    private static String project;
    private static final Logger logger = LoggerFactory.getLogger(RequestTestController.class);

    @Value("${NAME:Santi}")
    String name;

    @RestController
    class HelloworldController {
        @GetMapping("/hello")
        String hello() {
            return "Hello " + name + "!";
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

}