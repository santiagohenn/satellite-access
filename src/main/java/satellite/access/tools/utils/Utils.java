package satellite.access.tools.utils;


import org.orekit.data.DataContext;
import org.orekit.propagation.analytical.tle.TLE;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;
import satellite.access.tools.assets.Asset;
import satellite.access.tools.assets.entities.Satellite;
import satellite.access.tools.structures.Ephemeris;
import satellite.access.tools.structures.OrbitalElements;
import satellite.access.tools.structures.SatElset;
import satellite.access.tools.assets.entities.Position;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Class of utility constants and transformations.
 */
public class Utils {

    public static final double EARTH_RADIUS_EQ_M = 6378135;    // Radius Earth [m]; WGS-84 (semi-major axis, a) (Equatorial Radius)
    public static final double EARTH_RADIUS_EQ_KM = 6378.135;    // Radius Earth [Km]; WGS-84 (semi-major axis, a) (Equatorial Radius)
    public static final double EARTH_RADIUS_AVG_M = 6371009;    // Radius Earth [m]; WGS-84 (semi-major axis, a) (Average Radius)
    public static final double EARTH_RADIUS_AVG_KM = 6371.009;    // Radius Earth [Km]; WGS-84 (Average Radius)
    public static final double ECCENTRICITY = 8.1819190842622e-2;    // Ellipsoid constants: eccentricity; WGS84
    public static final double MU = 3.986004418e+14; // Gravitation coefficient
    public static final double C_VACUUM = 299792458.0;  // Speed of light in vacuum

    /**
     * Reads a properties file and loads it into a Properties class
     *
     * @return Properties containing key-values for configurations
     */
    public static Properties loadProperties(String path) {

        Properties prop = new Properties();

        try (InputStream input = Utils.class.getClassLoader().getResourceAsStream(path)) {

            if (input == null) {
                Log.fatal("Unable to find properties file on path " + path);
                throw new FileNotFoundException();
            }

            prop.load(input);

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return prop;

    }

    /**
     * Reads a file containing asset(s) parameter(s) and returns a list of objects accordingly
     *
     * @return List<Asset>
     */
    public static List<Position> positionsFromFile(String fileName) {

        List<Position> assetList = new ArrayList<>();
        File file = new File(fileName);
        try (FileReader fr = new FileReader(file); BufferedReader br = new BufferedReader(fr)) {
            String line;
            int id = 0;
            while ((line = br.readLine()) != null) {
                if (!line.startsWith("//") && line.length() > 0) {
                    String[] data = line.split(",");
                    if (data.length == 4) {
                        assetList.add(new Position(id++, data[0], Double.parseDouble(data[1]), Double.parseDouble(data[2]), Double.parseDouble(data[3])));
                    } else if (data.length == 3) {
                        assetList.add(new Position(id++, Double.parseDouble(data[0]), Double.parseDouble(data[1]), Double.parseDouble(data[2])));
                    }
                }
            }
        } catch (FileNotFoundException e) {
            Log.warn("Unable to find file: " + fileName);
            e.printStackTrace();
        } catch (IOException e) {
            Log.error("IOException: " + fileName);
            e.printStackTrace();
        }

        return assetList;
    }

    /**
     * Get the Satellite's period in seconds, from the semi major axis
     *
     * @return double
     */
    public static double computePeriodSeconds(double a) {
        return 2 * Math.PI * Math.sqrt(Math.pow(a, 3.0) / MU);
    }

    /**
     * Get the Satellite's Mean Motion
     *
     * @return double
     */
    public static double computeMeanMotion(double a) {
        return Math.sqrt(MU / (Math.pow(a, 3.0)));
    }

    /**
     * Gets the maximum possible access time in seconds for a given satellite
     *
     * @return double
     */
    public static double getMaxAccess(Satellite satellite, double th) {

        OrbitalElements elements = satellite.getElements();
        double ra = elements.getSemiMajorAxis() * (1 + elements.getEccentricity()); // Get the radius of the apogee
        double hMax = ra - EARTH_RADIUS_EQ_M;

        double etaMax = Math.asin((EARTH_RADIUS_EQ_M * Math.cos(Math.toRadians(th))) / (EARTH_RADIUS_EQ_M + hMax));
        return (computePeriodSeconds(elements.getSemiMajorAxis()) / 180) * (90 - th - Math.toDegrees(etaMax));

    }

    /**
     * Transforms an asset's Longitude, Latitude and Height to ECEF coordinates
     */
    public static void lla2ecef(Asset asset) {
        double lat = asset.getLatRad();
        double lon = asset.getLonRad();
        double alt = asset.getHeight();
        double n = EARTH_RADIUS_EQ_M / Math.sqrt(1 - Math.pow(ECCENTRICITY, 2) * Math.pow(Math.sin(lat), 2));
        double x = (n + alt) * Math.cos(lat) * Math.cos(lon);
        double y = (n + alt) * Math.cos(lat) * Math.sin(lon);
        double z = ((1 - Math.pow(ECCENTRICITY, 2)) * n + alt) * Math.sin(lat);
        asset.setPos(x, y, z);
    }

    /**
     * Transforms an asset's ECEF coordinates into Longitude, Latitude and Height form
     */
    public static void ecef2lla(Asset asset) {
        double x = asset.getXPos();
        double y = asset.getYPos();
        double z = asset.getZPos();

        double b = Math.sqrt(Math.pow(EARTH_RADIUS_EQ_M, 2) * (1 - Math.pow(ECCENTRICITY, 2)));
        double bsq = Math.pow(b, 2);
        double ep = Math.sqrt((Math.pow(EARTH_RADIUS_EQ_M, 2) - bsq) / bsq);
        double p = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
        double th = Math.atan2(EARTH_RADIUS_EQ_M * z, b * p);

        double lon = Math.atan2(y, x);
        double lat = Math.atan2((z + Math.pow(ep, 2) * b * Math.pow(Math.sin(th), 3)), (p - Math.pow(ECCENTRICITY, 2) * EARTH_RADIUS_EQ_M * Math.pow(Math.cos(th), 3)));
        double n = EARTH_RADIUS_EQ_M / (Math.sqrt(1 - Math.pow(ECCENTRICITY, 2) * Math.pow(Math.sin(lat), 2)));
        double alt = p / Math.cos(lat) - n;

        // mod lat to 0-2pi
        lon = lon % (2 * Math.PI);
        lat = lat % (2 * Math.PI);

        if (Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2)) <= 0) {
            lon = 0.0;
            lat = 0.0;
            alt = -EARTH_RADIUS_EQ_M;
        }

