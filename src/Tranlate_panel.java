import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class Tranlate_panel extends JPanel {

    private static final long serialVersionUID = 1L;
    private Database db;
    private JComboBox<String> tableCB;
    private JTextField adorntableNameTF;
    private JPanel content;
    private JButton confirm;
    private int view_id;
    private final ComboBoxListener comboBoxListener = new ComboBoxListener();

    public Tranlate_panel(Database db) {
        this.db = db;
        initComponent();
        addAcctionListener();
    }

    private void initComponent() {
        tableCB = new JComboBox<>();
        tableCB.setPreferredSize(new Dimension(120, 20));
        adorntableNameTF = new JTextField();
        adorntableNameTF.setPreferredSize(new Dimension(120, 20));
        content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        JScrollPane contentJS = new JScrollPane(content);
        confirm = new JButton("确定");

        setLayout(new BorderLayout());
        JPanel head = new JPanel();
        head.setLayout(new GridLayout(2, 1));
        JPanel temppanel = new JPanel();
        temppanel.add(new JLabel("选择表"));
        temppanel.add(tableCB);
        head.add(temppanel);
        temppanel = new JPanel();
        temppanel.add(new JLabel("翻译  "));
        temppanel.add(adorntableNameTF);
        head.add(temppanel);
        add(head, BorderLayout.NORTH);
        add(contentJS, BorderLayout.CENTER);
        temppanel = new JPanel();
        temppanel.add(confirm);

        add(temppanel, BorderLayout.SOUTH);
    }


    public void refersh() {
        String tablename = "";
        if (tableCB.getSelectedItem() != null) {
            tablename = tableCB.getSelectedItem().toString();
        }
        tableCB.removeActionListener(comboBoxListener);
        tableCB.removeAllItems();
        ArrayList<String> unadornViewList = db.getUnadornViewList();
        for (int i = 0; i < unadornViewList.size(); i++) {
            tableCB.addItem(unadornViewList.get(i));
        }
        tableCB.addActionListener(comboBoxListener);
        if (!tablename.equals("")) {
            tableCB.setSelectedItem(tablename);
        }
    }

    private void addAcctionListener() {
        // 开始时加载未翻译的表
        ArrayList<String> list;
        list = db.getUnadornViewList();
        for (int i = 0; i < list.size(); i++) {
            tableCB.addItem(list.get(i));
        }
        // 当表中选项被选中时 加载列名 查看是否有翻译的表名
        tableCB.addActionListener(comboBoxListener);
        if (tableCB.getSelectedItem() != null) {
            tableCB.setSelectedItem(tableCB.getSelectedItem().toString());
        }
        // 点击确定 更新表名 列名
        confirm.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                try {
                    String tablename = tableCB.getSelectedItem().toString();
                    String adorntableName = adorntableNameTF.getText();
                    db.setTableName(tablename, adorntableName);
                    Component[] com = content.getComponents();
                    for (int i = 0; i < com.length; i++) {
                        record record = (record) com[i];
                        db.setColname(tablename, record.getColName(), record.getFlag(), record.getChinese(), record.getxh());
                    }
                    JOptionPane.showMessageDialog(Tranlate_panel.this, "修改成功");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    class ComboBoxListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String onselecttable = tableCB.getSelectedItem().toString();
            view_id = db.getidByunadornname(onselecttable);
            adorntableNameTF.setText(db.getAdornTableName(onselecttable));
            content.removeAll();
            ArrayList<String> list = db.getColumnList(onselecttable);
            System.out.println("------------ getColumnList -- -    "+onselecttable);
            for (String val : db.getColumnList(onselecttable)) {
                System.out.println(val);
            }

            for (int i = 0; i < list.size(); i++) {
                String colname = list.get(i);
                record record;
                ArrayList<String> set = db.getRecord(view_id, colname);
                if (set.size() == 3) {
                    record = new record(set.get(0), colname, set.get(1),
                            Integer.parseInt(set.get(2)));
                } else {
                    record = new record(colname);
                }
                content.add(record);
            }
            content.updateUI();
        }
    }
    class record extends JPanel {
        private static final long serialVersionUID = 1L;
        public record(String Flag, String colName, String chinese, int no) {
            setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
            this.setMaximumSize(new Dimension(450, 35));
            jck = new JCheckBox();
            bn = new JLabel();
            zwm = new JTextField(25);
            xh = new JTextField(3);
            if (Flag.equals("Y")) {
                jck.setSelected(true);
            } else jck.setSelected(false);
            bn.setPreferredSize(new Dimension(100, 35));
            bn.setText(colName);
            zwm.setText(chinese);
            xh.setText(no + "");
            add(jck);
            add(bn);
            add(new JLabel("翻译"));
            add(zwm);
            add(xh);
        }
        public record(String colName) {
            this("N", colName, "", 1);
        }
        public String getChinese() {
            return zwm.getText();
        }
        public String getFlag() {
            return jck.isSelected() ? "Y" : "N";
        }
        public int getxh() {
            return Integer.parseInt(xh.getText());
        }
        public String getColName() {
            return bn.getText();
        }
        public void reset(String flag, String chinese, String xh) {
            boolean f = flag == "Y" ? true : false;
            jck.setSelected(f);
            zwm.setText(chinese);
            this.xh.setText(xh);
        }
        public void clear() {
            jck.setSelected(false);
            zwm.setText("");
            xh.setText("0");
        }

        JTextField zwm;
        JTextField xh;
        JCheckBox jck;
        JLabel bn;
    }


}
