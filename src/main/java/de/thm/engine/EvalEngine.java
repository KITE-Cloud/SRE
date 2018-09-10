package de.thm.engine;


import de.thm.container.*;
import de.thm.exceptions.MissingInputException;
import de.thm.utililities.*;
import org.semanticweb.owlapi.model.*;
import org.swrlapi.builtins.arguments.SWRLBuiltInArgument;
import org.swrlapi.builtins.arguments.SWRLLiteralBuiltInArgument;
import org.swrlapi.builtins.arguments.SWRLVariableBuiltInArgument;
import org.swrlapi.core.SWRLAPIBuiltInAtom;
import org.swrlapi.core.SWRLAPIRule;
import org.swrlapi.parser.SWRLParseException;
import org.swrlapi.sqwrl.SQWRLQueryEngine;
import org.swrlapi.sqwrl.SQWRLResult;
import org.swrlapi.sqwrl.exceptions.SQWRLException;
import org.swrlapi.sqwrl.values.SQWRLLiteralResultValue;
import org.swrlapi.sqwrl.values.SQWRLNamedIndividualResultValue;
import org.swrlapi.sqwrl.values.SQWRLResultValue;

import java.util.*;

import static de.thm.utililities.Constants.MARK_INDIVIDUAL;
import static de.thm.utililities.Constants.SRERULENAME;

/**
 * Created by Jannik Geyer on 17.07.2017.
 */
public class EvalEngine {

    private SQWRLQueryEngine queryEngine;
    SWRLAPIRule swrlRule;
    HashMap<String, String> variableList;
    HashMap<String, ArrayList<String>> masterVariableList;

    Node ruleRootElement;
    Node evaluatedRuleRootNode;

    OWLUtil owlUtil;

    Scanner scanner;


    //-----------------------------------------------------------------------------------------------------------------
    //-------------------------------------------- Konstruktoren ------------------------------------------------------


    public void setSWRLRule(String ruleName){
        this.swrlRule = owlUtil.getRuleByName(ruleName);
        this.ruleRootElement = new Node();
        this.ruleRootElement.setResult(true);
        this.evaluatedRuleRootNode = new Node();
        this.evaluatedRuleRootNode.setResult(true);
        this.masterVariableList = null;
    }

    public EvalEngine(OWLUtil owlUtil, String ruleName) {
        this.owlUtil = owlUtil;
        this.queryEngine = owlUtil.getSQWRLQueryEngine();
        this.swrlRule = owlUtil.getRuleByName(ruleName);

        this.ruleRootElement = new Node();
        this.ruleRootElement.setResult(true);
        this.evaluatedRuleRootNode = new Node();
        this.evaluatedRuleRootNode.setResult(true);

        this.scanner = new Scanner(System.in);
    }

    //-----------------------------------------------------------------------------------------------------------------
    //-------------------------------------------- Execute EvalEngine -------------------------------------------------


    public void execute() throws Exception {

        if(owlUtil != null && swrlRule != null) {

            Updater.getInstance().sendLogInfo("EvalEngine: Start execution");
            Updater.getInstance().sendLogInfo("Prepare EvalEngine and Ontology for execution");
            this.prepareOntology();

            Updater.getInstance().sendLogInfo("Generate Ruleslist");
            this.generateRulesList();

            ArrayList<Node> childNodes = ruleRootElement.getChildNodes();
            HashMap<String, String> inputList = new HashMap<>();
            for (Node rootNode : childNodes) {
                inputList.putAll(this.generateUserQueryListFromRuleTree(rootNode, true));
            }
            Updater.getInstance().sendLogInfo("Ask for User input");
            variableList = Updater.getInstance().sendVariableList(inputList);
            for (Map.Entry<String, String> varListEntry : variableList.entrySet()) {
                addEntryToMasterlist(varListEntry.getKey(),varListEntry.getValue());
            }

            if (variableList != null) {
                prepareVariableList(variableList, inputList);
                Updater.getInstance().sendLogInfo("Start Evaluation");
                //this.generateQuery();
                long startTime = System.currentTimeMillis();
                this.runEvaluation(this.ruleRootElement, this.evaluatedRuleRootNode, this.variableList);
                long stopTime = System.currentTimeMillis();
                long elapsedTime = stopTime - startTime;
                Updater.getInstance().sendLogInfo("Finished Evaluation. Finished in " + elapsedTime + " ms");
                System.out.println("Finished: " + elapsedTime + " - ms");

                Updater.getInstance().sendEvaluatedRule(evaluatedRuleRootNode);
            }
        }
    }

