package wsn;

import java.util.logging.Logger;

public class Log {
    static Logger logger = Logger.getLogger(Log.class.getName());
    public static void i(String... messages) {
        String log = String.join(" ", messages);
        logger.info(log);
    }
}
