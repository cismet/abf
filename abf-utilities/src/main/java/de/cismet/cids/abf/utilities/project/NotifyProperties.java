/*
 * NotifyProperties.java, encoding: UTF-8
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
 * thorsten.hell@cismet.de
 * martin.scholl@cismet.de
 *----------------------------
 *
 * Created on 04.02.2010, 15:54:55
 *
 */

package de.cismet.cids.abf.utilities.project;

import java.util.Properties;
import org.netbeans.spi.project.ProjectState;

/**
 *
 * @author thorsten.hell@cismet.de
 * @author martin.scholl@cismet.de
 */
public class NotifyProperties extends Properties
{
    private final transient ProjectState state;

    public NotifyProperties(final ProjectState state)
    {
        this.state = state;
    }

    @Override
    public Object put(final Object key, final Object val)
    {
        final Object result = super.put(key, val);
        if(((result == null) != (val == null)) 
                || (result != null && val != null && !val.equals(result)))
        {
            state.markModified();
        }
        return result;
    }
}