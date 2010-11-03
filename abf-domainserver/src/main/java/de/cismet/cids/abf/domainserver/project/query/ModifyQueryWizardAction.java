/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.query;

import org.openide.WizardDescriptor;
import org.openide.nodes.Node;

import de.cismet.cids.jpa.entity.query.Query;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public class ModifyQueryWizardAction extends QueryManipulationWizardAction {

    //~ Methods ----------------------------------------------------------------

    @Override
    public String getName() {
        return org.openide.util.NbBundle.getMessage(
                ModifyQueryWizardAction.class,
                "ModifyQueryWizardAction.getName().returnvalue"); // NOI18N
    }

    @Override
    protected void performAction(final Node[] nodes) {
        final Query query = nodes[0].getCookie(QueryContextCookie.class).getQuery();
        if (query == null) {
            throw new IllegalStateException("query cannot be null"); // NOI18N
        }
        final WizardDescriptor wizard = new WizardDescriptor(getPanels());
        wizard.putProperty(QUERY_PROPERTY, query);
        performAction(nodes, wizard);
    }

    @Override
    protected boolean enable(final Node[] nodes) {
        if (!super.enable(nodes)) {
            return false;
        }
        if (nodes.length != 1) {
            return false;
        }
        return nodes[0].getCookie(QueryContextCookie.class) != null;
    }
}
