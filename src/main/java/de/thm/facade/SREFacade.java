package de.thm.facade;

import de.thm.exceptions.OWLUtilExeption;
import de.thm.interfaces.SREMainClient;
import de.thm.utililities.OWLUtil;
import de.thm.engine.EvalEngine;
import de.thm.utililities.Updater;
import de.thm.interfaces.SREObserver;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

/**
 * Die SREFacade Klasse bildet den zentralen Einstiegspunkt der SRE dar. Sie definiert alle Funktionen,
 * welche von Nutzern der API ausgeführt werden können und versteckt die komplexität des Systems.
 */
public class SREFacade {

    OWLUtil owlUtil = null;
    EvalEngine evalEngine = null;

/*    public OWLUtil initializeSRE(OWLOntology mainOntology){
        owlUtil = new OWLUtil(mainOntology);
        return owlUtil;
    }

    public OWLUtil initializeSRE(OWLOntology mainOntology, OWLOntology... importOntologies){
        owlUtil = new OWLUtil(mainOntology, importOntologies);
        return owlUtil;
    }*/

    public OWLUtil initializeSRE(OWLReasoner customReasoner, OWLOntology mainOntology, OWLOntology... importOntologies){
        owlUtil = new OWLUtil(customReasoner, mainOntology, importOntologies);
        return owlUtil;
    }

    public void executeEvaluation(String ruleName) throws Exception {

        if(owlUtil != null) {
            if(evalEngine == null)
                evalEngine = new EvalEngine(owlUtil, ruleName);
            else
                evalEngine.setSWRLRule(ruleName);
            evalEngine.execute();
        }else throw new OWLUtilExeption("No OWLUtil instance available.");

    }

    public void registerAsLogObserver(SREObserver observer){
        Updater updater = Updater.getInstance();
        updater.addLogObserver(observer);
    }

    public void registerAsMainClient(SREMainClient mainClient){
        Updater updater = Updater.getInstance();
        updater.addMainClient(mainClient);
    }


}
