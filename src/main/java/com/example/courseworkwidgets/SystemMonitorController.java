package com.example.courseworkwidgets;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;

public class SystemMonitorController {
    @FXML
    public AreaChart<Number, Number> cpuChart;
    @FXML
    public NumberAxis cpuTimeAxis;
    @FXML
    public NumberAxis cpuLoadAxis;
    @FXML
    public Label cpuLable;
    private XYChart.Series<Number, Number> cpuSeries;
    @FXML
    public AreaChart<Number, Number> memoryChart;
    @FXML
    public NumberAxis memoryTimeAxis;
    @FXML
    public NumberAxis memoryLoadAxis;
    @FXML
    public Label memoryLable;
    private XYChart.Series<Number, Number> memorySeries;
    private Thread systemMonitorThread;
    private SystemMonitor systemMonitor;

    public void start(SystemMonitor systemMonitor) {
        this.systemMonitor = systemMonitor;
        cpuSeries = new XYChart.Series<>();
        memorySeries = new XYChart.Series<>();
        Platform.runLater(() -> {
            cpuChart.getData().add(cpuSeries);
            memoryChart.getData().add(memorySeries);
            memoryLoadAxis.setUpperBound(systemMonitor.getTotalMemory());
        });
        //Thread for updating the chart every second
        systemMonitorThread = new Thread(() -> {
            while (!Thread.interrupted()) {
                try {
                    Platform.runLater(() -> {
                        try {
                            //Copying obtained series to the local series
                            cpuSeries.getData().clear();
                            for (XYChart.Data<Number, Number> data : systemMonitor.getCpuSeries().getData()) {
                                cpuSeries.getData().add(new XYChart.Data<>(data.getXValue(), data.getYValue()));
                            }
                            memorySeries.getData().clear();
                            for (XYChart.Data<Number, Number> data : systemMonitor.getMemorySeries().getData()) {
                                memorySeries.getData().add(new XYChart.Data<>(data.getXValue(), data.getYValue()));
                            }
                            updateCPU(systemMonitor.getCpuLoad());
                            updateMemory(systemMonitor.getMemoryLoad());
                        } catch (Exception ignored) {
                        }
                    });
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        systemMonitorThread.start();
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

    private void updateMemory(double memotyload) {
        //Changes the label to the current memory load in gigabytes along with the percentage value
        Platform.runLater(() -> {
            double memUsedPr = (double) Math.round((memotyload * 100 / systemMonitor.getTotalMemory()) * 10) / 10;
            memoryLable.setText(memotyload + "/" + systemMonitor.getTotalMemory() + " GB\n(" + memUsedPr + "%)");
        });
    }

    public void exitWidget() {
        systemMonitorThread.interrupt();
    }
}
