package de.cismet.cids.abf.domainserver.project.cidsclass;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

/**
 * Action which shows ClassDiagram component.
 */
public class ClassDiagramAction extends AbstractAction
{
    public ClassDiagramAction()
    {
        super(NbBundle.getMessage(ClassDiagramAction.class,
                "CTL_ClassDiagramAction"));
        putValue(SMALL_ICON, new ImageIcon(Utilities.loadImage(
                ClassDiagramTopComponent.ICON_PATH, true)));
    }

    public void actionPerformed(final ActionEvent evt)
    {
        final TopComponent win = ClassDiagramTopComponent.getDefault();
        win.open();
        win.requestActive();
    }
}