package ca.ak.QMhacks;

import static ca.ak.QMhacks.StartupHook.LOGGING_PREFIX;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import eu.qualimaster.adaptation.events.AdaptationEvent;
import eu.qualimaster.coordination.commands.CoordinationCommand;
import eu.qualimaster.events.EventHandler;
import eu.qualimaster.events.EventManager;
import eu.qualimaster.infrastructure.PipelineLifecycleEvent;

/**
 * Custom {@link EventHandler}s for different QM events.
 */
public class CustomEventHandlers {
    
    private static final Logger LOGGER = LogManager.getLogger(CustomEventHandlers.class);
    
    private static class CustomAdaptationEventHandler extends EventHandler<AdaptationEvent> {
        
        public CustomAdaptationEventHandler() {
            super(AdaptationEvent.class);
        }
        
        @Override
        protected void handle(AdaptationEvent event) {
            LOGGER.info(LOGGING_PREFIX + "Got AdaptationEvent: " + event.getClass().getSimpleName());
        }
        
    }
    
    private static class CustomCoordinationCommandHandler extends EventHandler<CoordinationCommand> {
        
        public CustomCoordinationCommandHandler() {
            super(CoordinationCommand.class);
        }

        @Override
        protected void handle(CoordinationCommand event) {
            LOGGER.info(LOGGING_PREFIX + "Got CoordinationCommand: " + event.getClass().getSimpleName());
        }
        
    }
    
    private static class CustomPipelineLifecycleEventHandler extends EventHandler<PipelineLifecycleEvent> {
        
        private Map<String, PipelineLifecycleEvent.Status> pipelines;
        
        public CustomPipelineLifecycleEventHandler() {
            super(PipelineLifecycleEvent.class);
            
            this.pipelines = new HashMap<>();
        }
        
        @Override
        protected void handle(PipelineLifecycleEvent event) {
            String name = event.getPipeline();
            if (name != null) {
                pipelines.put(name, event.getStatus());
                
                LOGGER.info(LOGGING_PREFIX + "Pipelines status: " + pipelines);
            }
            
            switch (event.getStatus()) {
            
            /*
             * Startup sequence
             */
            
            case CHECKING:
                ConfigurationExtraktion.INSTANCE.begin();
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
                ConfigurationExtraktion.INSTANCE.end();
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
        EventManager.register(new CustomCoordinationCommandHandler());
        EventManager.register(new CustomPipelineLifecycleEventHandler());
    }

}
