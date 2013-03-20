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
public final class ModifyQueryRightsWizardAction extends ModifyQueryWizardAction {

    //~ Methods ----------------------------------------------------------------

    @Override
    protected void performAction(final Node[] nodes) {
        final Query query = nodes[0].getCookie(QueryContextCookie.class).getQuery();
        if (query == null) {
            throw new IllegalStateException("query cannot be null"); // NOI18N
        }
        final WizardDescriptor.ArrayIterator<WizardDescriptor> it =
            new WizardDescriptor.ArrayIterator<WizardDescriptor>(getPanels());
        it.nextPanel();
        it.nextPanel();
        final WizardDescriptor wizard = new WizardDescriptor(it);
        wizard.putProperty(QUERY_PROPERTY, query);
        performAction(nodes, wizard);
    }

    @Override
    public String getName() {
        return org.openide.util.NbBundle.getMessage(
                ModifyQueryRightsWizardAction.class,
                "ModifyQueryRightsWizardAction.getName().returnvalue"); // NOI18N
    }
}
