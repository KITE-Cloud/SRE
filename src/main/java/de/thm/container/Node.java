package de.thm.container;

import org.semanticweb.owlapi.model.SWRLAtom;

import java.util.ArrayList;

/**
 * Klasse, zur Realisierung einer verketten Liste
 */
public class Node {

    Boolean result;
    SWRLAtom singleAtom;
    Node parentNode;
    ArrayList<Node> childNodes = new ArrayList<Node>();
    String concludedParameter;

    public Node(SWRLAtom singleAtom){
        this.singleAtom = singleAtom;
        this.result = false;
    }
    public Node(){
        this.result = false;
    }

    public String getConcludedParameter() {
        return concludedParameter;
    }

    public void setConcludedParameter(String concludedParameter) {
        this.concludedParameter = concludedParameter;
    }

    public Boolean getResult() {
        return result;
    }

    public void setResult(Boolean result) {
        this.result = result;
    }

    public SWRLAtom getSingleAtom() {
        return singleAtom;
    }

    public void setSingleAtom(SWRLAtom singleAtom) {
        this.singleAtom = singleAtom;
    }

    public Node getParentNode() {
        return parentNode;
    }

    public void setParentNode(Node parentNode) {
        this.parentNode = parentNode;
        parentNode.addChildNode(this);
    }

    public void setChildNodes(ArrayList<Node> childNodes) {
        this.childNodes = childNodes;
    }

    public ArrayList<Node> getChildNodes() {
        return childNodes;
    }

    public void addChildNode(Node childNode){
        if(this.childNodes == null) this.childNodes = new ArrayList<>();
        childNode.parentNode = this;
        this.childNodes.add(childNode);
    }

}
