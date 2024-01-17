package com.example.courseworkwidgets;

import com.sun.management.OperatingSystemMXBean;
import javafx.scene.chart.XYChart;

import java.lang.management.ManagementFactory;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SystemMonitor {
    private ScheduledExecutorService executorService;
    private OperatingSystemMXBean osBean;
    private double cpuLoad;
    private double cpuUpperBound;
    private XYChart.Series<Number, Number> cpuSeries;
    private double totalMemory;
    private double memoryLoad;
    private XYChart.Series<Number, Number> memorySeries;
    private int count;

    public SystemMonitor() {
        osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        cpuUpperBound = 100;

        totalMemory = osBean.getTotalMemorySize() / Math.pow(1024, 3);
        totalMemory = Math.round(totalMemory * 10.0) / 10.0;

        cpuSeries = new XYChart.Series<>();
        memorySeries = new XYChart.Series<>();
        count = 0;

        executorService = Executors.newScheduledThreadPool(1);

        startMonitoring();
    }

    private void startMonitoring() {
        executorService.scheduleAtFixedRate(() -> {
            try {
                while (!Thread.interrupted()) {
                    double cpuLoad = roundToOne(osBean.getCpuLoad() * 100);
                    double memoryLoad = roundToOne((osBean.getTotalMemorySize() - osBean.getFreeMemorySize()) / Math.pow(1024, 3));

                    this.cpuLoad = cpuLoad;
                    cpuSeries.getData().add(new XYChart.Data<>(count, cpuLoad));
                    this.memoryLoad = memoryLoad;
                    memorySeries.getData().add(new XYChart.Data<>(count, memoryLoad));
                    rescaleTimeLine();

                    if (count % 10 == 0 && count != 0) {
                        double highestCPU = -1;
                        for (XYChart.Data<Number, Number> data : cpuSeries.getData()) {
                            double loadCPU = (double) data.getYValue();
                            if (loadCPU > highestCPU) {
                                highestCPU = loadCPU;
                            }
                        }
                        cpuUpperBound = ((double) ((int) highestCPU / 10) + 1) * 10;
                        if (cpuUpperBound > 100) {
                            cpuUpperBound = 100;
                        }
                    }

                    count++;

                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    public void stopMonitoring() {
        executorService.shutdown();
    }

    private Double roundToOne(Number n) {
        return (double) (Math.round((double) n * 10) / 10);
    }

    private void rescaleTimeLine() {
        if (count >= 60) {
            cpuSeries.getData().removeFirst();
            memorySeries.getData().removeFirst();
            count = 60;
        }
        for (int i = 0; i < cpuSeries.getData().size(); i++) {
            cpuSeries.getData().get(i).setXValue(i);
            memorySeries.getData().get(i).setXValue(i);
        }
    }

    public int getCount() {
        return count;
    }

    public double getCpuLoad() {
        return cpuLoad;
    }

    public double getCPUUpperBound() {
        return cpuUpperBound;
    }

    public XYChart.Series<Number, Number> getCpuSeries() {
        return cpuSeries;
    }

    public double getTotalMemory() {
        return totalMemory;
    }

    public double getMemoryLoad() {
        return memoryLoad;
    }

    public XYChart.Series<Number, Number> getMemorySeries() {
        return memorySeries;
    }

    @Override
    public String toString() {
        return "SystemMonitor{" +
                "\n\tcount=" + count +
                "\n\ttotalMemory=" + totalMemory +
                "\n\tcpuSeries=" + cpuSeries.getData() +
                "\n\tmemorySeries=" + memorySeries.getData() +
                "\n}";
    }
}
