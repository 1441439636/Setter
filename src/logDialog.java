import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
public class logDialog extends JPanel
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JTextField username;
	JPasswordField password;
	JTextField address;
	JTextField databasename;	
	JButton confirm;
	JButton cancle;
	boolean ok;
	JDialog dialog;
	public logDialog() throws Exception
	{
		UIManager
		.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
SwingUtilities.updateComponentTreeUI(this);
		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(200, 300));
		
		JPanel panel=new JPanel();
		panel.setLayout(new FlowLayout());
		// 登录界面布局使用FlowLayout 设置大小就自动挤压成一行一行的
		panel.add(new JLabel("账号:    "));
		
		username = new JTextField("scott");
		username.setPreferredSize(new Dimension(120, 20));
		panel.add(username);
				
		panel.add(new JLabel("密码:    "));
		
		password = new JPasswordField("tiger");
		password.setPreferredSize(new Dimension(120, 20));
		panel.add(password);
		
		panel.add(new JLabel("地址:    "));
		
		address = new JTextField("127.0.0.1:1521");
		address.setPreferredSize(new Dimension(120, 20));
		panel.add(address);
		
		panel.add(new JLabel("数据库名:"));
		
		databasename = new JTextField("orcl");
		databasename.setPreferredSize(new Dimension(120, 20));
		panel.add(databasename);
	
	
		add(panel,BorderLayout.CENTER);
		
		confirm=new JButton("确定");
		confirm.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				ok=true;
				dialog.setVisible(false);
			}
		});
		cancle=new JButton("取消");
		cancle.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				dialog.setVisible(false);
			}
		});
		JPanel buttonPanel=new JPanel();
		buttonPanel.add(confirm);
		buttonPanel.add(cancle);
		add(buttonPanel,BorderLayout.SOUTH);
	}
	public String getUserName()
	{
		return username.getText();
	}
	public String getPassword()
	{
		return new String(password.getPassword());
	}
	public String getAddress()
	{
		return address.getText();
	}
	public String getDatabaseName()
	{
		return databasename.getText();
	}
	
	public boolean showdia(Component parent,String title)
	{
		ok=false;
		Frame ower=null;
		if(parent instanceof Frame)ower=(Frame)parent;
		else ower=(Frame)SwingUtilities.getAncestorOfClass(Frame.class, parent);
		
		if(dialog==null||dialog.getOwner()!=ower)
		{
			dialog=new JDialog(ower,true);
			dialog.add(this);
			dialog.getRootPane().setDefaultButton(confirm);
			dialog.pack();
		}
		dialog.setTitle(title);
		dialog.setVisible(true);
		return ok;
	}
	
}