    private void prepareVariableList(HashMap<String, String> variableList, HashMap<String, String> inputList) throws MissingInputException {
        ArrayList<String> toDelete = new ArrayList<>();
        for (Map.Entry<String, String> entry : variableList.entrySet()) {
            if(entry.getValue().equals("") || entry.getValue().equals(null))toDelete.add(entry.getKey());
        }
        for (String key : toDelete) {
            variableList.remove(key);
        }
    }


    //-----------------------------------------------------------------------------------------------------------------
    //-------------------------------------------- Kern Funktionen ----------------------------------------------------



    /**
     * Ensures that the engine can properly query the used ontology.
     * Erstellt eine Kopie der übergebenen Ontologie und bereitet sie darauf vor Anfragen anzunehmen und zu bearbeiten.
     */
    private void prepareOntology() {
        this.owlUtil.addNamedIndividual(Constants.SREINDIVIDUAL, "owl:Thing");
        this.owlUtil.addObjectPropertyToOntology(Constants.SREOBJECTPROPERTY);
        this.owlUtil.addDataPropertyToOntology(Constants.SREDATAPROPERTY);
    }

    /**
     * Diese Funktion hat die Aufgabe, eine verkettete Liste zu generieren.
     * Die Liste verbindet die logisch zusammengehörigen atomaren Bestandteile einer SWRL Regel.
     *
     * Beispiel:
     *
     * Person(?p)
     * --> hasDriverLicence(?p, ?d)
     * ----> issuedStateOf(?d, "NY")
     * --> hasAge(?p, ?a)
     */
    private void generateRulesList() {

        List<SWRLAtom> bodyAtoms = new ArrayList<>(swrlRule.getBody());

        Set<SWRLAtom> rootAtoms = new HashSet<>();

        for (SWRLAtom bodyAtom : bodyAtoms) {
            List<SWRLAtom> rootAtom = findRootAtom(bodyAtoms, bodyAtom);
            for (SWRLAtom swrlAtom : rootAtom) {
                rootAtoms.add(swrlAtom);
            }
        }

        for (SWRLAtom rootAtom : rootAtoms) {
            Node rootNode = new Node(rootAtom);
            this.ruleRootElement.addChildNode(rootNode);

            findChildAtoms(bodyAtoms, rootNode);
        }
    }

    /**
     * Die Aufgabe der Funktion ist es SWRL Atome zu finden, welche keine Abhängigkeiten zu anderen SWRL Atomen haben.
     *
     * Info: Rekursiver Ansatz
     *
     * @param searchSpace
     * @param rootAtom
     * @return
     */
    private List<SWRLAtom> findRootAtom(List<SWRLAtom> searchSpace, SWRLAtom rootAtom) {

        List<SWRLIArgument> rootArguments = new ArrayList<>();
        if (rootAtom instanceof SWRLClassAtom) {
            rootArguments.add(((SWRLClassAtom) rootAtom).getArgument());
        }

        if (rootAtom instanceof SWRLDataPropertyAtom || rootAtom instanceof SWRLObjectPropertyAtom) {

            List<SWRLArgument> collect = (List<SWRLArgument>) rootAtom.getAllArguments();

            rootArguments.add((SWRLIArgument) collect.get(0));
        }


        if (rootAtom instanceof SWRLAPIBuiltInAtom) {

            List<SWRLBuiltInArgument> builtInArguments = ((SWRLAPIBuiltInAtom) rootAtom).getBuiltInArguments();

            for (SWRLBuiltInArgument builtInArgument : builtInArguments) {
                if (builtInArgument.isVariable()) {

                    if (((SWRLVariableBuiltInArgument) builtInArgument).isBound()) {
                        rootArguments.add((SWRLIArgument) builtInArgument);
                    }
                    

                }
            }

        }
        SWRLIArgument childArgument;
        List<SWRLAtom> returnList = new ArrayList<>();
        for (SWRLAtom swrlAtom : searchSpace) {
            childArgument = null;
            if (swrlAtom != rootAtom) {
                if (swrlAtom instanceof SWRLClassAtom) {
                    childArgument = ((SWRLClassAtom) swrlAtom).getArgument();
                }

                if (swrlAtom instanceof SWRLDataPropertyAtom || swrlAtom instanceof SWRLObjectPropertyAtom) {

                    List<SWRLArgument> collect = (List<SWRLArgument>) swrlAtom.getAllArguments();
                    childArgument = (SWRLIArgument) collect.get(1);
                }

                if (swrlAtom instanceof SWRLAPIBuiltInAtom) {
                    List<SWRLBuiltInArgument> builtInArguments = ((SWRLAPIBuiltInAtom) swrlAtom).getBuiltInArguments();

                    SWRLBuiltInArgument firstArgument = builtInArguments.get(0);
                    if (firstArgument instanceof SWRLVariableBuiltInArgument ) {
                        if(((SWRLVariableBuiltInArgument) firstArgument).isUnbound())
                            childArgument = (SWRLIArgument) firstArgument;
                    }
                }


                for (SWRLIArgument rootArgument : rootArguments) {
                    if(childArgument != null)
                        if (((SWRLVariable) childArgument).getIRI().equals(((SWRLVariable) rootArgument).getIRI())) {
                            returnList.addAll(findRootAtom(searchSpace, swrlAtom));
                        }
                }

            }

        }
        if (!returnList.isEmpty()) {
            return returnList;
        }


        returnList.add(rootAtom);
        return returnList;
    }

