package de.thm.utililities;

import de.thm.container.Node;
import de.thm.interfaces.SREMainClient;
import de.thm.interfaces.SREObserver;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Diese Klasse realisiert das Observer Pattern.
 * Sie bietet Funktionen zum registrieren, entfernen und benachtigen von SREMainClient und SREObserver Objekten.
 */
public class Updater {
    private static Updater instance = new Updater();

    public static Updater getInstance() {
        return instance;
    }

    private Updater() {
        observerList = new ArrayList<>();
    }

    private ArrayList<SREObserver> observerList;
    private SREMainClient mainClient;


    public void addMainClient(SREMainClient mainClient){
        this.mainClient = mainClient;
    }

    public void addLogObserver(SREObserver observer){

        if(!observerList.contains(observer))
            observerList.add(observer);

    }

    public void removeLogObserver(SREObserver observer){

        observerList.remove(observer);

    }

    public void clearLogObserverList(){

        observerList.removeAll(observerList);

    }

    public void sendLogInfo(String log){
        DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
        Date dateobj = new Date();

        log = "[LOG] " + df.format(dateobj) + " - " + log;

        String finalLog = log;
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (SREObserver sreObserver : observerList) {
                    sreObserver.receiveLogStrings(finalLog);
                }
            }
        }).start();
    }

    public void sendEvaluatedRule(Node evaluatedRule){

        new Thread(new Runnable() {
            @Override
            public void run() {
                for (SREObserver sreObserver : observerList) {
                    sreObserver.receiveEvaluatedRule(evaluatedRule);
                }
            }
        }).start();


    }

    public HashMap<String,String> sendVariableList(HashMap<String,String> variableList) throws Exception {

        HashMap<String, String> userInputList = mainClient.receiveInputData(variableList);

       // Controller.printToLogger(getClass().getName(), userInputList.toString(), 0);
        for (String value : userInputList.values()) {
            if(value.equals("true")) {
                throw new Exception("Invalid map structure");
            }
        }

        return userInputList;
    }

}
