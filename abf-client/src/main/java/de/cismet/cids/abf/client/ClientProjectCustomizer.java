/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.client;

import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.CustomizerProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.CategoryComponentProvider;

import org.openide.util.ImageUtilities;

import java.awt.Dialog;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.util.Properties;

import javax.swing.JComponent;

import de.cismet.cids.abf.librarysupport.project.customizer.LibrarySupportProjectCustomizer;
import de.cismet.cids.abf.librarysupport.project.customizer.PropertyProvider;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class ClientProjectCustomizer implements CustomizerProvider {

    //~ Instance fields --------------------------------------------------------

    private final transient Image icon;
    private final transient Project project;
    private final transient Category[] categories;
    private final transient CategoryComponentProvider provider;

    private transient String keystorePathBackup;
    private transient String keystorePwBackup;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ClientProjectCustomizer object.
     *
     * @param  project  DOCUMENT ME!
     */
    public ClientProjectCustomizer(final Project project) {
        this.project = project;

        icon = ImageUtilities.loadImage(
                ClientProjectCustomizer.class.getPackage().getName().replaceAll("\\.", "/")
                        + "/key.png"); // NOI18N
        categories = getCategories();
        provider = getComponentProvider();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Category[] getCategories() {
        final ProjectCustomizer.Category deployKeystore = ProjectCustomizer.Category.create(
                LibrarySupportProjectCustomizer.DEPLOY_KEYSTORE,
                "Keystore",
                icon,
                (ProjectCustomizer.Category[])null);

        return new ProjectCustomizer.Category[] { deployKeystore };
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private CategoryComponentProvider getComponentProvider() {
        // currently only one category available
        return new CategoryComponentProvider() {

                @Override
                public JComponent create(final Category ctgr) {
                    return new KeystoreVisualPanel(project, ctgr);
                }
            };
    }

    @Override
    public void showCustomizer() {
        final Properties projectProperties = project.getLookup().lookup(Properties.class);
        if (projectProperties == null) {
            throw new IllegalStateException("no project properties available"); // NOI18N
        }

        keystorePathBackup = projectProperties.getProperty(PropertyProvider.KEY_GENERAL_KEYSTORE);
        keystorePwBackup = projectProperties.getProperty(PropertyProvider.KEY_GENERAL_KEYSTORE_PW);

        final CancelL cancelL = new CancelL();
        final Dialog dialog = ProjectCustomizer.createCustomizerDialog(
                categories,
                provider,
                null,
                cancelL,
                null);
        dialog.addWindowListener(cancelL);
        dialog.setTitle("Customise project properties");
        dialog.setVisible(true);
        dialog.requestFocus();
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private final class CancelL extends WindowAdapter implements ActionListener {

        //~ Instance fields ----------------------------------------------------

        private boolean cancelled = true;

        //~ Methods ------------------------------------------------------------

        @Override
        public void actionPerformed(final ActionEvent e) {
            cancelled = false;
        }

        @Override
        public void windowClosed(final WindowEvent e) {
            if (cancelled) {
                final Properties projectProps = project.getLookup().lookup(Properties.class);
                if (projectProps == null) {
                    throw new IllegalStateException("project properties not availabe for project: " + project); // NOI18N
                }

                projectProps.put(PropertyProvider.KEY_GENERAL_KEYSTORE, keystorePathBackup);
                projectProps.put(PropertyProvider.KEY_GENERAL_KEYSTORE_PW, keystorePwBackup);
            }
        }
    }
}
