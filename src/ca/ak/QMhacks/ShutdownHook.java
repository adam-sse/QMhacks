package ca.ak.QMhacks;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * <p>
 * This class is loaded via <code>Class.forName()</code> before <code>eu.qualimaster.adaptation.platform.Main</code>
 * shuts down the pipeline.
 * </p>
 * <p>
 * Note that this requires manually modifying the AdapationLayer, as upstream does not provide a hook infrastructure.
 * Do something like:
 * <code><pre>
 *   try {
 *       Class.forName("ca.ak.QMhacks.ShutdownHook");
 *   } catch (Exception e) {
 *       LOGGER.error("Can't load ca.ak.QMhacks.ShutdownHook", e);
 *   }
 * </pre></code>
 * in the appropriate place in AdaptionLayer.
 * </p>
 */
public class ShutdownHook {
    
    public static final String LOGGING_PREFIX = "CA/AK: ";
    
    private static final Logger LOGGER = LogManager.getLogger(ShutdownHook.class);
    
    static {
        // this block is executed when the infrastructure is up
        
        LOGGER.info(LOGGING_PREFIX + "ShutdownHook called");
    }
    
}
