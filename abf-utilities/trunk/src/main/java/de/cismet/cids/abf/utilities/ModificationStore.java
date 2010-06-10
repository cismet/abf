/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * DOCUMENT ME!
 *
 * @author   mscholl
 * @version  1.1
 */
public final class ModificationStore extends Observable {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(ModificationStore.class);

    /** To indicate that the object has simply been changed.* */
    public static final String MOD_CHANGED = "mod_changed";  // NOI18N

    private static ModificationStore store;

    //~ Instance fields --------------------------------------------------------

    // first the object that was modified, then the modification list
    private final transient Map<String, HashSet<String>> modifications;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of ModificationStore.
     */
    private ModificationStore() {
        modifications = new HashMap<String, HashSet<String>>();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static synchronized ModificationStore getInstance() {
        if (store == null) {
            store = new ModificationStore();
        }
        return store;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   whoWasModified   DOCUMENT ME!
     * @param   whatHasBeenDone  DOCUMENT ME!
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    public void putModification(final String whoWasModified,
            final String whatHasBeenDone) {
        if (whoWasModified == null) {
            throw new IllegalArgumentException("whoWasModified must not be null");          // NOI18N
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("putModification for: " + whoWasModified + " :: " + whatHasBeenDone); // NOI18N
        }
        synchronized (modifications) {
            if (modifications.get(whoWasModified) == null) {
                modifications.put(whoWasModified, new HashSet<String>());
            }
            modifications.get(whoWasModified).add(whatHasBeenDone);
        }
        setChanged();
        notifyObservers();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   oldName  DOCUMENT ME!
     * @param   newName  DOCUMENT ME!
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    public void renameElement(final String oldName, final String newName) {
        if ((oldName == null) || (oldName.length() == 0)) {
            throw new IllegalArgumentException("oldName must not be null or empty: " + oldName); // NOI18N
        } else if ((newName == null) || (newName.length() == 0)) {
            throw new IllegalArgumentException("newName must not be null or empty: " + newName); // NOI18N
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("renameElement: " + oldName + " --> " + newName);                          // NOI18N
        }
        synchronized (modifications) {
            final HashSet<String> mods = modifications.get(oldName);
            if (mods == null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("no modifications present, putting new default mod");              // NOI18N
                }
                putModification(newName, MOD_CHANGED);
            } else {
                modifications.remove(oldName);
                modifications.put(newName, mods);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  whoWasModified  DOCUMENT ME!
     * @param  theModToRemove  DOCUMENT ME!
     */
    public void removeModification(final String whoWasModified,
            final String theModToRemove) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("removeModification for: " + whoWasModified + " :: " + theModToRemove); // NOI18N
        }
        synchronized (modifications) {
            final HashSet<String> mods = modifications.get(whoWasModified);
            if (mods != null) {
                mods.remove(theModToRemove);
                if (mods.isEmpty()) {
                    modifications.remove(whoWasModified);
                }
            }
        }
        setChanged();
        notifyObservers();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  theModToRemove  DOCUMENT ME!
     */
    public void removeAllModifications(final String theModToRemove) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("removeAllModification for mod: " + theModToRemove); // NOI18N
        }
        synchronized (modifications) {
            final Iterator<String> it = modifications.keySet().iterator();
            while (it.hasNext()) {
                removeModification(it.next(), theModToRemove);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   who             DOCUMENT ME!
     * @param   theModToRemove  DOCUMENT ME!
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    public void removeAllModifications(final String[] who, final String theModToRemove) {
        if (who == null) {
            throw new IllegalArgumentException("who must not be null"); // NOI18N
        }
        synchronized (modifications) {
            for (final String s : who) {
                removeModification(s, theModToRemove);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   context  DOCUMENT ME!
     * @param   theMod   DOCUMENT ME!
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    public void removeAllModificationsInContext(final String context, final String theMod) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null");                 // NOI18N
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("removeAllModificationsInContext for: " + context + " :: " + theMod); // NOI18N
        }
        final List<String> toRemove = new ArrayList<String>();

        synchronized (modifications) {
            final Iterator<String> it = modifications.keySet().iterator();
            while (it.hasNext()) {
                final String key = it.next();
                if (key.contains(context)
                            && modifications.get(key).contains(theMod)) {
                    toRemove.add(key);
                }
            }
            removeAllModifications(
                toRemove.toArray(new String[toRemove.size()]),
                theMod);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   whoWasModified  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Set<String> getModifications(final String whoWasModified) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getModifications for: " + whoWasModified); // NOI18N
        }
        return modifications.get(whoWasModified);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   context          DOCUMENT ME!
     * @param   theModification  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    public boolean anyModifiedInContext(final String context, final String theModification) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null");               // NOI18N
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("anyModifiedInContext for: " + context + " :: " + theModification); // NOI18N
        }
        synchronized (modifications) {
            final Iterator<String> it = modifications.keySet().iterator();
            while (it.hasNext()) {
                final String key = it.next();
                if (key.contains(context)
                            && modifications.get(key).contains(theModification)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   who              DOCUMENT ME!
     * @param   theModification  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    public boolean anyModified(final String[] who, final String theModification) {
        if (who == null) {
            throw new IllegalArgumentException("who must not be null"); // NOI18N
        }
        synchronized (modifications) {
            for (final String s : who) {
                final HashSet<String> mod = modifications.get(s);
                if ((mod != null) && mod.contains(theModification)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   whoWasModified   DOCUMENT ME!
     * @param   theModification  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean wasModified(final String whoWasModified, final String theModification) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("wasModified for: " + whoWasModified + " :: " + theModification); // NOI18N
        }
        final HashSet<String> mods = modifications.get(whoWasModified);
        if (mods != null) {
            return mods.contains(theModification);
        }
        return false;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer(150);
        sb.append("=============================\nModifcationStore:\n");                  // NOI18N
        for (final Iterator<Entry<String, HashSet<String>>> it = modifications.entrySet().iterator(); it.hasNext();) {
            final Entry<String, HashSet<String>> entry = it.next();
            sb.append(entry.getKey()).append(" :: ");                                     // NOI18N
            final HashSet<String> values = entry.getValue();
            final Iterator<String> iter = values.iterator();
            while (iter.hasNext()) {
                sb.append(iter.next()).append(", ");                                      // NOI18N
            }
            sb.deleteCharAt(sb.length() - 1).deleteCharAt(sb.length() - 1).append(";\n"); // NOI18N
        }
        sb.append("=============================\n");                                     // NOI18N
        return sb.toString();
    }
}
