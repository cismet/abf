/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project;

import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * Dummy action to add to a node's contextual menu to indicate that a refresh is in progress. Action is never enabled.
 *
 * @author   martin.scholl@cismet.de
 * @version  1.0
 */
public final class RefreshIndicatorAction extends NodeAction {

    //~ Methods ----------------------------------------------------------------

    @Override
    protected void performAction(final Node[] activatedNodes) {
        // never enabled
    }

    @Override
    protected boolean enable(final Node[] activatedNodes) {
        return false;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(RefreshIndicatorAction.class, "RefreshIndicatorAction.getName().refreshInProgress"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
}
