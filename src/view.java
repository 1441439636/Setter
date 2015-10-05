import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicTabbedPaneUI.TabbedPaneLayout;


public class view extends JFrame {

	DatabaseConnect db = null;	
	private static final long serialVersionUID = 1L;

	public view(){
		if (loginDatabase()) {
			initFrame();
			setVisible(true);
		} else {
			JOptionPane.showMessageDialog(null, "账号/密码错误",
					"T_T", JOptionPane.ERROR_MESSAGE);
			setVisible(false);
		}
	}
	
	public boolean loginDatabase() {		 
		try
		{
			logDialog log = new logDialog();
			if (log.showdia(this, "登录"))
			{
				db = new DatabaseConnect();
				return db.connect(log.getUserName(), log.getPassword(),
					log.getAddress(), log.getDatabaseName());
			}
			return false;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
	}
	private void initFrame() {
		setFeel();
		setSize(500, 600);
		Toolkit kit=Toolkit.getDefaultToolkit();
		Dimension screenSize=kit.getScreenSize();
		setSize(screenSize.width/2,screenSize.height/4*3);
		setLocationByPlatform(true);
		setTitle("自定义数据库查询");
		
		JTabbedPane rootPanel = new JTabbedPane();
		rootPanel.addTab("翻译", null,new tranlatePanel(db));
		rootPanel.addTab("权限设置", null,new permissionPanel(db));
		rootPanel.addTab("账号设置", null,new accountPanel(db));
		add(rootPanel);
		
		
		
		
		
		rootPanel.addChangeListener(new ChangeListener() {
			
			public void stateChanged(ChangeEvent e) {
				JTabbedPane root=(JTabbedPane)e.getSource();
				Component com=root.getSelectedComponent();
				int i=root.getSelectedIndex();
				switch(i)
				{
					case 0:
						tranlatePanel tn=(tranlatePanel)com;				
						break;
					case 1:
						permissionPanel pe=(permissionPanel)com;
						pe.refresh();
						break;		
					case 2:
						accountPanel ac =(accountPanel)com;			
						break;
				}
			}
		});
	}
	private final void setFeel() {
		try {
			UIManager
					.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
			SwingUtilities.updateComponentTreeUI(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void main(String[] args) throws Exception {
		EventQueue.invokeLater(new Runnable() {		
			public void run() {
				view frame = new view();
				frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
			}
		});
	}
}