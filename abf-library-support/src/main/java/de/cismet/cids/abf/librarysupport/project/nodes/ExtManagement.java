/*
 * ExtManagement.java, encoding: UTF-8
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
 * Created on 13. August 2007, 12:39
 *
 */

package de.cismet.cids.abf.librarysupport.project.nodes;

import de.cismet.cids.abf.librarysupport.project.LibrarySupportProject;
import java.awt.Image;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author mscholl
 * @version 1.2
 */
public final class ExtManagement extends FilterNode
{
    public ExtManagement(final LibrarySupportProject project, final Node n)
    {
        super(n, new FilterNode.Children(n), new ProxyLookup(new Lookup[] { 
                Lookups.singleton(project), n.getLookup() }));
    }

    @Override
    public Image getOpenedIcon(final int i)
    {
        return getIcon(i);
    }

    @Override
    public Image getIcon(final int i)
    {
        return ImageUtilities.loadImage(LibrarySupportProject.IMAGE_FOLDER
                + "extern_16.png"); // NOI18N
    }

    @Override
    public String getDisplayName()
    {
        return getName();
    }

    @Override
    public String getName()
    {
        return org.openide.util.NbBundle.getMessage(
                ExtManagement.class, "ExtManagement.getName().returnvalue"); // NOI18N
    }
}
