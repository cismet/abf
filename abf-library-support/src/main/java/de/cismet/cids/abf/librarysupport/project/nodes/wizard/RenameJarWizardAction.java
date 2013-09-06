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
import org.openide.nodes.Node;

import java.awt.Dialog;

import java.io.FileNotFoundException;

import java.text.MessageFormat;

import de.cismet.cids.abf.librarysupport.project.nodes.cookies.LibrarySupportContextCookie;
import de.cismet.cids.abf.librarysupport.project.nodes.cookies.SourceContextCookie;

/**
 * DOCUMENT ME!
 *
 * @author   mscholl
 * @version  1.1
 */
public final class RenameJarWizardAction extends NewJarWizardAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(
            RenameJarWizardAction.class);

    //~ Methods ----------------------------------------------------------------

    @Override
    protected boolean enable(final Node[] nodes) {
        if ((nodes == null) || (nodes.length != 1) || !super.enable(nodes)) {
            return false;
        }
        return nodes[0].getCookie(SourceContextCookie.class) != null;
    }

    @Override
    public String getName() {
        return org.openide.util.NbBundle.getMessage(
                RenameJarWizardAction.class,
                "RenameJarWizardAction.getName().returnvalue"); // NOI18N
    }

    @Override
    public String iconResource() {
        return null;
    }

    @Override
    protected void performAction(final Node[] nodes) {
        final LibrarySupportContextCookie lscc = nodes[0].getCookie(
                LibrarySupportContextCookie.class);
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
        wizard.putProperty(PROP_SOURCE_DIR, srcDir.getParent());
        wizard.setTitleFormat(new MessageFormat("{0}"));    // NOI18N
        wizard.setTitle(org.openide.util.NbBundle.getMessage(
                RenameJarWizardAction.class,
                "RenameJarWizardAction.performAction(Node[]).wizard.title")); // NOI18N
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(wizard);
        dialog.setVisible(true);
        dialog.toFront();
        final boolean cancelled = wizard.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            nodes[0].setName(wizard.getProperty(PROP_NEW_JAR_NAME).toString());
        }
    }
}
