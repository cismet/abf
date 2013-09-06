/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.query;

import org.apache.log4j.Logger;

import org.openide.nodes.Node;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public class NewQueryWizardAction extends QueryManipulationWizardAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(
            NewQueryWizardAction.class);

    //~ Methods ----------------------------------------------------------------

    @Override
    public String getName() {
        return org.openide.util.NbBundle.getMessage(
                NewQueryWizardAction.class,
                "NewQueryWizardAction.getName().returnvalue"); // NOI18N
    }

    @Override
    protected void performAction(final Node[] node) {
        try {
            performAction(node, null);
        } catch (final Exception e) {
            LOG.error("error during perform action: " // NOI18N
                        + e.getMessage(), e);
        }
    }
}
