/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.registry.cookie;

import Sirius.server.registry.rmplugin.util.RMUser;

import org.openide.nodes.Children;
import org.openide.nodes.Node;

import java.util.HashSet;
import java.util.Set;

/**
 * DOCUMENT ME!
 *
 * @author   Martin Scholl
 * @version  $Revision$, $Date$
 */
public class RMUserCookieImpl implements RMUserCookie {

    //~ Instance fields --------------------------------------------------------

    private final transient Children children;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new RMUserCookieImpl object.
     *
     * @param  children  DOCUMENT ME!
     */
    public RMUserCookieImpl(final Children children) {
        this.children = children;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Set<RMUser> getRMUsers() {
        final Set<RMUser> users = new HashSet<RMUser>();
        for (final Node n : children.getNodes()) {
            final RMUserCookie cookie = n.getCookie(RMUserCookie.class);
            if (cookie != null) {
                users.addAll(cookie.getRMUsers());
            }
        }
        return users;
    }
}
