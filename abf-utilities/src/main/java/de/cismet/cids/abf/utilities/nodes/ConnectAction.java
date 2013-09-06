/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.utilities.nodes;

import org.apache.log4j.Logger;

import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;
import org.openide.windows.TopComponent;

import java.awt.EventQueue;

import de.cismet.cids.abf.utilities.Connectable;
import de.cismet.cids.abf.utilities.ConnectionEvent;
import de.cismet.cids.abf.utilities.ConnectionListener;
import de.cismet.cids.abf.utilities.UtilityCommons;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public final class ConnectAction extends CookieAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(ConnectAction.class);

    //~ Instance fields --------------------------------------------------------

    private final transient ConnectionListener conL;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ConnectAction object.
     */
    public ConnectAction() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("create new connectaction: be careful that this is done only once!"); // NOI18N
        }

        conL = new ConnL();
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public String getName() {
        final Connectable c = getConnectable();
        if ((c != null) && c.isConnected()) {
            return org.openide.util.NbBundle.getMessage(
                    ConnectAction.class,
                    "ConnectAction.getName().returnvalue.disconnect"); // NOI18N
        } else {
            return org.openide.util.NbBundle.getMessage(
                    ConnectAction.class,
                    "ConnectAction.getName().returnvalue.connect");    // NOI18N
        }
    }

    @Override
    protected String iconResource() {
        final Connectable c = getConnectable();
        if ((c != null) && c.isConnected()) {
            return UtilityCommons.IMAGE_FOLDER + "connect_established.png"; // NOI18N
        } else {
            return UtilityCommons.IMAGE_FOLDER + "connect_no.png";          // NOI18N
        }
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    /**
     * DOCUMENT ME!
     */
    private void refreshUI() {
        firePropertyChange(PROP_ICON, null, super.getIcon());
        firePropertyChange(SMALL_ICON, null, super.getIcon());
        firePropertyChange(NAME, null, getName());
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Connectable getConnectable() {
        final Node[] n = TopComponent.getRegistry().getActivatedNodes();
        if ((n.length == 1) && (n[0].getLookup().lookup(Connectable.class) != null)) {
            return n[0].getLookup().lookup(Connectable.class);
        } else {
            return null;
        }
    }

    @Override
    protected int mode() {
        return MODE_EXACTLY_ONE;
    }

    @Override
    protected Class<?>[] cookieClasses() {
        return new Class[] { Connectable.class };
    }

    @Override
    protected void performAction(final Node[] nodes) {
        final Connectable c = nodes[0].getLookup().lookup(Connectable.class);
        c.addConnectionListener(conL);
        c.setConnected(!c.isConnected());
    }

    @Override
    protected boolean enable(final Node[] nodes) {
        final boolean enable = super.enable(nodes);
        if (!enable) {
            return false;
        }

        return !nodes[0].getLookup().lookup(Connectable.class).isConnectionInProgress();
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private final class ConnL implements ConnectionListener {

        //~ Methods ------------------------------------------------------------

        @Override
        public void connectionStatusChanged(final ConnectionEvent event) {
            EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        refreshUI();
                    }
                });
        }
    }
}
