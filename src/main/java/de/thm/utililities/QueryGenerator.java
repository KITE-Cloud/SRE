package de.thm.utililities;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.SWRLClassAtom;
import org.semanticweb.owlapi.model.SWRLDataPropertyAtom;
import org.semanticweb.owlapi.model.SWRLObjectPropertyAtom;
import org.swrlapi.core.SWRLAPIBuiltInAtom;

/**
 * Klasse zum erstellen von SRE SQWRL Queries
 */
public class QueryGenerator {


    public static String generateClassQueryText(SWRLClassAtom classAtom, String individualName){
        String query = "";
        query += ((OWLEntity)classAtom.getPredicate()).getIRI().getShortForm() + "(" + individualName + ") -> sqwrl:select(true)";

        Updater.getInstance().sendLogInfo("Query: " + query);

        return query;
    }


    public static String generateObjectPropertyQueryText(SWRLObjectPropertyAtom singleAtom, String origin, String target) {
        String query = "";

        query += ((OWLEntity)singleAtom.getPredicate()).getIRI().getShortForm() + "(" + origin + ", " + target + ") -> sqwrl:select("+ target +")";

        Updater.getInstance().sendLogInfo("Query: " + query);

        return query;
    }

    public static String generateDataPropertyQueryText(SWRLDataPropertyAtom singleAtom, String origin, String target) {
        String query = "";

        query += ((OWLEntity)singleAtom.getPredicate()).getIRI().getShortForm() + "(" + origin + ", " + target + ") -> sqwrl:select("+ target +")";

        Updater.getInstance().sendLogInfo("Query: " + query);

        return query;
    }


    public static String generateUnboundedBuiltInQueryText(SWRLAPIBuiltInAtom singleAtom, String parameters, String returnValue) {

        String query = "";

        query += singleAtom.getBuiltInPrefixedName() + "(" + parameters + ") -> sqwrl:select(" + returnValue+ ")";

        Updater.getInstance().sendLogInfo("Query: " + query);

        return query;

    }

    public static String generateBoundedBuiltInQueryText(SWRLAPIBuiltInAtom singleAtom, String parameters) {

        String query = "";

        query += singleAtom.getBuiltInPrefixedName() + "(" + parameters + ") -> sqwrl:select(true)";

        Updater.getInstance().sendLogInfo("Query: " + query);

        return query;

    }
}
