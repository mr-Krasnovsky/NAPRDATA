package CadastrApp.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConstants {

    private static Properties properties =new Properties();

    static {
        try (InputStream inputStream =AppConstants.class.getClassLoader().getResourceAsStream("app.properties")) {
                properties.load(inputStream);
            } catch (IOException e) {
            e.printStackTrace();
            }
        }

        public static final String WGS84 = (properties.getProperty("coordSys.wgs84"));
        public static final String UTM38 = (properties.getProperty("coordSys.utm38n"));
        public static final String UTM37 = (properties.getProperty("coordSys.utm37n"));

        public static final String mapUrl = properties.getProperty("mapUrl");
        public static final String mapUrlResult = properties.getProperty("mapUrlResult");
        public static final String dataUrl = properties.getProperty("dataUrl");

    }

