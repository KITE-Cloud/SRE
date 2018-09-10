package de.thm.utililities;

import de.thm.container.*;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.swrlapi.core.SWRLAPIBuiltInAtom;

import java.util.List;

/**
 * Diese Klasse implementiert das Factory pattern, in dem es die Erstellung von Node Objekten übernimmt. Diese Node Objekte bilden die verkettete Liste der evaluierten Atome
 */
public class NodeFactory {

    public static ClassNode genTrueClassNode(Node evaluatedRuleRootNode, SWRLAtom singleAtom, String individualName, String markIndividual){
        ClassNode evaluatedNode = new ClassNode();

        evaluatedNode.setResult(true);
        evaluatedNode.setParentNode(evaluatedRuleRootNode);
        evaluatedNode.setSingleAtom(singleAtom);
        evaluatedNode.setIndividualName(individualName);
        evaluatedNode.setDatatype(markIndividual);
        evaluatedNode.setConcludedParameter(individualName);

        return evaluatedNode;
    }

    public static DataPropertyNode gentrueDataPropertyNode(Node evaluatedRuleRootNode, SWRLAtom singleAtom, String originIndividualName, String literal, OWLDatatype datatype){
        DataPropertyNode evaluatedNode = new DataPropertyNode();

        evaluatedNode.setResult(true);
        evaluatedNode.setParentNode(evaluatedRuleRootNode);
        evaluatedNode.setSingleAtom(singleAtom);

        evaluatedNode.setIndividualName(originIndividualName);
        evaluatedNode.setConcludedParameter(literal);
        evaluatedNode.setOwlDatatype(datatype);

        return evaluatedNode;
    }

    public static ObjectPropertyNode genTrueObjectPropertyNode(Node evaluatedRuleRootNode, SWRLAtom singleAtom, String originIndividualName, String concludedParameter){
        ObjectPropertyNode evaluatedNode = new ObjectPropertyNode();

        evaluatedNode.setResult(true);
        evaluatedNode.setParentNode(evaluatedRuleRootNode);
        evaluatedNode.setSingleAtom(singleAtom);

        evaluatedNode.setIndividualName(originIndividualName);
        evaluatedNode.setConcludedParameter(concludedParameter);

        return evaluatedNode;
    }

    public static BoundedBuiltInNode genTrueBoundedBuiltInNode(Node evaluatedRuleRootNode, SWRLAtom singleAtom, String parameterOne, String parameterTwo) {
        BoundedBuiltInNode evaluatedNode = new BoundedBuiltInNode();

        evaluatedNode.setResult(true);
        evaluatedNode.setParentNode(evaluatedRuleRootNode);
        evaluatedNode.setSingleAtom(singleAtom);
        evaluatedNode.setBuiltInName(((SWRLAPIBuiltInAtom) singleAtom).getBuiltInPrefixedName());
        evaluatedNode.setParameterOne(parameterOne);
        evaluatedNode.setParameterTwo(parameterTwo);
        evaluatedNode.setConcludedParameter("true");

        return evaluatedNode;
    }

    public static UnboundedBuiltInNode genTrueUnboundedBuiltInNode(Node evaluatedRuleRootNode, SWRLAtom singleAtom, List<String> queryParameterList, String concludedParam) {
        UnboundedBuiltInNode evaluatedNode = new UnboundedBuiltInNode();

        evaluatedNode.setResult(true);
        evaluatedNode.setParentNode(evaluatedRuleRootNode);
        evaluatedNode.setSingleAtom(singleAtom);
        evaluatedNode.setBuiltInName(((SWRLAPIBuiltInAtom) singleAtom).getBuiltInPrefixedName());
        evaluatedNode.setParameterList(queryParameterList);
        evaluatedNode.setConcludedParameter(concludedParam);

        return evaluatedNode;
    }

    public static void genFalseNode(Node evaluatedRuleRootNode, SWRLAtom singleAtom) {
        Node node = new Node();

        node.setResult(false);
        node.setParentNode(evaluatedRuleRootNode);
        node.setSingleAtom(singleAtom);
        node.setConcludedParameter("null");
    }
}
