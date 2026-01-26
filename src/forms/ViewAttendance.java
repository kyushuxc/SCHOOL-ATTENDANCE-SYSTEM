/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package forms;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import dao.ConnectionProvider;
import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import static org.apache.xmlbeans.impl.schema.StscState.end;
import static org.apache.xmlbeans.impl.schema.StscState.start;
import utility.BDUtility;

/**
 *
 * @author Pejj
 */
public class ViewAttendance extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ViewAttendance.class.getName());

    /**
     * Creates new form ViewAttendance
     */
 public ViewAttendance() {
    initComponents();
    BDUtility.setImage(this, "images/newbgs (1).jpg", 1020, 528);
    this.getRootPane().setBorder(BorderFactory.createMatteBorder(6, 6, 6, 6, Color.GRAY));
    
    setupGenerateExcel(generateBtn, studentTable);

    startMidnightClearThread(); // ✅ Add this line 
      
     maleBtn.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        filterByGender("Male");
    }
});

fmaleBtn.addActionListener(new java.awt.event.ActionListener() {
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        filterByGender("Female");
    }
});

 
      }      
private void filterByGender(String genderKeyword) {
    List<String> columns = Arrays.asList("LRN", "NAME", "GENDER", "DATE", "TIME-IN", "GRADE & SEC");
    DefaultTableModel model = new DefaultTableModel();
    model.setColumnIdentifiers(columns.toArray());
    studentTable.setModel(model);

    LocalDate today = LocalDate.now();

    String sql = "SELECT combined.studentId, combined.name, " +
                 "COALESCE(s.gender, combined.gender, 'N/A') as gender, " +
                 "combined.date, combined.timeIn, combined.section " +
                 "FROM (" +
                 "SELECT studentId, name, gender, date, timeIn, section FROM studentAttendance " + // ✅ Grade 11 table
                 "UNION ALL " +
                 "SELECT studentId, name, gender, date, timeIn, section FROM studentAttendanceArchive" +
                 ") AS combined " +
                 "LEFT JOIN student s ON combined.studentId = s.id " +
                 "WHERE DATE(combined.date) = ? " +
                 "AND COALESCE(s.gender, combined.gender) = ? " +
                 "AND combined.section = '12-Java'"; // ✅ Section filter

    long presentCount = 0;

    try (Connection con = ConnectionProvider.getCon();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setDate(1, java.sql.Date.valueOf(today));
        ps.setString(2, genderKeyword);

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            List<Object> row = new ArrayList<>();
            row.add(rs.getString("studentId"));
            row.add(rs.getString("name"));
            row.add(rs.getString("gender"));

            LocalDate rawDate = rs.getDate("date").toLocalDate();
            LocalTime rawTime = rs.getTime("timeIn").toLocalTime();

            ZoneId dbZone = ZoneId.of("UTC");
            ZoneId phZone = ZoneId.of("Asia/Manila");

            LocalDateTime utcDateTime = LocalDateTime.of(rawDate, rawTime);
            ZonedDateTime phDateTime = utcDateTime.atZone(dbZone).withZoneSameInstant(phZone);

            String formattedDate = phDateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
            String formattedTime = phDateTime.format(DateTimeFormatter.ofPattern("hh:mma")).toLowerCase();

            row.add(formattedDate);
            row.add(formattedTime);
            row.add(rs.getString("section"));

            model.addRow(row.toArray());

            presentCount++;
        }

        // ✅ Calculate absent count dynamically for this gender in 11-Andriod
        long totalStudentsOfGender = 0;
        String countSql = "SELECT COUNT(*) AS total FROM student WHERE section = '12-Java' AND gender = ?";
        try (PreparedStatement psCount = con.prepareStatement(countSql)) {
            psCount.setString(1, genderKeyword);
            ResultSet rsCount = psCount.executeQuery();
            if (rsCount.next()) {
                totalStudentsOfGender = rsCount.getLong("total");
            }
        }

        long absentCount = totalStudentsOfGender - presentCount;

        lblPresent.setText(String.valueOf(presentCount));
        lblAbsent.setText(String.valueOf(absentCount));

    } catch (Exception ex) {
        JOptionPane.showMessageDialog(null, "Error filtering by gender: " + ex.getMessage());
        ex.printStackTrace();
    }
}


