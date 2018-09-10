package de.thm.container;

import org.semanticweb.owlapi.model.OWLDatatype;

/**
 * Created by Jannik Geyer on 26.07.2017.
 */
public class DataPropertyNode extends Node{

    private String individualName;
    private OWLDatatype owlDatatype;

    public String getIndividualName() {
        return individualName;
    }

    public void setIndividualName(String individualName) {
        this.individualName = individualName;
    }

    public OWLDatatype getOwlDatatype() {
        return owlDatatype;
    }

    public void setOwlDatatype(OWLDatatype owlDatatype) {
        this.owlDatatype = owlDatatype;
    }
}
