package com.gun3y.pagerank.crawler.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.TextArea;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gun3y.pagerank.mongo.MongoManager;

import edu.uci.ics.crawler4j.crawler.CrawlController;

public class LogViewer extends JDialog {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogViewer.class);

    private static final long serialVersionUID = 7177084019100796437L;
    private TextArea textArea;
    private JPanel panel;
    private JButton btnStopCrawling;
    private JLabel lblPageSize;
    private JLabel lblPageItems;

    private CrawlController controller;

    private MongoManager mongoManager;

    private Thread statRunner = new Thread(new Runnable() {

        @Override
        public void run() {
            while (true) {

                if (LogViewer.this.mongoManager != null) {
                    long count = LogViewer.this.mongoManager.getHtmlPageCount();
                    LogViewer.this.lblPageSize.setText(count + "");
                }
                else {
                    return;
                }

                try {
                    Thread.sleep(2000);
                }
                catch (InterruptedException e) {
                    LOGGER.error(e.getMessage());
                }
            }

        }
    });

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {

                    LogViewer dialog = new LogViewer(null, null, null);
                    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                    dialog.setVisible(true);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the dialog.
     *
     * @param controller
     */
    public LogViewer(CrawlerFrame crawlerFrame, CrawlController controller, MongoManager mongoManager) {
        super(crawlerFrame);
        this.controller = controller;
        this.mongoManager = mongoManager;
        this.setModal(true);
        this.setModalityType(ModalityType.APPLICATION_MODAL);
        this.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
        this.setBounds(100, 100, 881, 553);
        this.getContentPane().add(this.getTextArea(), BorderLayout.CENTER);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - this.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - this.getHeight()) / 2);
        this.setLocation(x, y);

        SwingLogAdapter adapter = new SwingLogAdapter(this.getTextArea());
        this.statRunner.start();
        this.getContentPane().add(this.getPanel(), BorderLayout.SOUTH);
    }

    private TextArea getTextArea() {
        if (this.textArea == null) {
            this.textArea = new TextArea();
            this.textArea.setName("textArea");
        }
        return this.textArea;
    }

    private JPanel getPanel() {
        if (this.panel == null) {
            this.panel = new JPanel();
            this.panel.setName("panel");
            GroupLayout gl_panel = new GroupLayout(this.panel);
            gl_panel.setHorizontalGroup(gl_panel.createParallelGroup(Alignment.LEADING).addGroup(
                    Alignment.TRAILING,
                    gl_panel.createSequentialGroup().addContainerGap(678, Short.MAX_VALUE).addComponent(this.getLblPageItems())
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(this.getLblPageSize(), GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(ComponentPlacement.RELATED).addComponent(this.getBtnStopCrawling()).addContainerGap()));
            gl_panel.setVerticalGroup(gl_panel.createParallelGroup(Alignment.LEADING).addGroup(
                    gl_panel.createSequentialGroup()
                    .addContainerGap(10, Short.MAX_VALUE)
                    .addGroup(
                            gl_panel.createParallelGroup(Alignment.BASELINE).addComponent(this.getBtnStopCrawling())
                            .addComponent(this.getLblPageSize()).addComponent(this.getLblPageItems())).addGap(10)));
            this.panel.setLayout(gl_panel);
        }
        return this.panel;
    }

    private JButton getBtnStopCrawling() {
        if (this.btnStopCrawling == null) {
            this.btnStopCrawling = new JButton("Stop");
            this.btnStopCrawling.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (LogViewer.this.controller != null) {

                        LogViewer.this.mongoManager = null;

                        LogViewer.this.controller.shutdown();
                        LogViewer.this.controller.waitUntilFinish();

                        LogViewer.this.dispose();
                    }
                }
            });
            this.btnStopCrawling.setFont(new Font("Tahoma", Font.PLAIN, 16));
            this.btnStopCrawling.setName("btnStopCrawling");
        }
        return this.btnStopCrawling;
    }

    private JLabel getLblPageSize() {
        if (this.lblPageSize == null) {
            this.lblPageSize = new JLabel("0");
            this.lblPageSize.setFont(new Font("Tahoma", Font.PLAIN, 16));
            this.lblPageSize.setName("lblPageSize");
        }
        return this.lblPageSize;
    }

    private JLabel getLblPageItems() {
        if (this.lblPageItems == null) {
            this.lblPageItems = new JLabel("Crawled Pages:");
            this.lblPageItems.setFont(new Font("Tahoma", Font.PLAIN, 16));
            this.lblPageItems.setName("lblPageItems");
        }
        return this.lblPageItems;
    }
}
