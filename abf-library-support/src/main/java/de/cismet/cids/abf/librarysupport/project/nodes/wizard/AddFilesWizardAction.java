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
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.EventQueue;

import java.io.File;
import java.io.IOException;

import java.text.MessageFormat;

import java.util.HashMap;

import javax.swing.JComponent;

import de.cismet.cids.abf.librarysupport.project.LibrarySupportProject;
import de.cismet.cids.abf.librarysupport.project.nodes.cookies.PackageContextCookie;
import de.cismet.cids.abf.utilities.files.PackageUtils;

/**
 * DOCUMENT ME!
 *
 * @author   mscholl
 * @version  1.7
 */
public final class AddFilesWizardAction extends NodeAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(AddFilesWizardAction.class);

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
    // it is impossible to create a typed array
    @SuppressWarnings("unchecked")
    private WizardDescriptor.Panel<WizardDescriptor>[] getPanels(final FileObject root,
            final FileObject cur) {
        final WizardDescriptor.Panel<WizardDescriptor>[] panels = new WizardDescriptor.Panel[] { new AddFilesWizardPanel1(root, cur) };
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
                jc.putClientProperty(
                    WizardDescriptor.PROP_CONTENT_SELECTED_INDEX,
                    Integer.valueOf(i));
                // Sets steps names for a panel
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA,
                    steps);
                // Turn on subtitle creation on each step
                jc.putClientProperty(
                    WizardDescriptor.PROP_AUTO_WIZARD_STYLE,
                    Boolean.TRUE);
                // Show steps on the left side with the image on the
                // background
                jc.putClientProperty(
                    WizardDescriptor.PROP_CONTENT_DISPLAYED,
                    Boolean.TRUE);
                // Turn on numbering of all steps
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED,
                    Boolean.TRUE);
            }
        }
        
        return panels;
    }

    @Override
    public String getName() {
        return org.openide.util.NbBundle.getMessage(
                AddFilesWizardAction.class,
                "AddFilesWizardAction.getName().returnvalue"); // NOI18N
    }

    @Override
    public String iconResource() {
        return LibrarySupportProject.IMAGE_FOLDER + "file_(add)_16x16.png"; // NOI18N
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
        final PackageContextCookie cookie = nodes[0].getCookie(PackageContextCookie.class);
        final WizardDescriptor wizard = new WizardDescriptor(getPanels(
                    cookie.getRootFolder(),
                    cookie.getCurrentFolder()));
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wizard.setTitleFormat(new MessageFormat("{0}"));               // NOI18N
        wizard.setTitle(org.openide.util.NbBundle.getMessage(
                AddFilesWizardAction.class,
                "AddFilesWizardAction.performAction().wizard.title")); // NOI18N
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(wizard);
        dialog.setVisible(true);
        dialog.toFront();
        final boolean cancelled = wizard.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            final File[] choosenFiles = (File[])wizard.getProperty(
                    AddFilesWizardPanel1.CHOOSEN_FILES);
            final String pakkage = (String)wizard.getProperty(
                    AddFilesWizardPanel1.CHOOSEN_PACKAGE);
            if ((choosenFiles == null)
                        || (choosenFiles.length < 1)
                        || (pakkage == null)
                        || pakkage.equals(""))                         // NOI18N
            {
                return;
            }
            EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        final FileObject destinationFO = PackageUtils.toFileObject(
                                cookie.getRootFolder(),
                                pakkage);
                        FileLock destLock = null;
                        final HashMap<FileObject, Throwable> erroneous = new HashMap<FileObject, Throwable>();
                        try {
                            destLock = destinationFO.lock();
                            FileLock srcLock = null;
                            FileObject srcFile = null;
                            for (final File file : choosenFiles) {
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug("chosen file: " // NOI18N
                                                + file.getName());
                                }
                                try {
                                    srcFile = FileUtil.toFileObject(file);
                                    srcLock = srcFile.lock();
                                    srcFile.copy(destinationFO, srcFile.getName(),
                                        srcFile.getExt());
                                } catch (IOException ex) {
                                    LOG.error("could not copy file '" // NOI18N
                                                + srcFile.getNameExt()
                                                + "'", ex); // NOI18N
                                    erroneous.put(srcFile, ex);
                                } finally {
                                    if ((srcLock != null) && srcLock.isValid()) {
                                        srcLock.releaseLock();
                                    }
                                }
                            }
//                        if(!erroneous.isEmpty())
//                        {
//                            // TODO annotate errors
//                        }
                        } catch (final IOException ex) {
                            LOG.error("problem with destination folder", // NOI18N
                                ex);
                            ErrorManager.getDefault()
                                    .annotate(
                                        ex,
                                        org.openide.util.NbBundle.getMessage(
                                            AddFilesWizardAction.class,
                                            "AddFilesWizardAction.performAction().ErrorManager.message")); // NOI18N
                        } finally {
                            if ((destLock != null) && destLock.isValid()) {
                                destLock.releaseLock();
                            }
                        }
                    }
                });
        }
    }

    @Override
    protected boolean enable(final Node[] nodes) {
        if ((nodes == null) || (nodes.length != 1)) {
            return false;
        }
        return nodes[0].getCookie(PackageContextCookie.class) != null;
    }
}