    /**
     * Die Funktion sucht verknüpft ein Root Atom mit einem Kind Atom.
     *
     * Info: Rekursiver Ansatz
     *
     * Beispiel:
     * RootAtom: Person(?p) -- ConnectTo --> ChildAtom: hasDriverLicence(?p, ?d)
     *
     * @param searchSpace
     * @param rootNode
     */
    private void findChildAtoms(List<SWRLAtom> searchSpace, Node rootNode) {

        SWRLAtom rootAtom = rootNode.getSingleAtom();

        SWRLVariable argument = null;

        if (rootAtom instanceof SWRLClassAtom) {
            argument = (SWRLVariable) ((SWRLClassAtom) rootAtom).getArgument();
        }

        if (rootAtom instanceof SWRLAPIBuiltInAtom) {
            SWRLBuiltInArgument inArgument = ((SWRLAPIBuiltInAtom) rootAtom).getBuiltInArguments().get(0);
            if (inArgument.isVariable()) {
                if (!((SWRLVariableBuiltInArgument) inArgument).isBound()) {

                    argument = (SWRLVariable) inArgument;

                }
            }

        }

        if (rootAtom instanceof SWRLDataPropertyAtom || rootAtom instanceof SWRLObjectPropertyAtom) {


            List<SWRLArgument> collect = (List<SWRLArgument>) rootAtom.getAllArguments();

            argument = (SWRLVariable) collect.get(1);
        }

        if (argument != null) {
            for (SWRLAtom swrlAtom : searchSpace) {

                if (swrlAtom != rootAtom) {

                    SWRLVariable childVariable = null;
                    List<SWRLVariable> buildInVariables = new ArrayList<>();
                    if (swrlAtom instanceof SWRLClassAtom) {
                        childVariable = (SWRLVariable) ((SWRLClassAtom) swrlAtom).getArgument();
                    } else if (swrlAtom instanceof SWRLAPIBuiltInAtom) {
                        if (!(rootAtom instanceof SWRLObjectPropertyAtom)) {
                            List<SWRLArgument> collect = (List<SWRLArgument>) swrlAtom.getAllArguments();
                            buildInVariables = new ArrayList<>();

                            SWRLBuiltInArgument inArgument = ((SWRLAPIBuiltInAtom) swrlAtom).getBuiltInArguments().get(0);
                            if (inArgument.isVariable()) {
                                if (!((SWRLVariableBuiltInArgument) inArgument).isBound()) {

                                    for (int i = 0; i < collect.size(); i++) {
                                        if (collect.get(i) instanceof SWRLVariable && i > 0) {
                                            buildInVariables.add((SWRLVariable) collect.get(i));
                                        }
                                    }
                                } else {
                                    for (SWRLArgument swrlArgument : collect) {
                                        if (swrlArgument instanceof SWRLVariable) {
                                            buildInVariables.add((SWRLVariable) swrlArgument);
                                        }
                                    }
                                }

                            } else {
                                for (SWRLArgument swrlArgument : collect) {
                                    if (swrlArgument instanceof SWRLVariable) {
                                        buildInVariables.add((SWRLVariable) swrlArgument);
                                    }
                                }
                            }
                        }
                    } else if (swrlAtom instanceof SWRLDataPropertyAtom || swrlAtom instanceof SWRLObjectPropertyAtom) {
                        if (!(rootAtom instanceof SWRLObjectPropertyAtom)) {
                            List<SWRLArgument> collect = (List<SWRLArgument>) swrlAtom.getAllArguments();
                            childVariable = (SWRLVariable) collect.get(0);
                        }
                    }

                    if (childVariable != null) {
                        if (childVariable.getIRI().equals(argument.getIRI())) {
                            Node childNode = new Node(swrlAtom);
                            rootNode.addChildNode(childNode);
                            findChildAtoms(searchSpace, childNode);
                        }
                    }
                    if (buildInVariables.size() > 0) {
                        for (SWRLVariable buildInVariable : buildInVariables) {
                            if (buildInVariable.getIRI().equals(argument.getIRI())) {
                                Node childNode = new Node(swrlAtom);
                                rootNode.addChildNode(childNode);
                                findChildAtoms(searchSpace, childNode);
                            }
                        }
                    }

                }

            }
        }
    }

