import tool.L;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.xml.bind.SchemaOutputResolver;


public class Role_permission_panel extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private Database db;
    private JComboBox<String> roleCB;
    private JComboBox<String> tableCB;
    private JScrollPane jsc;
    private JPanel content;
    private JButton confirm;

    private JButton selectall;
    private JButton selectnone;
    private final ComboBoxListener com = new ComboBoxListener();

    Role_permission_panel(Database db) {
        this.db = db;
        initComponent();
        addAcctionListener();
    }

    private void initComponent() {
        setLayout(new BorderLayout());
        JPanel head = new JPanel();
        head.setLayout(new GridLayout(2, 2));
        JPanel panel = new JPanel();
        roleCB = new JComboBox<>();
        roleCB.setPreferredSize(new Dimension(120, 20));
        tableCB = new JComboBox<>();
        tableCB.setPreferredSize(new Dimension(120, 20));


        confirm = new JButton("确定");

        panel.add(new JLabel("选择角色"));
        panel.add(roleCB);

        head.add(panel);

        panel = new JPanel();
        panel.add(new JLabel("选择表 "));
        panel.add(tableCB);
        head.add(panel);
        panel = new JPanel();
        selectall = new JButton("全选");
        panel.add(selectall);
        head.add(panel);
        panel = new JPanel();
        selectnone = new JButton("反选");
        panel.add(selectnone);
        head.add(panel);
        add(head, BorderLayout.NORTH);
        content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        jsc = new JScrollPane(content);
        add(jsc, BorderLayout.CENTER);
        panel = new JPanel();
        panel.add(confirm);
        add(panel, BorderLayout.SOUTH);
    }

    private void loadRolename() {
        roleCB.removeAllItems();
        ArrayList<String[]> list;
        list = db.getRoleList(0);
        for (int i = 0; i < list.size(); i++) {
            roleCB.addItem(list.get(i)[1]);
        }
    }

    private void loadTablename() {
        tableCB.removeAllItems();
        ArrayList<String> list;
        list = db.getAdornTablenameList();
        for (int i = 0; i < list.size(); i++) {
            tableCB.addItem(list.get(i));
        }
    }

    private void addAcctionListener() {
        loadRolename();
        loadTablename();

        //切换角色时   清空之前显示的列
        roleCB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                content.removeAll();
                content.updateUI();
            }
        });
        tableCB.addActionListener(com);
        confirm.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                Component[] com = content.getComponents();
                try {
                    int role_id = 0;
                    int table_id = 0;
                    if (roleCB.getSelectedItem().toString() != null && tableCB.getSelectedItem().toString() != null) {
                        role_id = db.getRoleid(roleCB.getSelectedItem().toString());
                        table_id = db.getTableid(tableCB.getSelectedItem().toString());
                        db.deleteRolePermission(role_id, table_id);
                        ArrayList<String> list = new ArrayList<String>();
                        for (int i = 0; i < com.length; i++) {
                            permissionRecord record = (permissionRecord) com[i];
                            if (record.isSelectd()) {
                                list.add(record.getName());
                            }
                        }
                        db.addRolePermission(role_id, table_id, list);
                        JOptionPane.showMessageDialog(Role_permission_panel.this, "修改成功");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        //全选就是把全部列选中
        selectall.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Component[] comp = content.getComponents();
                if (comp.length > 0) {
                    System.out.println("comp.length > 0   " + (comp.length > 0));
                    permissionRecord p;
                    for (Component c : comp) {
                        p = (permissionRecord) c;
                        p.setSelect(true);
                    }
                    content.updateUI();
                }
            }
        });

        selectnone.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                Component[] comp = content.getComponents();
                if (comp.length > 0) {
                    permissionRecord p;
                    for (Component c : comp) {
                        p = (permissionRecord) c;
                        p.setSelect(false);
                    }
                    content.updateUI();
                }
            }
        });


    }

    public void refresh() {
        if (tableCB != null && content != null && roleCB != null) {
            L.d("refersn");
            content.removeAll();
            tableCB.removeActionListener(com);
            String onselect = (tableCB.getSelectedItem() == null) ? "" : tableCB.getSelectedItem().toString();

            loadRolename();
            loadTablename();
            tableCB.addActionListener(com);
            tableCB.setSelectedItem(onselect);
        }
    }

    class ComboBoxListener implements ActionListener {
        public void actionPerformed(ActionEvent arg0) {
            L.d("on lis");
            try {
                content.removeAll();
                String tablename = tableCB.getSelectedItem().toString();
                ArrayList<String> list = db.getAdornColumnList(tablename);

                if (roleCB.getSelectedItem() != null) {
                    int role_id = db.getRoleid(roleCB.getSelectedItem().toString());
                    int table_id = db.getTableid(tableCB.getSelectedItem().toString());
                    for (int i = 0; i < list.size(); i++) {
                        permissionRecord record = new permissionRecord("N", list.get(i));

                        if (db.hasRolePermission(role_id, table_id, list.get(i)))
                            record.setSelect(true);
                        content.add(record);
                    }
                    content.updateUI();
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    class permissionRecord extends JPanel {
        /**
         *
         */
        private static final long serialVersionUID = 1L;
        JCheckBox jck;
        JLabel bn;
        boolean flag;

        permissionRecord(String Flag, String colname) {
            flag = Flag.equals("Y");
            setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
            this.setMaximumSize(new Dimension(450, 35));

            jck = new JCheckBox();
            jck.setSelected(flag);
            bn = new JLabel(colname);
            add(jck);
            add(bn);
            this.setMaximumSize(new Dimension(450, 35));
        }

        public boolean isSelectd() {
            return jck.isSelected();
        }

        public String getName() {
            return bn.getText();
        }

        public void setSelect(boolean b) {
            this.jck.setSelected(b);
        }

    }


}

