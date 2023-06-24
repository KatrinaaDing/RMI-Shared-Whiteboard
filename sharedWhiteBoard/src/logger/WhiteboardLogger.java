package logger;

import java.io.IOException;
import java.util.logging.*;

/**
 * This class is used to log the information
 */
public class WhiteboardLogger {
    private static final Logger logger = Logger.getLogger(WhiteboardLogger.class.getName());

    private WhiteboardLogger() throws IOException {
        Handler handler = null;
        Formatter simpleFormatter = null;

        handler = new ConsoleHandler();
        simpleFormatter = new SimpleFormatter();
        logger.addHandler(handler);
        handler.setFormatter(simpleFormatter);

        handler.setLevel(Level.FINEST);
        logger.setLevel(Level.FINEST);
    }

    /**
     * Get the logger. If not exists, create a new one
     * @return the logger
     */
    private static Logger getLogger() {
        if (logger == null) {
            try {
                new WhiteboardLogger();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return logger;
    }

    /**
     * Log the information
     * @param msg the message to be logged
     */
    public static void info(String msg) {
        getLogger().info(msg);
    }

    /**
     * Log the error
     * @param msg the message to be logged
     */
    public static void error(String msg) {
        getLogger().severe(msg);
    }


}
