/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.cidsclass;

import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

import java.beans.PropertyChangeEvent;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.cidsclass.graph.ClassDiagramTopComponent;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class AddClassWithRelationsToCurrentDiagramAction extends DiagramAction {

    //~ Methods ----------------------------------------------------------------

    @Override
    public void performAction() {
        final TopComponent tc = TopComponent.getRegistry().getActivated();
        if (tc instanceof ClassDiagramTopComponent) {
            ((ClassDiagramTopComponent)tc).addClassesWithRelations(
                getSelectedCidsClassNodes());
        }
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(
                AddClassWithRelationsToCurrentDiagramAction.class,
                "AddClassWithRelationsToCurrentDiagramAction.getName().returnvalue");
    }

    @Override
    protected String iconResource() {
        return DomainserverProject.IMAGE_FOLDER + "add_class_diagram_with_relations.png";
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        super.propertyChange(evt);
        if (lastDiagramTopComponent == null) {
            setEnabled(false);
        }
    }
}
