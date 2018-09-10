package de.thm.utililities;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.OntologyCopy;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.swrlapi.core.IRIResolver;
import org.swrlapi.core.SWRLAPIRule;
import org.swrlapi.core.SWRLRuleEngine;
import org.swrlapi.core.SWRLRuleRenderer;
import org.swrlapi.exceptions.SWRLRuleException;
import org.swrlapi.factory.SWRLAPIFactory;
import org.swrlapi.factory.SWRLAPIInternalFactory;
import org.swrlapi.sqwrl.SQWRLQueryEngine;
import java.io.*;
import java.util.*;


/**
 * Die OWLUtil ist eine Hilfsklasse, welche den Zugriff auf eine OWLOntologie erleichtert.
 */
public class OWLUtil {


    OWLDataFactory dataFactory;
    OWLReasoner reasoner;
    PrefixManager prefixManager;
    OWLOntologyManager ontologyManager;
    OWLOntology owlOntology;


    //private static de.thm.engine.OWLUtil instance = new de.thm.engine.OWLUtil();

    /*public static de.thm.engine.OWLUtil getInstance(){
        return instance;
    }*/


    public void saveOntology(OWLOntology ontology) {
        final RDFXMLDocumentFormat format = new RDFXMLDocumentFormat();
        format.setPrefixManager(prefixManager);

        String filePath = System.getProperty("user.dir");


            File output = new File(filePath + "/src/main/resources/save/test.owl");

            IRI documentIRI = IRI.create(output.toURI());
            // save in owl/XML format

            try {


                ontologyManager.saveOntology(ontology, new OWLXMLDocumentFormat(), documentIRI);


            } catch (OWLOntologyStorageException e) {
                e.printStackTrace();
            }

    }

/*    public OWLUtil(OWLOntology mainOntology){
        ontologyManager = create(mainOntology, null);
        //ontologyManager = create();
        //owlOntology = mainOntology;
        owlOntology = ontologyManager.getOntology(mainOntology.getOntologyID());

        reasoner = PelletReasonerFactory.getInstance().createReasoner(owlOntology);
        prefixManager = new DefaultPrefixManager(null,null,owlOntology.getOntologyID().getOntologyIRI().get().toString() + "#");
        saveOntology(owlOntology);
        this.flushReasoner();

        System.out.println("IRI == " + owlOntology.getOntologyID().getOntologyIRI().get().toString() + "#");
    }

    public OWLUtil(OWLOntology mainOntology, OWLOntology[] importOntologies){
        ontologyManager = create(mainOntology, importOntologies);
        //ontologyManager = create();
        //owlOntology = mainOntology;
        owlOntology = ontologyManager.getOntology(mainOntology.getOntologyID());

        reasoner = PelletReasonerFactory.getInstance().createReasoner(owlOntology);
        prefixManager = new DefaultPrefixManager(null,null,owlOntology.getOntologyID().getOntologyIRI().get().toString() + "#");
        saveOntology(owlOntology);
        this.flushReasoner();

        System.out.println("IRI == " + owlOntology.getOntologyID().getOntologyIRI().get().toString() + "#");
    }*/

    public OWLUtil(OWLReasoner customReasoner, OWLOntology mainOntology, OWLOntology... importOntologies){
        ontologyManager = create(mainOntology, importOntologies);
        owlOntology = ontologyManager.getOntology(mainOntology.getOntologyID());
        reasoner = customReasoner;
        prefixManager = new DefaultPrefixManager(null,null,owlOntology.getOntologyID().getOntologyIRI().get().toString() + "#");

        this.flushReasoner();

        System.out.println("IRI == " + owlOntology.getOntologyID().getOntologyIRI().get().toString() + "#");
    }

    public OWLOntologyManager create() {
        OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        dataFactory = m.getOWLDataFactory();
        return m;
    }

