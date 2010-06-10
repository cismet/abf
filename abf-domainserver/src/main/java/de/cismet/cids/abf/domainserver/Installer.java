/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;

import org.openide.modules.ModuleInstall;
import org.openide.windows.TopComponent;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.cidsclass.ClassDiagramTopComponent;

/**
 * Manages a module's lifecycle. Remember that an installer is optional and often not needed at all.
 *
 * @author   thorsten.hell@cismet.de
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public class Installer extends ModuleInstall {

    //~ Static fields/initializers ---------------------------------------------

    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = 4627615630086207927L;

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean closing() {
        for (final Object o : TopComponent.getRegistry().getOpened()) {
            if (o instanceof ClassDiagramTopComponent) {
                ((ClassDiagramTopComponent)o).componentClosed();
            }
        }
        for (final Project p : OpenProjects.getDefault().getOpenProjects()) {
            if (p instanceof DomainserverProject) {
                ((DomainserverProject)p).setConnected(false);
            }
        }
        
        return super.closing();
    }
}
