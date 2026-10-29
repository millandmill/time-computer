import java.awt.Font;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.plaf.FontUIResource;

public class TimeComputer extends JFrame {

    private JLabel lblClock;
    private JLabel lblStatus;
    private JLabel lblCountdown; // ⭐ เพิ่ม

    private JRadioButton rbCountdown;
    private JRadioButton rbTimeOfDay;

    private JSpinner spMinute;
    private JSpinner spHour;
    private JSpinner spMin;

    private JButton btnStart;
    private JButton btnCancel;

    private Timer clockTimer;
    private Timer shutdownTimer;
    private Timer countdownUI; // ⭐ timer สำหรับอัปเดตเวลา

    private long remainingMillis = 0; // ⭐ เก็บเวลาที่เหลือ

    public TimeComputer() {
        setUIFont(new Font("Tahoma", Font.PLAIN, 14));
		setIconImage(new ImageIcon(getClass().getResource("/icon.png")).getImage());
        initUI();
        startClock();
    }

    private void initUI() {

        setTitle("ตั้งเวลาปิดคอมพิวเตอร์");
        setSize(520, 380);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(null);

        JLabel lblTitle = new JLabel("เวลาปัจจุบัน");
        lblTitle.setBounds(30, 20, 150, 20);
        add(lblTitle);

        lblClock = new JLabel("00:00:00");
        lblClock.setFont(new Font("Tahoma", Font.BOLD, 28));
        lblClock.setBounds(30, 45, 250, 40);
        add(lblClock);

        // โหมด
        rbCountdown = new JRadioButton("นับถอยหลัง (นาที)");
        rbTimeOfDay = new JRadioButton("ปิดตามเวลา (ชั่วโมง:นาที)");

        rbCountdown.setBounds(30, 100, 200, 25);
        rbTimeOfDay.setBounds(250, 100, 220, 25);

        ButtonGroup group = new ButtonGroup();
        group.add(rbCountdown);
        group.add(rbTimeOfDay);

        rbCountdown.setSelected(true);

        add(rbCountdown);
        add(rbTimeOfDay);

        // input countdown
        spMinute = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));
        spMinute.setBounds(30, 130, 80, 30);
        add(spMinute);

        // input time
        spHour = new JSpinner(new SpinnerNumberModel(0, 0, 23, 1));
        spHour.setBounds(250, 130, 60, 30);
        add(spHour);

        spMin = new JSpinner(new SpinnerNumberModel(0, 0, 59, 1));
        spMin.setBounds(320, 130, 60, 30);
        add(spMin);

        // ปุ่ม
        btnStart = new JButton("เริ่ม");
        btnStart.setBounds(120, 200, 120, 40);
        add(btnStart);

        btnCancel = new JButton("ยกเลิก");
        btnCancel.setBounds(260, 200, 120, 40);
        add(btnCancel);

        // ⭐ แสดง countdown จริง
        lblCountdown = new JLabel("00:00:00");
        lblCountdown.setFont(new Font("Tahoma", Font.BOLD, 26));
		lblCountdown.setForeground(java.awt.Color.RED);
        lblCountdown.setBounds(180, 260, 200, 40);
        add(lblCountdown);

        lblStatus = new JLabel("Stand-by");
        lblStatus.setFont(new Font("Tahoma", Font.BOLD, 18));
        lblStatus.setBounds(200, 310, 200, 30);
        add(lblStatus);

        btnStart.addActionListener(e -> startShutdown());
        btnCancel.addActionListener(e -> cancelShutdown());
    }

    // ================= CLOCK =================
    private void startClock() {
        clockTimer = new Timer(1000, e -> {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            lblClock.setText(sdf.format(new Date()));
        });
        clockTimer.start();
    }

    // ================= START =================
    private void startShutdown() {

        stopAll();

        if (rbCountdown.isSelected()) {
            startCountdown();
        } else {
            startTimeOfDay();
        }
    }

    // ================= COUNTDOWN =================
    private void startCountdown() {

        int minute = (Integer) spMinute.getValue();
        remainingMillis = minute * 60L * 1000L;

        // timer ปิดเครื่อง
        shutdownTimer = new Timer((int) remainingMillis, e -> shutdownPC());
        shutdownTimer.setRepeats(false);
        shutdownTimer.start();

        // ⭐ timer อัปเดต UI ทุก 1 วิ
        countdownUI = new Timer(1000, e -> {

            remainingMillis -= 1000;

            if (remainingMillis <= 0) {
                lblCountdown.setText("00:00:00");
                countdownUI.stop();
                return;
            }

            long sec = remainingMillis / 1000;
            long h = sec / 3600;
            long m = (sec % 3600) / 60;
            long s = sec % 60;

            lblCountdown.setText(String.format("%02d:%02d:%02d", h, m, s));
        });

        countdownUI.start();

        lblStatus.setText("นับถอยหลัง " + minute + " นาที");
    }

    // ================= TIME OF DAY =================
    private void startTimeOfDay() {

        int hour = (Integer) spHour.getValue();
        int min = (Integer) spMin.getValue();

        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

        try {
            Date nowTime = sdf.parse(sdf.format(now));
            Date targetTime = sdf.parse(String.format("%02d:%02d", hour, min));

            long diff = targetTime.getTime() - nowTime.getTime();

            if (diff <= 0) {
                diff += 24 * 60 * 60 * 1000;
            }

            remainingMillis = diff;

            shutdownTimer = new Timer((int) diff, e -> shutdownPC());
            shutdownTimer.setRepeats(false);
            shutdownTimer.start();

            // ⭐ แสดง countdown เช่นกัน
            countdownUI = new Timer(1000, e -> {

                remainingMillis -= 1000;

                long sec = remainingMillis / 1000;
                long h = sec / 3600;
                long m = (sec % 3600) / 60;
                long s = sec % 60;

                lblCountdown.setText(String.format("%02d:%02d:%02d", h, m, s));
            });

            countdownUI.start();

            lblStatus.setText("จะปิดเวลา " + String.format("%02d:%02d", hour, min));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // ================= CANCEL =================
    private void cancelShutdown() {
        stopAll();
        lblStatus.setText("Stand-by");
        lblCountdown.setText("00:00:00");
    }

    // ================= STOP ALL =================
    private void stopAll() {

        if (shutdownTimer != null) shutdownTimer.stop();
        if (countdownUI != null) countdownUI.stop();
    }

    // ================= SHUTDOWN =================
    private void shutdownPC() {
        try {
            Runtime.getRuntime().exec("shutdown -s -t 0");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // ================= FONT FIX =================
    private void setUIFont(Font font) {
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);

            if (value instanceof FontUIResource) {
                UIManager.put(key, new FontUIResource(font));
            }
        }
    }

    // ================= MAIN =================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TimeComputer().setVisible(true);
        });
    }
}