     public OWLOntologyManager create(OWLOntology mainOntology, OWLOntology[] importOntologies) {
        OWLOntologyManager m = OWLManager.createOWLOntologyManager();

        try {
            OWLOntology mainOntologyCopy = null;
            InputStream inputStream;
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            try {
                if(importOntologies != null)
                for (OWLOntology importOntology : importOntologies) {
                    importOntology.saveOntology(outputStream);
                    inputStream = new ByteArrayInputStream(outputStream.toByteArray());
                    m.loadOntologyFromOntologyDocument(inputStream);
                }

               // mainOntologyCopy = m.copyOntology(mainOntology, OntologyCopy.DEEP);
                mainOntology.saveOntology(outputStream);
                inputStream = new ByteArrayInputStream(outputStream.toByteArray());
                m.loadOntologyFromOntologyDocument(inputStream);
                mainOntologyCopy = m.getOntology(mainOntology.getOntologyID());
            } catch (OWLOntologyStorageException e) {
                e.printStackTrace();
            }

            Set<OWLImportsDeclaration> importsDeclarations = mainOntologyCopy.getImportsDeclarations();

            for (OWLImportsDeclaration importsDeclaration : importsDeclarations) {
                m.applyChange(new AddImport(mainOntologyCopy, importsDeclaration));
            }

        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
        dataFactory = m.getOWLDataFactory();
        return m;
    }



    /**
     * This function creates one Named Individual and directly binds it to an existing Class.
     *
     * @param individualName
     * @param connectedClass
     */
    public void addNamedIndividual(String individualName, String connectedClass){

        OWLClass correspondingClass = dataFactory.getOWLClass(connectedClass, prefixManager);
        OWLNamedIndividual namedIndividual = dataFactory.getOWLNamedIndividual(individualName, prefixManager);

        OWLClassAssertionAxiom assertionAxiom = dataFactory.getOWLClassAssertionAxiom(correspondingClass, namedIndividual);

        ontologyManager.addAxiom(owlOntology, assertionAxiom);

    }

    public SWRLRuleRenderer getRuleRenderer(){

        IRIResolver iriResolver = SWRLAPIFactory.createIRIResolver(prefixManager.getDefaultPrefix());
        SWRLRuleRenderer swrlRuleRenderer = SWRLAPIInternalFactory.createSWRLRuleRenderer(owlOntology, iriResolver);

        return swrlRuleRenderer;

    }

    public List<SWRLAPIRule> getSWRLAPIRules(){

        Set<SWRLAPIRule> swrlRules = getSWRLRuleEngine().getSWRLRules();

        return new ArrayList<SWRLAPIRule>(swrlRules);
    }

    public void addObjectPropertyToOntology(String propertyName){

        OWLObjectProperty top = dataFactory.getOWLObjectProperty("owl:topObjectProperty",prefixManager);
        OWLObjectProperty sub = dataFactory.getOWLObjectProperty(propertyName, prefixManager);

        OWLSubObjectPropertyOfAxiom owlSubObjectPropertyOfAxiom = dataFactory.getOWLSubObjectPropertyOfAxiom(sub, top );

        ontologyManager.addAxiom(owlOntology, owlSubObjectPropertyOfAxiom);

    }

    public void addDataPropertyToOntology(String propertyName){

        OWLDataProperty top = dataFactory.getOWLDataProperty("owl:topDataProperty",prefixManager);
        OWLDataProperty sub = dataFactory.getOWLDataProperty(propertyName, prefixManager);

        OWLSubDataPropertyOfAxiom owlSubObjectPropertyOfAxiom = dataFactory.getOWLSubDataPropertyOfAxiom(sub, top);

        ontologyManager.addAxiom(owlOntology, owlSubObjectPropertyOfAxiom);

    }

    public Set<OWLLiteral> getInferredDataPropertyValues(String namedIndividualString) {

        OWLNamedIndividual individual = dataFactory.getOWLNamedIndividual(namedIndividualString, prefixManager);
        OWLDataProperty owlDataProperty = dataFactory.getOWLDataProperty(Constants.SREDATAPROPERTY, prefixManager);
        Set<OWLLiteral> dataPropertyValues = reasoner.getDataPropertyValues(individual, owlDataProperty);

        return dataPropertyValues;

    }

    public NodeSet<OWLNamedIndividual> getInferredObjectPropertyValues(String namedIndividualString) {

        OWLNamedIndividual individual = dataFactory.getOWLNamedIndividual(namedIndividualString, prefixManager);
        OWLObjectProperty owlObjectProperty = dataFactory.getOWLObjectProperty(Constants.SREOBJECTPROPERTY, prefixManager);
        NodeSet<OWLNamedIndividual> objectPropertyValues = reasoner.getObjectPropertyValues(individual, owlObjectProperty);
        saveOntology();

        return objectPropertyValues;

    }

    public void removeSRERule(){
        getSWRLRuleEngine().deleteSWRLRule(Constants.SRERULENAME);
    }

    public void flushReasoner(){
        reasoner.flush();
    }

    public boolean checkExistenceOfIndividual(OWLIndividual individual){

        Set<OWLNamedIndividual> individualsInSignature = owlOntology.getIndividualsInSignature();

        for (OWLIndividual owlIndividual : individualsInSignature) {
            if(owlIndividual.equals(individual)) return true;
        }

        return false;
    }

    public static void main(String[] args) {

    }

    public OWLNamedIndividual getIndividualByName(String individualName){

        OWLNamedIndividual individual = dataFactory.getOWLNamedIndividual(individualName,prefixManager);

        return individual;
    }



    public SQWRLQueryEngine getSQWRLQueryEngine(){
        IRIResolver iriResolver = SWRLAPIFactory.createIRIResolver(prefixManager.getDefaultPrefix());

        SQWRLQueryEngine sqwrlQueryEngine = SWRLAPIFactory.createSQWRLQueryEngine(owlOntology,iriResolver);

        return sqwrlQueryEngine;
    }


    public SWRLRuleEngine getSWRLRuleEngine (){
        IRIResolver iriResolver = SWRLAPIFactory.createIRIResolver(prefixManager.getDefaultPrefix());
        SWRLRuleEngine ruleEngine = SWRLAPIFactory.createSWRLRuleEngine(owlOntology, iriResolver);

        return ruleEngine;
    }

    public SWRLAPIRule getRuleByName(String name){

        Optional<SWRLAPIRule> swrlRule = null;
        try {
            swrlRule = getSQWRLQueryEngine().getSWRLRule(name);
        } catch (SWRLRuleException e) {
            e.printStackTrace();
        }

        return swrlRule.get();
    }


    public void saveOntology() {
        File output = new File("resources/Test.owl");
        //File output = new File("main/resources/ULTRA.owl");

        IRI documentIRI = IRI.create(output.toURI());
        // save in owl/XML format

        try {
            ontologyManager.saveOntology(owlOntology, new OWLXMLDocumentFormat(), documentIRI);
        } catch (OWLOntologyStorageException e) {
            e.printStackTrace();
        }
    }



}
