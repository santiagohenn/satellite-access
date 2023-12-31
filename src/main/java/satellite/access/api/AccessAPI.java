package satellite.access.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import satellite.access.tools.Simulation;
import satellite.access.tools.assets.entities.Position;
import satellite.access.tools.assets.entities.Satellite;
import satellite.access.tools.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
public class AccessAPI {

    @GetMapping("/test")
    public String status() {
        return "Service working";
    }

    @GetMapping("/testOrekitDataAccess")
    public String findDirs3() {
        File[] contents = null;
        ArrayList<String> fileNames = new ArrayList<>();
        try {
            File orekitFile = new File("/orekit-data/");
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

    @GetMapping("/listFiles")
    public String findDirs4() {
        File[] contents = null;
        ArrayList<String> fileNames = new ArrayList<>();
        try {
            File orekitFile = new File("/");
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

    @GetMapping("/api/access/{tle1}/{tle2}/{position}/{startDate}/{endDate}/{timeStep}/{visTH}")
    public ResponseEntity<List<String>> multiplyByTwo(@PathVariable String tle1,
                                                      @PathVariable String tle2,
                                                      @PathVariable String position,
                                                      @PathVariable String startDate,
                                                      @PathVariable String endDate,
                                                      @PathVariable String timeStep,
                                                      @PathVariable String visTH) {

        List<String> response = new ArrayList<>();

        String[] LLA = position.split(",");
        double timeStepSeconds = Double.parseDouble(timeStep);
        double visTHDegrees = Double.parseDouble(visTH);

        Satellite satellite = new Satellite(tle1, tle2);

        if (LLA.length < 2) {
            return ResponseEntity.ok(Collections.singletonList("Invalid LLA data"));
        }

        double lat = Double.parseDouble(LLA[0]);
        double lon = Double.parseDouble(LLA[1]);
        double height = 0;

        if (LLA.length == 3) {
            height = Double.parseDouble(LLA[2]);
        }

        // Declare a Device with its Latitude, Longitude and Height (deg, deg, meters)
        Position device = new Position(lat, lon, height);

        // Instanciate a tools.simulation with a start time, end time, devices involved, time step in seconds and
        // visibility threshold in degrees
        Simulation simulation = new Simulation(startDate, endDate,
                device, satellite, timeStepSeconds, visTHDegrees);

        // Compute access intervals
        simulation.computeAccess();

        simulation.getIntervals().forEach(interval -> {
            response.add(Utils.unix2stamp(interval.getStart()) + ","
                    + Utils.unix2stamp(interval.getEnd()) + "," + interval.getDuration() / 1000.0);
        });

        return ResponseEntity.ok(response);

    }

}
