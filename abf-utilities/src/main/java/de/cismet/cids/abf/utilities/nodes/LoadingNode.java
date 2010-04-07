/*
 * ConnectAction.java, encoding: UTF-8
 *
 * Copyright (C) by:
 *
 *----------------------------
 * cismet GmbH
 * Altenkesslerstr. 17
 * Gebaeude D2
 * 66115 Saarbruecken
 * http://www.cismet.de
 *----------------------------
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * See: http://www.gnu.org/licenses/lgpl.txt
 *
 *----------------------------
 * Author:
 * martin.scholl@cismet.de
 *----------------------------
 *
 * Created on ???
 *
 */

package de.cismet.cids.abf.utilities.nodes;

import de.cismet.cids.abf.utilities.UtilityCommons;
import java.awt.Image;
import org.apache.log4j.Logger;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;

/**
 * Very very simple node icon animation
 *
 * @author mscholl
 */
public class LoadingNode extends AbstractNode
{
    private static final transient Logger LOG = Logger.getLogger(
            LoadingNode.class);

    private transient int counter;
    private transient boolean process;
    final transient Thread loadingThread;

    // preload images to prevent expensive file access during animation
    private enum LoadingImage
    {
        IMAGE_1 ("circling_arrows_1.gif"), // NOI18N
        IMAGE_2 ("circling_arrows_2.gif"), // NOI18N
        IMAGE_3 ("circling_arrows_3.gif"), // NOI18N
        IMAGE_4 ("circling_arrows_4.gif"), // NOI18N
        IMAGE_5 ("circling_arrows_5.gif"), // NOI18N
        IMAGE_6 ("circling_arrows_6.gif"), // NOI18N
        IMAGE_7 ("circling_arrows_7.gif"), // NOI18N
        IMAGE_8 ("circling_arrows_8.gif"), // NOI18N
        IMAGE_9 ("circling_arrows_9.gif"); // NOI18N

        private final Image image;

        LoadingImage(final String name)
        {
            image = ImageUtilities.loadImage(
                    UtilityCommons.IMAGE_FOLDER + name);
        }

        public Image getImage()
        {
            return image;
        }
    }


    // TODO: check if thread is disposed if node shall be gc'ed
    public LoadingNode()
    {
        super(Children.LEAF);
        setDisplayName(org.openide.util.NbBundle.getMessage(
                LoadingNode.class, "Dsc_pleaseWait")); // NOI18N
        counter = 1;
        final Thread t = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    while(!Thread.interrupted())
                    {
                        if(!process)
                        {
                            fireIconChange();
                        }
                        Thread.sleep(40);
                    }
                }catch(final Exception e)
                {
                    if(LOG.isInfoEnabled())
                    {
                        LOG.info("animation interrupted", e); // NOI18N
                    }
                }
                if(LOG.isInfoEnabled())
                {
                    LOG.info("exiting animation thread"); // NOI18N
                }
            }
        });
        t.setName("loadingnode animation thread"); // NOI18N
        t.setPriority(4);
        loadingThread = t;
    }

    @Override
    public Image getIcon(final int arg0)
    {
        if(!loadingThread.isAlive()
                && !loadingThread.getState().equals(Thread.State.TERMINATED))
        {
            loadingThread.start();
        }
        process = true;
        final Image i = LoadingImage.values()[counter].getImage();
        if(++counter == 9)
        {
            counter = 1;
        }
        process = false;
        return i;
    }

    @Override
    public boolean canCopy()
    {
        return false;
    }

    @Override
    public boolean canCut()
    {
        return false;
    }

    @Override
    public boolean canDestroy()
    {
        return false;
    }

    @Override
    public boolean canRename()
    {
        return false;
    }

    public void dispose()
    {
        if(loadingThread.isAlive())
        {
            loadingThread.interrupt();
        }
    }
}