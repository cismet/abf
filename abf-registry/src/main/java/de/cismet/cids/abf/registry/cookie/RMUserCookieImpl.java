/*
 * RMUserCookieImpl.java, encoding: UTF-8
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
 * Created on 04.02.2010, 12:07:56
 *
 */

package de.cismet.cids.abf.registry.cookie;

import Sirius.server.registry.rmplugin.util.RMUser;
import java.util.HashSet;
import java.util.Set;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author Martin Scholl
 */
public class RMUserCookieImpl implements RMUserCookie
{
    private final transient Children children;

    public RMUserCookieImpl(final Children children)
    {
        this.children = children;
    }

    @Override
    public Set<RMUser> getRMUsers()
    {
        final Set<RMUser> users = new HashSet<RMUser>();
        for(final Node n : children.getNodes())
        {
            final RMUserCookie cookie = n.getCookie(RMUserCookie.class);
            if(cookie != null)
            {
                users.addAll(cookie.getRMUsers());
            }
        }
        return users;
    }
}