/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.client;

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

import de.cismet.cids.abf.librarysupport.project.customizer.LibrarySupportProjectCustomizer;
import de.cismet.cids.abf.librarysupport.project.customizer.PanelProvider;
import de.cismet.cids.abf.librarysupport.project.customizer.PropertyProvider;

import de.cismet.tools.PasswordEncrypter;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  1.0
 */
public final class ClientProjectCustomizer implements CustomizerProvider {

    //~ Instance fields --------------------------------------------------------

    private final transient ClientProject project;
    private final transient Image keystoreIcon;
    private final transient Image deployIcon;
    private final transient Image serviceIcon;
    private final transient Image preferencesIcon;

    private transient ProjectCustomizer.Category[] categories;
    private transient ProjectCustomizer.CategoryComponentProvider panelProvider;
    private transient DeployVisualPanel deployVis;
    private transient KeystoreVisualPanel keystoreVis;
    private transient SignServiceVisualPanel signServiceVis;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of ClientProjectCustomizer.
     *
     * @param  project  DOCUMENT ME!
     */
    public ClientProjectCustomizer(final ClientProject project) {
        this.project = project;
        final String pakkage = ClientProjectCustomizer.class.getPackage().getName().replaceAll("\\.", "/"); // NOI18N
        keystoreIcon = ImageUtilities.loadImage(pakkage + "/key.png");                                      // NOI18N
        deployIcon = ImageUtilities.loadImage(pakkage + "/jar_16.png");                                     // NOI18N
        serviceIcon = ImageUtilities.loadImage(pakkage + "/service_16.png");                                // NOI18N
        preferencesIcon = ImageUtilities.loadImage(pakkage + "/preferences_16.png");                        // NOI18N
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    private void init() {
        final ProjectCustomizer.Category deployKeystore = ProjectCustomizer.Category.create(
                LibrarySupportProjectCustomizer.DEPLOY_KEYSTORE,
                org.openide.util.NbBundle.getMessage(
                    ClientProjectCustomizer.class,
                    "ClientProjectCustomizer.init().deployKeystore.label"), // NOI18N
                keystoreIcon,
                (ProjectCustomizer.Category[])null);
        final ProjectCustomizer.Category deploySignService = ProjectCustomizer.Category.create(
                LibrarySupportProjectCustomizer.DEPLOY_KEYSTORE,
                "Sign Service",
                serviceIcon,
                (ProjectCustomizer.Category[])null);
        final ProjectCustomizer.Category[] deployCategories = new ProjectCustomizer.Category[2];
        deployCategories[0] = deployKeystore;
        deployCategories[1] = deploySignService;
        final ProjectCustomizer.Category deploy = ProjectCustomizer.Category.create(
                LibrarySupportProjectCustomizer.DEPLOY_CATEGORY,
                org.openide.util.NbBundle.getMessage(
                    ClientProjectCustomizer.class,
                    "ClientProjectCustomizer.init().deploy.label"), // NOI18N
                deployIcon,
                deployCategories);
        categories = new ProjectCustomizer.Category[1];
        categories[0] = deploy;
        final Map<ProjectCustomizer.Category, JComponent> panels = new HashMap<ProjectCustomizer.Category, JComponent>(
                6);
        deployVis = new DeployVisualPanel(project);
        keystoreVis = new KeystoreVisualPanel(project, deployKeystore);
        signServiceVis = new SignServiceVisualPanel(project, deploySignService);
        panels.put(deploy, deployVis);
        panels.put(deployKeystore, keystoreVis);
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
                LibrarySupportProjectCustomizer.DEPLOY_CATEGORY,
                okListener,
                null);
        dialog.addWindowListener(okListener);
        final String title =
            org.openide.util.NbBundle.getMessage(
                        ClientProjectCustomizer.class,
                        "ClientProjectCustomizer.showCustomizer().dialog.title") // NOI18N
                    + ProjectUtils.getInformation(project)
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
            final String keystore = keystoreVis.getKeystore();
            final String keystorePW = PasswordEncrypter.encryptString(String.valueOf(keystoreVis.getPassword()));
            final String keystoreAlias = keystoreVis.getAlias();
            final String deployStrategy = deployVis.getStrategy();
            final String signServiceUrl = signServiceVis.getUrl();
            final String signServiceUsername = signServiceVis.getUsername();
            final String signServicePassword = PasswordEncrypter.encryptString(String.valueOf(
                        signServiceVis.getPassWord()));
            final String signServiceLogLevel = signServiceVis.getLogLevel();
            provider.put(PropertyProvider.KEY_GENERAL_KEYSTORE, keystore);
            provider.put(PropertyProvider.KEY_GENERAL_KEYSTORE_PW, keystorePW);
            provider.put(PropertyProvider.KEY_KEYSTORE_ALIAS, keystoreAlias);
            provider.put(PropertyProvider.KEY_DEPLOYMENT_STRATEGY, deployStrategy);
            provider.put(PropertyProvider.KEY_SIGN_SERVICE_URL, signServiceUrl);
            provider.put(PropertyProvider.KEY_SIGN_SERVICE_USERNAME, signServiceUsername);
            provider.put(PropertyProvider.KEY_SIGN_SERVICE_PASSWORD, signServicePassword);
            provider.put(PropertyProvider.KEY_SIGN_SERVICE_LOG_LEVEL, signServiceLogLevel);
            provider.save();
        }

        @Override
        public void windowClosed(final WindowEvent we) {
            PropertyProvider.getInstance(project.getProjectProperties()).clear();
        }
    }
}
