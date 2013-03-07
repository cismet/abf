/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.cidsclass;

import org.apache.log4j.Logger;

import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;
import org.openide.windows.WindowManager;

import javax.swing.JOptionPane;

import de.cismet.cids.abf.domainserver.project.DomainserverContext;

import de.cismet.cids.jpa.backend.service.Backend;

import de.cismet.cids.util.Cancelable;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class IndexAction extends CookieAction implements Cancelable {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(
            IndexAction.class);

    //~ Instance fields --------------------------------------------------------

    private transient Backend backend;
    private transient boolean canceled;

    //~ Methods ----------------------------------------------------------------

    @Override
    protected void performAction(final Node[] nodes) {
        JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
            NbBundle.getMessage(IndexAction.class, "IndexAction.performAction(Node[]).notAvailableDialog.message"),
            NbBundle.getMessage(IndexAction.class, "IndexAction.performAction(Node[]).notAvailableDialog.title"),
            JOptionPane.INFORMATION_MESSAGE);
        // TODO: this is the old reindex action and subject to change canceled = false; final LinkedList<CidsClass>
        // classes = new LinkedList<CidsClass>(); for (final Node n : nodes) { final CidsClassContextCookie classCookie
        // = n.getCookie(CidsClassContextCookie.class); classes.add(classCookie.getCidsClass()); } final
        // DomainserverContext domainCookie = nodes[0].getCookie(DomainserverContext.class); backend =
        // domainCookie.getDomainserverProject().getCidsDataObjectBackend(); final IndexActionDialog dialog = new
        // IndexActionDialog( WindowManager.getDefault().getMainWindow(), true, classes.size(), (Cancelable)this);
        // backend.addProgressListener(dialog); final Thread indexActionThread = new Thread(new Runnable() {
        //
        // @Override public void run() { try { final Iterator<CidsClass> it = classes.iterator(); while (it.hasNext() &&
        // !canceled) { backend.refreshIndex(it.next()); if (!canceled) { dialog.nextClass(); } } } catch (final
        // SQLException ex) { LOG.error("could not index class", ex);                    // NOI18N LOG.error("next
        // ex", ex.getNextException());               // NOI18N dialog.setError(
        // org.openide.util.NbBundle.getMessage( IndexAction.class,
        // "IndexAction.performAction(Node[]).dialog.error"), // NOI18N ex); } } });
        // indexActionThread.setPriority(6); EventQueue.invokeLater(new Runnable() {
        //
        // @Override public void run() { dialog.setVisible(true); } }); indexActionThread.start();
    }

    @Override
    protected int mode() {
        return CookieAction.MODE_ALL;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(
                IndexAction.class,
                "IndexAction.getName().returnvalue"); // NOI18N
    }

    @Override
    protected Class[] cookieClasses() {
        return new Class[] {
                CidsClassContextCookie.class,
                DomainserverContext.class
            };
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
    protected boolean enable(final Node[] nodes) {
        if (!super.enable(nodes)) {
            return false;
        }
        return nodes[0].getCookie(DomainserverContext.class).getDomainserverProject().isConnected();
    }

    @Override
    public void cancel() {
        canceled = true;
        backend.cancel();
    }
}