    /**
     * Diese Funktion sucht alle möglichen Eingabemöglichkeiten, die der Nutzer der EvalEngine treffen kanm, zusammen und speichert diese in einer Map.
     *
     * @param node
     * @param isRootNode
     * @return
     */
    private HashMap<String, String> generateUserQueryListFromRuleTree(Node node, boolean isRootNode) {
        HashMap<String, String> variableList = new HashMap<>();

        if (node.getSingleAtom() instanceof SWRLClassAtom) {
            variableList.put("?" + ((SWRLVariable) ((SWRLClassAtom) node.getSingleAtom()).getArgument()).getIRI().getShortForm(), Boolean.toString(isRootNode));
        }
        else if(node.getSingleAtom() instanceof  SWRLDataPropertyAtom){
            variableList.put("?" + ((SWRLVariable) ((SWRLDataPropertyAtom) node.getSingleAtom()).getSecondArgument()).getIRI().getShortForm(), Boolean.toString(false));
        }

        ArrayList<Node> childNodes = node.getChildNodes();
        for (Node childNode : childNodes) {
            variableList.putAll(generateUserQueryListFromRuleTree(childNode, false));
        }


        return variableList;
    }

    /**
     * Fragt den Nutzer nach zu testenden Individuen.
     */
    public void getUserInputs() {

        System.out.println("[INFO] - User inputs required.");

        ArrayList<String> removeKeys = new ArrayList<>();

        for (Map.Entry<String, String> entry : variableList.entrySet()) {
            if (entry.getValue() == "true") {
                System.out.println("[INFO] - Mandatory field");
            }

            System.out.print("[INPUT] - " + entry.getKey() + ":  ");
            String next = scanner.nextLine();
            if (!next.equals("")) {
                variableList.put(entry.getKey(), next);
            } else {
                removeKeys.add(entry.getKey());
            }
        }

        for (String removeKey : removeKeys) {
            variableList.remove(removeKey);
        }

        masterVariableList = new HashMap<>();
        for (Map.Entry<String, String> entryVar : variableList.entrySet()) {
            addEntryToMasterlist(entryVar.getKey(), entryVar.getValue());
        }


    }

