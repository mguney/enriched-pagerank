package com.gun3y.pagerank.web;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.util.Collections;
import java.util.List;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.GridPane;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import com.gun3y.pagerank.web.d3.Stat;

public class BarChartFrame extends JFrame {

    private static final long serialVersionUID = 7607963134775027603L;

    private JPanel contentPane;

    JFXPanel fxPanel;

    List<Stat> stats;

    double min;

    double max;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    BarChartFrame frame = new BarChartFrame();
                    frame.setVisible(true);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    public BarChartFrame() {
        this(Collections.<Stat>emptyList(), 10d, 200d);
    }
    /**
     * Create the frame.
     */
    public BarChartFrame(List<Stat> stats, double min, double max) {

        this.stats = stats;
        this.min = min;
        this.max = max;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(1000, 600);
        int x = (int) ((dimension.getWidth() - this.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - this.getHeight()) / 2);

        setLocation(x, y);

        fxPanel = new JFXPanel();

        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.add(fxPanel);
        contentPane.add(fxPanel, BorderLayout.CENTER);

        initFxComponents();
    }

    private void initFxComponents() {

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                GridPane grid = new GridPane();
                Scene scene = new Scene(grid, 2000, 600);

                /**
                 * Construct and populate Bar chart.
                 * It uses 2 series of data.
                 */
                NumberAxis lineYAxis = new NumberAxis(0, max, min);
                lineYAxis.setLabel("Scores");
                CategoryAxis lineXAxis = new CategoryAxis();
                lineXAxis.setLabel("URLs");
                BarChart barChart = new BarChart(lineXAxis, lineYAxis);
                barChart.setMinHeight(600);
                barChart.setMinWidth(2000);

                XYChart.Series<String, Double> bar1 = new XYChart.Series<String, Double>();
                bar1.setName("Domain A");
                for (Stat stat : stats) {
                    bar1.getData().add(getData(stat.value, stat.id + ""));
                }

                //                bar1.getData().add(getData(30_000, "Netbooks"));
                //                bar1.getData().add(getData(70_000, "Tablets"));
                //                bar1.getData().add(getData(90_000, "Smartphones"));

                //                XYChart.Series<String, Double> bar2 = new XYChart.Series<String, Double>();
                //                bar2.setName("Consumer Goods");
                //                bar2.getData().add(getData(60_000, "Washing Machines"));
                //                bar2.getData().add(getData(70_000, "Telivision"));
                //                bar2.getData().add(getData(50_000, "Microwave Ovens"));
                //                bar2.getData().add(getData(90_000, "2"));
                //                bar2.getData().add(getData(80_000, "3"));
                //                bar2.getData().add(getData(20_000, "4"));

                barChart.getData().addAll(bar1);
                grid.setVgap(40);
                grid.setHgap(40);
                grid.add(barChart, 0, 0);
                fxPanel.setScene(scene);
            }
        });

    }

    private XYChart.Data<String, Double> getData(double x, String y) {
        XYChart.Data<String, Double> data = new XYChart.Data<String, Double>();
        data.setYValue(x);
        data.setXValue(y);
        return data;
    }

}
