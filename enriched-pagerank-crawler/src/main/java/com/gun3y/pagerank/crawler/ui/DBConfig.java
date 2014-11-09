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
        initialize();
    }

    /**
     * Create the dialog.
     */
    public DBConfig() {

        initialize();
    }

    private void initialize() {
        setTitle("DB Config");
        setBounds(100, 100, 290, 145);
        BorderLayout borderLayout = new BorderLayout();
        getContentPane().setLayout(borderLayout);
        getContentPane().add(getPanelContent(), BorderLayout.CENTER);
        getContentPane().add(getPanelButtons(), BorderLayout.SOUTH);
        setType(Type.UTILITY);
        setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
        setModalityType(ModalityType.APPLICATION_MODAL);
        setModal(true);

        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - getHeight()) / 2);
        setLocation(x, y);
    }

    private JPanel getPanelContent() {
        if (panelContent == null) {
            panelContent = new JPanel();
            panelContent.setName("panelContent");
            panelContent.setLayout(new GridLayout(0, 2, 2, 2));
            panelContent.add(getLblServerUrl());
            panelContent.add(getTxtServerUrl());
            panelContent.add(getLblDbName());
            panelContent.add(getTxtDBName());
        }
        return panelContent;
    }

    private JPanel getPanelButtons() {
        if (panelButtons == null) {
            panelButtons = new JPanel();
            panelButtons.setName("panelButtons");
            panelButtons.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
            panelButtons.add(getBtnOK());
            panelButtons.add(getBtnCancel());
        }
        return panelButtons;
    }

    private JButton getBtnOK() {
        if (btnOK == null) {
            btnOK = new JButton("OK");
            btnOK.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (crawlerFrame != null) {
                        crawlerFrame.dbHost = txtServerUrl.getText();
                        crawlerFrame.dbName = txtDBName.getText();

                        DBConfig.this.dispose();
                    }
                }
            });
            btnOK.setName("btnOK");
        }
        return btnOK;
    }

    private JButton getBtnCancel() {
        if (btnCancel == null) {
            btnCancel = new JButton("Cancel");
            btnCancel.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    DBConfig.this.dispose();
                }
            });
            btnCancel.setName("btnCancel");
        }
        return btnCancel;
    }

    private JLabel getLblServerUrl() {
        if (lblServerUrl == null) {
            lblServerUrl = new JLabel("Server URL");
            lblServerUrl.setName("lblServerUrl");
        }
        return lblServerUrl;
    }

    private JTextField getTxtServerUrl() {
        if (txtServerUrl == null) {
            txtServerUrl = new JTextField();
            txtServerUrl.setText("localhost");
            txtServerUrl.setName("txtServerUrl");
            txtServerUrl.setColumns(10);
        }
        return txtServerUrl;
    }

    private JLabel getLblDbName() {
        if (lblDbName == null) {
            lblDbName = new JLabel("DB Name");
            lblDbName.setName("lblDbName");
        }
        return lblDbName;
    }

    private JTextField getTxtDBName() {
        if (txtDBName == null) {
            txtDBName = new JTextField();
            txtDBName.setText("PageRankCrawlDB");
            txtDBName.setName("txtDBName");
            txtDBName.setColumns(10);
        }
        return txtDBName;
    }
}
