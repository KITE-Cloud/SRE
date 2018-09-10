package view;

import addOns.ExportImageAction;
import ca.uvic.cs.chisel.cajun.actions.LayoutAction;
import ca.uvic.cs.chisel.cajun.graph.DefaultGraphModel;
import ca.uvic.cs.chisel.cajun.graph.FlatGraph;
import ca.uvic.cs.chisel.cajun.graph.arc.DefaultGraphArcStyle;
import ca.uvic.cs.chisel.cajun.graph.node.DefaultGraphNodeStyle;
import ca.uvic.cs.chisel.cajun.graph.node.GraphNodeCollectionEvent;
import ca.uvic.cs.chisel.cajun.graph.node.GraphNodeCollectionListener;
import ca.uvic.cs.chisel.cajun.graph.node.NodeCollection;
import ca.uvic.cs.chisel.cajun.graph.ui.DefaultFlatGraphView;
import controller.Constants;
import controller.Controller;
import de.thm.container.Node;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.GraphModelAdapter;
import view.popUps.DialogParameterInput;
import view.popUps.DialogPopUp;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Jannik Geyer on 25.10.2017.
 */
public class RuleEvaluationUI extends JPanel implements ActionListener, GraphNodeCollectionListener, OWLOntologyChangeListener {

    private static final Logger log = LoggerFactory.getLogger(RuleEvaluationUI.class);

    private JPanel ruleBar;
    private JComboBox<String> ruleBox;
    private JLabel loadingAnimation;
    private DefaultFlatGraphView cajunView;
    private FlatGraph cajunGraph;
    private LayoutAction preferedLayoutAction;
    private JPanel liveConsolePanel;
    private JTextArea liveConsole;
    private boolean consoleAdded;
    private boolean evaluating;

    public RuleEvaluationUI() {

        System.out.println("vorher setLayout");
        this.setLayout(new BorderLayout());

        System.out.println("vorher initializeRuleBar");
        initializeRuleBar();
        System.out.println("vorher initializeCajunGraph");
        initializeCajunGraph();
        System.out.println("vorher initializeConsoleArea");
        initializeConsoleArea();
        System.out.println("vorher extendToolbar");
        extendToolbar();
    }

