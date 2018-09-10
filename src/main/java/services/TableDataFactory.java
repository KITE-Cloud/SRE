package services;


import de.thm.container.Node;
import org.semanticweb.owlapi.model.*;
import org.swrlapi.core.SWRLAPIRule;
import org.swrlapi.core.SWRLRuleRenderer;
import org.swrlapi.exceptions.SWRLBuiltInException;
import org.swrlapi.factory.DefaultSWRLAPIRule;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Jannik Geyer on 02.08.2017.
 */
public class TableDataFactory {
    private SWRLRuleRenderer ruleRenderer;
    public ArrayList<ArrayList<String>> getDataList(Node swrlNode, SWRLRuleRenderer ruleRenderer){

        this.ruleRenderer = ruleRenderer;
        ArrayList<ArrayList<String>> data = new ArrayList<>();

        if(swrlNode instanceof Node){
            Node node = (Node) swrlNode;

            data.add(getRow("Node Type", getNodeType(node.getSingleAtom())));

            String renderedAtom = getRenderedAtom(node.getSingleAtom());
            data.add(getRow("Atom", renderedAtom));

            String shortenedPredicate = "";
            if(node.getSingleAtom() instanceof SWRLBuiltInAtom){
                IRI predicateofAtom = (IRI)node.getSingleAtom().getPredicate();
                shortenedPredicate = predicateofAtom.getShortForm();
            }else{
                OWLEntity predicateOfAtom = (OWLEntity) node.getSingleAtom().getPredicate();
                shortenedPredicate = predicateOfAtom.getIRI().getShortForm();
            }
            data.add(getRow("Predicate", shortenedPredicate));

            Collection<SWRLArgument> allArguments = node.getSingleAtom().getAllArguments();
            int index = 0;
            for (SWRLArgument arg : allArguments) {
                if(arg instanceof SWRLVariable){
                    data.add(getRow("Argument " + ++index, ((SWRLVariable) arg).getIRI().getShortForm()));
                }else{
                    data.add(getRow("Argument " + ++index, arg.toString()));
                }
            }

            data.add(getRow("Result", node.getResult().toString()));

            String concludedParameter = node.getConcludedParameter();
            data.add(getRow("Concluded",concludedParameter));

            Node parentNode = node.getParentNode();
            data.add(getRow("Parent Node",getRenderedAtom(parentNode.getSingleAtom())));

            ArrayList<Node> childNodes = node.getChildNodes();
            index = 0;
            for (Node childNode : childNodes) {
                data.add(getRow("Child " + ++index,getRenderedAtom(childNode.getSingleAtom())));
            }

        }

        return data;

    }

    private String getNodeType(SWRLAtom swrlAtom) {
        if(swrlAtom instanceof SWRLClassAtom)return "Class Node";
        else if(swrlAtom instanceof SWRLDataPropertyAtom) return "Data Property Node";
        else if(swrlAtom instanceof SWRLObjectPropertyAtom) return "Object Property Node";
        else if(swrlAtom instanceof SWRLBuiltInAtom)return "Built In Node";
        else return "Unknown";
    }

    static int tempRuleName = 10000;
    private String getRenderedAtom(SWRLAtom swrlAtom){
        if(swrlAtom != null) {
            SWRLAPIRule rule = null;
            try {
                ArrayList<SWRLAtom> list  =new ArrayList<>();
                list.add(swrlAtom);
                rule = new DefaultSWRLAPIRule("" + tempRuleName++, list, new ArrayList<>(), "", false);
            } catch (SWRLBuiltInException e) {
                e.printStackTrace();
            }

            String s = ruleRenderer.renderSWRLRule(rule);

            return s;
        }else{
            return "Single Rule Evaluation";
        }

    }


    private ArrayList<String> getRow(String arg1, String arg2){
        ArrayList<String> row = new ArrayList<>();
        row.add(arg1);
        row.add(arg2);

        return row;
    }
}
