import tool.L;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

public class Role_account_panel extends JPanel {

    private static final long serialVersionUID = 1L;
    private Database db;
    private JComboBox<String> roleCB;
    private JPanel content;
    private JButton confirm;
    private int role_id = 0;
    private boolean isSelect;

    public Role_account_panel(Database db) {
        this.db = db;
        initComponent();
        addAcctionListener();
    }

    private void initComponent() {
        setLayout(new BorderLayout());
        JPanel head = new JPanel();
        JPanel panel = new JPanel();
        roleCB = new JComboBox<String>();
        roleCB.setPreferredSize(new Dimension(120, 20));
        confirm = new JButton("确定");
        panel.add(new JLabel("选择角色"));
        panel.add(roleCB);
        head.add(panel);
        add(head, BorderLayout.NORTH);
        content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        JScrollPane jsc = new JScrollPane(content);
        add(jsc, BorderLayout.CENTER);
        panel = new JPanel();
        panel.add(confirm);
        add(panel, BorderLayout.SOUTH);
    }

    //壮哉我大lisp 这里应该传控件名和方法进去的
    private void loadRolename() {
        roleCB.removeAllItems();
        ArrayList<String[]> list;
        list = db.getRoleList(1);
        for (int i = 0; i < list.size(); i++) {
            roleCB.addItem(list.get(i)[1]);
        }
    }

    private void loadAllAccount() {
        content.removeAll();
        ArrayList<String[]> list = db.getAllAccount();
        for (int i = 0; i < list.size(); i++) {
            String[] s = list.get(i);
            content.add(new record_role_account(Integer.parseInt(s[0]), s[1], false));
        }
        content.updateUI();
    }

    private void addAcctionListener() {
        loadRolename();
        loadAllAccount();
        roleCB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                if (roleCB == null || roleCB.getSelectedItem() == null) return;
                role_id = db.getRoleid(roleCB.getSelectedItem().toString());
                Component[] com = content.getComponents();
                HashMap<String, String> selectAccount = db.getAccountByRole(role_id);
                for (Component c : com) {
                    record_role_account item = (record_role_account) c;
                    if (selectAccount.containsKey(item.getName())) {
                        item.setSelect(true);
                    } else {
                        item.setSelect(false);
                    }
                }
                content.updateUI();
            }
        });

        confirm.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                db.deleteRoleAccount(role_id);
                Component[] com = content.getComponents();
                for (Component c : com) {
                    record_role_account item = (record_role_account) c;
                    if (item.isSelect()) {
                        L.d(item.getName());
                        db.addRoleAccount(role_id, item.getAccountId());
                    }
                }
                JOptionPane.showMessageDialog(Role_account_panel.this, "修改成功");
            }
        });
    }

    public void refresh() {
        content.removeAll();
        loadRolename();
        loadAllAccount();
        if (roleCB.getSelectedItem() != null) {
            String onselect = roleCB.getSelectedItem().toString();
            roleCB.setSelectedItem(onselect);
        }
    }

    class record_role_account extends JPanel {
        private static final long serialVersionUID = 1L;
        private JCheckBox select;
        private JTextField account;
        private boolean hasSelect;
        private int account_id;
        private String account_name;

        record_role_account(int account_id, String account_name, boolean hasSelect) {
            this.account_id = account_id;
            this.hasSelect = hasSelect;
            this.account_name = account_name;
            init_r();
        }

        private void init_r() {
            setMaximumSize(new Dimension(600, 35));
            select = new JCheckBox();
            select.setSelected(hasSelect);
            add(select);
            add(new JLabel("账号名"));
            account = new JTextField();
            account.setPreferredSize(new Dimension(120, 20));
            account.setText(account_name);
            account.setEditable(false);
            add(account);
            select.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent arg0) {
                    hasSelect = !hasSelect;
                }
            });
        }

        public void setSelect(boolean s) {
            hasSelect = s;
            select.setSelected(hasSelect);

        }

        public boolean isSelect() {
            return hasSelect;
        }

        public String getName() {
            return account_name;
        }

        public int getAccountId() {
            return account_id;
        }


    }

}

