/*
 * DiagramAction.java, encoding: UTF-8
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
 * Created on 16. Januar 2007, 16:59
 *
 */
package de.cismet.cids.abf.domainserver.project.cidsclass;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Vector;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;
import org.openide.util.actions.CallableSystemAction;
import org.openide.windows.TopComponent;

/**
 *
 * @author thorsten.hell@cismet.de
 * @author martin.scholl@cismet.de
 */
public abstract class DiagramAction extends CallableSystemAction implements PropertyChangeListener, LookupListener
{

    protected ClassDiagramTopComponent lastDiagramTopComponent;
    private Lookup.Result result = null;

    public DiagramAction()
    {
        Lookup.Template tpl = new Lookup.Template(ClassDiagramTopComponent.class);
        result = Utilities.actionsGlobalContext().lookup(tpl);
        result.addLookupListener(this);
        setEnabled(false);
    }

    protected void initialize()
    {
        super.initialize();
        TopComponent.getRegistry().addPropertyChangeListener(this);
    }

    public HelpCtx getHelpCtx()
    {
        return HelpCtx.DEFAULT_HELP;
    }

    protected boolean asynchronous()
    {
        return false;
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        Node[] na = TopComponent.getRegistry().getActivatedNodes();
        for(Node n : na)
        {
            Object o = n.getLookup().lookup(CidsClassNode.class);
            if(o == null || !(o instanceof CidsClassNode))
            {
                setEnabled(false);
                return;
            }
        }
        setEnabled(na.length > 0);

    }

    protected Vector<CidsClassNode> getSelectedCidsClassNodes()
    {
        Vector<CidsClassNode> v = new Vector<CidsClassNode>();
        Node[] na = TopComponent.getRegistry().getActivatedNodes();
        for(Node n : na)
        {
            Object o = n.getLookup().lookup(CidsClassNode.class);
            if(o != null && o instanceof CidsClassNode)
            {
                v.add((CidsClassNode) o);
            }
        }
        return v;
    }

    protected DomainserverProject getDomainserverprojectForSelectedCidsClassNodes()
    {
        Vector<CidsClassNode> v = getSelectedCidsClassNodes();
        for(CidsClassNode n : v)
        {
            return n.getDomainserverProject();
        }
        return null;
    }

    public void resultChanged(LookupEvent lookupEvent)
    {
        try
        {
            Lookup.Result r = (Lookup.Result) lookupEvent.getSource();
            Collection col = result.allInstances();
            lastDiagramTopComponent = (ClassDiagramTopComponent) col.iterator().next();
        }catch(Exception ex)
        {
        }
    }

}
