/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.utilities.windows;

import de.cismet.cids.abf.utilities.UtilityCommons;

import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.Image;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.windows.WindowManager;

/**
 * DOCUMENT ME!
 *
 * @author   mscholl
 * @version  $Revision$, $Date$
 */
public final class ErrorUtils {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ErrorUtils object.
     */
    private ErrorUtils() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  message  DOCUMENT ME!
     * @param  t        DOCUMENT ME!
     */
    public static void showErrorMessage(final String message, final Throwable t) {
        showErrorMessage(message, org.openide.util.NbBundle.getMessage(ErrorUtils.class, "Dsc_unexpectedError"), t); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @param  message  DOCUMENT ME!
     * @param  title    DOCUMENT ME!
     * @param  t        DOCUMENT ME!
     */
    public static void showErrorMessage(final String message, final String title, final Throwable t) {
        EventQueue.invokeLater(
            new Runnable() {

                @Override
                public void run() {
                    final Image errorImage = ImageUtilities.loadImage(UtilityCommons.IMAGE_FOLDER + "error.png"); // NOI18N
                    final Image bugImage = ImageUtilities.loadImage(UtilityCommons.IMAGE_FOLDER + "bug.png");     // NOI18N
                    final Image mergedImage = ImageUtilities.mergeImages(errorImage, bugImage, 35, 38);
                    final ErrorPanel errorPanel = new ErrorPanel(message, t, mergedImage);
                    final DialogDescriptor dd = new DialogDescriptor(
                            errorPanel,
                            title,
                            true,
                            new Object[] { DialogDescriptor.OK_OPTION },
                            null,
                            DialogDescriptor.DEFAULT_ALIGN,
                            HelpCtx.DEFAULT_HELP,
                            null);
                    final Dialog d = DialogDisplayer.getDefault().createDialog(dd);
                    d.setLocationRelativeTo(WindowManager.getDefault().getMainWindow());
                    d.setVisible(true);
                    d.toFront();
                }
            });
    }
}
