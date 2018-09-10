package de.thm.container;

import java.util.List;

/**
 * Created by Jannik Geyer on 31.07.2017.
 */
public class UnboundedBuiltInNode extends Node{


    List<String> parameterList;
    String builtInName;

    public List<String> getParameterList() {
        return parameterList;
    }

    public void setParameterList(List<String> parameterList) {
        this.parameterList = parameterList;
    }

    public String getBuiltInName() {
        return builtInName;
    }

    public void setBuiltInName(String builtInName) {
        this.builtInName = builtInName;
    }
}
