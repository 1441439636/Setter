import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class logDialog extends JPanel {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private JTextField username;
    private JPasswordField password;
    private JButton confirm;
    private JButton cancle;
    private int ok;
    private JDialog dialog;
    private int left;
    private int top;
    private int width;
    private int height;


    public logDialog(int left, int top, int width, int height) {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.updateComponentTreeUI(this);

        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(width, height));
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        panel.add(new JLabel("账号:    "));
        username = new JTextField("");
        username.setPreferredSize(new Dimension(120, 20));
        panel.add(username);
        panel.add(new JLabel("密码:    "));
        password = new JPasswordField("");
        password.setPreferredSize(new Dimension(120, 20));
        panel.add(password);
        add(panel, BorderLayout.CENTER);

        confirm = new JButton("确定");
        confirm.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                ok = 1;
                dialog.setVisible(false);
            }
        });

        cancle = new JButton("取消");
        cancle.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                ok = -1;
                dialog.setVisible(false);
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(confirm);
        buttonPanel.add(cancle);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public String getUserName() {
        return username.getText();
    }

    public String getPassword() {
        return new String(password.getPassword());
    }

    public int showdia(Component parent, String title) {
        ok = 0;
        Frame ower = null;
        if (parent instanceof Frame) ower = (Frame) parent;
        else ower = (Frame) SwingUtilities.getAncestorOfClass(Frame.class, parent);
        if (dialog == null || dialog.getOwner() != ower) {
            dialog = new JDialog(ower, true) {
                private static final long serialVersionUID = 1L;
                protected void processWindowEvent(WindowEvent e) {
                    super.processWindowEvent(e);
                    if (e.getID() == WindowEvent.WINDOW_CLOSING) {
                        ok = -2;
                    }
                }
            };
            dialog.add(this);
            dialog.setBounds(left, top, width, height);
            dialog.getRootPane().setDefaultButton(confirm);
            dialog.pack();
        }
        dialog.setTitle(title);
        dialog.setVisible(true);
        return ok;
    }

}