/*
 * PackageContextCookieImpl.java, encoding: UTF-8
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
 * Created on 20. August 2007, 14:01
 *
 */

package de.cismet.cids.abf.librarysupport.project.nodes.cookies;

import org.openide.filesystems.FileObject;

/**
 *
 * @author mscholl
 * @version 1.2
 */
public final class PackageContextCookieImpl implements PackageContextCookie
{
    private final transient FileObject root;
    private final transient FileObject current;
    
    public PackageContextCookieImpl(final FileObject root, final FileObject cur)
    {
        this.root = root;
        this.current = cur;
    }

    @Override
    public FileObject getRootFolder()
    {
        return root;
    }

    @Override
    public FileObject getCurrentFolder()
    {
        return current;
    }
}