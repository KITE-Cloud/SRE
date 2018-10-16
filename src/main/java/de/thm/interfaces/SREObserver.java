package de.thm.interfaces;

import de.thm.container.Node;

import java.util.Observer;

/**
 * Interface das die funktionalität von SRE Observern definiert. Diese sind in der Lage log Informationen und die evaluierte Regel zu erhalten.
 */
public interface SREObserver{

    void receiveLogStrings(String log);

    void receiveEvaluatedRule(Node evaluatedRule);



}
