package com.gun3y.pagerank.crawler.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.lang3.StringUtils;

public class DBConfig extends JDialog {

    private static final long serialVersionUID = 6251892444431503259L;
    private JPanel panelContent;
    private JPanel panelButtons;
    private JButton btnOK;
    private JButton btnCancel;
    private JLabel lblServerUrl;
    private JTextField txtServerUrl;
    private JLabel lblDbName;
    private JTextField txtDBName;

    private CrawlerFrame crawlerFrame;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        try {
            DBConfig dialog = new DBConfig();
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setVisible(true);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public DBConfig(CrawlerFrame crawlerFrame) {
        super(crawlerFrame);
        this.crawlerFrame = crawlerFrame;
        this.initialize();
    }

    /**
     * Create the dialog.
     */
    public DBConfig() {

        this.initialize();
    }

    private void initialize() {
        this.setTitle("DB Config");
        this.setBounds(100, 100, 290, 145);
        BorderLayout borderLayout = new BorderLayout();
        this.getContentPane().setLayout(borderLayout);
        this.getContentPane().add(this.getPanelContent(), BorderLayout.CENTER);
        this.getContentPane().add(this.getPanelButtons(), BorderLayout.SOUTH);
        this.setType(Type.UTILITY);
        this.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
        this.setModalityType(ModalityType.APPLICATION_MODAL);
        this.setModal(true);

        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - this.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - this.getHeight()) / 2);
        this.setLocation(x, y);
    }

    private JPanel getPanelContent() {
        if (this.panelContent == null) {
            this.panelContent = new JPanel();
            this.panelContent.setName("panelContent");
            this.panelContent.setLayout(new GridLayout(0, 2, 2, 2));
            this.panelContent.add(this.getLblServerUrl());
            this.panelContent.add(this.getTxtServerUrl());
            this.panelContent.add(this.getLblDbName());
            this.panelContent.add(this.getTxtDBName());
        }
        return this.panelContent;
    }

    private JPanel getPanelButtons() {
        if (this.panelButtons == null) {
            this.panelButtons = new JPanel();
            this.panelButtons.setName("panelButtons");
            this.panelButtons.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
            this.panelButtons.add(this.getBtnOK());
            this.panelButtons.add(this.getBtnCancel());
        }
        return this.panelButtons;
    }

    private JButton getBtnOK() {
        if (this.btnOK == null) {
            this.btnOK = new JButton("OK");
            this.btnOK.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (DBConfig.this.crawlerFrame != null) {
                        DBConfig.this.crawlerFrame.dbHost = DBConfig.this.txtServerUrl.getText();
                        DBConfig.this.crawlerFrame.dbName = DBConfig.this.txtDBName.getText();

                        DBConfig.this.dispose();
                    }
                }
            });
            this.btnOK.setName("btnOK");
        }
        return this.btnOK;
    }

    private JButton getBtnCancel() {
        if (this.btnCancel == null) {
            this.btnCancel = new JButton("Cancel");
            this.btnCancel.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    DBConfig.this.dispose();
                }
            });
            this.btnCancel.setName("btnCancel");
        }
        return this.btnCancel;
    }

    private JLabel getLblServerUrl() {
        if (this.lblServerUrl == null) {
            this.lblServerUrl = new JLabel("Server URL");
            this.lblServerUrl.setName("lblServerUrl");
        }
        return this.lblServerUrl;
    }

    private JTextField getTxtServerUrl() {
        if (this.txtServerUrl == null) {
            this.txtServerUrl = new JTextField();

            if (StringUtils.isBlank(this.crawlerFrame.dbHost)) {
                this.txtServerUrl.setText("localhost");
            }
            else {
                this.txtServerUrl.setText(this.crawlerFrame.dbHost);
            }

            this.txtServerUrl.setName("txtServerUrl");
            this.txtServerUrl.setColumns(10);
        }
        return this.txtServerUrl;
    }

    private JLabel getLblDbName() {
        if (this.lblDbName == null) {
            this.lblDbName = new JLabel("DB Name");
            this.lblDbName.setName("lblDbName");
        }
        return this.lblDbName;
    }

    private JTextField getTxtDBName() {
        if (this.txtDBName == null) {
            this.txtDBName = new JTextField();
            if (StringUtils.isBlank(this.crawlerFrame.dbName)) {
                this.txtDBName.setText("PageRankCrawlDB");
            }
            else {
                this.txtDBName.setText(this.crawlerFrame.dbName);
            }

            this.txtDBName.setName("txtDBName");
            this.txtDBName.setColumns(10);
        }
        return this.txtDBName;
    }
}
