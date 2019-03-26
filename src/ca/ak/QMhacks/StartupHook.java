package ca.ak.QMhacks;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * <p>
 * This class is loaded via <code>Class.forName()</code> when <code>eu.qualimaster.adaptation.platform.Main</code>
 * started up the pipeline.
 * </p>
 * <p>
 * (Note that this requires manually modifying the AdapationLayer, as upstream does not provide a hook infrastructure.)
 * </p>
 */
public class StartupHook {
    
    public static final String LOGGING_PREFIX = "CA/AK: ";
    
    private static final Logger LOGGER = LogManager.getLogger(StartupHook.class);
    
    static {
        // this block is executed when the infrastructure is up
        
        LOGGER.info(LOGGING_PREFIX + "StartupHook called");
    }
    
}
