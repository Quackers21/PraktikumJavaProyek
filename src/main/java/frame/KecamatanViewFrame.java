package frame;

import helpers.Koneksi;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;

public class KecamatanViewFrame extends JFrame {
    private JPanel mainPanel;
    private JTextField cariTextField;
    private JButton cariButton;
    private JPanel cariPanel;
    private JScrollPane viewScrollPanel;
    private JTable viewTable;
    private JPanel buttonPanel;
    private JButton tambahButton;
    private JButton ubahButton;
    private JButton hapusButton;
    private JButton batalButton;
    private JButton cetakButton;
    private JButton tutupButton;

    public KecamatanViewFrame() {
        tutupButton.addActionListener(e -> {dispose();});
        batalButton.addActionListener(e -> {isiTable();});
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e){
                isiTable();
            }
        });

        //cari button
        cariButton.addActionListener(e -> {

            if (cariTextField.getText().equals("")) {
                JOptionPane.showMessageDialog(
                        null,
                        "Isi Kata Kunci Pencarian",
                        "Validasi Kata Kunci kosong",
                        JOptionPane.WARNING_MESSAGE
                );
                cariTextField.requestFocus();
                return;
            }

            Connection c = Koneksi.getConnection();
            String keyword = "%" + cariTextField.getText() + "%";
            String searchSQL = "SELECT K.*, B.nama AS nama_kabupaten FROM kecamatan K LEFT JOIN kabupaten B ON K.kabupaten_id = B.id WHERE K.nama like ? OR B.nama like ?";
            try {
                PreparedStatement ps = c.prepareStatement(searchSQL);
                ps.setString(1, keyword);
                ps.setString(2, keyword);
                ResultSet rs = ps.executeQuery();
                DefaultTableModel dtm = (DefaultTableModel) viewTable.getModel();
                dtm.setRowCount(0);
                Object[] row = new Object[6];
                while (rs.next()) {
                    row[0] = rs.getInt("id");
                    row[1] = rs.getString("nama");
                    row[2] = rs.getString("nama_kabupaten");
                    row[3] = rs.getString("klasifikasi");
                    dtm.addRow(row);
                    row[4] = rs.getInt("populasi");
                    row[5] = rs.getDouble("luas");
                }
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
        //end cari button

        //perintah hapus button
        hapusButton.addActionListener(e -> {
            int barisTerpilih = viewTable.getSelectedRow();
            if (barisTerpilih < 0) {
                JOptionPane.showMessageDialog(null, "Pilih data dulu");
                return;
            }
            int pilihan = JOptionPane.showConfirmDialog(
                    null,
                    "Yakin mau hapus?",
                    "Konfirmasi Hapus",
                    JOptionPane.YES_NO_OPTION
            );

            if (pilihan == 0 ) {
                TableModel tm = viewTable.getModel();
                int id = Integer.parseInt(tm.getValueAt(barisTerpilih,0).toString());
                Connection c = Koneksi.getConnection();
                String deleteSQL = "DELETE FROM kecamatan WHERE id = ?";
                try {
                    PreparedStatement ps = c.prepareStatement(deleteSQL);
                    ps.setInt(1, id);
                    ps.executeUpdate();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        //end perintah hapus button

        //tambah button
        tambahButton.addActionListener(e -> {
            KecamatanInputFrame inputFrame = new KecamatanInputFrame();
            inputFrame.setVisible(true);
        });

        ubahButton.addActionListener(e -> {
            int barisTerpilih = viewTable.getSelectedRow();
            if (barisTerpilih < 0) {
                JOptionPane.showMessageDialog(
                        null,
                        "Pilih data dulu"
                );
                return;
            }
            TableModel tm = viewTable.getModel();
            int id = Integer.parseInt(tm.getValueAt(barisTerpilih, 0).toString());
            KecamatanInputFrame inputFrame = new KecamatanInputFrame();
            inputFrame.setId(id);
            inputFrame.isiKomponen();
            inputFrame.setVisible(true);
        });

        isiTable();
        init();
    }

    public void init() {
        setContentPane(mainPanel);
        setTitle("Data Kecamatan");
        pack();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    public void isiTable() {
        Connection c = Koneksi.getConnection();
        String selectSQL = "SELECT K.*, B.nama AS nama_kabupaten FROM kecamatan K LEFT JOIN kabupaten B ON K.kabupaten_id=B.id";
        try {
            Statement s = c.createStatement();
            ResultSet rs = s.executeQuery(selectSQL);
            String header[] = {"Id", "Nama Kecamatan", "Nama Kabupaten", "Klasifikasi", "populasi", "luas", "email", "tanggal Mulai"};
            DefaultTableModel dtm = new DefaultTableModel(header, 0);
            viewTable.setModel(dtm);

            viewTable.getColumnModel().getColumn(0).setMaxWidth(32);
            viewTable.getColumnModel().getColumn(1).setMaxWidth(150);
            viewTable.getColumnModel().getColumn(2).setMaxWidth(150);
            viewTable.getColumnModel().getColumn(3).setMaxWidth(150);

            DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
            rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
            viewTable.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
            viewTable.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);

            viewTable.getColumnModel().getColumn(6).setMaxWidth(150);

//            //hidden kolom (tugas 1)
//            viewTable.removeColumn(viewTable.getColumnModel().getColumn(0));
//
//            //atur lebar kolom (tugas 1)
//            viewTable.getColumnModel().getColumn(0).setWidth(100);
//            viewTable.getColumnModel().getColumn(0).setMaxWidth(300);
//            viewTable.getColumnModel().getColumn(0).setMinWidth(32);
//            viewTable.getColumnModel().getColumn(0).setPreferredWidth(100);

            Object[] row = new Object[8];
            while (rs.next()) {

                NumberFormat nf = NumberFormat.getInstance(Locale.US);
                String rowPopulasi = nf.format(rs.getInt("populasi"));
                String rowLuas = String.format("%,.2f", rs.getDouble("luas"));

                row[0] = rs.getInt("id");
                row[1] = rs.getString("nama");
                row[2] = rs.getString("nama_kabupaten");
                row[3] = rs.getString("klasifikasi");
                row[4] = rowPopulasi;
                row[5] = rowLuas;
                row[6] = rs.getString("email");
                row[7] = rs.getString("tanggalmulai");
                dtm.addRow(row);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
