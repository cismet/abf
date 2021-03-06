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
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;
import org.openide.windows.WindowManager;

import java.awt.Component;
import java.awt.Dialog;

import java.io.File;
import java.io.IOException;

import java.text.MessageFormat;

import java.util.Enumeration;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import de.cismet.cids.abf.librarysupport.project.nodes.cookies.PackageContextCookie;
import de.cismet.cids.abf.utilities.files.FileUtils;
import de.cismet.cids.abf.utilities.files.PackageUtils;

/**
 * DOCUMENT ME!
 *
 * @author   mscholl
 * @version  1.5
 */
public final class RenamePackageWizardAction1 extends NodeAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(RenamePackageWizardAction1.class);

    //~ Methods ----------------------------------------------------------------

    /**
     * Initialize panels representing individual wizard's steps and sets various properties for them influencing wizard
     * appearance.
     *
     * @param   root  DOCUMENT ME!
     * @param   cur   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private WizardDescriptor.Panel<WizardDescriptor>[] getPanels(final FileObject root, final FileObject cur) {
        // it is impossible to create a typed array
        @SuppressWarnings("unchecked")
        final WizardDescriptor.Panel<WizardDescriptor>[] panels = new WizardDescriptor.Panel[] {
                new RenamePackageWizardPanel1(root, cur)
            };
        final String[] steps = new String[panels.length];
        for (int i = 0; i < panels.length; i++) {
            final Component c = panels[i].getComponent();
            // Default step name to component name of panel. Mainly useful
            // for getting the name of the target chooser to appear in the
            // list of steps.
            steps[i] = c.getName();
            if (c instanceof JComponent) { // assume Swing components
                final JComponent jc = (JComponent)c;
                // Sets step number of a component
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, Integer.valueOf(i));
                // Sets steps names for a panel
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                // Turn on subtitle creation on each step
                jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE);
                // Show steps on the left side with the image on the
                // background
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.TRUE);
                // Turn on numbering of all steps
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE);
            }
        }

        return panels;
    }

    @Override
    public String getName() {
        return org.openide.util.NbBundle.getMessage(
                RenamePackageWizardAction1.class,
                "RenamePackageWizardAction1.getName().returnvalue"); // NOI18N
    }

    @Override
    public String iconResource() {
        return null;
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
    protected void performAction(final Node[] nodes) {
        final PackageContextCookie pcc = nodes[0].getCookie(PackageContextCookie.class);
        final FileObject root = pcc.getRootFolder();
        final FileObject current = pcc.getCurrentFolder();
        final WizardDescriptor wizard = new WizardDescriptor(getPanels(root, current));
        // {0} will be replaced by WizardDesriptor.Panel.getComponent()
        // .getName()
        wizard.setTitleFormat(new MessageFormat("{0}"));                           // NOI18N
        wizard.setTitle(org.openide.util.NbBundle.getMessage(
                RenamePackageWizardAction1.class,
                "RenamePackageWizardAction1.performAction(Node[]).wizard.title")); // NOI18N
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(wizard);
        dialog.setVisible(true);
        dialog.toFront();
        final boolean cancelled = wizard.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            final String newPackage = wizard.getProperty(
                    RenamePackageWizardPanel1.NEW_PACKAGE_NAME_PROPERTY).toString();
            final File newPackageFile = new File(PackageUtils.toAbsolutePath(
                        root,
                        newPackage,
                        true));
            final String[] dirs = newPackage.split("\\.");                         // NOI18N
            // first create folders to ensure filelisteners will be attached
            try {
                FileObject cycleFO = root;
                for (final String dir : dirs) {
                    final FileObject toCreate = cycleFO.getFileObject(dir);
                    if (toCreate == null) {
                        cycleFO = cycleFO.createFolder(dir);
                    } else {
                        cycleFO = toCreate;
                    }
                }
            } catch (final Exception ex) {
                LOG.error("could not create dir", ex);                                                                   // NOI18N
                JOptionPane.showMessageDialog(
                    WindowManager.getDefault().getMainWindow(),
                    org.openide.util.NbBundle.getMessage(
                        RenamePackageWizardAction1.class,
                        "RenamePackageWizardAction1.performAction(Node[]).JOptionPane.folderCouldNotBeCreated.message"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        RenamePackageWizardAction1.class,
                        "RenamePackageWizardAction1.performAction(Node[]).JOptionPane.folderCouldNotBeCreated.title"),   // NOI18N
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            // do not move the files to ensure that the files will stay in place
            // in case of operation failure
            try {
                FileUtils.copyContent(FileUtil.toFile(current), newPackageFile,
                    new FileUtils.FilesFilter(), false);
            } catch (final Exception ex) {
                LOG.error("could not copy file", ex);                                                                                // NOI18N
                JOptionPane.showMessageDialog(
                    WindowManager.getDefault().getMainWindow(),
                    org.openide.util.NbBundle.getMessage(
                        RenamePackageWizardAction1.class,
                        "RenamePackageWizardAction1.performAction(Node[]).JOptionPane.fileOfSourcePackageCouldNotBeCopied.message"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        RenamePackageWizardAction1.class,
                        "RenamePackageWizardAction1.performAction(Node[]).JOptionPane.fileOfSourcePackageCouldNotBeCopied.title"),   // NOI18N
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            // now delete the files and the parent directory if it does not
            // contain any other directories
            try {
                if (current.getFolders(false).hasMoreElements()) {
                    for (final Enumeration<? extends FileObject> e = current.getData(false); e.hasMoreElements();) {
                        e.nextElement().delete();
                    }
                } else {
                    current.delete();
                }
            } catch (final IOException ex) {
                LOG.error("could not delete source files", ex);                                                                       // NOI18N
                JOptionPane.showMessageDialog(
                    WindowManager.getDefault().getMainWindow(),
                    org.openide.util.NbBundle.getMessage(
                        RenamePackageWizardAction1.class,
                        "RenamePackageWizardAction1.performAction(Node[]).JOptionPane.fileOfSourcePackageCouldNotBeDeleted.message"), // NOI18N
                    org.openide.util.NbBundle.getMessage(
                        RenamePackageWizardAction1.class,
                        "RenamePackageWizardAction1.performAction(Node[]).JOptionPane.fileOfSourcePackageCouldNotBeDeleted.title"),   // NOI18N
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    protected boolean enable(final Node[] nodes) {
        if ((nodes == null) || (nodes.length != 1) || nodes[0].getName().contains("<root>")) { // NOI18N
            return false;
        }

        return nodes[0].getCookie(PackageContextCookie.class) != null;
    }
}
