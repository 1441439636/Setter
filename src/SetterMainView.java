import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

//服务
public class SetterMainView extends JFrame {
    private static String DNAME = null;
    private Database db = null;
    private static final long serialVersionUID = 1L;
    private int screen_height;
    private int screen_width;
    private static final int log_height = 300;
    private static final int log_width = 200;

    private static final int height = 600;
    private static final int width = 500;

    public SetterMainView() {
        DNAME = RegistUtil.read()[0];
        //初始化时首先调用登录界面
        //登录成功初始化界面
        //失败退出程序
        if (loginDatabase()) {
            initFrame();
            setVisible(true);
        } else {
            JOptionPane.showMessageDialog(null, "账号/密码错误", "T_T", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }

    public boolean loginDatabase() {
        try {
            Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
            screen_width = (int) screensize.getWidth();
            screen_height = (int) screensize.getHeight();
            logDialog log = new logDialog(screen_width / 2 - (log_width / 2), screen_height / 2 - (log_height / 2), log_width, log_height);
            int state = log.showdia(this, "登录");
            System.out.println("state=" + state);
            if (state == 1) {
                if (DNAME.equals("Oracle")) {
                    System.out.println("Oracle=");
                    db = new DBOracle();
                } else {
                    System.out.println("SqlServer=");
                    db = new DBSqlServer();
                }
                //用登录界面的用户名密码尝试登录数据库
                return db.connect(log.getUserName(), log.getPassword());
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void initFrame() {
        setFeel();
        setBounds((screen_width - width) / 2, (screen_height - height) / 2, width, height);
        setTitle("自定义数据库查询");
        JTabbedPane rootPanel = new JTabbedPane();
        rootPanel.addTab("翻译", null, new Tranlate_panel(db));
        rootPanel.addTab("角色", null, new Role_panel(db));
        rootPanel.addTab("角色权限", null, new Role_permission_panel(db));
        rootPanel.addTab("账号", null, new Account_panel(db));
        rootPanel.addTab("角色账号", null, new Role_account_panel(db));
        add(rootPanel);

        rootPanel.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JTabbedPane root = (JTabbedPane) e.getSource();
                Component com = root.getSelectedComponent();
                int i = root.getSelectedIndex();
                switch (i) {
                    case 0:
                        Tranlate_panel tn = (Tranlate_panel) com;
                        tn.refersh();
                        break;
                    case 1:
                        Role_panel rp = (Role_panel) com;
                        rp.refresh();
                        break;
                    case 2:
                        Role_permission_panel rpp = (Role_permission_panel) com;
                        rpp.refresh();
                        break;
                    case 3:
                        Account_panel ap = (Account_panel) com;
                        ap.refresh();
                        break;
                    case 4:
                        Role_account_panel rap = (Role_account_panel) com;
                        rap.refresh();
                        break;
                }
            }
        });
    }

    private final void setFeel() {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws Exception {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                SetterMainView frame = new SetterMainView();
                frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
            }
        });
    }
}