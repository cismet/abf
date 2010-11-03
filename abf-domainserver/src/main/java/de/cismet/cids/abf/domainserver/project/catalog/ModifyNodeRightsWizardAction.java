/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.catalog;

import org.openide.WizardDescriptor;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

import de.cismet.cids.abf.domainserver.project.DomainserverContext;

import de.cismet.cids.jpa.entity.catalog.CatNode;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public class ModifyNodeRightsWizardAction extends NewCatalogNodeWizardAction {

    //~ Methods ----------------------------------------------------------------

    @Override
    protected void performAction(final Node[] nodes) {
        final CatNode node = nodes[0].getCookie(CatalogNodeContextCookie.class).getCatNode();
        assert node != null;
        final WizardDescriptor.ArrayIterator it = new WizardDescriptor.ArrayIterator(getPanels());
        it.nextPanel();
        final WizardDescriptor wizard = new WizardDescriptor(it);
        wizard.setTitle(NbBundle.getMessage(
                ModifyNodeRightsWizardAction.class,
                "ModifyNodeRightsWizardAction.performAction(Node[]).wizard.title")); // NOI18N
        performAction(nodes, node, wizard);
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(
                ModifyNodeRightsWizardAction.class,
                "ModifyNodeRightsWizardAction.getName().returnvalue"); // NOI18N
    }

    @Override
    protected boolean customEnable(final Node[] nodes) {
        final CatalogNodeContextCookie cncc = nodes[0].getCookie(CatalogNodeContextCookie.class);
        if (cncc == null) {
            return false;
        }
        final CatNode node = cncc.getCatNode();
        if (((node.getDerivePermFromClass() != null) && node.getDerivePermFromClass()) || (node.getId() == null)
                    || (node.getId() < 1)) {
            return false;
        }
        return nodes[0].getCookie(DomainserverContext.class).getDomainserverProject().isConnected();
    }
}
