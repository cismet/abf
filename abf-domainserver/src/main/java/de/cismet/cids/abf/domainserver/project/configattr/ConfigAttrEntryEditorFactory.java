/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.configattr;

import org.apache.log4j.Logger;

import org.openide.nodes.Node;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

import java.awt.EventQueue;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import de.cismet.cids.jpa.entity.configattr.ConfigAttrType.Types;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class ConfigAttrEntryEditorFactory {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(ConfigAttrEntryEditorFactory.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ConfigAttrEntryEditorFactory object.
     */
    private ConfigAttrEntryEditorFactory() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   node  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static ConfigAttrEditor createEditor(final Node node) {
        final ConfigAttrEditor editor = getEditor(node);

        if (editor == null) {
            final ConfigAttrEntryCookie cookie = node.getCookie(ConfigAttrEntryCookie.class);
            if (Types.CONFIG_ATTR.equals(cookie.getEntry().getType().getAttrType())) {
                return new StringConfigAttrEditor(node);
            } else if (Types.ACTION_TAG.equals(cookie.getEntry().getType().getAttrType())) {
                return new ActionConfigAttrEditor(node);
            } else if (Types.XML_ATTR.equals(cookie.getEntry().getType().getAttrType())) {
                return new XMLConfigAttrEditor(node);
            } else {
                // unknown entry type
                return null;
            }
        } else {
            return editor;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   node  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static ConfigAttrEditor getEditor(final Node node) {
        if (node == null) {
            return null;
        }

        final ConfigAttrEntryCookie cookie = node.getCookie(ConfigAttrEntryCookie.class);
        if (cookie == null) {
            return null;
        }

        for (final TopComponent tc : TopComponent.getRegistry().getOpened()) {
            if (tc instanceof ConfigAttrEditor) {
                final ConfigAttrEditor editor = (ConfigAttrEditor)tc;
                final Node[] activatedNodes = editor.getActivatedNodes();

                if ((activatedNodes != null) && (activatedNodes.length == 1)) {
                    final ConfigAttrEntryCookie activeCookie = activatedNodes[0].getCookie(ConfigAttrEntryCookie.class);

                    if ((activeCookie != null) && activeCookie.getEntry().equals(cookie.getEntry())) {
                        if (EventQueue.isDispatchThread()) {
                            if (WindowManager.getDefault().isEditorTopComponent(editor)) {
                                return editor;
                            }
                        } else {
                            final FutureTask<Boolean> isEditor = new FutureTask<Boolean>(new Callable<Boolean>() {

                                        @Override
                                        public Boolean call() throws Exception {
                                            return WindowManager.getDefault().isEditorTopComponent(editor);
                                        }
                                    });
                            try {
                                EventQueue.invokeAndWait(isEditor);
                                if (isEditor.get()) {
                                    return editor;
                                }
                            } catch (final Exception e) {
                                LOG.error("cannot get editor", e); // NOI18N
                            }
                        }
                    }
                }
            }
        }

        return null;
    }
}
