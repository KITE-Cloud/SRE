package de.thm.container;

/**
 * Created by Jannik Geyer on 26.07.2017.
 */
public class ClassNode extends Node{

    private String individualName;
    private String datatype;

    public String getIndividualName() {
        return individualName;
    }

    public void setIndividualName(String individualName) {
        this.individualName = individualName;
    }

    public String getDatatype() {
        return datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }
}
