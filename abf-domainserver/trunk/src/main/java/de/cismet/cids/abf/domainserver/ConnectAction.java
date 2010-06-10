/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver;

import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.CookieAction;
import org.openide.windows.TopComponent;

import java.awt.EventQueue;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.utilities.Connectable;
import de.cismet.cids.abf.utilities.ConnectionListener;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class ConnectAction extends CookieAction {

    //~ Static fields/initializers ---------------------------------------------

    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = 8552885133594513530L;

    //~ Methods ----------------------------------------------------------------

    @Override
    public String getName() {
        final Connectable c = getConnectable();
        if ((c != null) && c.isConnected()) {
            return org.openide.util.NbBundle.getMessage(ConnectAction.class, "Dsc_disconnect"); // NOI18N
        } else {
            return org.openide.util.NbBundle.getMessage(ConnectAction.class, "Dsc_connect");    // NOI18N
        }
    }

    @Override
    protected String iconResource() {
        final Connectable c = getConnectable();
        if ((c != null) && c.isConnected()) {
            return DomainserverProject.IMAGE_FOLDER + "connect_established.png"; // NOI18N
        } else {
            return DomainserverProject.IMAGE_FOLDER + "connect_no.png";          // NOI18N
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
        if (EventQueue.isDispatchThread()) {
            firePropertyChange(PROP_ICON, null, super.getIcon());
            firePropertyChange(SMALL_ICON, null, super.getIcon());
            firePropertyChange(NAME, null, getName());
        } else {
            EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        firePropertyChange(PROP_ICON, null, ConnectAction.super.getIcon());
                        firePropertyChange(SMALL_ICON, null, ConnectAction.super.getIcon());
                        firePropertyChange(NAME, null, getName());
                    }
                });
        }
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
        c.addConnectionListener(new ConnL(c));
        c.setConnected(!c.isConnected());
    }

    @Override
    protected boolean enable(final Node[] nodes) {
        if (!super.enable(nodes)) {
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

        //~ Instance fields ----------------------------------------------------

        private final transient Connectable c;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new ConnL object.
         *
         * @param  c  DOCUMENT ME!
         */
        ConnL(final Connectable c) {
            this.c = c;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void connectionStatusChanged(final boolean isConnected) {
            RequestProcessor.getDefault().post(new Runnable() {

                    @Override
                    public void run() {
                        c.removeConnectionListener(ConnL.this); // TODO ConcurrentModificationException REFACTOR THIS
                                                                // ACTION !!!
                    }
                }, 100);
            refreshUI();
        }

        @Override
        public void connectionStatusIndeterminate() {
            // not needed
        }
    }
}
