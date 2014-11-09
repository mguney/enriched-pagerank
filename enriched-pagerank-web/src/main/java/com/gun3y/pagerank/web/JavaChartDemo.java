package com.gun3y.pagerank.web;

import java.awt.BorderLayout;
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
import javax.swing.SwingUtilities;

import com.gun3y.pagerank.web.d3.Stat;

public class JavaChartDemo {
    public static void main(String[] args) {
        //        SwingUtilities.invokeLater(new Runnable() {
        //            @Override
        //            public void run() {
        //                ChartFrame mainFrame = new ChartFrame();
        //                mainFrame.setVisible(true);
        //            }
        //        });
    }
}

class ChartFrame extends JFrame {

    private static final long serialVersionUID = -3364545580906836140L;

    JFXPanel fxPanel;

    List<Stat> stats;

    double min;

    double max;

    public ChartFrame(List<Stat> stats, double min, double max) {
        this.stats = stats;
        this.min = min;
        this.max = max;

        initSwingComponents();

        initFxComponents();
    }

    private void initSwingComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        fxPanel = new JFXPanel();
        
        JScrollPane jScrollPane = new JScrollPane();
        jScrollPane.add(fxPanel, BorderLayout.CENTER);

        this.add(jScrollPane);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(1000, 600);
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