/**
 * Copyright 1998-2006, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package addOns;

import view.RuleEvaluationUI;

import javax.swing.*;
import java.net.URL;


/**
 * Interface for storing icons used by this tool.
 * 
 * @author Sean Falconer
 */
public abstract class IconConstants {

	public static final Icon ICON_EXPORT_IMAGE = loadImageIcon(RuleEvaluationUI.class, "/icon_export.gif");

	@SuppressWarnings("unchecked")
	public static ImageIcon loadImageIcon(Class clas, String iconPath) {
        ImageIcon icon = null;
        URL url = clas.getResource(iconPath);
        if (url != null) {
            icon = new ImageIcon(url);
        }
        else {
        	icon = new ImageIcon(iconPath);
        }
        return icon;
    }
}
