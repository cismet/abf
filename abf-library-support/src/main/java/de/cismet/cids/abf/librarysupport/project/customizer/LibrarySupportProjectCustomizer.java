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

import org.openide.util.ImageUtilities;

import java.awt.Dialog;
import java.awt.Image;
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

    public static final String GENERAL_CATEGORY = "general";       // NOI18N
    public static final String DEPLOY_CATEGORY = "deploy";         // NOI18N
    public static final String DEPLOY_KEYSTORE = "deployKeystore"; // NOI18N

    //~ Instance fields --------------------------------------------------------

    private final transient LibrarySupportProject project;
    private final transient Image keystoreIcon;
    private final transient Image deployIcon;
    private final transient Image serviceIcon;
    private final transient Image scriptIcon;
    private final transient Image preferencesIcon;

    private transient ProjectCustomizer.Category[] categories;
    private transient ProjectCustomizer.CategoryComponentProvider panelProvider;
    private transient DeployVisualPanel deployVis;
    private transient ManifestVisualPanel manifestVis;
    private transient KeystoreVisualPanel keystoreVis;
    private transient SignServiceVisualPanel signServiceVis;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of LibrarySupportProjectCustomizer.
     *
     * @param  project  DOCUMENT ME!
     */
    public LibrarySupportProjectCustomizer(final LibrarySupportProject project) {
        this.project = project;
        final String pakkage = LibrarySupportProjectCustomizer.class.getPackage().getName().replaceAll("\\.", "/"); // NOI18N
        keystoreIcon = ImageUtilities.loadImage(pakkage + "/key.png");                                              // NOI18N
        deployIcon = ImageUtilities.loadImage(pakkage + "/jar_16.png");                                             // NOI18N
        serviceIcon = ImageUtilities.loadImage(pakkage + "/service_16.png");                                        // NOI18N
        scriptIcon = ImageUtilities.loadImage(pakkage + "/script_16.png");                                          // NOI18N
        preferencesIcon = ImageUtilities.loadImage(pakkage + "/preferences_16.png");                                // NOI18N
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
                keystoreIcon,
                (ProjectCustomizer.Category[])null);
        final ProjectCustomizer.Category deployManifest = ProjectCustomizer.Category.create(
                DEPLOY_KEYSTORE,
                org.openide.util.NbBundle.getMessage(
                    LibrarySupportProjectCustomizer.class,
                    "LibrarySupportProjectCustomizer.init().deployManifest.label"), // NOI18N
                scriptIcon,
                (ProjectCustomizer.Category[])null);
        final ProjectCustomizer.Category deploySignService = ProjectCustomizer.Category.create(
                DEPLOY_KEYSTORE,
                "Sign Service",
                serviceIcon,
                (ProjectCustomizer.Category[])null);
        final ProjectCustomizer.Category[] deployCategories = new ProjectCustomizer.Category[3];
        deployCategories[0] = deployKeystore;
        deployCategories[1] = deployManifest;
        deployCategories[2] = deploySignService;
        final ProjectCustomizer.Category general = ProjectCustomizer.Category.create(
                GENERAL_CATEGORY,
                org.openide.util.NbBundle.getMessage(
                    LibrarySupportProjectCustomizer.class,
                    "LibrarySupportProjectCustomizer.init().general.label"), // NOI18N
                preferencesIcon,
                (ProjectCustomizer.Category[])null);
        final ProjectCustomizer.Category deploy = ProjectCustomizer.Category.create(
                DEPLOY_CATEGORY,
                org.openide.util.NbBundle.getMessage(
                    LibrarySupportProjectCustomizer.class,
                    "LibrarySupportProjectCustomizer.init().deploy.label"), // NOI18N
                deployIcon,
                deployCategories);
        categories = new ProjectCustomizer.Category[2];
        categories[0] = general;
        categories[1] = deploy;
        final Map<ProjectCustomizer.Category, JComponent> panels = new HashMap<ProjectCustomizer.Category, JComponent>(
                6);
        final GeneralVisualPanel generalVis = new GeneralVisualPanel();
        deployVis = new DeployVisualPanel(project);
        keystoreVis = new KeystoreVisualPanel(project);
        manifestVis = new ManifestVisualPanel(project);
        signServiceVis = new SignServiceVisualPanel(project, deploySignService);
        panels.put(general, generalVis);
        panels.put(deploy, deployVis);
        panels.put(deployKeystore, keystoreVis);
        panels.put(deployManifest, manifestVis);
        panels.put(deploySignService, signServiceVis);
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
            final String deployStrategy = deployVis.getStrategy();
            final String signServiceUrl = signServiceVis.getUrl();
            provider.put(PropertyProvider.KEY_GENERAL_KEYSTORE, mainKeystore);
            provider.put(PropertyProvider.KEY_GENERAL_KEYSTORE_PW, mainKeystorePW);
            provider.put(PropertyProvider.KEY_GENERAL_MANIFEST, basicManPath);
            provider.put(PropertyProvider.KEY_DEPLOYMENT_STRATEGY, deployStrategy);
            provider.put(PropertyProvider.KEY_SIGN_SERVICE_URL, signServiceUrl);
            provider.save();
        }

        @Override
        public void windowClosed(final WindowEvent we) {
            PropertyProvider.getInstance(project.getProjectProperties()).clearInternal();
            project.getLookup().lookup(LibrarySupportProjectNode.class).firePropertiesChange();
        }
    }
}
