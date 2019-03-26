package ca.ak.QMhacks;

import static ca.ak.QMhacks.StartupHook.LOGGING_PREFIX;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import eu.qualimaster.adaptation.events.AdaptationEvent;
import eu.qualimaster.events.EventHandler;
import eu.qualimaster.events.EventManager;
import eu.qualimaster.infrastructure.PipelineLifecycleEvent;

/**
 * Custom {@link EventHandler}s for different QM events.
 */
public class CustomEventHandlers {
    
    private static final Logger LOGGER = LogManager.getLogger(CustomEventHandlers.class);
    
    private static class CustomAdaptationEventHandler extends EventHandler<AdaptationEvent> {
        
        protected CustomAdaptationEventHandler() {
            super(AdaptationEvent.class);
        }
        
        @Override
        protected void handle(AdaptationEvent event) {
            
        }
        
    }
    
    private static class CustomPipelineLifecycleEventHandler extends EventHandler<PipelineLifecycleEvent> {
        
        private Map<String, PipelineLifecycleEvent.Status> pipelines;
        
        protected CustomPipelineLifecycleEventHandler() {
            super(PipelineLifecycleEvent.class);
            
            this.pipelines = new HashMap<>();
        }
        
        @Override
        protected void handle(PipelineLifecycleEvent event) {
            String name = event.getMainPipeline();
            if (name != null) {
                pipelines.put(name, event.getStatus());
                
                LOGGER.info(LOGGING_PREFIX + "Pipelines status: " + pipelines);
            }
            
            switch (event.getStatus()) {
            
            /*
             * Startup sequence
             */
            
            case CHECKING:
                break;
                
            case CHECKED:
                break;
                
            case STARTING:
                break;
                
            case CREATED:
                break;
                
            case INITIALIZED:
                break;
                
            /*
             * Running
             */
                
            case STARTED:
                break;
                
            /*
             * Shutdown sequence
             */
                
            case STOPPING:
                break;
                
            case STOPPED:
                break;
                
            /*
             * Special cases
             */
                
            case DISAPPEARED:
                break;
                
            case UNKNOWN:
                break;
            }
        }
        
    }
    
    /**
     * Registers the custom event handlers. Typically called by {@link StartupHook}.
     */
    public static void initialize() {
        LOGGER.info(LOGGING_PREFIX + "Registering custom EventHandlers...");
        
        EventManager.register(new CustomAdaptationEventHandler());
        EventManager.register(new CustomPipelineLifecycleEventHandler());
    }

}
