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
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;

import java.awt.Component;
import java.awt.Dialog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.text.MessageFormat;

import java.util.Arrays;

import javax.swing.JComponent;

import de.cismet.cids.abf.librarysupport.project.nodes.cookies.LibrarySupportContextCookie;
import de.cismet.cids.abf.librarysupport.project.nodes.cookies.SourceContextCookie;
import de.cismet.cids.abf.librarysupport.project.nodes.cookies.StarterManagementContextCookie;

/**
 * DOCUMENT ME!
 *
 * @author   mscholl
 * @version  1.8
 */
public final class NewStarterWizardAction extends NodeAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(NewStarterWizardAction.class);

    public static final String PROP_SOURCE_DIR = "property_sourceDir";                     // NOI18N
    public static final String PROP_PROJECT = "property_project";                          // NOI18N
    public static final String PROP_NEW_STARTER_BASE_FILE = "property_newStarterBaseFile"; // NOI18N
    public static final String PROP_NEW_STARTER_NAME = "property_newStarterName";          // NOI18N

    //~ Instance fields --------------------------------------------------------

    private transient WizardDescriptor.Panel<WizardDescriptor>[] panels;

    //~ Methods ----------------------------------------------------------------

    /**
     * Initialize panels representing individual wizard's steps and sets various properties for them influencing wizard
     * appearance.
     *
     * @return  DOCUMENT ME!
     */
    // it is impossible to create a typed array
    @SuppressWarnings("unchecked")
    private WizardDescriptor.Panel<WizardDescriptor>[] getPanels() {
        if (panels == null) {
            panels = new WizardDescriptor.Panel[] { new NewStarterWizardPanel1() };
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
        }
        
        return Arrays.copyOf(panels, panels.length);
    }

    @Override
    public String getName() {
        return org.openide.util.NbBundle.getMessage(
                NewStarterWizardAction.class,
                "NewStarterWizardAction.getName().returnvalue");
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
        final LibrarySupportContextCookie lscc = nodes[0].getCookie(LibrarySupportContextCookie.class);
        final SourceContextCookie scc = nodes[0].getCookie(SourceContextCookie.class);
        
        assert lscc != null;
        assert scc != null;
        
        final FileObject srcDir;
        try {
            srcDir = scc.getSourceObject();
        } catch (final FileNotFoundException fnfe) {
            LOG.error("could not obtain source dir", fnfe); // NOI18N
            return;
        }
        final WizardDescriptor wizard = new WizardDescriptor(getPanels());
        wizard.putProperty(PROP_SOURCE_DIR, srcDir);
        wizard.putProperty(PROP_PROJECT, lscc.getLibrarySupportContext());
        wizard.setTitleFormat(new MessageFormat("{0}"));    // NOI18N
        wizard.setTitle(org.openide.util.NbBundle.getMessage(
                NewStarterWizardAction.class,
                "NewStarterWizardAction.performAction(Node[]).wizard.title"));
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(wizard);
        dialog.setVisible(true);
        dialog.toFront();
        final boolean cancelled = wizard.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            final String manBaseFile = wizard.getProperty(
                    PROP_NEW_STARTER_BASE_FILE).toString();
            final String manName = wizard.getProperty(PROP_NEW_STARTER_NAME).toString();
            try {
                srcDir.createFolder(manName);
                File file = new File(manBaseFile);
                if (file.exists() && file.isFile()) {
                    final FileObject baseMan = FileUtil.toFileObject(file);
                    final String tPath = FileUtil.toFile(srcDir).getAbsolutePath();
                    file = new File(tPath);
                    final FileObject target = FileUtil.toFileObject(file);
                    baseMan.copy(target, manName, "mf");    // NOI18N
                } else {
                    srcDir.createData(manName, "mf");       // NOI18N
                }
            } catch (final IOException ex) {
                LOG.error("could not create new source folder", ex); // NOI18N
                ErrorManager.getDefault()
                        .annotate(
                            ex,
                            org.openide.util.NbBundle.getMessage(
                                NewStarterWizardAction.class,
                                "NewStarterWizardAction.performAction(Node[]).ErrorManager.couldNotCreateNewSourceFolderError")); // NOI18N
            }
        }
    }

    @Override
    protected boolean enable(final Node[] nodes) {
        if ((nodes == null) || (nodes.length != 1)) {
            return false;
        }
        return nodes[0].getCookie(StarterManagementContextCookie.class) != null;
    }
}
