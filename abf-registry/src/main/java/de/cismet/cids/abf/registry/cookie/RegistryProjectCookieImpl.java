/*
 * RegistryProjectCookieImpl.java, encoding: UTF-8
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
 * Created on 04.02.2010, 12:23:33
 *
 */

package de.cismet.cids.abf.registry.cookie;

import de.cismet.cids.abf.registry.RegistryProject;

/**
 *
 * @author Martin Scholl
 */
public class RegistryProjectCookieImpl implements RegistryProjectCookie
{
    private final transient RegistryProject project;

    public RegistryProjectCookieImpl(final RegistryProject project)
    {
        this.project = project;
    }

    @Override
    public RegistryProject getProject()
    {
        return project;
    }
}