    /**
     * Kernfunktion der EvalEngine.
     * Evaluiert die entschrprechenden Knoten(SWRLAtoms) und erstellt zeitgleich eine zweite verkettete Liste, in welcher
     * Informationen zu dem Evaluierungsverlauf gespeichert werden.
     *
     * Info: Rekursiver Ansatz
     *
     * @param ruleRootElement
     * @param evaluatedRuleRootNode
     * @param variableList
     */
    @SuppressWarnings("Duplicates")
    private void runEvaluation(Node ruleRootElement, Node evaluatedRuleRootNode, HashMap<String, String> variableList) {

        ArrayList<Node> rule = ruleRootElement.getChildNodes();

        for (Node node : rule) {
            SWRLAtom singleAtom = node.getSingleAtom();                                                                 //Atom from childnode

            if (singleAtom instanceof SWRLClassAtom) {
                String individualName = "";
                List<IRI> iriList = this.getIRIsFromAtom(singleAtom);

                if (!iriList.isEmpty()) {
                    if (variableList.containsKey(getVariableListKey(iriList.get(0)))) {
                        individualName = variableList.get(getVariableListKey(iriList.get(0)));
                    }
                }

                if (individualName == "") {
                    individualName = evaluatedRuleRootNode.getConcludedParameter();
                }

                String query = QueryGenerator.generateClassQueryText((SWRLClassAtom) singleAtom, individualName);    //create the query / needed:
                SQWRLResult result = executeQuery(query);
                if (result != null) {
                    node.setResult(true);

                    ClassNode evaluatedNode = NodeFactory.genTrueClassNode(evaluatedRuleRootNode, singleAtom, individualName, MARK_INDIVIDUAL);

                    runEvaluation(node, evaluatedNode, variableList);
                } else {
                    NodeFactory.genFalseNode(evaluatedRuleRootNode, singleAtom);
                }
            }

            if (singleAtom instanceof SWRLDataPropertyAtom) {



                SWRLArgument origin = ((SWRLDataPropertyAtom) singleAtom).getFirstArgument();
                IRI originIRI = ((SWRLVariable) origin).getIRI();
                String originIndividualName;
                if (variableList.containsKey(getVariableListKey(originIRI))) {
                    originIndividualName = variableList.get(getVariableListKey(originIRI));
                } else {
                    originIndividualName = evaluatedRuleRootNode.getConcludedParameter();
                }

                SWRLArgument target = ((SWRLDataPropertyAtom) singleAtom).getSecondArgument();
                IRI targetIRI = ((SWRLVariable) target).getIRI();
                String targetIndividualName;
                if (variableList.containsKey(getVariableListKey(targetIRI))) {
                    targetIndividualName = variableList.get(getVariableListKey(targetIRI));
                } else {
                    targetIndividualName = getVariableListKey(targetIRI);
                }

                String query = QueryGenerator.generateDataPropertyQueryText((SWRLDataPropertyAtom) singleAtom, originIndividualName, targetIndividualName);

                SQWRLResult result = executeQuery(query);

                List<List<SQWRLResultValue>> rows  = getRowListFromQuery(result);

                if (!rows.isEmpty()) {
                    ArrayList<Map.Entry<String, String>> ListOfDataPropertyEntries = new ArrayList<>();
                    for (List<SQWRLResultValue> row : rows) {
                        for (SQWRLResultValue sqwrlResultValue : row) {
                            if (sqwrlResultValue instanceof SQWRLLiteralResultValue) {
                                Map.Entry entry = new AbstractMap.SimpleEntry(((SQWRLLiteralResultValue) sqwrlResultValue).getOWLLiteral().getLiteral(), ((SQWRLLiteralResultValue) sqwrlResultValue).getDatatypePrefixedName());
                                ListOfDataPropertyEntries.add(entry);
                                node.setResult(true);

                                DataPropertyNode evaluatedNode = NodeFactory.gentrueDataPropertyNode(evaluatedRuleRootNode, singleAtom, originIndividualName, ((SQWRLLiteralResultValue) sqwrlResultValue).getOWLLiteral().toString(), ((SQWRLLiteralResultValue) sqwrlResultValue).getOWLDatatype());

                                HashMap<String, String> newVariableList = new HashMap<>(variableList);
                                newVariableList.put(targetIndividualName, ((SQWRLLiteralResultValue) sqwrlResultValue).getOWLLiteral().toString());
                                addEntryToMasterlist(targetIndividualName, ((SQWRLLiteralResultValue) sqwrlResultValue).getOWLLiteral().toString());

                                runEvaluation(node, evaluatedNode, newVariableList);
                            }
                        }
                    }
                } else {
                    NodeFactory.genFalseNode(evaluatedRuleRootNode, singleAtom);
                }


            }

            if (singleAtom instanceof SWRLObjectPropertyAtom) {


                SWRLArgument origin = ((SWRLObjectPropertyAtom) singleAtom).getFirstArgument();
                IRI originIRI = ((SWRLVariable) origin).getIRI();
                String originIndividualName;
                if (variableList.containsKey(getVariableListKey(originIRI))) {
                    originIndividualName = variableList.get(getVariableListKey(originIRI));
                } else {
                    originIndividualName = evaluatedRuleRootNode.getConcludedParameter();
                }

                SWRLArgument target = ((SWRLObjectPropertyAtom) singleAtom).getSecondArgument();
                IRI targetIRI = ((SWRLVariable) target).getIRI();
                String targetIndividualName;
                if (variableList.containsKey(getVariableListKey(targetIRI))) {


                    targetIndividualName = variableList.get(getVariableListKey(targetIRI));
                } else {

                    targetIndividualName = getVariableListKey(targetIRI);
                }


                OWLNamedIndividual individualByName = owlUtil.getIndividualByName(targetIndividualName);

                    String query = QueryGenerator.generateObjectPropertyQueryText((SWRLObjectPropertyAtom) singleAtom, originIndividualName, targetIndividualName);

                    SQWRLResult result = executeQuery(query);

                    List<List<SQWRLResultValue>> rows = getRowListFromQuery(result);

                    if (!rows.isEmpty()) {
                        for (List<SQWRLResultValue> row : rows) {
                            for (SQWRLResultValue sqwrlResultValue : row) {
                                if (sqwrlResultValue instanceof SQWRLNamedIndividualResultValue) {
                                    node.setResult(true);

                                    ObjectPropertyNode evaluatedNode = NodeFactory.genTrueObjectPropertyNode(evaluatedRuleRootNode, singleAtom, originIndividualName, ((SQWRLNamedIndividualResultValue) sqwrlResultValue).getIRI().getShortForm());

                                    HashMap<String, String> newVariableList = new HashMap<>(variableList);
                                    newVariableList.put(targetIndividualName, (((SQWRLNamedIndividualResultValue) sqwrlResultValue).getIRI().getShortForm()));
                                    addEntryToMasterlist(targetIndividualName, (((SQWRLNamedIndividualResultValue) sqwrlResultValue).getIRI().getShortForm()));

                                    runEvaluation(node, evaluatedNode, newVariableList);
                                }
                            }
                        }
                    } else {
                        NodeFactory.genFalseNode(evaluatedRuleRootNode, singleAtom);
                    }

            }

            if (singleAtom instanceof SWRLAPIBuiltInAtom) {
                SWRLBuiltInArgument firstArgument = ((SWRLAPIBuiltInAtom) singleAtom).getBuiltInArguments().get(0);
                if (firstArgument.isVariable()) {
                    if (((SWRLVariableBuiltInArgument) firstArgument).isBound()) {

                        HashMap<Integer, ArrayList<String>> boundedQuery = createBoundedQuery(singleAtom, variableList);

                        if (boundedQuery.size() == 2) {
                            queries = new ArrayList<>();
                            generateBuiltInQueryList("", boundedQuery, 0);
                            System.out.println();

                            for (String query : queries) {
                                String executableQuery = QueryGenerator.generateBoundedBuiltInQueryText((SWRLAPIBuiltInAtom) singleAtom, query);

                                SQWRLResult result = executeQuery(executableQuery);
                                if (result != null) {
                                    try {
                                        if (result.next()) {
                                            node.setResult(true);
                                            BoundedBuiltInNode evaluatedNode = NodeFactory.genTrueBoundedBuiltInNode(evaluatedRuleRootNode, singleAtom, query.substring(0, query.indexOf(",")), query.substring(query.indexOf(",") - 1));

                                            runEvaluation(node, evaluatedNode, variableList);
                                        } else {
                                            NodeFactory.genFalseNode(evaluatedRuleRootNode, singleAtom);
                                        }
                                    } catch (SQWRLException e) {
                                        e.printStackTrace();
                                    }

                                } else {
                                    NodeFactory.genFalseNode(evaluatedRuleRootNode, singleAtom);
                                }

                            }
                        }
                    }
                } else if (firstArgument instanceof SWRLLiteralBuiltInArgument) {

                    HashMap<Integer, ArrayList<String>> boundedQuery = createBoundedQuery(singleAtom, variableList);

                    queries = new ArrayList<>();
                    generateBuiltInQueryList("", boundedQuery, 0);
                    System.out.println();

                    for (String query : queries) {
                        String executableQuery = QueryGenerator.generateBoundedBuiltInQueryText((SWRLAPIBuiltInAtom) singleAtom, query);


                        SQWRLResult result = executeQuery(executableQuery);
                        if (result != null) {
                            try {
                                if (result.next()) {
                                    node.setResult(true);
                                    BoundedBuiltInNode evaluatedNode = NodeFactory.genTrueBoundedBuiltInNode(evaluatedRuleRootNode, singleAtom, query.substring(0, query.indexOf(",")), query.substring(query.indexOf(",") - 1));

                                    runEvaluation(node, evaluatedNode, variableList);
                                } else {
                                    NodeFactory.genFalseNode(evaluatedRuleRootNode, singleAtom);
                                }
                            } catch (SQWRLException e) {
                                e.printStackTrace();
                            }

                        } else {
                            NodeFactory.genFalseNode(evaluatedRuleRootNode, singleAtom);
                        }
                    }
                }

                if (firstArgument.isVariable()) {
                    if (((SWRLVariableBuiltInArgument) firstArgument).isUnbound()) {
                        List<String> queryParameterList = new ArrayList<>();
                        List<String> missingParameterInPath = new ArrayList<>();

                        HashMap<Integer, String> queryParameterLists = new HashMap<>();

                        List<SWRLBuiltInArgument> builtInArguments = ((SWRLAPIBuiltInAtom) singleAtom).getBuiltInArguments();
                        int position = 0;
                        for (SWRLBuiltInArgument argument : builtInArguments) {

                            if (argument.isVariable()) {

                                if (!((SWRLVariableBuiltInArgument) argument).isBound()) {
                                    queryParameterList.add("?" + ((SWRLVariableBuiltInArgument) argument).getVariableName());
                                    queryParameterLists.put(position, "?" + ((SWRLVariableBuiltInArgument) argument).getVariableName());
                                } else {
                                    String value = variableList.get("?" + ((SWRLVariableBuiltInArgument) argument).getVariableName());
                                    if (value != null) {
                                        queryParameterList.add(value);
                                        queryParameterLists.put(position, value);
                                    } else {
                                        missingParameterInPath.add("?" + ((SWRLVariableBuiltInArgument) argument).getVariableName());
                                        queryParameterLists.put(position, "?" + ((SWRLVariableBuiltInArgument) argument).getVariableName());
                                    }
                                }

                            } else if (argument instanceof SWRLLiteralBuiltInArgument) {
                                OWLLiteral literal = ((SWRLLiteralBuiltInArgument) argument).getLiteral();
                                OWLDatatype datatype = literal.getDatatype();
                                String prefixedName = datatype.getBuiltInDatatype().getPrefixedName();
                                String parameter = "\"" + literal.getLiteral() + '"' + "^^" + prefixedName;
                                queryParameterList.add(parameter);
                                queryParameterLists.put(position, parameter);
                            }
                            position++;
                        }

                        HashMap<Integer, ArrayList<String>> params = new HashMap<>();
                        for (Map.Entry<Integer, String> entry : queryParameterLists.entrySet()) {

                            if (masterVariableList.containsKey(entry.getValue())) {
                                ArrayList<String> missingParams = masterVariableList.get(entry.getValue());
                                params.put(entry.getKey(), missingParams);
                            }

                        }



                        for (Map.Entry<Integer, String> entry : queryParameterLists.entrySet()) {
                            if (!entry.getValue().contains("?") || entry.getKey() == 0) {
                                ArrayList<String> tempList = new ArrayList<>();
                                tempList.add(entry.getValue());
                                params.put(entry.getKey(), tempList);
                            }
                        }


                        if (queryParameterList.size() != builtInArguments.size()) {
                            for (String missingParam : missingParameterInPath) {
                                if (masterVariableList.containsKey(missingParam)) {
                                    ArrayList<String> masterParamList = masterVariableList.get(missingParam);

                                    for (String masterParam : masterParamList) {

                                        queryParameterList.add(masterParam);

                                    }
                                }
                            }
                        }


                        if (params.size() == builtInArguments.size()) {
                            queries = new ArrayList<>();
                            generateBuiltInQueryList("", params, 0);
                            System.out.println();
                            for (String query : queries) {
                                String executableQuery = QueryGenerator.generateUnboundedBuiltInQueryText((SWRLAPIBuiltInAtom) singleAtom, query, params.get(0).get(0));
                                SQWRLResult result = executeQuery(executableQuery);

                                if (result != null) {
                                    List<SQWRLResultValue> row;
                                    try {
                                        result.next();
                                        row = result.getRow();
                                        SQWRLResultValue resultValue = row.get(0);
                                        variableList.put("?" + result.getColumnName(0), resultValue.asLiteralResult().toString());
                                        addEntryToMasterlist("?" + result.getColumnName(0), resultValue.asLiteralResult().toString());

                                    } catch (SQWRLException e) {
                                        e.printStackTrace();
                                    }


                                    node.setResult(true);
                                    UnboundedBuiltInNode evaluatedNode = null;
                                    try {
                                        evaluatedNode = NodeFactory.genTrueUnboundedBuiltInNode(evaluatedRuleRootNode, singleAtom, queryParameterList, variableList.get("?" + result.getColumnName(0)));
                                    } catch (SQWRLException e) {
                                        e.printStackTrace();
                                    }
                                    runEvaluation(node, evaluatedNode, variableList);
                                } else {
                                    NodeFactory.genFalseNode(evaluatedRuleRootNode, singleAtom);
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    //-----------------------------------------------------------------------------------------------------------------
    //-------------------------------------------- Hilfs Funktionen -----------------------------------------------------


    private SQWRLResult executeQuery(String query) {
        SQWRLResult result = null;
        System.out.println(query);
        try {
            result = queryEngine.runSQWRLQuery(SRERULENAME, query);
            queryEngine.deleteSWRLRule(SRERULENAME);
        } catch (SWRLParseException e) {
            e.printStackTrace();
        } catch (SQWRLException e) {
            e.printStackTrace();
        }

        return result;
    }

    private List<List<SQWRLResultValue>> getRowListFromQuery(SQWRLResult result) {

        List<List<SQWRLResultValue>> rows = new ArrayList<>();

        try {
            while (result.next()) {
                rows.add(result.getRow());
            }
        } catch (SQWRLException e) {
            e.printStackTrace();
        }

        return rows;
    }

    private HashMap<Integer, ArrayList<String>> createBoundedQuery(SWRLAtom singleAtom, HashMap<String, String> variableList) {

        Collection<SWRLArgument> arguments = singleAtom.getAllArguments();

        List<String> queries = new ArrayList<>();

        HashMap<Integer, ArrayList<String>> params = new HashMap<>();
        int position = 0;
        for (SWRLArgument arg : arguments) {

            if (arg instanceof SWRLVariableBuiltInArgument) {
                IRI iri = ((SWRLVariableBuiltInArgument) arg).getIRI();
                if (variableList.containsKey("?" + iri.getShortForm())) {
                    String pathVariable = variableList.get("?" + iri.getShortForm());
                    ArrayList<String> tempList = new ArrayList<>();
                    tempList.add(pathVariable);

                    params.put(position, tempList);
                } else {
                    String s = "?" + iri.getShortForm();
                    if (masterVariableList.containsKey("?" + iri.getShortForm())) {
                        ArrayList<String> masterVariables = masterVariableList.get("?" + iri.getShortForm());

                        params.put(position, masterVariables);
                    }
                }
            } else if (arg instanceof SWRLLiteralBuiltInArgument) {
                OWLLiteral literal = ((SWRLLiteralBuiltInArgument) arg).getLiteral();
                String literalString = literal.getLiteral();
                OWLDatatype datatype = literal.getDatatype();

                if (datatype != null) {
                    literalString = "\"" + literalString + "\"^^" + datatype.getBuiltInDatatype().getPrefixedName();
                }
                System.out.println();
                ArrayList<String> tempList = new ArrayList<>();
                tempList.add(literalString);
                params.put(position, tempList);
            }
            position++;
        }

        return params;
    }

    ArrayList<String> queries = new ArrayList<>();
    private void generateBuiltInQueryList(String query, HashMap<Integer, ArrayList<String>> paramsForQuery, int listIndex) {
        boolean queryFinished = (paramsForQuery.size() == listIndex);

        if (!queryFinished) {
            ArrayList<String> parameterList = paramsForQuery.get(listIndex++);

            for (String arg : parameterList) {
                String tempQuery = query;
                tempQuery += arg;
                if (listIndex < paramsForQuery.size()) tempQuery += ", ";
                generateBuiltInQueryList(tempQuery, paramsForQuery, listIndex);
            }
        } else {
            queries.add(query);
        }

    }

    /**
     * Hilfsklasse:
     * Erstellt einen Eintrag und schreibt ihn in die Masterliste.
     *
     * @param key
     * @param value
     */
    private void addEntryToMasterlist(String key, String value) {
        if(masterVariableList == null) masterVariableList = new HashMap<>();

        ArrayList<String> entries = masterVariableList.get(key);

        if (entries == null) {
            entries = new ArrayList<>();
        }

        entries.add(value);
        masterVariableList.put(key, entries);
    }

    /**
     * Hilfsfunktion:
     * Kovertiert eine Iri in eine ausgeschrieben SWRL Variable.
     *
     * @param originIRI
     * @return
     */
    private String getVariableListKey(IRI originIRI) {
        return "?" + originIRI.getShortForm();
    }

    /**
     * Hilfsfunktion:
     * Diese Funktion gibt alle IRIs der Argumente in einem SWRLAtom zurück.
     *
     * @param singleAtom
     * @return
     */
    private List<IRI> getIRIsFromAtom(SWRLAtom singleAtom) {

        List<SWRLArgument> arguments = new ArrayList<>();

        if (singleAtom instanceof SWRLClassAtom) {
            arguments.add(((SWRLClassAtom) singleAtom).getArgument());
        } else if (singleAtom instanceof SWRLDataPropertyAtom || singleAtom instanceof SWRLObjectPropertyAtom || singleAtom instanceof SWRLAPIBuiltInAtom) {
            arguments.addAll(singleAtom.getAllArguments());
        }

        List<IRI> iriList = new ArrayList<>();

        for (SWRLArgument argument : arguments) {
            if (argument instanceof SWRLVariable) {
                iriList.add(((SWRLVariable) argument).getIRI());
            }
        }

        return iriList;
    }

}