    private void initializeRuleBar() {

        ruleBar = new JPanel(new FlowLayout());
        ruleBox = new JComboBox<String>();
        fillRuleBox();
        ruleBox.setPreferredSize(new Dimension(1000, (int) ruleBox.getPreferredSize().getHeight()));
        ruleBox.setActionCommand(Constants.AC_RULE_BOX);
        ruleBox.addActionListener(this);

        JButton evaluateRuleButton = new JButton("Evaluate Rule");
        evaluateRuleButton.setActionCommand(Constants.AC_EVALUATE_RULE);
        evaluateRuleButton.addActionListener(this);

        JButton liveConsoleBtn = new JButton("Live Console");
        liveConsoleBtn.setActionCommand(Constants.AC_LIVE_CONSOLE);
        liveConsoleBtn.addActionListener(this);

        MediaTracker mediaTracker = new MediaTracker(this);
        URL resource = this.getClass().getResource("/LoadGif.gif");
        Image icon = new ImageIcon(resource).getImage();
        mediaTracker.addImage(icon, 1);
        loadingAnimation = new JLabel(new ImageIcon(icon));
        //loadingAnimation.setVisible(false);
        loadingAnimation.setEnabled(false);

        try {
            mediaTracker.waitForAll();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ruleBar.add(ruleBox, 0);
        ruleBar.add(evaluateRuleButton, 1);
        ruleBar.add(liveConsoleBtn, 2);
        ruleBar.add(loadingAnimation, 3);
        ruleBar.repaint();

        this.add(ruleBar, BorderLayout.NORTH);
    }

    public void fillRuleBox() {
        ArrayList<String> swrlRules = Controller.getInstance().getSWRLRules();
        ruleBox.removeActionListener(this);

        ActionListener[] actionListeners = ruleBox.getActionListeners();
        for (ActionListener actionListener : actionListeners) {
            ruleBox.removeActionListener(actionListener);
        }

        if (ruleBox.getItemCount() > 0) ruleBox.removeAllItems();
        for (String renderedRule : swrlRules) {
            ruleBox.addItem(renderedRule);
        }
        ruleBox.addActionListener(this);
        ruleBox.repaint();
    }


    private void initializeCajunGraph() {
        updateGraphModel(new Node());
        cajunView = new DefaultFlatGraphView(cajunGraph);

        this.add(cajunView, BorderLayout.CENTER);
    }

    @SuppressWarnings("Duplicates")
    public void updateGraphModel(Node evaluatedRootNode){

        evaluating = false;

        GraphModelAdapter modelAdapter = new GraphModelAdapter();
        DefaultGraphModel graphModel = modelAdapter.getGraphModel(evaluatedRootNode);
        if(this.cajunGraph == null) {
            //  SampleGraphModel model = new SampleGraphModel();
            this.cajunGraph = new FlatGraph(graphModel);
            this.preferedLayoutAction = cajunGraph.getLayout("Tree - Vertical");
            // color the nodes based on node type
            DefaultGraphNodeStyle nodeStyle = new DefaultGraphNodeStyle();
            nodeStyle.setNodeTypes(modelAdapter.getNodeTypes());
            this.cajunGraph.setGraphNodeStyle(nodeStyle);
            // color the arcs based on arc type
            DefaultGraphArcStyle arcStyle = new DefaultGraphArcStyle();
            arcStyle.setArcTypes(modelAdapter.getArcTypeList());
            this.cajunGraph.setGraphArcStyle(arcStyle);


            cajunGraph.addNodeSelectionListener(this);

            cajunGraph.performLayout(preferedLayoutAction);
        }else{
            this.cajunGraph.setModel(graphModel);

            cajunGraph.performLayout(preferedLayoutAction);
        }
    }


    private void initializeConsoleArea() {

        liveConsolePanel = new JPanel(new BorderLayout());

        liveConsolePanel.add(new JLabel("Live Console: "), BorderLayout.NORTH);

        liveConsole = new JTextArea();
        liveConsole.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(liveConsole);
        scrollPane.setPreferredSize(new Dimension(800, 200));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        liveConsolePanel.add(scrollPane, BorderLayout.CENTER);

        this.add(liveConsolePanel,BorderLayout.SOUTH);

        consoleAdded = true;
    }

    private void extendToolbar() {
        JToolBar toolBar = cajunView.getToolBar();
        JFrame mainWindow = (JFrame)SwingUtilities.windowForComponent(this);
        toolBar.add(new ExportImageAction(mainWindow, cajunGraph.getCanvas()));
    }


    @Override
    public void actionPerformed(ActionEvent e) {

        System.out.println("Actioncommand: " + e.getActionCommand());

        if (e.getActionCommand() == Constants.AC_RULE_BOX) {
            JComboBox<String> ruleBox = (JComboBox<String>) e.getSource();

            String selectedItem = (String) ruleBox.getSelectedItem();

            String selectedRuleNumber = selectedItem.substring(0, selectedItem.indexOf(":"));

            Controller.getInstance().updateSelectedRule(selectedRuleNumber);
        }

        if (e.getActionCommand() == Constants.AC_EVALUATE_RULE) {
            evaluating = true;
            Controller.getInstance().executeEvaluation();

        }

        if (e.getActionCommand() == Constants.AC_LIVE_CONSOLE) {
            if(consoleAdded){
                this.remove(this.liveConsolePanel);
                consoleAdded = false;
            }
            else {
                this.add(this.liveConsolePanel, BorderLayout.SOUTH);
                consoleAdded = true;
            }
            this.repaint();
        }

    }

    @Override
    public void collectionChanged(GraphNodeCollectionEvent event) {

        NodeCollection nodeCollection = event.getNodeCollection();
        System.out.println(nodeCollection);

        if (!nodeCollection.isEmpty()) {
            if(nodeCollection.getFirstNode().toString() != "Single Rule Evaluation")
                new DialogPopUp(nodeCollection, (JFrame)SwingUtilities.windowForComponent(this));
        }

    }

    public HashMap<String, String> askForParameters(HashMap<String, String> variableList) {

        DialogParameterInput parameterInput = new DialogParameterInput((JFrame)SwingUtilities.windowForComponent(this), variableList);

        if(parameterInput != null) {
            variableList = parameterInput.getResults();
            loadingAnimation.setEnabled(true);
            return variableList;
        }else return null;


    }

    public void writeToConsole(String log) {
        liveConsole.append(log + "\n");
        liveConsole.repaint();
    }

    public void disableLoadingBar(){
        loadingAnimation.setEnabled(false);
        loadingAnimation.repaint();
    }


    @Override
    public void ontologiesChanged(@Nonnull List<? extends OWLOntologyChange> changes) throws OWLException {
        if(!evaluating)
            fillRuleBox();
    }
}
