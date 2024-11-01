import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.util.Timer;
import java.util.TimerTask;

public class ServerMonitor {
    private static final double CPU_THRESHOLD = 0.8;    // 80% CPU usage
    private static final double MEMORY_THRESHOLD = 0.8; // 80% memory usage
    private static final double DISK_THRESHOLD = 0.9;   // 90% disk usage

    public static void main(String[] args) {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new MonitorTask(), 0, 60000); // Run every minute
    }

    static class MonitorTask extends TimerTask {
        @Override
        public void run() {
            try {
                double cpuLoad = getCPULoad();
                double memoryUsage = getMemoryUsage();
                double diskUsage = getDiskUsage();

                System.out.printf("CPU: %.2f%%, Memory: %.2f%%, Disk: %.2f%%\n", cpuLoad * 100, memoryUsage * 100, diskUsage * 100);

                if (cpuLoad > CPU_THRESHOLD || memoryUsage > MEMORY_THRESHOLD || diskUsage > DISK_THRESHOLD) {
                    sendAlertEmail(cpuLoad, memoryUsage, diskUsage);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private double getCPULoad() {
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            return osBean.getSystemLoadAverage() / osBean.getAvailableProcessors();
        }

        private double getMemoryUsage() {
            long totalMemory = Runtime.getRuntime().totalMemory();
            long freeMemory = Runtime.getRuntime().freeMemory();
            return 1.0 - (double) freeMemory / totalMemory;
        }

        private double getDiskUsage() throws IOException {
            double highestUsage = 0.0;
            for (FileStore store : FileSystems.getDefault().getFileStores()) {
                long totalSpace = store.getTotalSpace();
                long usedSpace = totalSpace - store.getUnallocatedSpace();
                double usage = (double) usedSpace / totalSpace;
                if (usage > highestUsage) highestUsage = usage;
            }
            return highestUsage;
        }

        private void sendAlertEmail(double cpu, double memory, double disk) {
            try {
                Email email = new SimpleEmail();
                email.setHostName("smtp.example.com");
                email.setSmtpPort(465);
                email.setAuthentication("your_email@example.com", "your_password");
                email.setSSLOnConnect(true);
                email.setFrom("your_email@example.com");
                email.setSubject("Server Alert: Resource Usage Exceeded");
                email.setMsg(String.format("Alert! CPU: %.2f%%, Memory: %.2f%%, Disk: %.2f%% exceeded limits!", cpu * 100, memory * 100, disk * 100));
                email.addTo("admin@example.com");
                email.send();
                System.out.println("Alert email sent successfully.");
            } catch (EmailException e) {
                e.printStackTrace();
            }
        }
    }
}
