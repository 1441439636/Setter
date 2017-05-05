import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;


//用于增加删除角色
//不会出现root
public class Role_panel extends JPanel {


    private static final long serialVersionUID = 1L;
    private Database db;
    private JButton edit;
    private JButton add;
    private JPanel content;
    private boolean isEdit = false;


    public Role_panel(Database db) {
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

        add = new JButton("添加角色");
        JPanel f = new JPanel();
        f.add(add);
        add(f, BorderLayout.SOUTH);
        loadRole();
    }

    //加载角色 不会加载root角色 (不允许 删除 更改root角色)
    private void loadRole() {
        content.removeAll();
        ArrayList<String[]> list = db.getRoleList(0);
        for (String[] s : list) {
            content.add(new record_role(Integer.parseInt(s[0]), s[1], false));
        }
        content.updateUI();
    }

    private void setState(boolean isEdit) {
        Component[] com = content.getComponents();
        for (Component c : com) {
            record_role rr = (record_role) c;
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
                String name = JOptionPane.showInputDialog("输入新增角色名");
                if (name == null || name.equals("")) return;
                int role_id = db.addRole(name);
                if (role_id < 0) {
                    if (role_id == -1) JOptionPane.showMessageDialog(null, "角色名重复");
                    return;
                }
                content.add(new record_role(role_id, name, false));
                content.updateUI();
            }
        });
    }


    public void refresh() {
        loadRole();
    }

    class record_role extends JPanel {
        private static final long serialVersionUID = 1L;
        private JTextField role;
        private JButton delete;
        private JButton confirm;
        private boolean isEdit;
        private int role_id;
        private String role_name;

        record_role(int role_id, String role_name, boolean isEdit) {
            this.role_id = role_id;
            this.isEdit = isEdit;
            this.role_name = role_name;
            init_r();
            addlistener_r();
        }

        private void addlistener_r() {
            confirm.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    String rolename = role.getText();
                    if (db.hasRole(rolename)) {
                        JOptionPane.showMessageDialog(null, "角色名重复");
                        return;
                    }
                    db.updateRole(role_id, rolename);
                    JOptionPane.showMessageDialog(record_role.this, "修改成功");
                }
            });
            delete.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    record_role.this.setVisible(false);
                    db.deleteRole(role_id);
                }
            });
        }

        public void setEdit(boolean isedit) {
            this.isEdit = isedit;
            role.setEditable(isEdit);
            confirm.setVisible(isEdit);
            delete.setVisible(isEdit);
        }

        private void init_r() {
            setMaximumSize(new Dimension(600, 35));
            add(new JLabel("角色名"));
            role = new JTextField();
            role.setPreferredSize(new Dimension(120, 20));
            role.setEditable(isEdit);
            role.setText(role_name);
            add(role);
            confirm = new JButton("确定");
            delete = new JButton("删除");
            confirm.setVisible(isEdit);
            delete.setVisible(isEdit);
            add(confirm);
            add(delete);
        }
    }

}
