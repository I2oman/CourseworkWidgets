package com.example.courseworkwidgets;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;

public class MemoryMonitorController {
    @FXML
    private AreaChart<Number, Number> memoryChart;
    private XYChart.Series<Number, Number> memorySeries;
    @FXML
    private Label memoryLable;
    @FXML
    private NumberAxis memoryLoadAxis;
    private Thread memoryMonitorThread;
    private SystemMonitor systemMonitor;

    public void start(SystemMonitor systemMonitor) {
        this.systemMonitor = systemMonitor;
        memorySeries = new XYChart.Series<>();
        Platform.runLater(() -> {
            memoryChart.getData().add(memorySeries);
            memoryLoadAxis.setUpperBound(systemMonitor.getTotalMemory());
        });
        memoryMonitorThread = new Thread(() -> {
            while (!Thread.interrupted()) {
                try {
                    Platform.runLater(() -> {
                        try {
                            memorySeries.getData().clear();
                            for (XYChart.Data<Number, Number> data : systemMonitor.getMemorySeries().getData()) {
                                memorySeries.getData().add(new XYChart.Data<>(data.getXValue(), data.getYValue()));
                            }
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
        memoryMonitorThread.start();
    }

    private void updateMemory(double memotyload) {
        Platform.runLater(() -> {
            double memUsedPr = (double) Math.round((memotyload * 100 / systemMonitor.getTotalMemory()) * 10) / 10;
            memoryLable.setText(memotyload + "/" + systemMonitor.getTotalMemory() + " GB (" + memUsedPr + "%)");
        });
    }

    public void exitWidget() {
        memoryMonitorThread.interrupt();
    }
}
