package services;

import ca.uvic.cs.chisel.cajun.graph.DefaultGraphModel;
import ca.uvic.cs.chisel.cajun.graph.node.GraphNode;
import de.thm.container.Node;
import org.semanticweb.owlapi.model.*;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Jannik Geyer on 01.08.2017.
 */
public class GraphModelAdapter {
    GraphNode rootNode;
    DefaultGraphModel graphModel;
    public DefaultGraphModel getGraphModel(Node evaluatedNodeElement){
        graphModel = new DefaultGraphModel();

        rootNode = graphModel.addNode(new Node(), "Single Rule Evaluation", null, "sre_start_node");

        ArrayList<Node> childNodes = evaluatedNodeElement.getChildNodes();

        for (Node childNode : childNodes) {
            buildTree(childNode, rootNode);
        }


        return graphModel;
    }
    int i = 0;
    private void buildTree(Node childNode, GraphNode rootGraphNode) {


        SWRLPredicate predicate = childNode.getSingleAtom().getPredicate();
        IRI iri;
        if(!(predicate instanceof IRI)){
            iri = ((OWLEntity)predicate).getIRI();
        }else{
            iri = (IRI) predicate;
        }
        GraphNode childGraphNode = null;

        if(childNode.getResult() == true){
            childGraphNode = graphModel.addNode(childNode, iri.getShortForm() + " --> " + childNode.getConcludedParameter(), null , getNodeTypeClass(childNode.getSingleAtom()));
        }else{
            childGraphNode = graphModel.addNode(childNode, iri.getShortForm() + " --> " + childNode.getResult(), null , "faulty_node");
        }

        graphModel.addArc(i++, rootGraphNode, childGraphNode , getArcTypeClass(childNode.getSingleAtom()));

        ArrayList<Node> childNodesList = childNode.getChildNodes();
        for (Node node : childNodesList) {
            buildTree(node, childGraphNode);
        }
    }


    private Object getNodeTypeClass(SWRLAtom atom) {
        if(atom instanceof SWRLClassAtom){
            return "individual_node";
        }else if(atom instanceof SWRLDataPropertyAtom){
            return "data_property_node";
        }else if(atom instanceof SWRLObjectPropertyAtom){
            return "object_property_node";
        }else if(atom instanceof SWRLBuiltInAtom){
            return "builtin_node";
        }else{
            return "undefined_node";
        }

    }

    public Collection<Object> getNodeTypes() {
        ArrayList<Object> types = new ArrayList(2);
        types.add("individual_node");
        types.add("data_property_node");
        types.add("object_property_node");
        types.add("builtin_node");
        types.add("undefined_node");
        return types;
    }


    private Object getArcTypeClass(SWRLAtom connectedToAtom) {

        if(connectedToAtom instanceof SWRLClassAtom){
            return "has_class_instance";
        }else if(connectedToAtom instanceof SWRLDataPropertyAtom){
            return "has_data_property";
        }else if(connectedToAtom instanceof SWRLObjectPropertyAtom){
            return "has_object_property";
        }else if(connectedToAtom instanceof SWRLBuiltInAtom){
            return "has_builtin";
        }else{
            return "undefined_arc_type";
        }

    }

    public Collection<Object> getArcTypeList() {
        ArrayList<Object> types = new ArrayList();
        types.add("has_data_property");
        types.add("has_object_property");
        types.add("has_builtin");
        types.add("has_class_instance");
        types.add("undefined_arc_type");
        return types;
    }

}
