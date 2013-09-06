/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.cidsclass;

import org.openide.nodes.Node;
import org.openide.util.NbBundle;

import de.cismet.cids.abf.domainserver.project.DomainserverContext;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class EditCidsClassWizardAction extends NewCidsClassWizardAction {

    //~ Methods ----------------------------------------------------------------

    @Override
    public String getName() {
        return NbBundle.getMessage(EditCidsClassWizardAction.class,
                "EditCidsClassWizardAction.getName().returnvalue"); // NOI18N
    }

    @Override
    protected Class[] cookieClasses() {
        return new Class[] {
                DomainserverContext.class,
                CidsClassContextCookie.class
            };
    }

    @Override
    protected void performAction(final Node[] node) {
        final CidsClassContextCookie cccc = node[0].getCookie(
                CidsClassContextCookie.class);
        performAction(node[0], cccc.getCidsClass());
    }
}
