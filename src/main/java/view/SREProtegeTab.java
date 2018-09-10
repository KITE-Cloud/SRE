package view;

import controller.Controller;
import org.protege.editor.owl.ui.OWLWorkspaceViewsTab;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Jannik Geyer on 25.10.2017.
 */
public class SREProtegeTab extends OWLWorkspaceViewsTab{

    private static final Logger log = LoggerFactory.getLogger(SREProtegeTab.class);

    public SREProtegeTab() {
        setToolTipText("Single Rule Evaluation");
    }

    @Override
    public void initialise() {
        super.initialise();
        Controller.printToLogger(getClass().getName(), "Tab created", 0);
    }

    @Override
    public void dispose() {
        super.dispose();
        Controller.printToLogger(getClass().getName(), "Tab disposed", 0);
    }
}
