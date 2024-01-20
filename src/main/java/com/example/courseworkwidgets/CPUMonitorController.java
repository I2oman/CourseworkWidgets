package com.example.courseworkwidgets;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;

public class CPUMonitorController {
    @FXML
    private AreaChart<Number, Number> cpuChart;
    private XYChart.Series<Number, Number> cpuSeries;
    @FXML
    private Label cpuLable;
    @FXML
    private NumberAxis cpuLoadAxis;
    private Thread cpuMonitorThread;
    private SystemMonitor systemMonitor;

    public void start(SystemMonitor systemMonitor) {
        this.systemMonitor = systemMonitor;
        cpuSeries = new XYChart.Series<>();
        Platform.runLater(() -> cpuChart.getData().add(cpuSeries));
        //Thread for updating the chart every second
        cpuMonitorThread = new Thread(() -> {
            while (!Thread.interrupted()) {
                try {
                    Platform.runLater(() -> {
                        try {
                            cpuSeries.getData().clear();
                            //Copying obtained series to the local series
                            for (XYChart.Data<Number, Number> data : systemMonitor.getCpuSeries().getData()) {
                                cpuSeries.getData().add(new XYChart.Data<>(data.getXValue(), data.getYValue()));
                            }
                            updateCPU(systemMonitor.getCpuLoad());
                        } catch (Exception ignored) {
                        }
                    });
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

        });
        cpuMonitorThread.start();
    }

    private void updateCPU(double cpuLoad) {
        //Updates the label with the CPU load percentage
        if (cpuLoad > cpuLoadAxis.getUpperBound()) {
            Platform.runLater(() -> cpuLoadAxis.setUpperBound(cpuLoad));
        }
        Platform.runLater(() -> {
            cpuLable.setText(cpuLoad + "%");
            cpuLoadAxis.setUpperBound(systemMonitor.getCPUUpperBound());
        });
    }

    public void exitWidget() {
        cpuMonitorThread.interrupt();
    }
}
