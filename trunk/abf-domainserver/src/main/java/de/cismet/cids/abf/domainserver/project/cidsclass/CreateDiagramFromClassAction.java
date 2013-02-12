/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.cidsclass;

import org.openide.util.NbBundle;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.cidsclass.graph.ClassDiagramTopComponent;
import de.cismet.cids.abf.domainserver.project.nodes.ViewManagement;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public final class CreateDiagramFromClassAction extends DiagramAction {

    //~ Methods ----------------------------------------------------------------

    @Override
    public void performAction() {
        final ClassDiagramTopComponent diagram = ClassDiagramTopComponent.getDefault();
        diagram.setDomainserverProject(getDomainserverprojectForSelectedCidsClassNodes());
        diagram.addClasses(getSelectedCidsClassNodes());
        diagram.open();
        diagram.requestActive();
        ((ViewManagement)diagram.getDomainserverProject().getLookup().lookup(ViewManagement.class)).refreshChildren();
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(
                CreateDiagramFromClassAction.class,
                "CreateDiagramFromClassAction.getName().returnvalue");
    }

    @Override
    protected String iconResource() {
        return DomainserverProject.IMAGE_FOLDER + "new_class_diagram.png";
    }
}
