/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.customizer;

import org.apache.log4j.Logger;

import org.netbeans.spi.project.ui.CustomizerProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;

import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import java.util.Properties;

import javax.swing.JComponent;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.DomainserverProjectFactory;
import de.cismet.cids.abf.domainserver.project.nodes.UserManagement;

import static de.cismet.cids.abf.domainserver.project.DomainserverProjectFactory.PROJECT_DIR;
import static de.cismet.cids.abf.domainserver.project.DomainserverProjectFactory.PROJECT_PROPFILE;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class DomainserverProjectCustomizer implements CustomizerProvider,
    ProjectCustomizer.CategoryComponentProvider {

    //~ Static fields/initializers ---------------------------------------------

    public static final String PROP_USER_SHOW_LEGACY_CFGATTR_PROPS = "domainserver.users.properties.showLegacyCfgAttr"; // NOI18N
    public static final String PROP_USER_SHOW_CFGATTR_PROPS = "domainserver.users.properties.showCfgAttr";              // NOI18N

    public static final String USER_CATEGORY = "userproperties"; // NOI18N

    /** LOGGER. */
    private static final transient Logger LOG = Logger.getLogger(DomainserverProjectCustomizer.class);

    //~ Instance fields --------------------------------------------------------

    private final transient DomainserverProject project;

    private transient UserPropertiesCustomizerPanel userPropPanel;
    private transient ProjectCustomizer.Category[] categories;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DomainserverProjectCustomizer object.
     *
     * @param  project  DOCUMENT ME!
     */
    public DomainserverProjectCustomizer(final DomainserverProject project) {
        this.project = project;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    private void init() {
        final ProjectCustomizer.Category userProperties = ProjectCustomizer.Category.create(
                USER_CATEGORY,
                NbBundle.getMessage(
                    DomainserverProjectCustomizer.class,
                    "DomainserverProjectCustomizer.init().userProperties.displayName"),
                null, // TODO: provide icon
                (ProjectCustomizer.Category[])null);
        categories = new ProjectCustomizer.Category[1];
        categories[0] = userProperties;
        userPropPanel = new UserPropertiesCustomizerPanel();
    }

    @Override
    public void showCustomizer() {
        init();
        final OptionListener okListener = new OptionListener();
        final Dialog dialog = ProjectCustomizer.createCustomizerDialog(
                categories,
                this,
                USER_CATEGORY,
                okListener,
                null);
        dialog.addWindowListener(okListener);
        final String title = NbBundle.getMessage(
                DomainserverProjectCustomizer.class,
                "DomainserverProjectCustomizer.showCustomizer().dialog.title");
        dialog.setTitle(title);
        dialog.setVisible(true);
        dialog.requestFocus();
    }

    @Override
    public JComponent create(final Category ctgr) {
        if (USER_CATEGORY.equals(ctgr.getName())) {
            userPropPanel.setShowCfgAttrProperties(Boolean.valueOf(
                    project.getProperties().getProperty(PROP_USER_SHOW_CFGATTR_PROPS)));
            userPropPanel.setShowLegacyCfgAttrProperties(Boolean.valueOf(
                    project.getProperties().getProperty(PROP_USER_SHOW_LEGACY_CFGATTR_PROPS)));

            return userPropPanel;
        }

        return null;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private final class OptionListener extends WindowAdapter implements ActionListener {

        //~ Methods ------------------------------------------------------------

        @Override
        public void actionPerformed(final ActionEvent e) {
            final Properties props = project.getProperties();
            props.put(PROP_USER_SHOW_CFGATTR_PROPS, String.valueOf(userPropPanel.isShowCfgAttrProperties()));
            props.put(
                PROP_USER_SHOW_LEGACY_CFGATTR_PROPS,
                String.valueOf(userPropPanel.isShowLegacyCfgAttrProperties()));
            OutputStream fos = null;
            FileLock lock = null;
            try {
                final File file = new File(
                        DomainserverProjectFactory.PROJECT_DIR,
                        DomainserverProjectFactory.PROJECT_PROPFILE);
                final FileObject fo = FileUtil.createData(file);
                lock = fo.lock();
                fos = fo.getOutputStream(lock);
                props.store(fos, "Cids Domainserver Project Properties");
            } catch (final Exception ex) {
                LOG.warn("cannot write project properties", ex);                                       // NOI18N
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException ex) {
                        LOG.warn("cannot close FileOutputStream when writing project properties", ex); // NOI18N
                    }
                }
                if (lock != null) {
                    lock.releaseLock();
                }
            }

            EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        project.getLookup().lookup(UserManagement.class).refreshProperties(false);
                    }
                });
        }
    }
}
