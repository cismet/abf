/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.cidsclass;

import org.apache.log4j.Logger;

import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.CookieAction;

import java.awt.Component;
import java.awt.Dialog;

import java.text.MessageFormat;

import java.util.Arrays;

import javax.swing.JComponent;

import de.cismet.cids.abf.domainserver.project.DomainserverContext;
import de.cismet.cids.abf.domainserver.project.DomainserverProject;
import de.cismet.cids.abf.utilities.windows.ErrorUtils;

import de.cismet.cids.jpa.entity.cidsclass.Attribute;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class AttributePermissionWizardAction extends CookieAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(
            AttributePermissionWizardAction.class);

    public static final String ATTRIBUTE_PROP = "attributeProperty"; // NOI18N
    public static final String PROJECT_PROP = "projectProperty";     // NOI18N

    //~ Instance fields --------------------------------------------------------

    private transient WizardDescriptor.Panel[] panels;

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private WizardDescriptor.Panel[] getPanels() {
        if (panels == null) {
            panels = new WizardDescriptor.Panel[] { new AttributePermissionWizardPanel1() };
            final String[] steps = new String[panels.length];
            for (int i = 0; i < panels.length; i++) {
                final Component c = panels[i].getComponent();
                // Default step name to component name of panel. Mainly useful
                // for getting the name of the target chooser to appear in the
                // list of steps.
                steps[i] = c.getName();
                if (c instanceof JComponent) {
                    // assume Swing components
                    final JComponent jc = (JComponent)c;
                    // Sets step number of a component
                    jc.putClientProperty(
                        WizardDescriptor.PROP_CONTENT_SELECTED_INDEX,
                        Integer.valueOf(i));
                    // Sets steps names for a panel
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA,
                        steps);
                    // Turn on subtitle creation on each step
                    jc.putClientProperty(
                        WizardDescriptor.PROP_AUTO_WIZARD_STYLE,
                        Boolean.TRUE);
                    // Show steps on the left side with the image on the
                    // background
                    jc.putClientProperty(
                        WizardDescriptor.PROP_CONTENT_DISPLAYED,
                        Boolean.TRUE);
                    // Turn on numbering of all steps
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED,
                        Boolean.TRUE);
                }
            }
        }
        return Arrays.copyOf(panels, panels.length);
    }

    @Override
    public String getName() {
        return "Rechte";
    }

    @Override
    public String iconResource() {
        return null;
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
    protected int mode() {
        return MODE_EXACTLY_ONE;
    }

    @Override
    protected Class[] cookieClasses() {
        return new Class[] {
                DomainserverContext.class,
                CidsAttributeContextCookie.class,
            };
    }

    @Override
    protected void performAction(final Node[] node) {
        final CidsAttributeContextCookie cacc = node[0].getCookie(CidsAttributeContextCookie.class);
        final DomainserverProject project = node[0].getCookie(DomainserverContext.class).getDomainserverProject();
        Attribute attr = cacc.getCidsAttribute();
        final WizardDescriptor wizard = new WizardDescriptor(getPanels());
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wizard.setTitleFormat(new MessageFormat("{0}")); // NOI18N
        wizard.setTitle("Ändern der Rechte am Attribut [" + attr.getName() + "]");
        wizard.putProperty(ATTRIBUTE_PROP, attr);
        wizard.putProperty(PROJECT_PROP, project);
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(wizard);
        dialog.setVisible(true);
        dialog.toFront();
        final boolean cancelled = wizard.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            final Object o = wizard.getProperty(ATTRIBUTE_PROP);
            if (o instanceof Attribute) {
                attr = (Attribute)o;
                try {
                    attr = project.getCidsDataObjectBackend().store(attr);
                    cacc.setCidsAttribute(attr);
                    RequestProcessor.getDefault().post(new Runnable() {

                            @Override
                            public void run() {
                                final Object o = node[0].getLookup()
                                            .lookupItem(new Lookup.Template(CidsClassContextCookie.class))
                                            .getInstance();
                                if (o instanceof CidsAttributeNode) {
                                    final Node n = ((CidsAttributeNode)o).getParentNode();
                                    if (n instanceof CidsClassNode) {
                                        ((CidsClassNode)n).refresh();
                                    }
                                }
                            }
                        }, 300);
                } catch (final Exception e) {
                    LOG.error("could not store attribute: " // NOI18N
                                + attr.getName());
                    ErrorUtils.showErrorMessage("Es ist ein Fehler beim Speichern des Attributes '" + attr.getName()
                                + "' aufgetreten. Bitte melden Sie den Fehler, damit er schnellstmöglich behoben werden kann.\n\nVielen Dank",
                        e);
                }
            }
        }
    }
}
