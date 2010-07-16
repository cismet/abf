/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
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
 * DOCUMENT ME!
 *
 * @author   mscholl
 * @version  1.5
 */
public final class Comparators {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new Comparators object.
     */
    private Comparators() {
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static final class UserGroups implements Comparator<UserGroup> {

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @param   ug1  DOCUMENT ME!
         * @param   ug2  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        @Override
        public int compare(final UserGroup ug1, final UserGroup ug2) {
            return ug1.getName().compareToIgnoreCase(ug2.getName());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static final class Users implements Comparator<User> {

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @param   u1  DOCUMENT ME!
         * @param   u2  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        @Override
        public int compare(final User u1, final User u2) {
            return u1.getLoginname().compareToIgnoreCase(u2.getLoginname());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static final class Permissions implements Comparator<Permission> {

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @param   p1  DOCUMENT ME!
         * @param   p2  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        @Override
        public int compare(final Permission p1, final Permission p2) {
            return p1.getKey().compareTo(p2.getKey());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static final class Domains implements Comparator<Domain> {

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @param   d1  DOCUMENT ME!
         * @param   d2  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        @Override
        public int compare(final Domain d1, final Domain d2) {
            return d1.getName().compareToIgnoreCase(d2.getName());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static final class URLs implements Comparator<URL> {

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @param   u1  DOCUMENT ME!
         * @param   u2  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        @Override
        public int compare(final URL u1, final URL u2) {
            return u1.toString().compareTo(u2.toString());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static final class Policies implements Comparator<Policy> {

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @param   p1  DOCUMENT ME!
         * @param   p2  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        @Override
        public int compare(final Policy p1, final Policy p2) {
            return p1.getName().compareTo(p2.getName());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static final class CidsClasses implements Comparator<CidsClass> {

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @param   cc1  DOCUMENT ME!
         * @param   cc2  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        @Override
        public int compare(final CidsClass cc1, final CidsClass cc2) {
            return cc1.getTableName().compareToIgnoreCase(cc2.getTableName());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static final class JavaClasses implements Comparator<JavaClass> {

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @param   jc1  DOCUMENT ME!
         * @param   jc2  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        @Override
        public int compare(final JavaClass jc1, final JavaClass jc2) {
            try {
                final String[] sa1 = jc1.getQualifier().toLowerCase().split("\\."); // NOI18N
                final String[] sa2 = jc2.getQualifier().toLowerCase().split("\\."); // NOI18N
                return sa1[sa1.length - 1].compareTo(sa2[sa2.length - 1]);
            } catch (final Exception e) {
                return jc1.getQualifier().compareTo(jc2.getQualifier());
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static final class Icons implements Comparator<Icon> {

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @param   i1  DOCUMENT ME!
         * @param   i2  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        @Override
        public int compare(final Icon i1, final Icon i2) {
            return i1.getName().toLowerCase().compareTo(i2.getName().toLowerCase());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static final class AttrTypes implements Comparator<Type> {

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @param   t1  DOCUMENT ME!
         * @param   t2  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        @Override
        public int compare(final Type t1, final Type t2) {
            return t1.getName().compareTo(t2.getName());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static final class CatNodes implements Comparator<CatNode> {

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @param   cn1  DOCUMENT ME!
         * @param   cn2  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        @Override
        public int compare(final CatNode cn1, final CatNode cn2) {
            return String.valueOf(cn1.getName()).compareTo(String.valueOf(cn2.getName()));
        }
    }
}
