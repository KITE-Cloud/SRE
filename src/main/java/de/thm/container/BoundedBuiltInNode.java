package de.thm.container;

/**
 * Created by Jannik Geyer on 27.07.2017.
 */
public class BoundedBuiltInNode extends Node {

    String parameterOne;
    String parameterTwo;
    String builtInName;

    public String getParameterOne() {
        return parameterOne;
    }

    public void setParameterOne(String parameterOne) {
        this.parameterOne = parameterOne;
    }

    public String getParameterTwo() {
        return parameterTwo;
    }

    public void setParameterTwo(String parameterTwo) {
        this.parameterTwo = parameterTwo;
    }

    public String getBuiltInName() {
        return builtInName;
    }

    public void setBuiltInName(String builtInName) {
        this.builtInName = builtInName;
    }
}
