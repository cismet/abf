/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.librarysupport.project.nodes.wizard;

import org.apache.log4j.Logger;

import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.text.MessageFormat;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import de.cismet.cids.abf.librarysupport.project.LibrarySupportProject;
import de.cismet.cids.abf.librarysupport.project.nodes.cookies.PackageContextCookie;
import de.cismet.cids.abf.librarysupport.project.nodes.cookies.SourceContextCookie;
import de.cismet.cids.abf.utilities.files.PackageUtils;

/**
 * DOCUMENT ME!
 *
 * @author   mscholl
 * @version  1.8
 */
public final class NewWizardAction extends CookieAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(
            NewWizardAction.class);

    static final String PROP_ROOT_DIR = "property_rootDir";       // NOI18N
    static final String PROP_CURRENT_DIR = "property_currentDir"; // NOI18N
    static final String PROP_IS_PACKAGE = "property_isPackage";   // NOI18N
    static final String PROP_EXT = "property_ext";                // NOI18N
    static final String PROP_PATH = "pathProperty";               // NOI18N
    static final String PROP_NAME = "nameProperty";               // NOI18N
    static final String PROP_PACKAGE = "packageProperty";         // NOI18N

    //~ Instance fields --------------------------------------------------------

    private final transient ImageIcon emptyImage;
    private final transient ImageIcon packageImage;
    private final transient ImageIcon propertiesImage;
    private final transient ImageIcon txtImage;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new NewWizardAction object.
     */
    public NewWizardAction() {
        emptyImage = new ImageIcon(ImageUtilities.loadImage(
                    LibrarySupportProject.IMAGE_FOLDER
                            + "empty.png"));            // NOI18N
        packageImage = new ImageIcon(ImageUtilities.loadImage(
                    LibrarySupportProject.IMAGE_FOLDER
                            + "package_16.png"));       // NOI18N
        propertiesImage = new ImageIcon(ImageUtilities.loadImage(
                    LibrarySupportProject.IMAGE_FOLDER
                            + "propertiesObject.png")); // NOI18N
        txtImage = new ImageIcon(ImageUtilities.loadImage(
                    LibrarySupportProject.IMAGE_FOLDER
                            + "txt.png"));              // NOI18N
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    protected void performAction(final Node[] nodes) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("performAction(Node): " + nodes); // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  node       DOCUMENT ME!
     * @param  isPackage  DOCUMENT ME!
     * @param  ext        DOCUMENT ME!
     */
    protected void performAction(final Node node, final boolean isPackage, final String ext) {
        final FileObject root;
        final FileObject current;
        final PackageContextCookie pc = node.getCookie(PackageContextCookie.class);
        if (pc == null) {
            final SourceContextCookie sc = node.getCookie(SourceContextCookie.class);
            try {
                root = sc.getSourceObject();
            } catch (FileNotFoundException ex) {
                LOG.error("could not fetch source dir", ex); // NOI18N
                return;
            }
            current = root;
        } else {
            root = pc.getRootFolder();
            current = pc.getCurrentFolder();
        }
        final WizardDescriptor.Iterator<WizardDescriptor> iterator = new NewWizardIterator();
        final WizardDescriptor wizard = new WizardDescriptor(iterator);
        wizard.putProperty(PROP_ROOT_DIR, root);
        wizard.putProperty(PROP_CURRENT_DIR, current);
        wizard.putProperty(PROP_EXT, ext);
        wizard.putProperty(PROP_IS_PACKAGE, Boolean.valueOf(isPackage));
        // {0} will be replaced by WizardDescriptor.Panel.getComponent().
        // getName()
        // {1} will be replaced by WizardDescriptor.Iterator.name()
        wizard.setTitleFormat(new MessageFormat("{0}"));           // NOI18N
        if (isPackage) {
            wizard.setTitle(org.openide.util.NbBundle.getMessage(
                    NewWizardAction.class,
                    "NewWizardAction.performAction(Node,boolean,String).wizard.title.createNewPackage"));
        } else {
            wizard.setTitle(org.openide.util.NbBundle.getMessage(
                    NewWizardAction.class,
                    "NewWizardAction.performAction(Node,boolean,String).wizard.title.createNewFile"));
        }
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(wizard);
        dialog.setVisible(true);
        dialog.toFront();
        final boolean cancelled = wizard.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            if (isPackage) {
                final String newPackage = wizard.getProperty(PROP_NAME).toString();
                FileObject cycle = PackageUtils.toFileObject(root, wizard.getProperty(PROP_PACKAGE).toString());
                final String[] dirs = newPackage.split("\\.");     // NOI18N
                try {
                    for (final String dir : dirs) {
                        final FileObject toCreate = cycle.getFileObject(dir);
                        if (toCreate == null) {
                            cycle = cycle.createFolder(dir);
                        } else {
                            cycle = toCreate;
                        }
                    }
                } catch (final IOException ex) {
                    LOG.error("could not create new package", ex); // NOI18N
                    EventQueue.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                JOptionPane.showMessageDialog(
                                    WindowManager.getDefault().getMainWindow(),
                                    org.openide.util.NbBundle.getMessage(
                                        NewWizardAction.class,
                                        "NewWizardAction.performAction(Node,boolean,String).JOptionPane.message"),
                                    org.openide.util.NbBundle.getMessage(
                                        NewWizardAction.class,
                                        "NewWizardAction.performAction(Node,boolean,String).JOptionPane.message"),
                                    JOptionPane.ERROR_MESSAGE);
                            }
                        });
                }
            } else {
                final File f = new File(wizard.getProperty(PROP_PATH).toString());
                try {
                    f.createNewFile();
                } catch (final IOException ex) {
                    LOG.error("could not create file", ex); // NOI18N
                    ErrorManager.getDefault().notify(ex);
                }
            }
        }
    }

    @Override
    protected int mode() {
        return CookieAction.MODE_EXACTLY_ONE;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(NewWizardAction.class, "CTL_NewAction"); // NOI18N
    }

    @Override
    protected Class[] cookieClasses() {
        return new Class[] {
                PackageContextCookie.class,
                SourceContextCookie.class
            };
    }

    @Override
    protected void initialize() {
        super.initialize();
        // see org.openide.util.actions.SystemAction.iconResource() javadoc
        // for more details
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
    public JMenuItem getPopupPresenter() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("requesting popup presenter");                       // NOI18N
        }
        final JMenu item = new JMenu(org.openide.util.NbBundle.getMessage(
                    NewWizardAction.class,
                    "NewWizardAction.getPopupPresenter().item.text"));     // NOI18N
        final JMenuItem file = new JMenuItem(org.openide.util.NbBundle.getMessage(
                    NewWizardAction.class,
                    "NewWizardAction.getPopupPresenter().file.text"));     // NOI18N
        final JMenuItem pakkage = new JMenuItem(org.openide.util.NbBundle.getMessage(
                    NewWizardAction.class,
                    "NewWizardAction.getPopupPresenter().pakkage.text"));  // NOI18N
        final JMenuItem propFile = new JMenuItem(org.openide.util.NbBundle.getMessage(
                    NewWizardAction.class,
                    "NewWizardAction.getPopupPresenter().propFile.text")); // NOI18N
        final JMenuItem txtFile = new JMenuItem(org.openide.util.NbBundle.getMessage(
                    NewWizardAction.class,
                    "NewWizardAction.getPopupPresenter().txtFile.text"));  // NOI18N
        file.setActionCommand("file");                                     // NOI18N
        pakkage.setActionCommand("package");                               // NOI18N
        propFile.setActionCommand("propFile");                             // NOI18N
        txtFile.setActionCommand("txtFile");                               // NOI18N
        // weaklistener cannot be used because it is immediately freed
        file.addActionListener(new NewActionListener());
        pakkage.addActionListener(new NewActionListener());
        propFile.addActionListener(new NewActionListener());
        txtFile.addActionListener(new NewActionListener());
        file.setIcon(emptyImage);
        pakkage.setIcon(packageImage);
        propFile.setIcon(propertiesImage);
        txtFile.setIcon(txtImage);
        item.add(file);
        item.add(pakkage);
        item.addSeparator();
        item.add(propFile);
        item.add(txtFile);
        return item;
    }

    // TODO: does not work as expected !!!
    // therefore the menu presentation is quoted in the layer.xml
    @Override
    public JMenuItem getMenuPresenter() {
        return getPopupPresenter();
    }

    @Override
    protected boolean enable(final Node[] nodes) {
        if (nodes.length != 1) {
            return false;
        }
        return super.enable(nodes);
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private final class NewActionListener implements ActionListener {

        //~ Methods ------------------------------------------------------------

        @Override
        public void actionPerformed(final ActionEvent e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("new action requested");              // NOI18N
            }
            final Node node = TopComponent.getRegistry().getActivatedNodes()[0];
            if ("file".equals(e.getActionCommand()))            // NOI18N
            {
                performAction(node, false, null);
            } else if ("package".equals(e.getActionCommand()))  // NOI18N
            {
                performAction(node, true, null);
            } else if ("propFile".equals(e.getActionCommand())) // NOI18N
            {
                performAction(node, false, "properties");       // NOI18N
            } else if ("txtFile".equals(e.getActionCommand()))  // NOI18N
            {
                performAction(node, false, "txt");              // NOI18N
            }
        }
    }
}