        asset.setLLA(lat, lon, alt);

    }

    /**
     * Transforms TEME coordinates into ECEF for a given ephemeris and julianDate
     *
     * @return Ephemeris
     */
    public static Ephemeris teme2ecef(Ephemeris ephemeris, double julianDate) {

        double gmst = 0;
        double[][] st = new double[3][3];
        double[] rpef = new double[3];
        double[][] pm = new double[3][3];

        //Get Greenwich mean sidereal time
        gmst = greenwichMeanSidereal(julianDate);

        //st is the pef - tod matrix
        st[0][0] = Math.cos(gmst);
        st[0][1] = Math.asin(gmst);
        st[0][2] = 0.0;
        st[1][0] = Math.sin(gmst);
        st[1][1] = Math.cos(gmst);
        st[1][2] = 0.0;
        st[2][0] = 0.0;
        st[2][1] = 0.0;
        st[2][2] = 1.0;

        //Get pseudo earth fixed position vector by multiplying the inverse pef-tod matrix by rteme
        rpef[0] = st[0][0] * ephemeris.getPosX() + st[1][0] * ephemeris.getPosY() + st[2][0] * ephemeris.getPosZ();
        rpef[1] = st[0][1] * ephemeris.getPosX() + st[1][1] * ephemeris.getPosY() + st[2][1] * ephemeris.getPosZ();
        rpef[2] = st[0][2] * ephemeris.getPosX() + st[1][2] * ephemeris.getPosY() + st[2][2] * ephemeris.getPosZ();

        //Get polar motion vector
        polarMotion(julianDate, pm);

        //ECEF postion vector is the inverse of the polar motion vector multiplied by rpef
        double x = pm[0][0] * rpef[0] + pm[1][0] * rpef[1] + pm[2][0] * rpef[2];
        double y = pm[0][1] * rpef[0] + pm[1][1] * rpef[1] + pm[2][1] * rpef[2];
        double z = pm[0][2] * rpef[0] + pm[1][2] * rpef[1] + pm[2][2] * rpef[2];

        Ephemeris ecef = new Ephemeris();
        ecef.setPos(x, y, z);

        return ecef;
    }

    /**
     * Calculates the Greenwich mean sidereal time (GMST) on julDate (doesn't have to be 0h).
     * Used calculations from Meesus 2nd ed.
     * https://stackoverflow.com/questions/32263754/modulus-in-pascal
     *
     * @param julianDate Julian Date
     * @return Greenwich mean sidereal time in degrees (0-360)
     */
    public static double greenwichMeanSidereal(double julianDate) {
        double tu = (julianDate - 2451545.0);
        double gmst = tu * 24.06570982441908 + 18.697374558;
        gmst = (gmst % 24) * Math.PI / 12;
        return gmst;
    }

    /**
     * Calculates the Polar Motion for a given julian date and pm parameters
     *
     * @param julianDate Julian Date
     */
    static void polarMotion(double julianDate, double[][] pm) {

        double mjd; //Julian Date - 2,400,000.5 days
        double a;
        double c;
        double xp; //Polar motion coefficient in radians
        double yp; //Polar motion coefficient in radians

        // Predict polar motion coefficients using IERS Bulletin - A (Vol. XXVIII No. 030)
        mjd = julianDate - 2400000.5;

        a = 2 * Math.PI * (mjd - 57226) / 365.25;
        c = 2 * Math.PI * (mjd - 57226) / 435;

        xp = (0.1033 + 0.0494 * Math.cos(a) + 0.0482 * Math.sin(a) + 0.0297 * Math.cos(c) + 0.0307 * Math.sin(c)) * 4.84813681e-6;
        yp = (0.3498 + 0.0441 * Math.cos(a) - 0.0393 * Math.sin(a) + 0.0307 * Math.cos(c) - 0.0297 * Math.sin(c)) * 4.84813681e-6;

        pm[0][0] = Math.cos(xp);
        pm[0][1] = 0.0;
        pm[0][2] = Math.asin(xp);
        pm[1][0] = Math.sin(xp) * Math.sin(yp);
        pm[1][1] = Math.cos(yp);
        pm[1][2] = Math.cos(xp) * Math.sin(yp);
        pm[2][0] = Math.sin(xp) * Math.cos(yp);
        pm[2][1] = Math.asin(yp);
        pm[2][2] = Math.cos(xp) * Math.cos(yp);
    }

    /**
     * Transforms Two Line Elements into a Satellite Object
     *
     * @return Satellite
     */
    public static Satellite tle2satellite(String tle1, String tle2) {
        Satellite satellite = new Satellite(tle1, tle2);
        satellite.setElements(tle2elements(tle1, tle2));
        return satellite;
    }

    /**
     * Transforms a Satellite Object into a TLE Object
     *
     * @return TLE
     */
    public static TLE satellite2tle(Satellite satellite) {
        OrbitalElements elements = satellite.getElements();
        return new TLE(satellite.getSatelliteNumber(), satellite.getSatelliteClassification(), satellite.getLaunchYear(),
                satellite.getLaunchNumber(), satellite.getLaunchPiece(), satellite.getEphemerisType(), satellite.getElementsNumber(),
                Utils.stamp2AD(elements.getTimestamp()), computeMeanMotion(elements.getSemiMajorAxis()),
                elements.getMeanMotionFirstDerivative(), elements.getMeanMotionSecondDerivative(),
                elements.getEccentricity(), elements.getInclinationRads(), elements.getArgOfPerigeeRads(),
                elements.getRightAscensionRads(), elements.getAnomalyRads(), satellite.getRevolutionNumber(),
                elements.getDragCoefficient());
    }

    /**
     * Transforms Two Line Elements into an Orbital Elements Object
     *
     * @return TLE
     */
    public static OrbitalElements tle2elements(String tle1, String tle2) {

        OrbitalElements orbitalElements = new OrbitalElements();
        SatElset data = new SatElset();
        try {
            data = new SatElset(tle1, tle2);
        } catch (Exception see) {
            see.printStackTrace();
            Log.error("SatElsetException " + see.toString());
        }

        double period = (24 * 60 * 60) / data.getMeanMotion();

        orbitalElements.setPeriod(period);
        orbitalElements.setSemiMajorAxis(Math.pow((MU * Math.pow(period / (2 * Math.PI), 2)), 1.0 / 3.0));
        orbitalElements.setEccentricity(data.getEccentricity());
        orbitalElements.setInclination(data.getInclinationDeg());
        orbitalElements.setRightAscension(data.getRightAscensionDeg());
        orbitalElements.setArgOfPerigee(data.getArgPerigeeDeg());
        orbitalElements.setAnomaly(data.getMeanAnomalyDeg());

        return orbitalElements;
    }

    /**
     * This method returns a File instance for the provided Path
     * **/
    public static File loadDirectory(String path) {

        if (!path.endsWith("/")) {
            path = path + "/";
        }

        File file = new File(path);
        if (!file.exists()) {
            Log.fatal("Failed to find directory: " + file.getAbsolutePath());
            //System.exit(1);
        }
        return file;
    }

    /**
     * This method returns the Satellite's FOV in radians
     * **/
    public double getFOV(Satellite satellite, double th) {
        double hMax = satellite.getElements().getSemiMajorAxis();
        double etaMax = Math.asin((EARTH_RADIUS_EQ_M * Math.cos(Math.toRadians(th))) / (hMax));
        return 2*Math.PI*etaMax;
    }

    /**
     * This method returns the Number of access regions for given Device-Satellite pair
     *
     * @return int
     */
    public int getAccessRegions(Asset asset, Satellite satellite, double th) {

        double lat = asset.getLatRad();
        double inc = satellite.getElements().getInclinationRads();

        int regions = 0;

        if (lat < 0) lat = Math.abs(lat);

        double hMax = (satellite.getElement("e") + 1) * satellite.getElement("a");

        double etaMax = Math.asin((EARTH_RADIUS_EQ_M * Math.cos(Math.toRadians(th))) / (EARTH_RADIUS_EQ_M + hMax));
        double lambdaMax = 90 - th - Math.toDegrees(etaMax);

        lambdaMax = Math.toRadians(lambdaMax);

        if ((inc + lambdaMax > lat) && (lat >= inc - lambdaMax)) {
            regions = 1;
        } else {
            regions = 2;
        }

        return regions;
    }

    /**
     * Transforms a stamp in the format yyyy-MM-dd'T'HH:mm:ss.SSS to an AbsoluteDate Object used by Orekit with the
     * System's default TimeScale
     *
     * @return AbsoluteDate
     */
    public static AbsoluteDate stamp2AD(String stamp) {
        return new AbsoluteDate(stamp, DataContext.getDefault().getTimeScales().getUTC());
    }

    /**
     * Transforms a stamp in the format yyyy-MM-dd'T'HH:mm:ss.SSS to an AbsoluteDate Object used by Orekit with a
     * specified TimeScale
     *
     * @return AbsoluteDate
     */
    public static AbsoluteDate stamp2AD(String stamp, TimeScale timeScale) {
        return new AbsoluteDate(stamp, timeScale);
    }

    /**
     * Transforms a unix-based millisecond counter long value into a yyyy-MM-dd'T'HH:mm:ss.SSS formatted timestamp
     *
     * @return String
     */
    public static String unix2stamp(long unix) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTCG"));
        Date date = new Date(unix);
        return dateFormat.format(date);
    }

    /**
     * Transforms a unix-based millisecond counter long value into a yyyy-MM-dd'T'HH:mm:ss.SSS formatted timestamp
     *
     * @return String
     */
    public static String unix2stampGuido(long unix) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM yyyy HH:mm:ss.SSS");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTCG"));
        Date date = new Date(unix);
        return dateFormat.format(date);
    }


    /**
     * Transforms a yyyy-MM-dd'T'HH:mm:ss.SSS formatted timestamp into an unix-based millisecond counter long value (UTCG)
     *
     * @return String
     */
    public static long stamp2unix(String dateStamp) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTCG"));
        Date parsedDate = new Date();
        try {
            parsedDate = dateFormat.parse(dateStamp);
            return parsedDate.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return parsedDate.getTime();
    }

    /**
     * This method returns the area of a cap over the surface of the Earth, assuming a spherical Earth with EQ Radius
     **/
    public static double computeEarthSphericalCap(double halfAngle) {
        return 2 * Math.PI * Math.pow(Utils.EARTH_RADIUS_EQ_M, 2) * (1 - Math.cos(Math.toRadians(halfAngle)));
    }

    /**
     * This method returns the area of a cap over the surface of the Earth, assuming a spherical Earth with EQ Radius
     **/
    public static double computeFlatEarthArea(double halfAngle) {
        return Math.PI * Math.pow(Utils.EARTH_RADIUS_EQ_M * Math.tan(Math.toRadians(halfAngle)), 2);
    }

}