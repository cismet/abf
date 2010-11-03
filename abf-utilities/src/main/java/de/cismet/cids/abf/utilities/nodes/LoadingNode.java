/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.utilities.nodes;

import org.apache.log4j.Logger;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;

import java.awt.Image;

import de.cismet.cids.abf.utilities.UtilityCommons;

/**
 * Very very simple node icon animation.
 *
 * @author   mscholl
 * @version  $Revision$, $Date$
 */
public class LoadingNode extends AbstractNode {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(LoadingNode.class);

    //~ Enums ------------------------------------------------------------------

    /**
     * preload images to prevent expensive file access during animation.
     *
     * @version  $Revision$, $Date$
     */
    private enum LoadingImage {

        //~ Enum constants -----------------------------------------------------

        IMAGE_1("circling_arrows_1.gif"), // NOI18N
        IMAGE_2("circling_arrows_2.gif"), // NOI18N
        IMAGE_3("circling_arrows_3.gif"), // NOI18N
        IMAGE_4("circling_arrows_4.gif"), // NOI18N
        IMAGE_5("circling_arrows_5.gif"), // NOI18N
        IMAGE_6("circling_arrows_6.gif"), // NOI18N
        IMAGE_7("circling_arrows_7.gif"), // NOI18N
        IMAGE_8("circling_arrows_8.gif"), // NOI18N
        IMAGE_9("circling_arrows_9.gif"); // NOI18N

        //~ Instance fields ----------------------------------------------------

        private final Image image;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LoadingImage object.
         *
         * @param  name  DOCUMENT ME!
         */
        LoadingImage(final String name) {
            image = ImageUtilities.loadImage(UtilityCommons.IMAGE_FOLDER + name);
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public Image getImage() {
            return image;
        }
    }

    //~ Instance fields --------------------------------------------------------

    final transient Thread loadingThread;

    private transient int counter;
    private transient boolean process;

    //~ Constructors -----------------------------------------------------------

    /**
     * TODO: check if thread is disposed if node shall be gc'ed
     */
    public LoadingNode() {
        super(Children.LEAF);
        setDisplayName(org.openide.util.NbBundle.getMessage(LoadingNode.class, "LoadingNode.displayName")); // NOI18N
        counter = 1;
        final Thread t = new Thread(
                new Runnable() {

                    @Override
                    public void run() {
                        try {
                            while (!Thread.interrupted()) {
                                if (!process) {
                                    fireIconChange();
                                }
                                Thread.sleep(40);
                            }
                        } catch (final Exception e) {
                            if (LOG.isInfoEnabled()) {
                                LOG.info("animation interrupted", e); // NOI18N
                            }
                        }
                        if (LOG.isInfoEnabled()) {
                            LOG.info("exiting animation thread");     // NOI18N
                        }
                    }
                });
        t.setName("loadingnode animation thread");                    // NOI18N
        t.setPriority(4);
        loadingThread = t;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Image getIcon(final int arg0) {
        if (!loadingThread.isAlive() && !loadingThread.getState().equals(Thread.State.TERMINATED)) {
            loadingThread.start();
        }
        process = true;
        final Image i = LoadingImage.values()[counter].getImage();
        if (++counter == 9) {
            counter = 1;
        }
        process = false;
        return i;
    }

    @Override
    public boolean canCopy() {
        return false;
    }

    @Override
    public boolean canCut() {
        return false;
    }

    @Override
    public boolean canDestroy() {
        return false;
    }

    @Override
    public boolean canRename() {
        return false;
    }

    /**
     * DOCUMENT ME!
     */
    public void dispose() {
        if (loadingThread.isAlive()) {
            loadingThread.interrupt();
        }
    }
}