private void startMidnightClearThread() {
    Thread midnightThread = new Thread(() -> {
        while (true) {
            LocalTime now = LocalTime.now();
            if (now.getHour() == 0 && now.getMinute() < 5) { // within first 5 minutes of midnight
                clearTablePreserveData();

                try {
                    Thread.sleep(300000); // sleep 5 minutes to avoid multiple triggers
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }

            try {
                Thread.sleep(60000); // check every minute
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    });
    midnightThread.setDaemon(true); // allows app to close properly
    midnightThread.start();
}
private void clearTablePreserveData() {
    DefaultTableModel model = (DefaultTableModel) studentTable.getModel();
    model.setRowCount(0); // Clears all rows
    lblPresent.setText("0");
    lblAbsent.setText("0");
    JOptionPane.showMessageDialog(null, "Attendance table cleared for the new day.");
}

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        studentTable = new javax.swing.JTable();
        generateBtn = new javax.swing.JButton();
        fmaleBtn = new javax.swing.JButton();
        maleBtn = new javax.swing.JButton();
        jPanel14 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        txtSearch = new javax.swing.JTextField();
        presentLBL = new javax.swing.JLabel();
        lblPresent = new javax.swing.JLabel();
        absentLBL = new javax.swing.JLabel();
        lblAbsent = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        exitbtn = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                formComponentShown(evt);
            }
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        studentTable.setFont(new java.awt.Font("Segoe UI", 3, 10)); // NOI18N
        studentTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(studentTable);

        generateBtn.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        generateBtn.setText("GENERATE TO EXCEL");
        generateBtn.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, java.awt.Color.darkGray, null, null));

        fmaleBtn.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        fmaleBtn.setText("FEMALE");
        fmaleBtn.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, java.awt.Color.darkGray, null, null));

        maleBtn.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        maleBtn.setText("MALE");
        maleBtn.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, java.awt.Color.darkGray, null, null));

        jPanel14 = new javax.swing.JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                // Semi-transparent background (black with alpha = 60/255)
                g2d.setColor(new Color(0, 0, 0, 60));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        jPanel14.setOpaque(false); // allow transparency
        jPanel14.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel4.setFont(new java.awt.Font("Segoe UI Black", 1, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("SEARCH: ");

        txtSearch.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtSearchKeyReleased(evt);
            }
        });

        presentLBL.setFont(new java.awt.Font("Segoe UI Black", 1, 14)); // NOI18N
        presentLBL.setForeground(new java.awt.Color(255, 255, 255));
        presentLBL.setText("PRESENT:");

        lblPresent.setFont(new java.awt.Font("Segoe UI Black", 1, 14)); // NOI18N
        lblPresent.setForeground(new java.awt.Color(51, 255, 51));
        lblPresent.setText("----------");

        absentLBL.setFont(new java.awt.Font("Segoe UI Black", 1, 14)); // NOI18N
        absentLBL.setForeground(new java.awt.Color(255, 255, 255));
        absentLBL.setText("ABSENT:");

        lblAbsent.setFont(new java.awt.Font("Segoe UI Black", 1, 14)); // NOI18N
        lblAbsent.setForeground(new java.awt.Color(255, 0, 0));
        lblAbsent.setText("----------");

        jLabel1.setFont(new java.awt.Font("Sitka Subheading", 3, 50)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText(" JAVA ATTENDANCE");

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel14Layout.createSequentialGroup()
                        .addComponent(absentLBL)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblAbsent))
                    .addGroup(jPanel14Layout.createSequentialGroup()
                        .addComponent(presentLBL)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblPresent)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 479, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel14Layout.createSequentialGroup()
                        .addGap(42, 42, 42)
                        .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 242, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel14Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel4)))
                .addContainerGap())
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addContainerGap(24, Short.MAX_VALUE)
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(presentLBL)
                        .addComponent(lblPresent))
                    .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(absentLBL)
                    .addComponent(lblAbsent))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel14Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel1))
        );

        exitbtn.setFont(new java.awt.Font("Segoe UI Black", 1, 12)); // NOI18N
        exitbtn.setText("X");
        exitbtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitbtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 970, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(generateBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(maleBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(fmaleBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(24, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(exitbtn))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(exitbtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 36, Short.MAX_VALUE)
                .addComponent(jPanel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 329, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(generateBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maleBtn)
                    .addComponent(fmaleBtn))
                .addGap(17, 17, 17))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void exitbtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitbtnActionPerformed
         this.dispose();
    }//GEN-LAST:event_exitbtnActionPerformed

    private void formComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentShown
     
    }//GEN-LAST:event_formComponentShown

      
    private void txtSearchKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtSearchKeyReleased
       loadDataInTable();
    }//GEN-LAST:event_txtSearchKeyReleased

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
  for(double i=0.0; i<=1.0;i +=0.1) {
            String s = i+"";
            float f = Float.valueOf(s);
            this.setOpacity(f);
            try {
                Thread.sleep(20);
            } catch (InterruptedException ex) {
            System.getLogger(ViewAttendance.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
        }        // TODO add your handling code here:
    }//GEN-LAST:event_formWindowOpened
 public static void setupGenerateExcel(JButton generateBtn, JTable attendanceTable) {
    generateBtn.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Attendance");

            // Header row
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < attendanceTable.getColumnCount(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(attendanceTable.getColumnName(i));
            }

            // ✅ Data rows with null-safe handling
            for (int i = 0; i < attendanceTable.getRowCount(); i++) {
                Row row = sheet.createRow(i + 1);
                for (int j = 0; j < attendanceTable.getColumnCount(); j++) {
                    Object value = attendanceTable.getValueAt(i, j);
                    String cellValue = (value != null && !value.toString().trim().isEmpty()) ? value.toString() : "N/A";
                    row.createCell(j).setCellValue(cellValue);
                }
            }

            // Step 1: Get all student names + gender
            Map<String, String> studentGenderMap = new HashMap<>();
            Set<String> allStudents = new HashSet<>();
            try (Connection con = ConnectionProvider.getCon();
                 Statement st = con.createStatement()) {

                // ✅ Default to "11-Andriod" if table is empty
                String section = attendanceTable.getRowCount() > 0
                        ? attendanceTable.getValueAt(0, 5).toString()
                        : "12-Java";

                ResultSet rs = st.executeQuery("SELECT name, gender FROM student WHERE section = '" + section + "'");
                while (rs.next()) {
                    String name = rs.getString("name");
                    String gender = rs.getString("gender");
                    allStudents.add(name);
                    studentGenderMap.put(name, gender);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Error loading student list: " + ex.getMessage());
            }

            // Step 2: Group present students by date (column index 3 = DATE)
            Map<String, Set<String>> presentByDate = new HashMap<>();
            for (int i = 0; i < attendanceTable.getRowCount(); i++) {
                String date = attendanceTable.getValueAt(i, 3) != null ? attendanceTable.getValueAt(i, 3).toString() : "";
                String name = attendanceTable.getValueAt(i, 1) != null ? attendanceTable.getValueAt(i, 1).toString() : "";
                presentByDate.computeIfAbsent(date, k -> new HashSet<>()).add(name);
            }

            // Step 3: Write summary with male/female separation
            int summaryStartRow = attendanceTable.getRowCount() + 3;
            Row header = sheet.createRow(summaryStartRow++);
            header.createCell(0).setCellValue("Date");
            header.createCell(1).setCellValue("Present Male");
            header.createCell(2).setCellValue("Present Female");
            header.createCell(3).setCellValue("Absent Male Count");
            header.createCell(4).setCellValue("Absent Female Count");
            header.createCell(5).setCellValue("Absent Male Names");
            header.createCell(6).setCellValue("Absent Female Names");

            CellStyle wrapStyle = workbook.createCellStyle();
            wrapStyle.setWrapText(true);

            for (Map.Entry<String, Set<String>> entry : presentByDate.entrySet()) {
                String date = entry.getKey();
                Set<String> presentStudents = entry.getValue();

                Set<String> absentStudents = new HashSet<>(allStudents);
                absentStudents.removeAll(presentStudents);

                int presentMale = 0, presentFemale = 0;
                int absentMale = 0, absentFemale = 0;
                List<String> absentMaleNames = new ArrayList<>();
                List<String> absentFemaleNames = new ArrayList<>();

                for (String s : presentStudents) {
                    if ("Male".equalsIgnoreCase(studentGenderMap.get(s))) presentMale++;
                    else if ("Female".equalsIgnoreCase(studentGenderMap.get(s))) presentFemale++;
                }

                for (String s : absentStudents) {
                    if ("Male".equalsIgnoreCase(studentGenderMap.get(s))) {
                        absentMale++;
                        absentMaleNames.add(s);
                    } else if ("Female".equalsIgnoreCase(studentGenderMap.get(s))) {
                        absentFemale++;
                        absentFemaleNames.add(s);
                    }
                }

                Row row = sheet.createRow(summaryStartRow++);
                row.createCell(0).setCellValue(date);
                row.createCell(1).setCellValue(presentMale);
                row.createCell(2).setCellValue(presentFemale);
                row.createCell(3).setCellValue(absentMale);
                row.createCell(4).setCellValue(absentFemale);

                Cell absentMaleCell = row.createCell(5);
                absentMaleCell.setCellValue(String.join("\n", absentMaleNames));
                absentMaleCell.setCellStyle(wrapStyle);

                Cell absentFemaleCell = row.createCell(6);
                absentFemaleCell.setCellValue(String.join("\n", absentFemaleNames));
                absentFemaleCell.setCellStyle(wrapStyle);
            }

            // Save to file
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Excel File");
            int userSelection = fileChooser.showSaveDialog(null);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                try (FileOutputStream fileOut = new FileOutputStream(fileToSave + ".xlsx")) {
                    workbook.write(fileOut);
                    workbook.close();
                    JOptionPane.showMessageDialog(null, "Excel file saved to: " + fileToSave.getAbsolutePath());
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "Error saving Excel file: " + ex.getMessage());
                }
            }
        }
    });
}


    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new ViewAttendance().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel absentLBL;
    private javax.swing.JButton exitbtn;
    private javax.swing.JButton fmaleBtn;
    private javax.swing.JButton generateBtn;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel4;
    javax.swing.JPanel jPanel14;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblAbsent;
    private javax.swing.JLabel lblPresent;
    private javax.swing.JButton maleBtn;
    private javax.swing.JLabel presentLBL;
    private javax.swing.JTable studentTable;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables

