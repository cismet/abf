/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.users;

import java.beans.PropertyChangeListener;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public interface PasswordComponentModel {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean isValid();
    /**
     * DOCUMENT ME!
     */
    void clear();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    char[] getPassword();
    /**
     * DOCUMENT ME!
     *
     * @param  password  DOCUMENT ME!
     */
    void setPassword(char[] password);

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    char[] getPasswordMatch();
    /**
     * DOCUMENT ME!
     *
     * @param  password  DOCUMENT ME!
     */
    void setPasswordMatch(char[] password);

    /**
     * DOCUMENT ME!
     *
     * @param  cl  DOCUMENT ME!
     */
    void addPropertyChangeListener(final PropertyChangeListener cl);
    /**
     * DOCUMENT ME!
     *
     * @param  cl  DOCUMENT ME!
     */
    void removePropertyChangeListener(final PropertyChangeListener cl);
}
