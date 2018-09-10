package view;

import controller.Controller;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;


/**
 * Created by Jannik Geyer on 25.10.2017.
 */
public class SREProtegeView extends AbstractOWLViewComponent{

   // private static final Logger log = LoggerFactory.getLogger(SREProtegeView.class);

    protected void initialiseOWLView() throws Exception {
        Controller.printToLogger(getClass().getName(), "initialise OWL View", 0);
        Controller.getInstance().start(this);
        Controller.printToLogger(getClass().getName(), "View created", 0);
    }

    protected void disposeOWLView() {
        Controller.getInstance().disposeUIComponent();
    }

}
