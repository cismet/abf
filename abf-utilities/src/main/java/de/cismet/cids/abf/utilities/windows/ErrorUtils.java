/*
 * ErrorUtils.java, encoding: UTF-8
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
 * Created on 25. Januar 2008, 11:15
 *
 */

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
 *
 * @author mscholl
 */
public final class ErrorUtils
{
    private ErrorUtils()
    {
    }
    
    public static void showErrorMessage(final String message, final Throwable t)
    {
        showErrorMessage(message, org.openide.util.NbBundle.getMessage(
                ErrorUtils.class, "Dsc_unexpectedError"), t); // NOI18N
    }
    
    public static void showErrorMessage(final String message, final String 
            title, final Throwable t)
    {
        EventQueue.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                final Image errorImage = ImageUtilities.loadImage(
                        UtilityCommons.IMAGE_FOLDER + "error.png"); // NOI18N
                final Image bugImage = ImageUtilities.loadImage(
                        UtilityCommons.IMAGE_FOLDER + "bug.png"); // NOI18N
                final Image mergedImage = ImageUtilities.mergeImages(
                        errorImage, bugImage, 35, 38);
                final ErrorPanel errorPanel = new ErrorPanel(message, t, 
                        mergedImage);
                final DialogDescriptor dd = new DialogDescriptor(errorPanel, 
                        title, true, new Object[] {DialogDescriptor.OK_OPTION}, 
                        null, DialogDescriptor.DEFAULT_ALIGN, HelpCtx.
                        DEFAULT_HELP, null);
                final Dialog d = DialogDisplayer.getDefault().createDialog(dd);
                d.setLocationRelativeTo(WindowManager.getDefault().
                        getMainWindow());
                d.setVisible(true);
                d.toFront();
            }
        });
    }
}