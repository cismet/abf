/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.options;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import org.netbeans.spi.options.OptionsPanelController;

import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.util.Properties;

import javax.swing.JComponent;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
//@OptionsPanelController.SubRegistration(
//    location = "ABF",
//    displayName = "#AdvancedOption_DisplayName_Logging",
//    keywords = "#AdvancedOption_Keywords_Logging",
//    keywordsCategory = "ABF/Logging"
//)
// TODO: more control over logging facilities
public final class LoggingOptionsPanelController extends OptionsPanelController {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(LoggingOptionsPanelController.class);

    //~ Instance fields --------------------------------------------------------

    private LoggingPanel panel;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private boolean changed;

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    public static void adjustLogLevel() {
        final String loglevel = NbPreferences.forModule(LoggingPanel.class).get(LoggingPanel.PROP_LOGLEVEL, "DEBUG"); // NOI18N

        final Properties p = new Properties();
        p.put("log4j.appender.Remote", "org.apache.log4j.net.SocketAppender"); // NOI18N
        p.put("log4j.appender.Remote.remoteHost", "localhost");                // NOI18N
        p.put("log4j.appender.Remote.port", "4445");                           // NOI18N
        p.put("log4j.appender.Remote.locationInfo", "true");                   // NOI18N

        p.put("log4j.rootLogger", "ALL,Remote"); // NOI18N

        p.put("log4j.logger.org.hibernate", "WARN,Remote");                     // NOI18N
        p.put("log4j.logger.com.mchange.v2", "WARN,Remote");                    // NOI18N
        p.put("log4j.logger.net.sf.ehcache", "WARN,Remote");                    // NOI18N
        p.put("log4j.logger.de.cismet.cids.abf", loglevel + ",Remote,Console"); // NOI18N
        PropertyConfigurator.configure(p);

        if (LOG.isDebugEnabled()) {
            LOG.debug("abf loglevel adjusted to: " + loglevel); // NOI18N
        }
    }

    @Override
    public void update() {
        getPanel().load();
        changed = false;
    }

    @Override
    public void applyChanges() {
        getPanel().store();
        changed = false;
        adjustLogLevel();
    }

    @Override
    public void cancel() {
        // need not do anything special, if no changes have been persisted yet
    }

    @Override
    public boolean isValid() {
        return getPanel().valid();
    }

    @Override
    public boolean isChanged() {
        return changed;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null; // new HelpCtx("...ID") if you have a help set
    }

    @Override
    public JComponent getComponent(final Lookup masterLookup) {
        return getPanel();
    }

    @Override
    public void addPropertyChangeListener(final PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(final PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private LoggingPanel getPanel() {
        if (panel == null) {
            panel = new LoggingPanel(this);
        }

        return panel;
    }

    /**
     * DOCUMENT ME!
     */
    void changed() {
        if (!changed) {
            changed = true;
            pcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
        }

        pcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
    }
}
