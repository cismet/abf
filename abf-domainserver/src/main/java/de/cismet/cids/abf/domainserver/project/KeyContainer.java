/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project;

import java.util.ArrayList;
import java.util.Collection;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class KeyContainer<T> {

    //~ Instance fields --------------------------------------------------------

    private final int hashcode;
    private final T object;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new KeyContainer object.
     *
     * @param  object  DOCUMENT ME!
     */
    public KeyContainer(final T object) {
        this.object = object;
        this.hashcode = object.hashCode();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public T getObject() {
        return object;
    }

    @Override
    public int hashCode() {
        return hashcode;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        final KeyContainer other = (KeyContainer)obj;

        return this.hashcode == other.hashcode;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   <C>    DOCUMENT ME!
     * @param   clazz  DOCUMENT ME!
     * @param   coll   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static <C extends Object> Collection<KeyContainer<C>> convertCollection(final Class<C> clazz,
            final Collection<C> coll) {
        final ArrayList<KeyContainer<C>> keys = new ArrayList<KeyContainer<C>>(coll.size());
        for (final C o : coll) {
            keys.add(new KeyContainer<C>(o));
        }

        return keys;
    }
}
