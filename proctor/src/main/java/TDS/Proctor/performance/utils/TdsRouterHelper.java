package TDS.Proctor.performance.utils;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TdsRouterHelper {

    private static final Logger logger = LoggerFactory.getLogger(TdsRouterHelper.class);

    public static String createRoutePrefix(String standardPrefix, String zone) {

        if (Strings.isNullOrEmpty(zone)) {
            logger.error("Zone argument null or missing returning passed prefix: ?", standardPrefix);
            return standardPrefix;
        }
        if (zone.length() > 1) {
            logger.error("Zone argument has incorrect length, zone: ?", zone);
            return standardPrefix;
        }
        if (Strings.isNullOrEmpty(standardPrefix)) {
            logger.error("Prefix argument null or missing returning passed prefix: ?", standardPrefix);
            return standardPrefix;
        }

        if (standardPrefix.length() < 4) {
            return zone.toUpperCase() + standardPrefix.toLowerCase();
        }

        return zone.toUpperCase() + standardPrefix.substring(1);
    }

}
