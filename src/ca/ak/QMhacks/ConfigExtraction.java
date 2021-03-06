package ca.ak.QMhacks;

import static ca.ak.QMhacks.StartupHook.LOGGING_PREFIX;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import eu.qualimaster.coordination.RepositoryConnector;
import eu.qualimaster.coordination.RepositoryConnector.Models;
import eu.qualimaster.coordination.RepositoryConnector.Phase;
import eu.qualimaster.easy.extension.internal.PipelineHelper;
import eu.qualimaster.monitoring.MonitoringManager;
import net.ssehub.easy.instantiation.core.model.common.VilException;
import net.ssehub.easy.instantiation.rt.core.model.rtVil.Executor;
import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.model.ModelQueryException;
import net.ssehub.easy.varModel.model.values.ReferenceValue;
import net.ssehub.easy.varModel.model.values.ValueDoesNotMatchTypeException;

public class ConfigExtraction implements Runnable {

    public static final ConfigExtraction INSTANCE = new ConfigExtraction();
    
    private static final Logger LOGGER = LogManager.getLogger(ConfigExtraction.class);

    private boolean isRunning = false;
    
    private boolean stop = false;
    
    private ConfigExtraction() {
    }
    
    public void extractData() {
        LOGGER.error(LOGGING_PREFIX + "#####################################################");
        Models monModel = RepositoryConnector.getModels(Phase.MONITORING);
        monModel.startUsing();
        Configuration configuration = monModel.getConfiguration();

        try {
            LOGGER.error(LOGGING_PREFIX + "MONITORING!!!!");
            printConfigInfo(configuration, "SwitchPip");
        } catch (ValueDoesNotMatchTypeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        monModel.endUsing();
        
        LOGGER.error(LOGGING_PREFIX + "=====================================================");
        Models adapModel = RepositoryConnector.getModels(Phase.ADAPTATION);
        adapModel.startUsing();
        configuration = adapModel.getConfiguration();
        Executor exec = RepositoryConnector.createExecutor(adapModel.getAdaptationScript(), RepositoryConnector.createTmpFolder(), configuration, null, MonitoringManager.getSystemState().freeze());
        exec.stopAfterBindValues();
        try {
            exec.execute();
        } catch (VilException e) { // be extremely careful
            LOGGER.error(LOGGING_PREFIX + "During value binding: " + e.getMessage(), e);
        }
        
        try {
            LOGGER.error(LOGGING_PREFIX + "ADAPTATION!!!!");
            printConfigInfo(configuration, "SwitchPip");
        } catch (ValueDoesNotMatchTypeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        // Change Algorithm Experiment
        try {
            IDecisionVariable node = configuration.getDecision("PipelineVar_2_FamilyElement0", true);
            IDecisionVariable algoSet = node.getNestedElement("available");
            IDecisionVariable algo = algoSet.getNestedElement(1);
            System.err.println(algo.getValue());
            System.err.println(node.getNestedElement("actual").getValue());
            node.getNestedElement("actual").getValue().setValue(algo);
        } catch (ModelQueryException | ValueDoesNotMatchTypeException | NullPointerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        // End of Change Algorithm Experiment
        
        adapModel.endUsing();
        
        LOGGER.error(LOGGING_PREFIX + "#####################################################");
      
        
    }
    
    private void printConfigInfo(Configuration configuration, String pipelineName) throws IOException, ValueDoesNotMatchTypeException {
        // Iterate through full pipe
        
        // Start with Pipeline
        IDecisionVariable pip = PipelineHelper.obtainPipelineByName(configuration, "SwitchPip");
        printNestedElement(pip, "numworkers");
        printNestedElement(pip, "hosts");
        printNestedElement(pip, "executors");
        
        // Experiment Start
        
        // Experiment End
        
        // Determine all Sources
        List<IDecisionVariable> sources = new ArrayList<IDecisionVariable>();
        IDecisionVariable sourcesVar = pip.getNestedElement("sources");
        int amountOfSources = sourcesVar.getNestedElementsCount();
        for (int i = 0; i < amountOfSources; i++) {
            sources.add(nestedElement2IDecisionVariable(sourcesVar.getNestedElement(i), configuration));
        }
        
        // For all bolts
        List<IDecisionVariable> bolts = new ArrayList<IDecisionVariable>();
        for (IDecisionVariable source : sources) {
            visitAllSuccessors(source, configuration, bolts);
        }
        
    }
    
    private void visitAllSuccessors(IDecisionVariable node, Configuration configuration, List<IDecisionVariable> visitedNodes) {
        // Determine Flows
        IDecisionVariable flows = node.getNestedElement("output");
        int amountOfFlows = flows.getNestedElementsCount();
        // For all flows
        for (int i = 0; i < amountOfFlows; i++) {
            IDecisionVariable flow = nestedElement2IDecisionVariable(flows.getNestedElement(i), configuration);
            IDecisionVariable destination = nestedElement2IDecisionVariable(flow.getNestedElement("destination"), configuration);
            if (!visitedNodes.contains(destination)) {
                visitedNodes.add(destination);
                // Print infos
                LOGGER.error(LOGGING_PREFIX + "Node: " + destination.getQualifiedName());
                printNestedElement(destination, "executors");
                printNestedElement(destination, "parallelism");
                printNestedElement(destination, "throughputVolume");
                printNestedElement(destination, "throughputItems");
                printNestedElement(destination, "items");
                printNestedElement(destination, "capacity");
                printNestedElement(destination, "actual");

                // If it is not a sink
                if (destination.getNestedElement("output") != null) {
                    visitAllSuccessors(destination, configuration, visitedNodes);
                }
            }
        }
    }
    
    private void printNestedElement(IDecisionVariable pip, String nestedElement) {
        LOGGER.error(LOGGING_PREFIX + nestedElement + ": " + pip.getNestedElement(nestedElement).getValue());
    }
    
    private IDecisionVariable nestedElement2IDecisionVariable(IDecisionVariable nestedElement, Configuration configuration) {
        return configuration.getDecision(((ReferenceValue)nestedElement.getValue()).getValue());
    }

    public synchronized void begin() {
        if (!isRunning) {
            Thread th = new Thread(this);
            th.start();
            isRunning = true;
            LOGGER.info(LOGGING_PREFIX + "ConfigExtraction started");
        }
    }
    
    public synchronized void end() {
        if (isRunning) {
            stop = true;
            LOGGER.info(LOGGING_PREFIX + "ConfigExtraction stopping...");
        }
    }
    
    @Override
    public void run() {
        while (true) {
            synchronized (this) {
                if (stop) {
                    stop = false;
                    isRunning = false;
                    LOGGER.info(LOGGING_PREFIX + "ConfigExtraction stopped");
                    break;
                }
            }
            
            extractData();
            
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
            }
        }
    }
}
