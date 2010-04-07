/*
 * Comparators.java, encoding: UTF-8
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
 * Created on 12. November 2007, 11:17
 *
 */

package de.cismet.cids.abf.utilities;

import de.cismet.cids.jpa.entity.catalog.CatNode;
import de.cismet.cids.jpa.entity.cidsclass.CidsClass;
import de.cismet.cids.jpa.entity.cidsclass.Icon;
import de.cismet.cids.jpa.entity.cidsclass.JavaClass;
import de.cismet.cids.jpa.entity.cidsclass.Type;
import de.cismet.cids.jpa.entity.common.Domain;
import de.cismet.cids.jpa.entity.common.URL;
import de.cismet.cids.jpa.entity.permission.Permission;
import de.cismet.cids.jpa.entity.permission.Policy;
import de.cismet.cids.jpa.entity.user.User;
import de.cismet.cids.jpa.entity.user.UserGroup;
import java.util.Comparator;

/**
 *
 * @author mscholl
 * @version 1.5
 */
public final class Comparators
{

    private Comparators()
    {
    }
    
    public static final class UserGroups implements Comparator<UserGroup>
    {
        @Override
        public int compare(final UserGroup ug1, final UserGroup ug2)
        {
            return ug1.getName().compareToIgnoreCase(ug2.getName());
        }
    }
    
    public static final class Users implements Comparator<User>
    {
        @Override
        public int compare(final User u1, final User u2)
        {
            return u1.getLoginname().compareToIgnoreCase(u2.getLoginname());
        }
    }
    
    public static final class Permissions implements Comparator<Permission>
    {
        @Override
        public int compare(final Permission p1, final Permission p2)
        {
            return p1.getKey().compareTo(p2.getKey());
        }
    }
    
    public static final class Domains implements Comparator<Domain>
    {
        @Override
        public int compare(final Domain d1, final Domain d2)
        {
            return d1.getName().compareToIgnoreCase(d2.getName());
        }
    }
    
    public static final class URLs implements Comparator<URL>
    {
        @Override
        public int compare(final URL u1, final URL u2)
        {
            return u1.toString().compareTo(u2.toString());
        }
    }

    public static final class Policies implements Comparator<Policy>
    {
        @Override
        public int compare(final Policy p1, final Policy p2)
        {
            return p1.getName().compareTo(p2.getName());
        }
    }
    
    public static final class CidsClasses implements Comparator<CidsClass>
    {
        @Override
        public int compare(final CidsClass cc1, final CidsClass cc2)
        {
            return cc1.getTableName().compareToIgnoreCase(cc2.getTableName());
        }
    }
    
    public static final class JavaClasses implements Comparator<JavaClass>
    {
        @Override
        public int compare(final JavaClass jc1, final JavaClass jc2)
        {
            try
            {
                final String[] sa1 = jc1.getQualifier().toLowerCase().
                        split("\\."); // NOI18N
                final String[] sa2 = jc2.getQualifier().toLowerCase().
                        split("\\."); // NOI18N
                return sa1[sa1.length-1].compareTo(sa2[sa2.length-1]);
            }catch(final Exception e)
            {
                return jc1.getQualifier().compareTo(jc2.getQualifier());
            }
        }
    }
    
    public static final class Icons implements Comparator<Icon>
    {
        @Override
        public int compare(final Icon i1, final Icon i2)
        {
            return i1.getName().toLowerCase().compareTo(i2.getName().
                    toLowerCase());
        }
    }
    
    public static final class AttrTypes implements Comparator<Type>
    {
        @Override
        public int compare(final Type t1, final Type t2)
        {
            return t1.getName().compareTo(t2.getName());
        }
    }
    
    public static final class CatNodes implements Comparator<CatNode>
    {
        @Override
        public int compare(final CatNode cn1, final CatNode cn2)
        {
            return cn1.getName().compareTo(cn2.getName());
        }
    }
}