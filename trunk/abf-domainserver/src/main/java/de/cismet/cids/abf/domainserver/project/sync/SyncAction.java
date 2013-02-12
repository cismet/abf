/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.sync;

import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;
import org.openide.windows.TopComponent;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.nodes.SyncManagement;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class SyncAction extends CallableSystemAction {

    //~ Methods ----------------------------------------------------------------

    // TODO: refactor action
    @Override
    public void performAction() {
        TopComponent.getRegistry().getActivatedNodes()[0].getLookup().lookup(SyncManagement.class).executeStatements();
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(SyncAction.class, "SyncAction.getName().returnvalue"); // NOI18N
    }

    @Override
    protected void initialize() {
        super.initialize();
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        if (!super.isEnabled()) {
            return false;
        }

        final Node[] na = TopComponent.getRegistry().getActivatedNodes();
        if ((na == null) || (na.length != 1)) {
            return false;
        }

        final DomainserverProject project = na[0].getLookup().lookup(DomainserverProject.class);
        if ((project == null) || !project.isConnected()) {
            return false;
        }

        return na[0].getLookup().lookup(SyncManagement.class) != null;
    }
}
