import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import javax.swing.BoxLayout;
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
	private static final int WIDTH = 500;
	private static final int HEIGHT = 650;

	private static final int SELECTPANEL_X = 100;
	private static final int SELECTPANEL_Y = 30;

	private static final int CONTENTPANEL_X = 25;
	private static final int CONTENTPANEL_Y = 150;
	private static final int CONTENT_WIDTH = 450;
	private static final int CONTENT_HEIGHT = 370;

	private static final int CHECKPANEL_X = 90;
	private static final int CHECKPANEL_Y = 525;

	private JPanel contentPanel;
	private JScrollPane jsc;
	private JTextField tf;
	private JComboBox<String> comboBox;
	private JComboBox<String> roleTable;
	private JButton confirm;
	private JButton cancel;
	DatabaseConnect db = null;
	JTabbedPane jtb;
	logDialog log;
	private JButton newRole;
	private JButton deleteRole;
	private JPanel rolePermissionListPanel;
	JButton roleConfirm;
	JButton roleCancle;
	Component[] comp=null;
	JPanel head=null;
	
	private static final long serialVersionUID = 1L;

	public view() throws Exception {
		if (loginDatabase()) {
			initFrame();
			addlistener();
			setVisible(true);
		} else {
			JOptionPane.showMessageDialog(null, "ÕËºÅ/ÃÜÂë/·þÎñÆ÷´íÎó~~(²»Ö§³ÖsysÕËºÅ)",
					"T_T", JOptionPane.ERROR_MESSAGE);
			setVisible(false);
		}
	}
	
	public boolean loginDatabase() throws Exception {
		logDialog log = new logDialog();
		if (log.showdia(this, "µÇÂ¼")) {
			db = new DatabaseConnect();
			return db.connect(log.getUserName(), log.getPassword(),
					log.getAddress(), log.getDatabaseName());
		}
		return false;
	}

	private void addlistener() throws Exception {
	
		Vector<String>tablename=db.getTableNameList();
		for(int i=0;i<tablename.size();i++)
			comboBox.addItem(tablename.get(i));

		// µ±ÏÂÀ­±íÖÐµÄÊý¾ÝÏî±»Ñ¡ÖÐ
		comboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Çå³ýÖ®Ç°µÄËùÓÐÏî
				contentPanel.removeAll();

				String selectItems = (String) comboBox.getSelectedItem();

				// »ñÈ¡·­ÒëºÃµÄÖÐÎÄ
				try {
					tf.setText(db.getChineseTableName(selectItems));

					// »ñÈ¡Ñ¡ÖÐ±íÖÐËùÓÐµÄÔªËØÃû
					ResultSet rs;
					rs = db.getRecord(selectItems);
					while (rs.next()) {
						contentPanel.add(new record(rs.getString(1), rs
								.getString(2), rs.getString(3), rs.getInt(4)));
					}
					view.this.contentPanel.updateUI();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		// È·Êµ°´Å¥°´ÏÂ
		confirm.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				String tablename = (String) comboBox.getSelectedItem();
				String text = tf.getText();

				try {
					db.updateTableName(tablename, text);
				} catch (SQLException e1) {
					e1.printStackTrace();
				}

				Component[] re = contentPanel.getComponents();
				record r;
				for (Component c : re) {
					r = (record) c;
					try {
						db.updateColName(r.getChinese(), r.getFlag(),
								r.getxh(), r.getColName());
					} catch (SQLException e1) {
						e1.printStackTrace();
					}
				}
			}
		});

		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Component[] re = contentPanel.getComponents();
				record r;
				for (Component c : re) {
					r = (record) c;
					r.clear();
				}
				contentPanel.updateUI();
			}
		});

		
		//¼ÓÔØ½ÇÉ«µ½ roleTable
		Vector<String> v=db.getRole();
		for(int i=0;i<v.size();i++)
		{
			roleTable.addItem(v.get(i));
		}
		//µ±½ÇÉ«±»Ñ¡ÖÐÊ±¼ÓÔØÕâ¸ö½ÇÉ«µÄ¿É·ÃÎÊ±íµ½rolePermissionListPanel
		//ÔõÃ´¸ø½ÇÉ«Ìí¼ÓÈ¨ÏÞ»¹²»´óÇå³þ Ö±½ÓÏÔÊ¾ËùÓÐµÄ±íÈ»ºóÔÙÑ¡ÖÐ°É
		
		for(int i=0;i<tablename.size();i++)
		{
			rolePermissionListPanel.add(new permissionRecord("N", tablename.get(i)));
		}

			
		comp=rolePermissionListPanel.getComponents();
		//µ±Ñ¡ÖÐµÄÊ±ºòÓ¦¸Ã½«¿ÉÒÔ·ÃÎÊµÄ±í±ê×¢Ò»ÏÂ
		//È»¶ø±ê×¢µÄÊÇn*mµÄ »òÕßÊ¹ÓÃhashÖ®ÀàµÄ±ä³ÉnµÄ
		roleTable.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				Vector<String> v= new Vector<String>();
				String rolename=roleTable.getSelectedItem().toString();
				//rolePermissionListPanel.removeAll();
				//System.out.println(rolename);
			}
		});
		newRole.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				String s= JOptionPane.showInputDialog("ÊäÈë½ÇÉ«Ãû");
				if(s.equals("")||s==null)
				{
					JOptionPane.showMessageDialog(null, "½ÇÉ«Ãû²»ÄÜÎª¿Õ",
							"Warnning", JOptionPane.ERROR_MESSAGE);
				}
				else
				{
					try {
						db.insertRole(s);
					
					permissionRecord p;
					
					for (Component c : comp) {
						p = (permissionRecord) c;
						try {
							if(p.isSelectd())
							db.insertRolepermission(s,p.getName());
									
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
					roleTable.addItem(s);
					db.addaccount(s, s, "pass");
					} catch (SQLException e1) {
						e1.printStackTrace();
					}
				}
				
			}
		});
		deleteRole.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				try {
					db.deleteRole(roleTable.getSelectedItem().toString());
				} catch (SQLException e) 
				{
					e.printStackTrace();
				}
				
				roleTable.removeAll();				
				roleTable.updateUI();
				head.updateUI();
				Vector<String> v;
				try {
					v = db.getRole();
				
				for(int i=0;i<v.size();i++)
				{
					roleTable.addItem(v.get(i));
				}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});
			
		roleConfirm.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				String role=roleTable.getSelectedItem().toString();
				try {
					db.deleteRolePermission(role);
					System.out.println("this");
				} catch (SQLException e) {
					e.printStackTrace();
					
				}
				for (Component c : comp) {
					permissionRecord p = (permissionRecord) c;
					try {
						if(p.isSelectd())
						db.insertRolepermission(role,p.getName());								
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		});
		roleCancle.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				for (Component c : comp) {
					permissionRecord p = (permissionRecord) c;
					if(p.isSelectd())
					p.setSelect(false);
				}
			}
		});
		
	}

	private void initFrame() {

		// ÉèÖÃ³ÌÐò½çÃæ

		JPanel tranlate = new JPanel();
		tranlate.setLayout(null);
		setTitle("ÉèÖÃ");
		setSize(WIDTH, HEIGHT);
		setResizable(false);
		setFeel();

		// ÒòÎª³ÌÐò½çÃæ¼òµ¥ Ê¹ÓÃ²¼¾Ö·´¶ø²»ºÃ¿ØÖÆÎ»ÖÃ ËùÒÔÖ±½ÓÍ¨¹ý¼¸¸öµãÀ´Ó²±àÂë
		//ÉÏÃæÊÇ´í¾õ ²»¹ý²¼¾ÖÂðËæÒâ 
		
		JLabel ckb = new JLabel("²é¿´±í:");
		ckb.setBounds(SELECTPANEL_X, SELECTPANEL_Y, 50, 20);
		tranlate.add(ckb);
		comboBox = new JComboBox<String>();
		comboBox.setBounds(SELECTPANEL_X + 60, SELECTPANEL_Y, 120, 20);
		tranlate.add(comboBox);
		JLabel zwe = new JLabel("ÖÐÎÄÃû:");
		zwe.setBounds(SELECTPANEL_X, SELECTPANEL_Y + 30, 50, 20);
		tranlate.add(zwe);
		tf = new JTextField(25);
		tf.setBounds(SELECTPANEL_X + 60, SELECTPANEL_Y + 30, 120, 20);
		tranlate.add(tf);
		JLabel tip = new JLabel("Ñ¡ÔñÏÔÊ¾Êý¾ÝÏîºÍ²éÑ¯Êý¾ÝÏî");
		tip.setBounds(SELECTPANEL_X, SELECTPANEL_Y + 60, 160, 20);
		tranlate.add(tip);

		// contentPanel ÏÔÊ¾Êý¾Ý ²¢ÇÒ¼ÓÉÏjscrollpanel ¿ÉÒÔ¹ö¶¯

		contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

		jsc = new JScrollPane(contentPanel);
		jsc.setBounds(CONTENTPANEL_X, CONTENTPANEL_Y, CONTENT_WIDTH,
				CONTENT_HEIGHT);
		tranlate.add(jsc);
		confirm = new JButton("È·¶¨");
		confirm.setBounds(CHECKPANEL_X, CHECKPANEL_Y, 75, 42);
		tranlate.add(confirm);
		// È¡ÏûµÄ»° ¾Í ¼òµ¥µÄ°ÑcontentPanel ÖÐµÄ×é¼þµÄÄÚÈÝÇåµô
		cancel = new JButton("È¡Ïû");
		cancel.setBounds(CHECKPANEL_X + 160, CHECKPANEL_Y, 75, 42);
		tranlate.add(cancel);

		JTabbedPane rootPanel = new JTabbedPane();
		rootPanel.addTab("·­Òë", null, tranlate);

		JPanel permissionSetPanel = new JPanel();
		permissionSetPanel.setLayout(new BorderLayout());
		
		head=new JPanel();
		head.setLayout(new FlowLayout());
		
		head.add(new JLabel("Ñ¡ÔñÒÑÓÐµÄ½ÇÉ«"));
		
		roleTable = new JComboBox<String>();
		roleTable.setPreferredSize(new Dimension(120, 20));
		head.add(roleTable);
		
		newRole = new JButton("ÐÂÔö½ÇÉ«");
		head.add(newRole);
		
		deleteRole=new JButton("É¾³ý½ÇÉ«");
		head.add(deleteRole);
		
		permissionSetPanel.add(head,BorderLayout.NORTH);
		rolePermissionListPanel=new JPanel();
		rolePermissionListPanel.setLayout(new BoxLayout(rolePermissionListPanel, BoxLayout.Y_AXIS));
		JScrollPane jscrole=new JScrollPane(rolePermissionListPanel);
		permissionSetPanel.add(jscrole,BorderLayout.CENTER);

		JPanel foot=new JPanel();
		roleConfirm=new JButton("È·¶¨");
		roleCancle=new JButton("È¡Ïû");
		foot.add(roleConfirm);
		foot.add(roleCancle);
		permissionSetPanel.add(foot,BorderLayout.SOUTH);

		rootPanel.addTab("È¨ÏÞÉèÖÃ", null, permissionSetPanel);
		add(rootPanel);
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
		view frame = new view();
		frame.setDefaultCloseOperation(EXIT_ON_CLOSE);

	}
}