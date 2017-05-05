import tool.L;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.*;

public class Account_panel extends JPanel {
    private static final long serialVersionUID = 1L;
    private Database db;
    private JButton edit;
    private JButton add;
    private JPanel content;
    private boolean isEdit = false;

    public Account_panel(Database db) {
        this.db = db;
        init();
        addlistener();
    }

    private void init() {
        setLayout(new BorderLayout());
        edit = new JButton("编辑");
        JPanel h = new JPanel();
        h.add(edit);
        add(h, BorderLayout.NORTH);

        content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        JScrollPane jsc = new JScrollPane(content);
        add(jsc, BorderLayout.CENTER);

        add = new JButton("添加账号");
        JPanel f = new JPanel();
        f.add(add);
        add(f, BorderLayout.SOUTH);

        loadAccount();
    }

    private void loadAccount() {
        content.removeAll();
        ArrayList<String[]> list = db.getAllAccount();
        for (String[] s : list) {
            content.add(new record_account(Integer.parseInt(s[0]), s[1], s[2], false));
            L.d(Integer.parseInt(s[0]) + s[1] + s[2]);
        }
        content.updateUI();
    }

    private void setState(boolean isEdit) {
        Component[] com = content.getComponents();
        for (Component c : com) {
            record_account rr = (record_account) c;
            rr.setEdit(isEdit);
        }
    }

    private void addlistener() {
        edit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                edit.setText(isEdit ? "编辑" : "完成");
                isEdit = !isEdit;
                setState(isEdit);
                content.updateUI();
            }
        });
        add.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                if (isEdit) {
                    edit.setText(isEdit ? "编辑" : "完成");
                    isEdit = !isEdit;
                    setState(isEdit);
                    content.updateUI();
                }
                String name = null;
                String pass = null;
                dialog d = new dialog();
                int result = JOptionPane.showConfirmDialog(null, d, "输入账号名和密码", JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    name = d.getName();
                    pass = d.getPass();
                } else return;
                if (name == null || name.equals("")) {
                    JOptionPane.showMessageDialog(Account_panel.this, "账号名不能为空");
                    return;
                }
                if (pass == null || pass.equals("")) {
                    JOptionPane.showMessageDialog(Account_panel.this, "密码不能为空");
                    return;
                }
                int account_id = db.addAccount(name, pass);
                if (account_id < 0) {
                    if (account_id == -1) JOptionPane.showMessageDialog(Account_panel.this, "账号名重复");
                    return;
                }
                content.add(new record_account(account_id, name, pass, false));
                content.updateUI();
            }
        });
    }

    public void refresh() {
        loadAccount();
    }

    class dialog extends JPanel {
        private static final long serialVersionUID = 1L;
        private JTextField name;
        private JTextField pass;

        public dialog() {
            name = new JTextField();
            name.setPreferredSize(new Dimension(120, 20));
            pass = new JPasswordField();
            pass.setPreferredSize(new Dimension(120, 20));
            add(new JLabel("账号:"));
            add(name);
            add(Box.createHorizontalStrut(15));
            add(new JLabel("密码:"));
            add(pass);
        }

        public String getName() {
            return name.getText();
        }

        public String getPass() {
            return pass.getText();
        }

    }

    class record_account extends JPanel {
        private static final long serialVersionUID = 1L;
        JTextField account;
        JTextField password;
        JButton delete;
        JButton confirm;
        boolean isEdit;
        int account_id;
        String account_name;
        String account_pass;

        record_account(int account_id, String account_name, String account_pass, boolean isEdit) {
            this.account_id = account_id;
            this.isEdit = isEdit;
            this.account_name = account_name;
            this.account_pass = account_pass;
            init_r();
            addlistener_r();
        }

        private void addlistener_r() {
            confirm.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent arg0) {
                    String name = account.getText();
                    String pass = password.getText();
                    if (!account_name.equals(name) && db.hasAccount(name)) {
                        JOptionPane.showMessageDialog(null, "账号名重复");
                        account.setText(account_name);
                        password.setText(account_pass);
                        return;
                    }
                    db.updateAccount(account_id, name, pass);
                    JOptionPane.showMessageDialog(record_account.this, "修改成功");
                }
            });

            delete.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    record_account.this.setVisible(false);
                    db.deleteAccount(account_id);
                }
            });
        }

        public void setEdit(boolean isedit) {
            this.isEdit = isedit;
            account.setEditable(isEdit);
            password.setEditable(isEdit);
            confirm.setVisible(isEdit);
            delete.setVisible(isEdit);
        }

        private void init_r() {
            setMaximumSize(new Dimension(600, 35));
            add(new JLabel("账号名"));
            account = new JTextField();
            account.setPreferredSize(new Dimension(120, 20));
            account.setText(account_name);
            add(account);
            add(new JLabel("密码"));
            password = new JTextField();
            password.setPreferredSize(new Dimension(120, 20));
            password.setText(account_pass);
            add(password);
            confirm = new JButton("确定");
            delete = new JButton("删除");
            add(confirm);
            add(delete);
            setEdit(false);
        }
    }
}
