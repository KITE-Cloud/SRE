package view.popUps;


import controller.Controller;
import view.RuleEvaluationUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jannik Geyer on 02.08.2017.
 */
public class DialogParameterInput extends JDialog implements ActionListener{


    HashMap<String, String> userSelection;
    ArrayList<JTextField> argumentInputFields = new ArrayList<>();
    boolean valid = false;
    RuleEvaluationUI parent;

    public DialogParameterInput(JFrame owner, HashMap<String, String> variableList) {
        super(owner,"Input Dialog", true);

        this.userSelection = variableList;

        this.setLayout(new BorderLayout());

        initComponents();


        this.setLocationRelativeTo(null);
        this.pack();
        this.setVisible(true);
    }

    private void initComponents() {

        JPanel parameterPanelContainer = new JPanel();
        parameterPanelContainer.setLayout(new GridLayout(2,1));

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(userSelection.size(), 2));

        for (Map.Entry<String, String> entry : userSelection.entrySet()) {

            JLabel argumentLabel = new JLabel(entry.getKey());
            JTextField inputField = new JTextField();
            inputField.setName(entry.getKey());
            if(entry.getValue().equals("true")){
                argumentLabel.setText(argumentLabel.getText() + "*");
                inputField.setName(inputField.getName() + "*");
                System.out.println("entry = " + entry.getValue());
            }





            argumentInputFields.add(inputField);

            inputPanel.add(argumentLabel);
            inputPanel.add(inputField);
        }

        JPanel infoPanel = new JPanel(new FlowLayout());


        parameterPanelContainer.add(inputPanel);
        parameterPanelContainer.add(new JLabel("* = Manadatory Field"));

        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton backButton = new JButton("Back");
        backButton.addActionListener(this);

        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(this);

        buttonPanel.add(backButton);
        buttonPanel.add(submitButton);

        this.add(parameterPanelContainer, BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.SOUTH);
    }


    @Override
    public void actionPerformed(ActionEvent e) {

        if(e.getActionCommand() == "Submit"){

            HashMap<String,String> userInputs = new HashMap<>();
            boolean mandatoryFieldsFilled = true;
            for (JTextField argumentInputField : argumentInputFields) {
                String text = argumentInputField.getText();
                Controller.printToLogger(getClass().getName(), "Text in variable: " + text, 0);
                if(argumentInputField.getName().contains("*")){
                    if(text.equals(""))mandatoryFieldsFilled = false;
                }
                if(!text.equals("")){
                    userInputs.put(argumentInputField.getName().replace("*", ""), text);
                }
            }

            if(mandatoryFieldsFilled){

                valid = true;
                userSelection = userInputs;
                this.setVisible(false);
            }

        }
        if(e.getActionCommand() == "Back"){
            this.dispose();
        }

    }

    public HashMap<String,String> getResults() {
        return userSelection;
    }
}
