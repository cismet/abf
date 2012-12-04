/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.users;

import org.openide.util.NbBundle;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.security.SecureRandom;

import java.util.Arrays;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public class DefaultPasswordComponentModel implements PasswordComponentModel {

    //~ Static fields/initializers ---------------------------------------------

    public static final String PROP_PW = "__prop_pw__";                     // NOI18N
    public static final String PROP_PW_MATCH = "__prop_pw_match__";         // NOI18N
    public static final String PROP_INFO_MESSAGE = "__prop_info_message__"; // NOI18N
    public static final String PROP_WARN_MESSAGE = "__prop_warn_message__"; // NOI18N

    //~ Instance fields --------------------------------------------------------

    private final transient PropertyChangeSupport changeSupport;

    private transient char[] password;
    private transient char[] passwordMatch;
    private transient String infoMessage;
    private transient String warnMessage;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DefaultPasswordComponentModel object.
     */
    public DefaultPasswordComponentModel() {
        this.changeSupport = new PropertyChangeSupport(this);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isValid() {
        if ((password != null) && (password.length > 4)) {
            if (Arrays.equals(password, passwordMatch)) {
                setWarnMessage(null);
                setInfoMessage(NbBundle.getMessage(
                        DefaultPasswordComponentModel.class,
                        "DefaultPasswordComponentModel.isValid().infoMessage.pwsMatch")); // NOI18N

                return true;
            } else {
                setWarnMessage(NbBundle.getMessage(
                        DefaultPasswordComponentModel.class,
                        "DefaultPasswordComponentModel.isValid().warnMessage.pwsDontMatch")); // NOI18N
            }
        } else {
            setWarnMessage(NbBundle.getMessage(
                    DefaultPasswordComponentModel.class,
                    "DefaultPasswordComponentModel.isValid().warnMessage.pwTooShort"));       // NOI18N
        }

        return false;
    }

    @Override
    public void clear() {
        final SecureRandom random = new SecureRandom();
        byte[] wipe;

        if (password != null) {
            wipe = new byte[password.length];

            random.setSeed(System.nanoTime());
            random.nextBytes(wipe);

            for (int i = 0; i < wipe.length; ++i) {
                password[i] = (char)wipe[i];
            }

            setPassword(null);
        }

        if (passwordMatch != null) {
            wipe = new byte[passwordMatch.length];

            random.setSeed(System.nanoTime());
            random.nextBytes(wipe);

            for (int i = 0; i < wipe.length; ++i) {
                passwordMatch[i] = (char)wipe[i];
            }

            setPasswordMatch(null);
        }
    }

    @Override
    public void setPassword(final char[] password) {
        final char[] oldPw = this.password;

        this.password = password;

        changeSupport.firePropertyChange(PROP_PW, oldPw, password);
    }

    @Override
    public char[] getPassword() {
        return password;
    }

    @Override
    public void setPasswordMatch(final char[] passwordMatch) {
        final char[] oldPwMatch = this.passwordMatch;

        this.passwordMatch = passwordMatch;

        changeSupport.firePropertyChange(PROP_PW_MATCH, oldPwMatch, passwordMatch);
    }

    @Override
    public char[] getPasswordMatch() {
        return passwordMatch;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getInfoMessage() {
        return infoMessage;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  infoMessage  DOCUMENT ME!
     */
    public void setInfoMessage(final String infoMessage) {
        final String old = this.infoMessage;

        this.infoMessage = infoMessage;

        changeSupport.firePropertyChange(PROP_INFO_MESSAGE, old, infoMessage);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getWarnMessage() {
        return warnMessage;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  warnMessage  DOCUMENT ME!
     */
    public void setWarnMessage(final String warnMessage) {
        final String old = this.warnMessage;

        this.warnMessage = warnMessage;

        changeSupport.firePropertyChange(PROP_WARN_MESSAGE, old, warnMessage);
    }

    @Override
    public void addPropertyChangeListener(final PropertyChangeListener cl) {
        changeSupport.addPropertyChangeListener(cl);
    }

    @Override
    public void removePropertyChangeListener(final PropertyChangeListener cl) {
        changeSupport.removePropertyChangeListener(cl);
    }
}
