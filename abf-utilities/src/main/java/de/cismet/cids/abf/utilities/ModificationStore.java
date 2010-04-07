/*
 * ModificationStore.java, encoding: UTF-8
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
 * Created on 23. August 2007, 12:03
 *
 */

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
 *
 * @author mscholl
 * @version 1.1
 */
public final class ModificationStore extends Observable
{
    private static final transient Logger LOG = Logger.getLogger(
            ModificationStore.class);
    
    /** To indicate that the object has simply been changed **/
    public static final String MOD_CHANGED = "mod_changed"; // NOI18N
    
    private static ModificationStore store;
    
    // first the object that was modified, then the modification list
    private final transient Map<String, HashSet<String>> modifications;
    
    /** Creates a new instance of ModificationStore */
    private ModificationStore()
    {
        modifications = new HashMap<String, HashSet<String>>();
    }
    
    public static synchronized ModificationStore getInstance()
    {
        if(store == null)
        {
            store = new ModificationStore();
        }
        return store;
    }
    
    public void putModification(final String whoWasModified, 
            final String whatHasBeenDone)
    {
        if(whoWasModified == null)
        {
            throw new IllegalArgumentException("whoWasModified must " // NOI18N
                    + "not be null"); // NOI18N
        }
        if(LOG.isDebugEnabled())
        {
            LOG.debug("putModification for: " // NOI18N
                    + whoWasModified
                    + " :: " // NOI18N
                    + whatHasBeenDone);
        }
        synchronized(modifications)
        {
            if(modifications.get(whoWasModified) == null)
            {
                modifications.put(whoWasModified, new HashSet<String>());
            }
            modifications.get(whoWasModified).add(whatHasBeenDone);
        }
        setChanged();
        notifyObservers();
    }
    
    public void renameElement(final String oldName, final String 
            newName)
    {
        if(oldName == null || oldName.length() == 0)
        {
            throw new IllegalArgumentException("oldName must not be" // NOI18N
                    + "null or empty: " + oldName); // NOI18N
        }else if(newName == null || newName.length() == 0)
        {
            throw new IllegalArgumentException("newName must not be" // NOI18N
                    + "null or empty: " + newName); // NOI18N
        }
        if(LOG.isDebugEnabled())
        {
            LOG.debug("renameElement: " + oldName + " --> " + newName);// NOI18N
        }
        synchronized(modifications)
        {
            final HashSet<String> mods = modifications.get(oldName);
            if(mods == null)
            {
                if(LOG.isDebugEnabled())
                {
                    LOG.debug("no modifications present, " // NOI18N
                            + "putting new default mod"); // NOI18N
                }
                putModification(newName, MOD_CHANGED);
            }else
            {
                modifications.remove(oldName);
                modifications.put(newName, mods);
            }
        }
    }
    
    public void removeModification(final String whoWasModified, 
            final String theModToRemove)
    {
        if(LOG.isDebugEnabled())
        {
            LOG.debug("removeModification for: " // NOI18N
                    + whoWasModified
                    + " :: " // NOI18N
                    + theModToRemove);
        }
        synchronized(modifications)
        {
            final HashSet<String> mods = modifications.get(whoWasModified);
            if(mods != null)
            {
                mods.remove(theModToRemove);
                if(mods.isEmpty())
                {
                    modifications.remove(whoWasModified);
                }
            }
        }
        setChanged();
        notifyObservers();
    }
    
    public void removeAllModifications(final String theModToRemove)
    {
        if(LOG.isDebugEnabled())
        {
            LOG.debug("removeAllModification for mod: " // NOI18N
                    + theModToRemove);
        }
        synchronized(modifications)
        {
            final Iterator<String> it = modifications.keySet().iterator();
            while(it.hasNext())
            {
                removeModification(it.next(), theModToRemove);
            }
        }
    }
    
    public void removeAllModifications(final String[] who, final String 
            theModToRemove)
    {
        if(who == null)
        {
            throw new IllegalArgumentException("who must not be null");// NOI18N
        }
        synchronized(modifications)
        {
            for(final String s : who)
            {
                removeModification(s, theModToRemove);
            }
        }
    }
    
    public void removeAllModificationsInContext(final String context, final 
            String theMod)
    {
        if(context == null)
        {
            throw new IllegalArgumentException(
                    "context must not be null"); // NOI18N
        }
        if(LOG.isDebugEnabled())
        {
            LOG.debug("removeAllModificationsInContext for: " // NOI18N
                    + context
                    + " :: " // NOI18N
                    + theMod);
        }
        final List<String> toRemove = new ArrayList<String>();
        
        synchronized(modifications)
        {
            final Iterator<String> it = modifications.keySet().iterator();
            while(it.hasNext())
            {
                final String key = it.next();
                if(key.contains(context)
                        && modifications.get(key).contains(theMod))
                {
                    toRemove.add(key);
                }
            }
            removeAllModifications(
                    toRemove.toArray(new String[toRemove.size()]), theMod);
        }
    }
    
    public Set<String> getModifications(final String whoWasModified)
    {
        if(LOG.isDebugEnabled())
        {
            LOG.debug("getModifications for: " + whoWasModified); // NOI18N
        }
        return modifications.get(whoWasModified);
    }
    
    public boolean anyModifiedInContext(final String context, final String 
            theModification)
    {
        if(context == null)
        {
            throw new IllegalArgumentException(
                    "context must not be null"); // NOI18N
        }
        if(LOG.isDebugEnabled())
        {
            LOG.debug("anyModifiedInContext for: " // NOI18N
                    + context
                    + " :: " // NOI18N
                    + theModification);
        }
        synchronized(modifications)
        {
            final Iterator<String> it = modifications.keySet().iterator();
            while(it.hasNext())
            {
                final String key = it.next();
                if(key.contains(context)
                        && modifications.get(key).contains(theModification))
                {
                    return true;
                }
            }
        }
        return false;
    }
    
    public boolean anyModified(final String[] who, final String theModification)
    {
        if(who == null)
        {
            throw new IllegalArgumentException("who must not be null");// NOI18N
        }
        synchronized(modifications)
        {
            for(final String s : who)
            {
                final HashSet<String> mod = modifications.get(s);
                if(mod != null && mod.contains(theModification))
                {
                    return true;
                }
            }
        }
        return false;
    }
    
    public boolean wasModified(final String whoWasModified, final String 
            theModification)
    {
        if(LOG.isDebugEnabled())
        {
            LOG.debug("wasModified for: " // NOI18N
                    + whoWasModified
                    + " :: " // NOI18N
                    + theModification);
        }
        final HashSet<String> mods = modifications.get(whoWasModified);
        if(mods != null)
        {
            return mods.contains(theModification);
        }
        return false;
    }
    
    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer(150);
        sb.append("=============================\nModifcationStore:\n");//NOI18N
        for(final Iterator<Entry<String, HashSet<String>>> it = modifications.
                entrySet().iterator(); it.hasNext();)
        {
            final Entry<String, HashSet<String>> entry = it.next();
            sb.append(entry.getKey()).append(" :: "); // NOI18N
            final HashSet<String> values = entry.getValue();
            final Iterator<String> iter = values.iterator();
            while(iter.hasNext())
            {
                sb.append(iter.next()).append(", "); // NOI18N
            }
            sb.deleteCharAt(sb.length()-1).deleteCharAt(sb.length()-1).
                    append(";\n"); // NOI18N
        }
        sb.append("=============================\n"); // NOI18N
        return sb.toString();
    }
}