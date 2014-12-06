package com.gun3y.pagerank.crawler.ui;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gun3y.pagerank.crawler.BasicCrawler;
import com.gun3y.pagerank.mongo.MongoManager;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class CrawlerFrame extends JFrame {

    private static final Logger LOGGER = LoggerFactory.getLogger(CrawlerFrame.class);

    private static final long serialVersionUID = 1731132804598008409L;

    private JTextField txtCrawlerThread;
    private JTextField txtPolitenessDelay;
    private JTextField txtMaxDepthOfCrawling;
    private JTextField txtMaxPagesToFetch;
    private JTextField txtMaxConnectionsPerHost;
    private JTextField txtMaxTotalConnections;
    private JTextField txtSocketTimeout;
    private JTextField txtConnectionTimeout;
    private JTextField txtMaxOutgoingLinksToFollow;
    private JTextField txtMaxDownloadSize;
    private JTextField txtCrawlerStorage;
    private JTextField txtSeeds;
    private JList<String> listSeeds;

    private DefaultListModel<String> seedListModel;

    private JFileChooser fc;
    private JButton btnBrowse;
    private JButton btnAddSeed;
    private JButton btnRemoveSeed;

    private JButton btnDBConfig;

    private JCheckBox chckbxIncludeBinaryContentinCrawling;

    private JCheckBox chckbxFollowRedirects;

    private JCheckBox chckbxIncludeHttpsPages;

    private JCheckBox chckbxResumableCrawling;

    private JPanel panelLeft;

    private JPanel panelRight;

    private JTextField txtUserAgent;

    private MongoManager mongoManager;

    private CrawlController controller;

    String dbHost;

    String dbName;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        }
        catch (Exception e) {
            // If Nimbus is not available, you can set the GUI to another look
            // and feel.
        }
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    CrawlerFrame window = new CrawlerFrame();
                    window.setVisible(true);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public CrawlerFrame() {
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (CrawlerFrame.this.mongoManager != null) {
                    CrawlerFrame.this.mongoManager.close();
                }
            }
        });
        this.initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        this.setTitle("Crawler");
        this.setResizable(false);
        this.setBounds(100, 100, 934, 573);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - this.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - this.getHeight()) / 2);
        this.setLocation(x, y);

        this.fc = new JFileChooser();
        this.fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        this.fc.setCurrentDirectory(new File("."));

        this.getContentPane().setLayout(null);
        this.getContentPane().add(this.getPanelLeft());
        this.getContentPane().add(this.getPanelRight());

        JButton btnStart = new JButton("Start");
        btnStart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CrawlerFrame.this.startCrawler();
            }
        });
        btnStart.setFont(new Font("SansSerif", Font.BOLD, 16));
        btnStart.setBounds(217, 472, 200, 50);
        this.getContentPane().add(btnStart);
        this.getContentPane().add(this.getBtnDBConfig());

    }

    private void startCrawler() {
        String dbHost = "localhost";
        if (StringUtils.isNotBlank(this.dbHost)) {
            dbHost = this.dbHost;
        }
        String dbName = "PageRankCrawlerDB";
        if (StringUtils.isNotBlank(this.dbName)) {
            dbName = this.dbName;
        }

        CrawlConfig config = new CrawlConfig();
        if (StringUtils.isNotBlank(this.txtConnectionTimeout.getText())) {
            int connectionTimeout = Integer.parseInt(this.txtConnectionTimeout.getText());
            config.setConnectionTimeout(connectionTimeout);
        }
        String crawlStorageFolder = "tempCrawlData";
        if (StringUtils.isNotBlank(this.txtCrawlerStorage.getText())) {
            crawlStorageFolder = this.txtCrawlerStorage.getText();
        }
        config.setCrawlStorageFolder(crawlStorageFolder);

        config.setFollowRedirects(this.chckbxFollowRedirects.isSelected());
        config.setIncludeBinaryContentInCrawling(this.chckbxIncludeBinaryContentinCrawling.isSelected());
        config.setIncludeHttpsPages(this.chckbxIncludeHttpsPages.isSelected());
        config.setResumableCrawling(this.chckbxResumableCrawling.isSelected());

        if (StringUtils.isNotBlank(this.txtMaxConnectionsPerHost.getText())) {
            int connectionPerHost = Integer.parseInt(this.txtMaxConnectionsPerHost.getText());
            config.setMaxConnectionsPerHost(connectionPerHost);
        }

        if (StringUtils.isNotBlank(this.txtMaxDepthOfCrawling.getText())) {
            int depthOfCrawling = Integer.parseInt(this.txtMaxDepthOfCrawling.getText());
            config.setMaxDepthOfCrawling(depthOfCrawling);
        }

        if (StringUtils.isNotBlank(this.txtMaxDownloadSize.getText())) {
            int maxDownloadSize = Integer.parseInt(this.txtMaxDownloadSize.getText());
            config.setMaxDownloadSize(maxDownloadSize);
        }

        if (StringUtils.isNotBlank(this.txtMaxOutgoingLinksToFollow.getText())) {
            int maxOutgoingLinksToFollow = Integer.parseInt(this.txtMaxOutgoingLinksToFollow.getText());
            config.setMaxOutgoingLinksToFollow(maxOutgoingLinksToFollow);
        }

        if (StringUtils.isNotBlank(this.txtMaxPagesToFetch.getText())) {
            int maxPagesToFetch = Integer.parseInt(this.txtMaxPagesToFetch.getText());
            config.setMaxPagesToFetch(maxPagesToFetch);
        }

        if (StringUtils.isNotBlank(this.txtMaxTotalConnections.getText())) {
            int maxTotalConnections = Integer.parseInt(this.txtMaxTotalConnections.getText());
            config.setMaxTotalConnections(maxTotalConnections);
        }

        if (StringUtils.isNotBlank(this.txtPolitenessDelay.getText())) {
            int politenessDelay = Integer.parseInt(this.txtPolitenessDelay.getText());
            config.setPolitenessDelay(politenessDelay);
        }

        if (StringUtils.isNotBlank(this.txtSocketTimeout.getText())) {
            int socketTimeout = Integer.parseInt(this.txtSocketTimeout.getText());
            config.setSocketTimeout(socketTimeout);
        }

        if (StringUtils.isNotBlank(this.txtUserAgent.getText())) {
            config.setUserAgentString(this.txtUserAgent.getText());
        }

        LOGGER.debug(config.toString());
        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();

        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        this.mongoManager = new MongoManager(dbHost, dbName);

        try {
            this.controller = new CrawlController(config, pageFetcher, robotstxtServer);
            this.mongoManager.init();
        }
        catch (UnknownHostException e) {
            JOptionPane.showMessageDialog(this, "Unknown Host", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        for (int i = 0; i < this.seedListModel.getSize(); i++) {
            String seed = this.seedListModel.getElementAt(i);
            LOGGER.info("Seed {}: {}", i, seed);
            this.controller.addSeed(seed);
        }
        int crawlerThread = 5;
        if (StringUtils.isNotBlank(this.txtCrawlerThread.getText())) {
            crawlerThread = Integer.parseInt(this.txtCrawlerThread.getText());
        }
        LOGGER.info("Crawler Thread: " + crawlerThread);
        this.controller.setCustomData(this.mongoManager);

        this.controller.startNonBlocking(BasicCrawler.class, crawlerThread);

        LogViewer logViewer = new LogViewer(this, this.controller, this.mongoManager);
        logViewer.setVisible(true);
    }

    public JPanel getPanelRight() {
        if (this.panelRight == null) {
            this.panelRight = new JPanel();
            this.panelRight.setBounds(373, 5, 548, 460);

            JLabel lblCrawlerStorage = new JLabel("Crawler Storage");

            this.txtCrawlerStorage = new JTextField();
            this.txtCrawlerStorage.setColumns(10);

            this.btnBrowse = new JButton("Browse");
            this.btnBrowse.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int returnVal = CrawlerFrame.this.fc.showOpenDialog(CrawlerFrame.this);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        CrawlerFrame.this.txtCrawlerStorage.setText(CrawlerFrame.this.fc.getSelectedFile().getAbsolutePath());
                    }

                }
            });

            this.seedListModel = new DefaultListModel<String>();

            this.txtSeeds = new JTextField();
            this.txtSeeds.addKeyListener(new KeyAdapter() {
                @Override
                public void keyTyped(KeyEvent e) {
                    if (e.getKeyChar() == '\n') {
                        CrawlerFrame.this.btnAddSeed.doClick();
                    }
                }
            });
            this.txtSeeds.setColumns(10);

            JLabel lblSeeds = new JLabel("Seeds");

            this.btnRemoveSeed = new JButton("Remove");
            this.btnRemoveSeed.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (CrawlerFrame.this.listSeeds.getSelectedIndex() >= 0) {
                        CrawlerFrame.this.seedListModel.remove(CrawlerFrame.this.listSeeds.getSelectedIndex());
                    }
                }
            });

            this.btnAddSeed = new JButton("Add");
            this.btnAddSeed.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String newSeed = CrawlerFrame.this.txtSeeds.getText();
                    if (StringUtils.isNotBlank(newSeed) && !CrawlerFrame.this.seedListModel.contains(newSeed)) {
                        if (this.checkURL(newSeed)) {
                            CrawlerFrame.this.seedListModel.addElement(newSeed);
                        }
                        else {
                            JOptionPane.showMessageDialog(CrawlerFrame.this, "Please, check the seed url", "Malformed URL",
                                    JOptionPane.ERROR_MESSAGE);
                        }

                    }
                }

                private boolean checkURL(String newSeed) {
                    try {
                        new URL(newSeed);
                    }
                    catch (MalformedURLException e) {
                        return false;
                    }
                    return true;
                }
            });

            JScrollPane scrollPaneSeeds = new JScrollPane();

            this.listSeeds = new JList<String>();
            this.listSeeds.addKeyListener(new KeyAdapter() {
                @Override
                public void keyTyped(KeyEvent e) {
                    if (e.getKeyChar() == 127) {
                        CrawlerFrame.this.btnRemoveSeed.doClick();
                    }
                }
            });
            this.listSeeds.setModel(this.seedListModel);
            scrollPaneSeeds.setViewportView(this.listSeeds);

            JLabel lblUserAgent = new JLabel("User Agent");
            lblUserAgent.setName("lblUserAgent");

            this.txtUserAgent = new JTextField();
            this.txtUserAgent.setColumns(10);

            GroupLayout gl_panelRight = new GroupLayout(this.panelRight);
            gl_panelRight.setHorizontalGroup(gl_panelRight.createParallelGroup(Alignment.LEADING).addGroup(
                    gl_panelRight
                    .createSequentialGroup()
                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(
                            gl_panelRight
                            .createParallelGroup(Alignment.TRAILING)
                            .addComponent(scrollPaneSeeds, GroupLayout.PREFERRED_SIZE, 420, GroupLayout.PREFERRED_SIZE)
                            .addGroup(
                                    gl_panelRight.createSequentialGroup().addComponent(this.btnAddSeed)
                                    .addPreferredGap(ComponentPlacement.RELATED).addComponent(this.btnRemoveSeed))
                                    .addComponent(this.btnBrowse)
                                    .addGroup(
                                            gl_panelRight
                                            .createSequentialGroup()
                                            .addGroup(
                                                    gl_panelRight
                                                    .createParallelGroup(Alignment.LEADING)
                                                    .addComponent(lblCrawlerStorage, GroupLayout.PREFERRED_SIZE,
                                                            104, GroupLayout.PREFERRED_SIZE)
                                                            .addComponent(lblUserAgent)
                                                            .addComponent(lblSeeds, GroupLayout.PREFERRED_SIZE, 80,
                                                                    GroupLayout.PREFERRED_SIZE))
                                                                    .addPreferredGap(ComponentPlacement.RELATED)
                                                                    .addGroup(
                                                                            gl_panelRight
                                                                            .createParallelGroup(Alignment.LEADING, false)
                                                                            .addComponent(this.txtUserAgent, Alignment.TRAILING)
                                                                            .addComponent(this.txtCrawlerStorage, Alignment.TRAILING,
                                                                                    GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE)
                                                                                    .addComponent(this.txtSeeds, GroupLayout.PREFERRED_SIZE, 420,
                                                                                            GroupLayout.PREFERRED_SIZE)))).addGap(12)));
            gl_panelRight.setVerticalGroup(gl_panelRight.createParallelGroup(Alignment.LEADING).addGroup(
                    gl_panelRight
                    .createSequentialGroup()
                    .addContainerGap()
                    .addGroup(
                            gl_panelRight
                            .createParallelGroup(Alignment.BASELINE)
                            .addComponent(lblCrawlerStorage, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
                            .addComponent(this.txtCrawlerStorage, GroupLayout.PREFERRED_SIZE, 30,
                                    GroupLayout.PREFERRED_SIZE))
                                    .addPreferredGap(ComponentPlacement.RELATED)
                                    .addComponent(this.btnBrowse, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(ComponentPlacement.RELATED)
                                    .addGroup(
                                            gl_panelRight.createParallelGroup(Alignment.BASELINE)
                                            .addComponent(this.txtUserAgent, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
                                            .addComponent(lblUserAgent))
                                            .addPreferredGap(ComponentPlacement.RELATED)
                                            .addGroup(
                                                    gl_panelRight.createParallelGroup(Alignment.BASELINE)
                                                    .addComponent(this.txtSeeds, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
                                                    .addComponent(lblSeeds, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
                                                    .addPreferredGap(ComponentPlacement.RELATED)
                                                    .addGroup(
                                                            gl_panelRight.createParallelGroup(Alignment.BASELINE)
                                                            .addComponent(this.btnRemoveSeed, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
                                                            .addComponent(this.btnAddSeed, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
                                                            .addPreferredGap(ComponentPlacement.RELATED)
                                                            .addComponent(scrollPaneSeeds, GroupLayout.PREFERRED_SIZE, 256, GroupLayout.PREFERRED_SIZE)
                                                            .addContainerGap(73, Short.MAX_VALUE)));
            gl_panelRight.setAutoCreateGaps(true);
            gl_panelRight.setAutoCreateContainerGaps(true);
            this.panelRight.setLayout(gl_panelRight);
        }
        return this.panelRight;
    }

    public JPanel getPanelLeft() {
        if (this.panelLeft == null) {
            this.panelLeft = new JPanel();
            this.panelLeft.setBounds(10, 10, 350, 450);
            this.panelLeft.setLayout(new GridLayout(0, 2, 2, 2));

            JLabel lblCrawlerThread = new JLabel("Crawler Thread");
            this.panelLeft.add(lblCrawlerThread);

            this.txtCrawlerThread = new JTextField();
            this.txtCrawlerThread.setColumns(10);
            this.txtCrawlerThread.addKeyListener(this.numericKeyAdapter);
            this.txtCrawlerThread.setText("5");
            this.panelLeft.add(this.txtCrawlerThread);

            JLabel lblPolitenessDelay = new JLabel("Politeness Delay");
            this.panelLeft.add(lblPolitenessDelay);

            this.txtPolitenessDelay = new JTextField();
            this.txtPolitenessDelay.setColumns(10);
            this.txtPolitenessDelay.addKeyListener(this.numericKeyAdapter);
            this.txtPolitenessDelay.setText("500");
            this.panelLeft.add(this.txtPolitenessDelay);

            JLabel lblMaxDepthOfCrawling = new JLabel("Crawling Depth");
            this.panelLeft.add(lblMaxDepthOfCrawling);

            this.txtMaxDepthOfCrawling = new JTextField();
            this.txtMaxDepthOfCrawling.setColumns(10);
            this.txtMaxDepthOfCrawling.addKeyListener(this.numericKeyAdapter);
            this.txtMaxDepthOfCrawling.setText("");
            this.panelLeft.add(this.txtMaxDepthOfCrawling);

            JLabel lblMaxPagesToFetch = new JLabel("Pages to Fetch (Max)");
            this.panelLeft.add(lblMaxPagesToFetch);

            this.txtMaxPagesToFetch = new JTextField();
            this.txtMaxPagesToFetch.setColumns(10);
            this.txtMaxPagesToFetch.addKeyListener(this.numericKeyAdapter);
            this.panelLeft.add(this.txtMaxPagesToFetch);

            JLabel lblMaxConnectionsPerHost = new JLabel("Connections Per Host (Max)");
            this.panelLeft.add(lblMaxConnectionsPerHost);

            this.txtMaxConnectionsPerHost = new JTextField();
            this.txtMaxConnectionsPerHost.setText("100");
            this.txtMaxConnectionsPerHost.setColumns(10);
            this.txtMaxConnectionsPerHost.addKeyListener(this.numericKeyAdapter);
            this.panelLeft.add(this.txtMaxConnectionsPerHost);

            JLabel lblMaxTotalConnections = new JLabel("Total Connections (Max)");
            this.panelLeft.add(lblMaxTotalConnections);

            this.txtMaxTotalConnections = new JTextField();
            this.txtMaxTotalConnections.setText("100");
            this.txtMaxTotalConnections.setColumns(10);
            this.txtMaxTotalConnections.addKeyListener(this.numericKeyAdapter);
            this.panelLeft.add(this.txtMaxTotalConnections);

            JLabel lblSocketTimeout = new JLabel("Socket Timeout");
            this.panelLeft.add(lblSocketTimeout);

            this.txtSocketTimeout = new JTextField();
            this.txtSocketTimeout.setText("20000");
            this.txtSocketTimeout.setColumns(10);
            this.txtSocketTimeout.addKeyListener(this.numericKeyAdapter);
            this.panelLeft.add(this.txtSocketTimeout);

            JLabel lblConnectionTimeout = new JLabel("Connection Timeout");
            this.panelLeft.add(lblConnectionTimeout);

            this.txtConnectionTimeout = new JTextField();
            this.txtConnectionTimeout.setText("30000");
            this.txtConnectionTimeout.setColumns(10);
            this.txtConnectionTimeout.addKeyListener(this.numericKeyAdapter);
            this.panelLeft.add(this.txtConnectionTimeout);

            JLabel lblMaxOutgoingLinksToFollow = new JLabel("Outgoing Links To Follow (Max)");
            this.panelLeft.add(lblMaxOutgoingLinksToFollow);

            this.txtMaxOutgoingLinksToFollow = new JTextField();
            this.txtMaxOutgoingLinksToFollow.setText("5000");
            this.txtMaxOutgoingLinksToFollow.setColumns(10);
            this.txtMaxOutgoingLinksToFollow.addKeyListener(this.numericKeyAdapter);
            this.panelLeft.add(this.txtMaxOutgoingLinksToFollow);

            JLabel lblMaxDownloadSize = new JLabel("Download Size");
            this.panelLeft.add(lblMaxDownloadSize);

            this.txtMaxDownloadSize = new JTextField();
            this.txtMaxDownloadSize.setText("1048576");
            this.txtMaxDownloadSize.setColumns(10);
            this.txtMaxDownloadSize.addKeyListener(this.numericKeyAdapter);
            this.panelLeft.add(this.txtMaxDownloadSize);

            JLabel lblResumableCrawling = new JLabel("Resumable Crawling");
            this.panelLeft.add(lblResumableCrawling);

            this.chckbxResumableCrawling = new JCheckBox("");
            this.panelLeft.add(this.chckbxResumableCrawling);

            JLabel lblIncludeHttpsPages = new JLabel("Includes Https Pages");
            this.panelLeft.add(lblIncludeHttpsPages);

            this.chckbxIncludeHttpsPages = new JCheckBox("");
            this.chckbxIncludeHttpsPages.setSelected(true);
            this.panelLeft.add(this.chckbxIncludeHttpsPages);

            JLabel lblIncludebinarycontentincrawling = new JLabel("Includes Binary Content");
            this.panelLeft.add(lblIncludebinarycontentincrawling);

            this.chckbxIncludeBinaryContentinCrawling = new JCheckBox("");
            this.panelLeft.add(this.chckbxIncludeBinaryContentinCrawling);

            JLabel lblFollowRedirects = new JLabel("Follow Redirects");
            this.panelLeft.add(lblFollowRedirects);

            this.chckbxFollowRedirects = new JCheckBox("");
            this.chckbxFollowRedirects.setSelected(true);
            this.panelLeft.add(this.chckbxFollowRedirects);

        }
        return this.panelLeft;
    }

    private KeyAdapter numericKeyAdapter = new KeyAdapter() {
        @Override
        public void keyTyped(KeyEvent e) {
            char c = e.getKeyChar();
            if (!Character.isDigit(c)) {
                e.consume();
            }
        }
    };

    private JButton getBtnDBConfig() {
        if (this.btnDBConfig == null) {
            this.btnDBConfig = new JButton("DB Config");
            this.btnDBConfig.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    DBConfig dbConfig = new DBConfig(CrawlerFrame.this);
                    dbConfig.setVisible(true);
                }
            });
            this.btnDBConfig.setFont(new Font("SansSerif", Font.BOLD, 16));
            this.btnDBConfig.setBounds(499, 472, 200, 50);
            this.btnDBConfig.setName("btnDBConfig");
        }
        return this.btnDBConfig;
    }
}
