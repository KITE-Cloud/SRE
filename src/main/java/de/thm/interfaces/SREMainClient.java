package de.thm.interfaces;

import java.util.HashMap;

/**
 * Durch die implementierung des SREMainClient Interfaces is eine Klasse bereichtigt, die Variablen Liste von der EvalEngine entgegen zu nehmen und zu modifizieren.
 */
public interface SREMainClient {

    HashMap<String, String> receiveInputData(HashMap<String,String> variableList);

}
