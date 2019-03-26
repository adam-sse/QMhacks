package ca.ak.QMhacks;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import eu.qualimaster.coordination.RepositoryConnector;
import eu.qualimaster.coordination.RepositoryConnector.Phase;
import eu.qualimaster.easy.extension.internal.PipelineHelper;
import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.model.values.ReferenceValue;
import net.ssehub.easy.varModel.model.values.ValueDoesNotMatchTypeException;

public class ConfigurationExtraktion implements Runnable {

    private static final Logger LOGGER = LogManager.getLogger(ConfigurationExtraktion.class);
    public static boolean isRunning = false;
    
    public void extractData() {
        Configuration configuration = RepositoryConnector.getModels(Phase.MONITORING).getConfiguration();
//        Configuration config2 = RepositoryConnector.getModels(Phase.ADAPTATION).getConfiguration();
//        IDecisionVariable pip = PipelineHelper.obtainPipelineByName(config2, "SwitchPip");
//        try {
//            pip.getNestedElement("hosts").getValue().setValue(new Integer(2));
//        } catch (ValueDoesNotMatchTypeException e1) {
//            // TODO Auto-generated catch block
//            e1.printStackTrace();
//        }
//        
        LOGGER.error("CA: Received Configuration!");
        
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("/tmp/Configuration_Log.txt", true));
            printConfigInfo(configuration, "SwitchPip", writer);
            writer.append("\n\n\n ########################################################################### \n\n\n");
            writer.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
    
    private void printConfigInfo(Configuration configuration, String pipelineName, BufferedWriter writer) throws IOException, ValueDoesNotMatchTypeException {
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
                LOGGER.error("CA: Node: " + destination.getQualifiedName());
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
        LOGGER.error("CA: " + nestedElement + ": " + pip.getNestedElement(nestedElement).getValue());
    }
    
    private IDecisionVariable nestedElement2IDecisionVariable(IDecisionVariable nestedElement, Configuration configuration) {
        return configuration.getDecision(((ReferenceValue)nestedElement.getValue()).getValue());
    }

    @Override
    public void run() {
        isRunning = true;
        long timestamp = System.currentTimeMillis();
        while (true) {
            if (System.currentTimeMillis() - timestamp > 10000) {
                timestamp = System.currentTimeMillis();
                extractData();
            }
        }
    }
}
