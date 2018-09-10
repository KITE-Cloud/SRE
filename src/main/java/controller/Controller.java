package controller;

import de.thm.container.Node;
import de.thm.facade.SREFacade;
import de.thm.interfaces.SREMainClient;
import de.thm.interfaces.SREObserver;
import de.thm.utililities.OWLUtil;
import model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swrlapi.core.SWRLAPIRule;
import services.TableDataFactory;
import view.RuleEvaluationUI;
import view.SREProtegeView;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Jannik Geyer on 26.10.2017.
 */
public class Controller implements SREObserver, SREMainClient {

    //private static final Logger log = LoggerFactory.getLogger(Controller.class);

    private static Controller ourInstance = new Controller();


    public static Controller getInstance() {
        return ourInstance;
    }

    private Controller() {
        model = new Model();
    }

    private final Model model;

    private SREProtegeView protegeView;
    private RuleEvaluationUI ruleEvaluationUI;

    private OWLUtil owlUtil;
    private SREFacade sreFacade;


    public void start(SREProtegeView sreProtegeView) {

        Controller.printToLogger(getClass().getName(), "System is starting", 0);

        protegeView = sreProtegeView;
        protegeView.setLayout(new BorderLayout());

        Controller.printToLogger(getClass().getName(), "Create Facade instance", 0);
        sreFacade = new SREFacade();
        owlUtil = sreFacade.initializeSRE(protegeView.getOWLModelManager().getReasoner(),protegeView.getOWLModelManager().getActiveOntology());

        Controller.printToLogger(getClass().getName(), "Create Main UI", 0);
        ruleEvaluationUI = new RuleEvaluationUI();

        Controller.printToLogger(getClass().getName(), "Add UI to the protege view", 0);
        protegeView.add(ruleEvaluationUI, BorderLayout.CENTER);

        Controller.printToLogger(getClass().getName(), "Initialize Ontology Change listener", 0);
        protegeView.getOWLModelManager().getActiveOntology().getOWLOntologyManager().addOntologyChangeListener(ruleEvaluationUI);

        Controller.printToLogger(getClass().getName(), "register as Eval Engine observer", 0);
        sreFacade.registerAsLogObserver(this);
        sreFacade.registerAsMainClient(this);
    }




    public void executeEvaluation() {
        if (model.getSelectedRule() != "" && model.getSelectedRule() != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        sreFacade.executeEvaluation(model.getSelectedRule());
                        SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    ruleEvaluationUI.disableLoadingBar();
                                }
                            }
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        }
    }


    public ArrayList<String> getSWRLRules() {
        ArrayList<String> swrlRulesAsString = new ArrayList<>();
        System.out.println("OWLUtil = " + owlUtil);

        List<SWRLAPIRule> swrlapiRules = owlUtil.getSWRLAPIRules();
        for (SWRLAPIRule swrlRule : swrlapiRules) {
            swrlRulesAsString.add(swrlRule.getRuleName() + ": " + owlUtil.getRuleRenderer().renderSWRLRule(swrlRule));
            System.out.println(swrlRule.getRuleName() + ": " + owlUtil.getRuleRenderer().renderSWRLRule(swrlRule));
        }

        return swrlRulesAsString;
    }

    public SWRLAPIRule getSWRLRuleByName(String selectedRuleNumber) {
        return owlUtil.getRuleByName(selectedRuleNumber);
    }


    //--------------------------------------------------------------------------------------------------------------
    //------------------------------------------- Observer Area ----------------------------------------------------

    @Override
    public HashMap<String, String> receiveInputData(HashMap<String, String> variableList) {

        variableList = ruleEvaluationUI.askForParameters(variableList);

        return variableList;
    }

    @Override
    public void receiveLogStrings(String log) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ruleEvaluationUI.writeToConsole(log);
            }
        });
    }

    @Override
    public void receiveEvaluatedRule(Node node) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ruleEvaluationUI.updateGraphModel(node);
            }
        });
    }


    //--------------------------------------------------------------------------------------------------------------
    //------------------------------------------- Model Interaction ------------------------------------------------

    public void updateSelectedRule(String selectedRuleNumber) {
        model.setSelectedRule(selectedRuleNumber);
    }


    public ArrayList<ArrayList<String>> getDataList(Node swrlNode) {
        if (swrlNode != null) {
            return new TableDataFactory().getDataList(swrlNode, owlUtil.getRuleRenderer());
        } else {
            return new ArrayList<>();
        }
    }

    public void disposeUIComponent() {


    }

   public static void printToLogger(String className, String message, int messageCode){
     /*    switch (messageCode){
            case 0: //info
                log.info("["+className+": "+getTimestamp()+"] - " + message);
                break;
            case 1: //warning
                log.warn("["+className+": "+getTimestamp()+"] - " + message);
                break;
            case 3: //error
                log.error("["+className+": "+getTimestamp()+"] - " + message);
                break;
        }*/
    }

    private static String getTimestamp(){
        return new SimpleDateFormat().format( new Date() );

    }

}
