package view.popUps;

import ca.uvic.cs.chisel.cajun.graph.node.GraphNode;
import ca.uvic.cs.chisel.cajun.graph.node.NodeCollection;
import controller.Controller;
import de.thm.container.Node;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * Created by Jannik Geyer on 02.08.2017.
 */
@SuppressWarnings("Duplicates")
public class DialogPopUp extends JDialog implements ActionListener {

    NodeCollection nodeCollection;
    JLabel nameLabel;
    JTable infoTable;
    DefaultTableModel infoTableModel;
    JButton backButton;

    public DialogPopUp(NodeCollection nodeCollection, JFrame parent) {
        super(parent, "Detail view", true);

        this.nodeCollection = nodeCollection;

        initComponents();
        fillTableModel();

        this.setLocationRelativeTo(null);
        this.pack();
        this.setVisible(true);
    }

    private void fillTableModel() {

        GraphNode firstNode = nodeCollection.getFirstNode();

        Node swrlNode = null;

        try {
            swrlNode = (Node)firstNode.getUserObject();
        } catch (Exception e){
            e.printStackTrace();
        }

        ArrayList<ArrayList<String>> data = Controller.getInstance().getDataList(swrlNode);


        for (ArrayList<String> row : data) {

            infoTableModel.addRow(row.toArray());

        }

    }

    private void initComponents() {
        this.setLayout(new BorderLayout());

        JPanel labelBar = new JPanel(new FlowLayout());

        labelBar.add(new Label("Node: "), 0 );

        nameLabel = new JLabel(nodeCollection.getFirstNode().toString());
        labelBar.add(nameLabel, 1 );


        infoTableModel = new DefaultTableModel();

        // Die Titel der Spalten setzen
        infoTableModel.setColumnIdentifiers( new Object[]{ "Attribute", "Value" });
        // Das model mit zufälligen Daten befüllen
     /*   Random random = new Random();
        for( int r = 0; r < 10; r++ ){
            Object[] row = new Object[ infoTableModel.getColumnCount() ];

            for( int c = 0; c < row.length; c++ ){
                row[c] = random.nextInt( 9 ) + 1;
            }

            infoTableModel.addRow( row );
        }*/
        infoTable = new JTable(infoTableModel);
        infoTable.setPreferredSize(new Dimension(500,300));
        JScrollPane scrollPaneForTable = new JScrollPane(infoTable);
        scrollPaneForTable.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);


        JPanel buttonBar = new JPanel(new FlowLayout());

        backButton = new JButton("Back");
        backButton.addActionListener(this);

        buttonBar.add(backButton);


        this.add(labelBar, BorderLayout.NORTH);
        this.add(infoTable, BorderLayout.CENTER);
        this.add(buttonBar, BorderLayout.SOUTH);

    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if(e.getSource() == backButton){
            this.dispose();
        }

    }
}
