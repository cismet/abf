/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.abf.domainserver.project.cidsclass;

import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.text.DecimalFormat;
import java.text.MessageFormat;

import javax.swing.ImageIcon;
import javax.swing.Timer;

import de.cismet.cids.util.Cancelable;

import de.cismet.commons.utils.ProgressEvent;
import de.cismet.commons.utils.ProgressEvent.State;
import de.cismet.commons.utils.ProgressListener;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  1.1
 */
// TODO: this class is a mess, refactor
public class IndexActionDialog extends javax.swing.JDialog implements ProgressListener {

    //~ Instance fields --------------------------------------------------------

    private final transient ActionListener aL;
    private final transient Cancelable cancelable;
    private final transient Timer timer;
    private final transient DecimalFormat minFormat;
    private final transient DecimalFormat secFormat;
    private final transient MessageFormat elapsedFormat;
    private final transient MessageFormat statusFormat;
    private final transient ImageIcon infoIcon;
    private final transient ImageIcon warnIcon;

    private transient ProgressEvent currentEvent;
    private transient int currentClass;
    private transient long startTime;
    private transient boolean canceled;
    private transient boolean lastEventDueToCancel;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private final transient javax.swing.JButton cmdCancel = new javax.swing.JButton();
    private final transient javax.swing.JButton cmdOK = new javax.swing.JButton();
    private final transient javax.swing.Box.Filler filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0),
            new java.awt.Dimension(0, 0),
            new java.awt.Dimension(32767, 0));
    private final transient javax.swing.Box.Filler filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0),
            new java.awt.Dimension(0, 0),
            new java.awt.Dimension(0, 32767));
    private final transient javax.swing.JLabel lblClass = new javax.swing.JLabel();
    private final transient javax.swing.JLabel lblElapsed = new javax.swing.JLabel();
    private final transient javax.swing.JLabel lblElapsedValue = new javax.swing.JLabel();
    private final transient javax.swing.JLabel lblEstimated = new javax.swing.JLabel();
    private final transient javax.swing.JLabel lblEstimatedValue = new javax.swing.JLabel();
    private final transient javax.swing.JLabel lblMessage = new javax.swing.JLabel();
    private final transient javax.swing.JLabel lblOverall = new javax.swing.JLabel();
    private final transient javax.swing.JLabel lblOverallStatus = new javax.swing.JLabel();
    private final transient javax.swing.JLabel lblStatus = new javax.swing.JLabel();
    private final transient javax.swing.JPanel pnlProgress = new javax.swing.JPanel();
    private final transient javax.swing.JProgressBar prgClass = new javax.swing.JProgressBar();
    private final transient javax.swing.JProgressBar prgOverall = new javax.swing.JProgressBar();
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new IndexActionDialog object.
     *
     * @param  parent       DOCUMENT ME!
     * @param  modal        DOCUMENT ME!
     * @param  noOfClasses  DOCUMENT ME!
     * @param  cancelable   DOCUMENT ME!
     */
    public IndexActionDialog(final Frame parent,
            final boolean modal,
            final int noOfClasses,
            final Cancelable cancelable) {
        super(parent, modal);

        this.canceled = false;
        this.lastEventDueToCancel = false;
        this.cancelable = cancelable;
        this.minFormat = new DecimalFormat("#00");                                      // NOI18N
        this.secFormat = new DecimalFormat("00");                                       // NOI18N
        this.elapsedFormat = new MessageFormat("{0}:{1} (mm:ss)");                      // NOI18N
        elapsedFormat.setFormatByArgumentIndex(0, minFormat);
        elapsedFormat.setFormatByArgumentIndex(1, secFormat);
        this.infoIcon = ImageUtilities.loadImageIcon(
                "de/cismet/cids/abf/domainserver/project/cidsclass/information_16.png", // NOI18N
                false);
        this.warnIcon = ImageUtilities.loadImageIcon(
                "de/cismet/cids/abf/domainserver/project/cidsclass/exclamation_16.png", // NOI18N
                false);

        this.setTitle((noOfClasses == 1)
                ? NbBundle.getMessage(IndexActionDialog.class, "IndexActionDialog.title.oneClass") // NOI18N
                : NbBundle.getMessage(
                    IndexActionDialog.class,
                    "IndexActionDialog.title.multClasses",                                         // NOI18N
                    new Object[] { noOfClasses }));

        prgClass.setStringPainted(true);
        prgOverall.setMinimum(0);
        prgOverall.setMaximum(noOfClasses);
        prgOverall.setValue(0);
        prgOverall.setIndeterminate(false);
        prgOverall.setStringPainted(true);

        initComponents();
        setLocationRelativeTo(parent);

        currentClass = 0;
        currentEvent = null;
        final String noOfClassesString = org.openide.util.NbBundle.getMessage(
                IndexActionDialog.class,
                (noOfClasses == 1)
                    ? "IndexActionDialog.IndexActionDialog(Frame,boolean,int,Cancelable).noOfClassesString.oneClass" // NOI18N
                    : "IndexActionDialog.IndexActionDialog(Frame,boolean,int,Cancelable).noOfClassesString.multClasses"); // NOI18N
        statusFormat = new MessageFormat(org.openide.util.NbBundle.getMessage(
                    IndexActionDialog.class,
                    "IndexActionDialog.IndexActionDialog(Frame,boolean,int,Cancelable).statusString",                // NOI18N
                    new Object[] {
                        "{0}",                                                                                       // NOI18N
                        noOfClasses,
                        noOfClassesString
                    }));

        lblMessage.setText(NbBundle.getMessage(
                IndexActionDialog.class,
                "IndexActionDialog.IndexActionDialog(Frame,boolean,int,Cancelable).lblMessage.text.working")); // NOI18N
        lblMessage.setIcon(infoIcon);
        lblOverallStatus.setText(statusFormat.format(new Object[] { 0 }));

        aL = new ActionListener() {

                @Override
                public void actionPerformed(final ActionEvent e) {
                    if ((currentClass == prgOverall.getMaximum()) || lastEventDueToCancel) {
                        // if it is enabled at this point this is a regular finish
                        if (cmdCancel.isEnabled()) {
                            lblMessage.setText(NbBundle.getMessage(
                                    IndexActionDialog.class,
                                    "IndexActionDialog.IndexActionDialog(Frame,boolean,int,Cancelable).al.lblMessage.indexingFinished", // NOI18N
                                    new Object[] {}));
                        } else {
                            lblMessage.setText(NbBundle.getMessage(
                                    IndexActionDialog.class,
                                    "IndexActionDialog.IndexActionDialog(Frame,boolean,int,Cancelable).al.lblMessage.indexingCancelled", // NOI18N
                                    new Object[] {}));
                        }

                        timer.stop();
                        cmdCancel.setEnabled(false);
                        cmdOK.setEnabled(true);
                        prgClass.setIndeterminate(false);
                        prgClass.setMinimum(0);
                        prgClass.setMaximum(1);
                        prgClass.setValue(1);
                    }
                    final long now = System.currentTimeMillis();
                    final long seconds = ((now - startTime) / 1000) % 60;
                    final long minutes = ((now - startTime) / 60000) % 60;

                    final String elapsed = elapsedFormat.format(new Object[] { minutes, seconds });

                    lblOverallStatus.setText(statusFormat.format(new Object[] { currentClass }));
                    lblElapsedValue.setText(elapsed);
                    prgOverall.setValue(currentClass);
                }
            };
        timer = new Timer(1000, aL);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    void nextClass() {
        final Runnable r = new Runnable() {

                @Override
                public void run() {
                    currentClass++;
                    aL.actionPerformed(null);
                }
            };

        if (EventQueue.isDispatchThread()) {
            r.run();
        } else {
            EventQueue.invokeLater(r);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  error  DOCUMENT ME!
     * @param  e      DOCUMENT ME!
     */
    void setError(final String error, final Throwable e) {
        final Runnable r = new Runnable() {

                @Override
                public void run() {
                    lblMessage.setText(error);
                    lblMessage.setIcon(warnIcon);
                    cancelable.cancel();
                    cmdCancel.setEnabled(false);
                    cmdOK.setEnabled(true);
                    timer.stop();
                }
            };

        if (EventQueue.isDispatchThread()) {
            r.run();
        } else {
            EventQueue.invokeLater(r);
        }
    }

    @Override
    public void progress(final ProgressEvent event) {
        if (!timer.isRunning()) {
            startTime = System.currentTimeMillis();
            timer.start();
        }

        lblStatus.setText(event.getMessage());
        if (event.isIndeterminate()) {
            prgClass.setIndeterminate(true);
        } else {
            prgClass.setMinimum(0);
            prgClass.setMaximum(event.getMaxSteps());
            if ((currentEvent != null) && currentEvent.isIndeterminate()) {
                prgClass.setIndeterminate(false);
                prgClass.setValue(0);
            }
        }
        currentEvent = event;

        if (canceled) {
            lastEventDueToCancel = true;
            nextClass();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        cmdOK.setText(org.openide.util.NbBundle.getMessage(IndexActionDialog.class, "IndexActionDialog.cmdOK.text")); // NOI18N
        cmdOK.setEnabled(false);
        cmdOK.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdOKActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        getContentPane().add(cmdOK, gridBagConstraints);

        cmdCancel.setText(org.openide.util.NbBundle.getMessage(
                IndexActionDialog.class,
                "IndexActionDialog.cmdCancel.text")); // NOI18N
        cmdCancel.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cmdCancelActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        getContentPane().add(cmdCancel, gridBagConstraints);

        pnlProgress.setOpaque(false);
        pnlProgress.setPreferredSize(new java.awt.Dimension(550, 164));
        pnlProgress.setLayout(new java.awt.GridBagLayout());

        lblStatus.setText(org.openide.util.NbBundle.getMessage(
                IndexActionDialog.class,
                "IndexActionDialog.lblStatus.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        pnlProgress.add(lblStatus, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        pnlProgress.add(prgClass, gridBagConstraints);

        lblOverallStatus.setText(org.openide.util.NbBundle.getMessage(
                IndexActionDialog.class,
                "IndexActionDialog.lblOverallStatus.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        pnlProgress.add(lblOverallStatus, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        pnlProgress.add(prgOverall, gridBagConstraints);

        lblElapsed.setText(NbBundle.getMessage(IndexActionDialog.class, "IndexActionDialog.lblElapsed.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 5);
        pnlProgress.add(lblElapsed, gridBagConstraints);

        lblEstimated.setText(NbBundle.getMessage(IndexActionDialog.class, "IndexActionDialog.lblEstimated.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 5);
        pnlProgress.add(lblEstimated, gridBagConstraints);

        lblElapsedValue.setText(NbBundle.getMessage(IndexActionDialog.class, "IndexActionDialog.lblElapsedValue.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        pnlProgress.add(lblElapsedValue, gridBagConstraints);

        lblEstimatedValue.setText(NbBundle.getMessage(
                IndexActionDialog.class,
                "IndexActionDialog.lblEstimatedValue.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        pnlProgress.add(lblEstimatedValue, gridBagConstraints);

        lblOverall.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        lblOverall.setText(org.openide.util.NbBundle.getMessage(
                IndexActionDialog.class,
                "IndexActionDialog.lblOverall.text",
                new Object[] {}));                                     // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        pnlProgress.add(lblOverall, gridBagConstraints);

        lblClass.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        lblClass.setText(org.openide.util.NbBundle.getMessage(
                IndexActionDialog.class,
                "IndexActionDialog.lblClass.text",
                new Object[] {}));                                   // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        pnlProgress.add(lblClass, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(pnlProgress, gridBagConstraints);

        lblMessage.setText(org.openide.util.NbBundle.getMessage(
                IndexActionDialog.class,
                "IndexActionDialog.lblMessage.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
        getContentPane().add(lblMessage, gridBagConstraints);

        filler1.setMaximumSize(new java.awt.Dimension(32767, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(filler1, gridBagConstraints);

        filler2.setMaximumSize(new java.awt.Dimension(0, 32767));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(filler2, gridBagConstraints);

        pack();
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdCancelActionPerformed(final java.awt.event.ActionEvent evt) //GEN-FIRST:event_cmdCancelActionPerformed
    {                                                                           //GEN-HEADEREND:event_cmdCancelActionPerformed
        canceled = true;
        cancelable.cancel();
        cmdCancel.setEnabled(false);

        // if the current event is still running we wait for it to finish, thus not enabling the OK buttong
        if ((currentEvent == null)
                    || ((State.STARTED != currentEvent.getState()) && (State.PROGRESSING != currentEvent.getState()))) {
            cmdOK.setEnabled(true);
            timer.stop();
        } else {
            lblMessage.setText(NbBundle.getMessage(
                    IndexActionDialog.class,
                    "IndexActionDialog.cmdCancelActionPerformed(ActionEvent).lblMessage.text.waitFinish")); // NOI18N
        }
    }                                                                                                       //GEN-LAST:event_cmdCancelActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cmdOKActionPerformed(final java.awt.event.ActionEvent evt) //GEN-FIRST:event_cmdOKActionPerformed
    {                                                                       //GEN-HEADEREND:event_cmdOKActionPerformed
        EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    setVisible(false);
                    dispose();
                }
            });
    } //GEN-LAST:event_cmdOKActionPerformed
}
