/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.cidsclass;

import org.openide.util.NbBundle;

import java.beans.PropertyChangeEvent;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class AddClassToCurrentDiagramAction extends DiagramAction {

    //~ Methods ----------------------------------------------------------------

    @Override
    public void performAction() {
        if (lastDiagramTopComponent != null) {
            lastDiagramTopComponent.addClasses(getSelectedCidsClassNodes());
        }
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(
                AddClassToCurrentDiagramAction.class,
                "AddClassToCurrentDiagramAction.getName().returnvalue"); // NOI18N
    }

    @Override
    protected String iconResource() {
        return DomainserverProject.IMAGE_FOLDER
                    + "add_class_diagram.png"; // NOI18N
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        super.propertyChange(evt);
        if (lastDiagramTopComponent == null) {
            setEnabled(false);
        }
    }
}
