/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.librarysupport.project.customizer;

import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.ui.CustomizerProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;

import de.cismet.cids.abf.librarysupport.project.LibrarySupportProject;
import de.cismet.cids.abf.librarysupport.project.LibrarySupportProjectNode;

import de.cismet.tools.PasswordEncrypter;

/**
 * DOCUMENT ME!
 *
 * @author   mscholl
 * @version  1.4
 */
public final class LibrarySupportProjectCustomizer implements CustomizerProvider {

    //~ Static fields/initializers ---------------------------------------------

    private static final String GENERAL_CATEGORY = "general";       // NOI18N
    private static final String DEPLOY_CATEGORY = "deploy";         // NOI18N
    private static final String DEPLOY_KEYSTORE = "deployKeystore"; // NOI18N

    //~ Instance fields --------------------------------------------------------

    private final transient LibrarySupportProject project;

    private transient ProjectCustomizer.Category[] categories;
    private transient ProjectCustomizer.CategoryComponentProvider panelProvider;
    private transient ManifestVisualPanel manifestVis;
    private transient KeystoreVisualPanel keystoreVis;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of LibrarySupportProjectCustomizer.
     *
     * @param  project  DOCUMENT ME!
     */
    public LibrarySupportProjectCustomizer(final LibrarySupportProject project) {
        this.project = project;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    private void init() {
        final ProjectCustomizer.Category deployKeystore = ProjectCustomizer.Category.create(
                DEPLOY_KEYSTORE,
                org.openide.util.NbBundle.getMessage(
                    LibrarySupportProjectCustomizer.class,
                    "LibrarySupportProjectCustomizer.init().deployKeystore.label"), // NOI18N
                null, // TODO: provide icon
                (ProjectCustomizer.Category[])null);
        final ProjectCustomizer.Category deployManifest = ProjectCustomizer.Category.create(
                DEPLOY_KEYSTORE,
                org.openide.util.NbBundle.getMessage(
                    LibrarySupportProjectCustomizer.class,
                    "LibrarySupportProjectCustomizer.init().deployManifest.label"), // NOI18N
                null, // TODO: provide icon
                (ProjectCustomizer.Category[])null);
        final ProjectCustomizer.Category[] deployCategories = new ProjectCustomizer.Category[2];
        deployCategories[0] = deployKeystore;
        deployCategories[1] = deployManifest;
        final ProjectCustomizer.Category general = ProjectCustomizer.Category.create(
                GENERAL_CATEGORY,
                org.openide.util.NbBundle.getMessage(
                    LibrarySupportProjectCustomizer.class,
                    "LibrarySupportProjectCustomizer.init().general.label"), // NOI18N
                null, // TODO: provide icon)
                (ProjectCustomizer.Category[])null);
        final ProjectCustomizer.Category deploy = ProjectCustomizer.Category.create(
                DEPLOY_CATEGORY,
                org.openide.util.NbBundle.getMessage(
                    LibrarySupportProjectCustomizer.class,
                    "LibrarySupportProjectCustomizer.init().deploy.label"), // NOI18N
                null, // TODO: provide icon
                deployCategories);
        categories = new ProjectCustomizer.Category[2];
        categories[0] = general;
        categories[1] = deploy;
        final Map<ProjectCustomizer.Category, JComponent> panels = new HashMap<ProjectCustomizer.Category, JComponent>(
                5);
        final GeneralVisualPanel generalVis = new GeneralVisualPanel();
        final DeployVisualPanel deployVis = new DeployVisualPanel();
        keystoreVis = new KeystoreVisualPanel(project);
        manifestVis = new ManifestVisualPanel(project);
        panels.put(general, generalVis);
        panels.put(deploy, deployVis);
        panels.put(deployKeystore, keystoreVis);
        panels.put(deployManifest, manifestVis);
        panelProvider = new PanelProvider(panels);
    }

    @Override
    public void showCustomizer() {
        init();
        final OptionListener okListener = new OptionListener();
        final Dialog dialog = ProjectCustomizer.createCustomizerDialog(
                categories,
                panelProvider,
                GENERAL_CATEGORY,
                okListener,
                null);
        dialog.addWindowListener(okListener);
        final String title = org.openide.util.NbBundle.getMessage(
                LibrarySupportProjectCustomizer.class,
                "LibrarySupportProjectCustomizer.showCustomizer().dialog.title")
                    + ProjectUtils // NOI18N
                    .getInformation(project)
                    .getDisplayName();
        dialog.setTitle(title);
        dialog.setVisible(true);
        dialog.requestFocus();
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
            final PropertyProvider provider = PropertyProvider.getInstance(project.getProjectProperties());
            final String mainKeystore = keystoreVis.getMainKeystore();
            final String mainKeystorePW = PasswordEncrypter.encryptString(keystoreVis.getPassword());
            final String basicManPath = manifestVis.getBasicManifestField().getText();
            provider.put(PropertyProvider.KEY_GENERAL_KEYSTORE, mainKeystore);
            provider.put(PropertyProvider.KEY_GENERAL_KEYSTORE_PW, mainKeystorePW);
            provider.put(PropertyProvider.KEY_GENERAL_MANIFEST, basicManPath);
            provider.save();
        }

        @Override
        public void windowClosed(final WindowEvent we) {
            PropertyProvider.getInstance(project.getProjectProperties()).clearInternal();
            project.getLookup().lookup(LibrarySupportProjectNode.class).firePropertiesChange();
        }
    }
}
