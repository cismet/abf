/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.client;

import org.apache.log4j.Logger;

import org.netbeans.api.diff.Diff;
import org.netbeans.api.diff.DiffView;
import org.netbeans.api.diff.StreamSource;

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;

import java.io.InputStreamReader;
import java.io.Reader;

import java.net.URL;
import java.net.URLClassLoader;

import java.text.DateFormat;

import java.util.Collection;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.Action;

/**
 * DOCUMENT ME!
 *
 * @version  1.0
 */
public final class DiffAction extends AbstractAction implements LookupListener, ContextAwareAction {

    //~ Static fields/initializers ---------------------------------------------

    /** LOGGER. */
    private static final transient Logger LOG = Logger.getLogger(DiffAction.class);

    //~ Instance fields --------------------------------------------------------

    private final Lookup context;
    private Lookup.Result<DataObject> result;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DiffAction object.
     */
    public DiffAction() {
        this(Utilities.actionsGlobalContext());
    }

    /**
     * Creates a new CreateSecurityJarAction object.
     *
     * @param  lookup  context DOCUMENT ME!
     */
    public DiffAction(final Lookup lookup) {
        this.context = lookup;

        putValue(Action.NAME, NbBundle.getMessage(DiffAction.class, "CTL_DiffAction"));
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent ev) {
        final DataObject dao = result.allInstances().iterator().next();
        final FileObject file = dao.getPrimaryFile();

        try {
            final Reader rJnlp;
            final Reader rJar;
            final String nameJnlp;
            final String nameJar;
            final URLClassLoader cl;
            if ("jnlp".equalsIgnoreCase(file.getExt())) {                                                   // NOI18N
                rJnlp = new InputStreamReader(file.getInputStream(), "UTF-8");                              // NOI18N
                final FileObject jarFo = file.getParent().getFileObject(file.getName() + "_security.jar");  // NOI18N
                cl = new URLClassLoader(new URL[] { jarFo.getURL() });
                nameJnlp = file.getNameExt();
                nameJar = jarFo.getNameExt();
            } else if ("jar".equalsIgnoreCase(file.getExt())) {                                             // NOI18N
                cl = new URLClassLoader(new URL[] { file.getURL() });
                final String name = file.getName().substring(0, file.getName().lastIndexOf('_')) + ".jnlp"; // NOI18N
                final FileObject jnlpFo = file.getParent().getFileObject(name);
                rJnlp = new InputStreamReader(jnlpFo.getInputStream(), "UTF-8");                            // NOI18N
                nameJnlp = jnlpFo.getNameExt();
                nameJar = file.getNameExt();
            } else {
                throw new IllegalStateException("action should have been disabled");                        // NOI18N
            }

            rJar = new InputStreamReader(cl.getResourceAsStream("JNLP-INF/APPLICATION.JNLP"), "UTF-8");          // NOI18N
            final StreamSource ssJnlp = StreamSource.createSource(nameJnlp, nameJnlp, "text/x-jnlp+xml", rJnlp); // NOI18N
            final StreamSource ssJar = StreamSource.createSource(nameJar, nameJar, "text/x-jnlp+xml", rJar);     // NOI18N

            final DiffView view = Diff.getDefault().createDiff(ssJar, ssJnlp);
            final String time = DateFormat.getTimeInstance().format(new Date(System.currentTimeMillis()));
            final TopComponent tc = new TopComponent();
            tc.setDisplayName(NbBundle.getMessage(
                    DiffAction.class,
                    "DiffAction.actionPerformed(ActionEvent).tc.displayName")); // NOI18N
            tc.setToolTipText(NbBundle.getMessage(
                    DiffAction.class,
                    "DiffAction.actionPerformed(ActionEvent).tc.tooltip",       // NOI18N
                    nameJar,
                    nameJnlp,
                    time));
            tc.setLayout(new BorderLayout());
            tc.add(view.getComponent(), BorderLayout.CENTER);
            tc.open();
            tc.requestActive();
        } catch (final Exception e) {
            LOG.error("cannot create diff view", e);                            // NOI18N
        }
    }

    @Override
    public boolean isEnabled() {
        init();

        return super.isEnabled();
    }

    /**
     * DOCUMENT ME!
     */
    private void init() {
        assert EventQueue.isDispatchThread();

        if (result == null) {
            result = context.lookupResult(DataObject.class);
            result.addLookupListener(this);
        }

        resultChanged(null);
    }

    @Override
    public void resultChanged(final LookupEvent le) {
        final Collection<? extends DataObject> daos = result.allInstances();
        if (daos.size() == 1) {
            final DataObject dao = daos.iterator().next();
            final FileObject file = dao.getPrimaryFile();
            if ("jnlp".equalsIgnoreCase(file.getExt())) {                                             // NOI18N
                setEnabled(file.getParent().getFileObject(file.getName() + "_security.jar") != null); // NOI18N
            } else if ("jar".equalsIgnoreCase(file.getExt())) {                                       // NOI18N
                final String name = file.getName().substring(0, file.getName().lastIndexOf('_'));
                setEnabled(file.getParent().getFileObject(name + ".jnlp") != null);                   // NOI18N
            } else {
                setEnabled(false);
            }
        } else {
            setEnabled(false);
        }
    }

    @Override
    public Action createContextAwareInstance(final Lookup lkp) {
        return new DiffAction(lkp);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static ContextAwareAction createAction() {
        return new DiffAction();
    }
}
