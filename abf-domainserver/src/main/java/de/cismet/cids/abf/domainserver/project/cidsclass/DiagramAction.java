/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.cidsclass;

import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;
import org.openide.util.actions.CallableSystemAction;
import org.openide.windows.TopComponent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.Collection;
import java.util.Vector;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.cidsclass.graph.ClassDiagramTopComponent;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten.hell@cismet.de
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public abstract class DiagramAction extends CallableSystemAction implements PropertyChangeListener, LookupListener {

    //~ Instance fields --------------------------------------------------------

    protected ClassDiagramTopComponent lastDiagramTopComponent;
    private Lookup.Result result = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DiagramAction object.
     */
    public DiagramAction() {
        final Lookup.Template tpl = new Lookup.Template(ClassDiagramTopComponent.class);
        result = Utilities.actionsGlobalContext().lookup(tpl);
        result.addLookupListener(this);
        setEnabled(false);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    protected void initialize() {
        super.initialize();
        TopComponent.getRegistry().addPropertyChangeListener(this);
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        final Node[] na = TopComponent.getRegistry().getActivatedNodes();
        for (final Node n : na) {
            final Object o = n.getLookup().lookup(CidsClassNode.class);
            if ((o == null) || !(o instanceof CidsClassNode)) {
                setEnabled(false);
                return;
            }
        }
        setEnabled(na.length > 0);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected Vector<CidsClassNode> getSelectedCidsClassNodes() {
        final Vector<CidsClassNode> v = new Vector<CidsClassNode>();
        final Node[] na = TopComponent.getRegistry().getActivatedNodes();
        for (final Node n : na) {
            final Object o = n.getLookup().lookup(CidsClassNode.class);
            if ((o != null) && (o instanceof CidsClassNode)) {
                v.add((CidsClassNode)o);
            }
        }
        return v;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected DomainserverProject getDomainserverprojectForSelectedCidsClassNodes() {
        final Vector<CidsClassNode> v = getSelectedCidsClassNodes();
        for (final CidsClassNode n : v) {
            return n.getDomainserverProject();
        }
        return null;
    }

    @Override
    public void resultChanged(final LookupEvent lookupEvent) {
        try {
            final Lookup.Result r = (Lookup.Result)lookupEvent.getSource();
            final Collection col = result.allInstances();
            lastDiagramTopComponent = (ClassDiagramTopComponent)col.iterator().next();
        } catch (Exception ex) {
        }
    }
}
