/*
 * ProjectNode.java, encoding: UTF-8
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
 * Created on 14. August 2007, 12:45
 *
 */

package de.cismet.cids.abf.librarysupport.project.nodes;

import de.cismet.cids.abf.librarysupport.project.nodes.cookies.LibrarySupportContextCookie;
import de.cismet.cids.abf.librarysupport.project.LibrarySupportProject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;

/**
 *
 * @author mscholl
 * @version 1.3
 */
public abstract class ProjectNode extends AbstractNode implements
        LibrarySupportContextCookie
{
    protected final transient LibrarySupportProject project;
    
    public ProjectNode(final Children ch, final LibrarySupportProject project)
    {
        super(ch);
        this.project = project;
        getCookieSet().add(this);
    }

    @Override
    public LibrarySupportProject getLibrarySupportContext()
    {
        return project;
    }
}