private void loadDataInTable() {
    List<String> columns = Arrays.asList("LRN", "NAME", "GENDER", "DATE", "TIME-IN", "GRADE & SEC");

    String searchText = txtSearch.getText().replaceAll("\\p{C}", "").trim().toLowerCase();
    LocalDate today = LocalDate.now();

    DefaultTableModel model = new DefaultTableModel();
    model.setColumnIdentifiers(columns.toArray());
    studentTable.setModel(model);

    // Query attendance records for "12-Java" only
    StringBuilder sql = new StringBuilder(
        "SELECT combined.studentId, combined.name, " +
        "COALESCE(s.gender, combined.gender, 'N/A') as gender, " +
        "combined.date, combined.timeIn, combined.section " +
        "FROM (" +
        "SELECT studentId, name, gender, date, timeIn, section FROM studentAttendance " +
        "UNION ALL " +
        "SELECT studentId, name, gender, date, timeIn, section FROM studentAttendanceArchive" +
        ") AS combined " +
        "LEFT JOIN student s ON combined.studentId = s.id " +
        "WHERE DATE(combined.date) = ? AND combined.section = '12-Java'"
    );

    if (!searchText.isEmpty()) {
        sql.append(" AND (LOWER(combined.name) LIKE ? OR combined.studentId LIKE ?)");
    }

    long presentCount = 0;
    Set<String> presentStudentIds = new HashSet<>(); // ✅ Track who's present

    try (Connection con = ConnectionProvider.getCon();
         PreparedStatement ps = con.prepareStatement(sql.toString())) {

        ps.setDate(1, java.sql.Date.valueOf(today));

        if (!searchText.isEmpty()) {
            ps.setString(2, "%" + searchText + "%");
            ps.setString(3, "%" + searchText + "%");
        }

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            List<Object> row = new ArrayList<>();
            String studentId = rs.getString("studentId");
            row.add(studentId);
            row.add(rs.getString("name"));
            row.add(rs.getString("gender"));

            LocalDate rawDate = LocalDate.parse(rs.getString("date"));
            LocalTime rawTime = LocalTime.parse(rs.getString("timeIn"));

            ZoneId dbZone = ZoneId.of("UTC");
            ZoneId localZone = ZoneId.of("Asia/Manila");

            LocalDateTime utcDateTime = LocalDateTime.of(rawDate, rawTime);
            ZonedDateTime localDateTime = utcDateTime.atZone(dbZone).withZoneSameInstant(localZone);

            String formattedDate = localDateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
            String formattedTime = localDateTime.format(DateTimeFormatter.ofPattern("hh:mma")).toLowerCase();

            row.add(formattedDate);
            row.add(formattedTime);
            row.add(rs.getString("section"));

            model.addRow(row.toArray());

            presentCount++;
            presentStudentIds.add(studentId); // ✅ Track this student as present
        }

        // ✅ Calculate absent count dynamically: Total students - Present students
        long totalStudents = 0;
        String countSql = "SELECT COUNT(*) AS total FROM student WHERE section = '12-Java'";
        try (PreparedStatement psCount = con.prepareStatement(countSql)) {
            ResultSet rsCount = psCount.executeQuery();
            if (rsCount.next()) {
                totalStudents = rsCount.getLong("total");
            }
        }

        long absentCount = totalStudents - presentCount; // ✅ Real-time calculation

        lblPresent.setVisible(true);
        lblAbsent.setVisible(true);
        presentLBL.setVisible(true);
        absentLBL.setVisible(true);

        lblPresent.setText(String.valueOf(presentCount));
        lblAbsent.setText(String.valueOf(absentCount));

    } catch (Exception ex) {
        JOptionPane.showMessageDialog(null, "Something went wrong: " + ex.getMessage());
        ex.printStackTrace();
    }
}

 private Long countWeekdays(LocalDate fromDate, LocalDate toDate) {
    long count = 0;
    LocalDate date = fromDate;
    while (date.isBefore(toDate) || date.equals(toDate)) {
        if (!(date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY)) {
            count++;
        }
        date = date.plusDays(1);
    }
    return count;
  }
}
  

    
    



