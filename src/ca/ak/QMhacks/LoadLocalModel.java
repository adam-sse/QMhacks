package ca.ak.QMhacks;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import eu.qualimaster.adaptation.platform.ToolBase;
import eu.qualimaster.coordination.CoordinationConfiguration;
import eu.qualimaster.coordination.RepositoryConnector;
import eu.qualimaster.coordination.RepositoryConnector.Models;
import eu.qualimaster.coordination.RepositoryConnector.Phase;
import eu.qualimaster.easy.extension.internal.PipelineHelper;
import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.model.AbstractVariable;
import net.ssehub.easy.varModel.model.ModelQueryException;
import net.ssehub.easy.varModel.model.values.ReferenceValue;
import net.ssehub.easy.varModel.model.values.Value;

/**
 * A simple program that loads a local QM pipeline model for manual inspection with Java code. 
 */
public class LoadLocalModel {

    @SuppressWarnings("unused")
    private static final Logger LOGGER = LogManager.getLogger(LoadLocalModel.class);

    public static void main(String[] args) throws ReflectiveOperationException {
        // call ToolBase.configureLogging via reflection (since it isn't public)
        Method configureLogging = ToolBase.class.getDeclaredMethod("configureLogging");
        configureLogging.setAccessible(true);
        configureLogging.invoke(null);
        
        if (args.length != 1) {
            System.err.println("Pass the path to the local model as the only argument");
            System.exit(1);
        }
        
        Properties props = new Properties();
        props.setProperty("repository.confModel.local", args[0]);
        CoordinationConfiguration.configure(props);
        
        RepositoryConnector.initialize();
        
        //test();
        christian();
    }
    
    private static void christian() {
        ConfigurationExtraktion.INSTANCE.begin();
    }
    
    private static void test() {
        System.out.println("######################################################");
        Models models = RepositoryConnector.getModels(Phase.ADAPTATION);

        Configuration config = models.getConfiguration();
        
        try {
            printConfigInfo(config, "PipelineVar_2");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printConfigInfo(Configuration configuration, String pipelineName)
            throws IOException {
        
        
        
//        for (IDecisionVariable decision : configuration) {
//            System.err.println(decision.getQualifiedName());
//            System.err.println(decision.getDeclaration().getName());
//        }
        
        try {
            System.out.println(configuration.getDecision("activePipelines", true));
            
            IDecisionVariable var = configuration.getDecision("PipelineVar_2", true);
            System.out.println(var);
            System.out.println(var.getNestedElement("executors").getValue());
            System.out.println(var.getValue());
            System.out.println(var.getQualifiedName());
            System.out.println(var.getDeclaration().getName());
           
        } catch (ModelQueryException e) {
            e.printStackTrace();
        }
        
        IDecisionVariable pip = PipelineHelper.obtainPipelineByName(configuration, "SwitchPip");
        System.out.println("CA: Pipeline obtained");
        int count = pip.getNestedElementsCount();
        System.out.println("CA: NestedElementsCount: " + count);
        System.out.println("CA: AttributesCount: " + pip.getAttributesCount());
//        for (int i = 0; i < count; i++) {
//            IDecisionVariable var = pip.getNestedElement(i);
//            System.out.println("Pip: " + var.getDeclaration().getName() + var.getValue());
//        }
        
        test2(pip, configuration);
    }
    
    private static void test2(IDecisionVariable pip, Configuration config) {
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        
        IDecisionVariable sources = pip.getNestedElement("sources");
        System.out.println(sources);
        
        IDecisionVariable i0 = sources.getNestedElement(0);
        Value value = i0.getValue();
        
        AbstractVariable var = ((ReferenceValue) value).getValue();
        IDecisionVariable sourceNode = config.getDecision(var);
        
//        AbstractVariable testVar = pip.getDeclaration();
//        Project project = testVar.getProject();
//        
//        Project project2 = config.getProject();
        
        System.out.println(sourceNode);
        
        
    }
    
}
