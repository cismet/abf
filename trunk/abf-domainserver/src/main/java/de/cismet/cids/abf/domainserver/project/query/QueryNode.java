/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.query;

import org.apache.log4j.Logger;

import org.openide.actions.DeleteAction;
import org.openide.nodes.Children;
import org.openide.nodes.Node.Property;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.CallableSystemAction;

import java.awt.Image;

import java.io.IOException;

import java.lang.reflect.InvocationTargetException;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Action;

import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.domainserver.project.ProjectNode;
import de.cismet.cids.abf.utilities.Refreshable;

import de.cismet.cids.jpa.entity.cidsclass.CidsClass;
import de.cismet.cids.jpa.entity.permission.QueryPermission;
import de.cismet.cids.jpa.entity.query.Query;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public class QueryNode extends ProjectNode implements QueryContextCookie {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(
            QueriesNode.class);

    //~ Instance fields --------------------------------------------------------

    private final transient Query query;
    private final transient Refreshable parent;
    private final transient Image icon;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new QueryNode object.
     *
     * @param  project  DOCUMENT ME!
     * @param  query    DOCUMENT ME!
     * @param  parent   DOCUMENT ME!
     */
    public QueryNode(final DomainserverProject project, final Query query,
            final Refreshable parent) {
        super(Children.LEAF, project);
        this.query = query;
        this.parent = parent;
        icon = ImageUtilities.loadImage(DomainserverProject.IMAGE_FOLDER
                        + "table_sql_16.png"); // NOI18N
        setName(query.getName());
        getCookieSet().add(this);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Image getOpenedIcon(final int i) {
        return icon;
    }

    @Override
    public Image getIcon(final int i) {
        return icon;
    }

    @Override
    protected Sheet createSheet() {
        final Sheet sheet = Sheet.createDefault();
        // <editor-fold defaultstate="collapsed" desc=" Create Property: QueryID ">
        final Property idProp;
        try {
            idProp = new PropertySupport.Reflection(query, Integer.class,
                    "getId", null);                          // NOI18N
            idProp.setName(org.openide.util.NbBundle.getMessage(
                    QueryNode.class,
                    "QueryNode.createSheet().idProp.name")); // NOI18N
        } catch (final NoSuchMethodException nsme) {
            LOG.error("could not get id", nsme);             // NOI18N
            return null;
        }                                                    // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc=" Create Property: QueryName ">
        final Property nameProp = new PropertySupport(
                "queryName",                                     // NOI18N
                String.class,
                org.openide.util.NbBundle.getMessage(
                    QueryNode.class,
                    "QueryNode.createSheet().nameProp.queryName"), // NOI18N
                org.openide.util.NbBundle.getMessage(
                    QueryNode.class,
                    "QueryNode.createSheet().nameProp.nameOfQuery"), // NOI18N
                true,
                false) {

                @Override
                public Object getValue() throws IllegalAccessException, InvocationTargetException {
                    return query.getName();
                }

                @Override
                public void setValue(final Object object) throws IllegalAccessException,
                    IllegalArgumentException,
                    InvocationTargetException {
                    if (!(object instanceof String)) {
                        throw new IllegalArgumentException(
                            "name must be a string"); // NOI18N
                    }
                    // TODO: check if it is a valid name;
                    query.setName((String)object);
                }
            }; // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc=" Create Property: QueryDescription ">
        final Property descProp = new PropertySupport(
                "queryDesc",                                     // NOI18N
                String.class,
                org.openide.util.NbBundle.getMessage(
                    QueryNode.class,
                    "QueryNode.createSheet().descProp.description"), // NOI18N
                org.openide.util.NbBundle.getMessage(
                    QueryNode.class,
                    "QueryNode.createSheet().descProp.descOfQuery"), // NOI18N
                true,
                false) {

                @Override
                public Object getValue() throws IllegalAccessException, InvocationTargetException {
                    return query.getDescription();
                }

                @Override
                public void setValue(final Object object) throws IllegalAccessException,
                    IllegalArgumentException,
                    InvocationTargetException {
                    if (!(object instanceof String)) {
                        throw new IllegalArgumentException(
                            "description must be a string"); // NOI18N
                    }
                    query.setDescription((String)object);
                }
            };                                               // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc=" Create Property: Result ">
        final Property resultProp = new PropertySupport(
                "queryResult",                                       // NOI18N
                Integer.class,
                org.openide.util.NbBundle.getMessage(
                    QueryNode.class,
                    "QueryNode.createSheet().resultProp.result"),    // NOI18N
                org.openide.util.NbBundle.getMessage(
                    QueryNode.class,
                    "QueryNode.createSheet().resultProp.resultOfQuery"), // NOI18N
                true,
                false) {

                @Override
                public Object getValue() throws IllegalAccessException, InvocationTargetException {
                    return query.getResult();
                }

                @Override
                public void setValue(final Object object) throws IllegalAccessException,
                    IllegalArgumentException,
                    InvocationTargetException {
                    // not needed
                }
            }; // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc=" Create Property: IsUpdate ">
        final Property updateProp = new PropertySupport(
                "queryUpdate",                                    // NOI18N
                String.class,
                org.openide.util.NbBundle.getMessage(
                    QueryNode.class,
                    "QueryNode.createSheet().updateProp.update"), // NOI18N
                org.openide.util.NbBundle.getMessage(
                    QueryNode.class,
                    "QueryNode.createSheet().updateProp.doesUpdate"), // NOI18N
                true,
                false) {

                @Override
                public Object getValue() throws IllegalAccessException, InvocationTargetException {
                    return query.getIsUpdate()
                        ? org.openide.util.NbBundle.getMessage(
                            QueryNode.class,
                            "QueryNode.createSheet().updateProp.getValue().yes")
                        :                                                        // NOI18N
                        org.openide.util.NbBundle.getMessage(
                            QueryNode.class,
                            "QueryNode.createSheet().updateProp.getValue().no"); // NOI18N
                }

                @Override
                public void setValue(final Object object) throws IllegalAccessException,
                    IllegalArgumentException,
                    InvocationTargetException {
                    // not needed
                }
            }; // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc=" Create Property: IsUnion ">
        final Property unionProp = new PropertySupport(
                "queryUnion",                                   // NOI18N
                String.class,
                org.openide.util.NbBundle.getMessage(
                    QueryNode.class,
                    "QueryNode.createSheet().unionProp.union"), // NOI18N
                org.openide.util.NbBundle.getMessage(
                    QueryNode.class,
                    "QueryNode.createSheet().unionProp.doesUnion"), // NOI18N
                true,
                false) {

                @Override
                public Object getValue() throws IllegalAccessException, InvocationTargetException {
                    return query.getIsUnion()
                        ? org.openide.util.NbBundle.getMessage(
                            QueryNode.class,
                            "QueryNode.createSheet().unionProp.getValue().yes")
                        :                                                       // NOI18N
                        org.openide.util.NbBundle.getMessage(
                            QueryNode.class,
                            "QueryNode.createSheet().unionProp.getValue().no"); // NOI18N
                }

                @Override
                public void setValue(final Object object) throws IllegalAccessException,
                    IllegalArgumentException,
                    InvocationTargetException {
                    // not needed
                }
            }; // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc=" Create Property: IsRoot ">
        final Property rootProp = new PropertySupport(
                "queryRoot",                                     // NOI18N
                String.class,
                org.openide.util.NbBundle.getMessage(
                    QueryNode.class,
                    "QueryNode.createSheet().rootProp.root"),    // NOI18N
                org.openide.util.NbBundle.getMessage(
                    QueryNode.class,
                    "QueryNode.createSheet().rootProp.rootTooltip"), // NOI18N
                true,
                false) {

                @Override
                public Object getValue() throws IllegalAccessException, InvocationTargetException {
                    return query.getIsRoot()
                        ? org.openide.util.NbBundle.getMessage(
                            QueryNode.class,
                            "QueryNode.createSheet().rootProp.getValue().yes")
                        :                                                      // NOI18N
                        org.openide.util.NbBundle.getMessage(
                            QueryNode.class,
                            "QueryNode.createSheet().rootProp.getValue().no"); // NOI18N
                }

                @Override
                public void setValue(final Object object) throws IllegalAccessException,
                    IllegalArgumentException,
                    InvocationTargetException {
                    // not needed
                }
            }; // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc=" Create Property: IsBatch ">
        final Property batchProp = new PropertySupport(
                "queryBatch",                                     // NOI18N
                String.class,
                org.openide.util.NbBundle.getMessage(
                    QueryNode.class,
                    "QueryNode.createSheet().batchProp.batch"),   // NOI18N
                org.openide.util.NbBundle.getMessage(
                    QueryNode.class,
                    "QueryNode.createSheet().batchProp.partOfBatch"), // NOI18N
                true,
                false) {

                @Override
                public Object getValue() throws IllegalAccessException, InvocationTargetException {
                    return query.getIsBatch()
                        ? org.openide.util.NbBundle.getMessage(
                            QueryNode.class,
                            "QueryNode.createSheet().batchProp.getValue().yes")
                        :                                                       // NOI18N
                        org.openide.util.NbBundle.getMessage(
                            QueryNode.class,
                            "QueryNode.createSheet().batchProp.getValue().no"); // NOI18N
                }

                @Override
                public void setValue(final Object object) throws IllegalAccessException,
                    IllegalArgumentException,
                    InvocationTargetException {
                    // not needed
                }
            }; // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc=" Create Property: Conjunction ">
        final Property conjProp = new PropertySupport(
                "queryConj",                                            // NOI18N
                String.class,
                org.openide.util.NbBundle.getMessage(
                    QueryNode.class,
                    "QueryNode.createSheet().conjProp.conjunction"),    // NOI18N
                org.openide.util.NbBundle.getMessage(
                    QueryNode.class,
                    "QueryNode.createSheet().conjProp.conjunctionTooltip"), // NOI18N
                true,
                false) {

                @Override
                public Object getValue() throws IllegalAccessException, InvocationTargetException {
                    return query.getIsConjunction()
                        ? org.openide.util.NbBundle.getMessage(
                            QueryNode.class,
                            "QueryNode.createSheet().conjProp.getValue().yes")
                        :                                                      // NOI18N
                        org.openide.util.NbBundle.getMessage(
                            QueryNode.class,
                            "QueryNode.createSheet().conjProp.getValue().no"); // NOI18N
                }

                @Override
                public void setValue(final Object object) throws IllegalAccessException,
                    IllegalArgumentException,
                    InvocationTargetException {
                    // not needed
                }
            }; // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc=" Create Property: IsSearch ">
        final Property searchProp = new PropertySupport(
                "querySearch",                                       // NOI18N
                String.class,
                org.openide.util.NbBundle.getMessage(
                    QueryNode.class,
                    "QueryNode.createSheet().searchProp.search"),    // NOI18N
                org.openide.util.NbBundle.getMessage(
                    QueryNode.class,
                    "QueryNode.createSheet().searchProp.searchTooltip"), // NOI18N
                true,
                false) {

                @Override
                public Object getValue() throws IllegalAccessException, InvocationTargetException {
                    return query.getIsSearch()
                        ? org.openide.util.NbBundle.getMessage(
                            QueryNode.class,
                            "QueryNode.createSheet().searchProp.getValue().yes")
                        :                                                        // NOI18N
                        org.openide.util.NbBundle.getMessage(
                            QueryNode.class,
                            "QueryNode.createSheet().searchProp.getValue().no"); // NOI18N
                }

                @Override
                public void setValue(final Object object) throws IllegalAccessException,
                    IllegalArgumentException,
                    InvocationTargetException {
                    // not needed
                }
            }; // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc=" Create Property: Parameter ">
        final Property paramProp = new PropertySupport(
                "queryParam",                                           // NOI18N
                Integer.class,
                org.openide.util.NbBundle.getMessage(
                    QueryNode.class,
                    "QueryNode.createSheet().paramProp.paramCount"),    // NOI18N
                org.openide.util.NbBundle.getMessage(
                    QueryNode.class,
                    "QueryNode.createSheet().paramProp.paramCountTooltip"), // NOI18N
                true,
                false) {

                @Override
                public Object getValue() throws IllegalAccessException, InvocationTargetException {
                    return query.getQueryParameters().size();
                }

                @Override
                public void setValue(final Object object) throws IllegalAccessException,
                    IllegalArgumentException,
                    InvocationTargetException {
                    // not needed
                }
            }; // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc=" Create Property: QueryPermissions ">
        final List<Property> rightProps = new LinkedList<Property>();
        if (query.getQueryPermissions() != null) {
            final Iterator<QueryPermission> it = query.getQueryPermissions().iterator();
            while (it.hasNext()) {
                final QueryPermission qp = it.next();
                rightProps.add(new PropertySupport(
                        "queryRight"
                                + qp.toString(),                          // NOI18N
                        String.class,
                        qp.getUserGroup().getName(),
                        org.openide.util.NbBundle.getMessage(
                            QueryNode.class,
                            "QueryNode.createSheet().rightProps.aUsergroup"), // NOI18N
                        true,
                        false) {

                        @Override
                        public Object getValue() throws IllegalAccessException, InvocationTargetException {
                            String s = qp.getPermission().getDescription();
                            if (s == null) {
                                s = qp.getPermission().getKey();
                            }
                            return s;
                        }

                        @Override
                        public void setValue(final Object object) throws IllegalAccessException,
                            IllegalArgumentException,
                            InvocationTargetException {
                            // not needed
                        }
                    });
            }
        } // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc=" Create Property: ClassPermissions ">
        final List<Property> classProps = new LinkedList<Property>();
        if (query.getCidsClasses() != null) {
            final Iterator<CidsClass> it = query.getCidsClasses().iterator();
            while (it.hasNext()) {
                final CidsClass cc = it.next();
                rightProps.add(new PropertySupport(
                        "classRight"
                                + cc.toString(),                      // NOI18N
                        String.class,
                        cc.getId().toString(),
                        org.openide.util.NbBundle.getMessage(
                            QueryNode.class,
                            "QueryNode.createSheet().rightProps.aClass"), // NOI18N
                        true,
                        false) {

                        @Override
                        public Object getValue() throws IllegalAccessException, InvocationTargetException {
                            return cc.getName();
                        }

                        @Override
                        public void setValue(final Object object) throws IllegalAccessException,
                            IllegalArgumentException,
                            InvocationTargetException {
                            // not needed
                        }
                    });
            }
        }                                                       // </editor-fold>
        final Sheet.Set main = Sheet.createPropertiesSet();
        final Sheet.Set clazz = Sheet.createPropertiesSet();
        final Sheet.Set rights = Sheet.createPropertiesSet();
        main.setName("nodeProps");                              // NOI18N
        clazz.setName("nodeClassProps");                        // NOI18N
        rights.setName("nodeRights");                           // NOI18N
        main.setDisplayName(org.openide.util.NbBundle.getMessage(
                QueryNode.class,
                "QueryNode.createSheet().main.displayName"));   // NOI18N
        clazz.setDisplayName(org.openide.util.NbBundle.getMessage(
                QueryNode.class,
                "QueryNode.createSheet().clazz.displayName"));  // NOI18N
        rights.setDisplayName(org.openide.util.NbBundle.getMessage(
                QueryNode.class,
                "QueryNode.createSheet().rights.displayName")); // NOI18N
        main.put(idProp);
        main.put(nameProp);
        main.put(descProp);
        main.put(paramProp);
        main.put(resultProp);
        main.put(updateProp);
        main.put(unionProp);
        main.put(rootProp);
        main.put(batchProp);
        main.put(conjProp);
        main.put(searchProp);
        for (final Property prop : rightProps) {
            rights.put(prop);
        }
        for (final Property prop : classProps) {
            clazz.put(prop);
        }
        sheet.put(main);
        sheet.put(clazz);
        sheet.put(rights);
        return sheet;
    }

    @Override
    public Action[] getActions(final boolean b) {
        return new Action[] {
                CallableSystemAction.get(ModifyQueryWizardAction.class),
                CallableSystemAction.get(ModifyQueryRightsWizardAction.class),
                null,
                CallableSystemAction.get(TestQueryWizardAction.class),
                null,
                CallableSystemAction.get(DeleteAction.class)
            };
    }

    @Override
    public Query getQuery() {
        return project.getCidsDataObjectBackend().getEntity(Query.class, query.getId());
    }

    @Override
    public void destroy() throws IOException {
        try {
            project.getCidsDataObjectBackend().delete(query);
        } catch (final Exception ex) {
            LOG.error("could not delete query", ex); // NOI18N
        }
        parent.refresh();
    }

    @Override
    public boolean canDestroy() {
        return true;
    }
}
