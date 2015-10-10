import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;


public class accountPanel extends JPanel 
{

	private  DatabaseConnect db;
	private  JButton selectAll;
	private  JComboBox<String>roleCB;
	private  JButton addAccount;
	private  JPanel content;
	private  ArrayList<String>rolelist;
	private  ComboBoxListener conmlis=new ComboBoxListener();
	accountPanel(DatabaseConnect db)
	{
		this.db=db;
		initComponent();
		addAcctionListener();
	}
	
	private void initComponent() 
	{
		setLayout(new BorderLayout());
		JPanel temp=new JPanel();
		selectAll =new JButton("选择全部账号");
		temp.add(selectAll);
		temp.add(new JLabel("根据角色选择账号"));
		roleCB=new JComboBox<String>();
		roleCB.setPreferredSize(new Dimension(120,20));
		temp.add(roleCB);
		addAccount=new JButton("添加账号");
		temp.add(addAccount);
		add(temp,BorderLayout.NORTH);
		content=new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		JScrollPane jsc=new JScrollPane(content);
		add(jsc,BorderLayout.CENTER);
		
	}
	private void loadrole()
	{
		roleCB.removeItemListener(conmlis);
		roleCB.removeAllItems();
		rolelist=db.getRoleList();
		for(int i=0;i<rolelist.size();i++)
		{
			roleCB.addItem(rolelist.get(i));
		}
		roleCB.addItemListener(conmlis);
	}
	private void addAcctionListener() 
	{
		roleCB.addItemListener(conmlis);
		loadrole();
		selectAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					ArrayList<String[]>list=db.getAllAccount();
					content.removeAll();
					for(int i=0;i<list.size();i++)
					{
						content.add(new accountRecord(list.get(i)));			
					}
					content.updateUI();
					} catch (SQLException e) {
						e.printStackTrace();
				}
			}
		});
		addAccount.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				
				content.removeAll();
				if(roleCB.getSelectedItem()!=null)
				{
				content.add(new accountRecord(roleCB.getSelectedItem().toString()));
				}
				content.updateUI();
			}
		});
	}
		
	public void refersh()
	{
		loadrole();
	}
	class accountRecord extends JPanel
	{
		private static final long serialVersionUID = 1L;
		JTextField name;
		JTextField password;
		JComboBox<String>roleCB;
		String role;
		JButton confirm;
		JButton cancle;
		public accountRecord(String role)
		{
			initCompontent();
			roleCB.setSelectedItem(role);
			addlistener();
		}
		public accountRecord(String[] strings) {
			initCompontent();
			role=strings[0];
			roleCB.setSelectedItem(role);
			name.setText(strings[1]);
			password.setText(strings[2]);	
			addlistener();
		}
			
		private void initCompontent()
		{
			setMaximumSize(new Dimension(600, 35));
			add(new JLabel("角色名"));
			roleCB=new JComboBox<String>();
			roleCB.setPreferredSize(new Dimension(120,20));	
			add(roleCB);
			name=new JTextField();
			name.setPreferredSize(new Dimension(120,20));
			add(name);
			password=new JTextField();
			password.setPreferredSize(new Dimension(120,20));
			add(password);
			confirm=new JButton("确定");
			cancle=new JButton("删除");
			add(confirm);
			add(cancle);
			for(int i=0;i<rolelist.size();i++)
			{
				roleCB.addItem(rolelist.get(i));
			}
		}
		private void addlistener()
		{
			confirm.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent arg0) {
					try {
						if(isEmpty())
						{
							JOptionPane.showMessageDialog(null, "账号/密码 不能为空","Warnning", JOptionPane.ERROR_MESSAGE);
							return;
						}
						db.setaccount(roleCB.getSelectedItem().toString(),name.getText(),password.getText());
					} catch (SQLException e) {
						e.printStackTrace();
					}	
				}
			});
			cancle.addActionListener(new ActionListener() {		
				public void actionPerformed(ActionEvent arg0) {
					accountRecord.this.setVisible(false);
					if(isEmpty())return;
					try {
						db.deleteaccount(roleCB.getSelectedItem().toString(),name.getText());
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			});				
		}
		private boolean isEmpty()
		{
			return name.getText().equals("")||password.getText().equals("");
		}
	}
	
	
	class ComboBoxListener implements ItemListener
	{
		public void itemStateChanged(ItemEvent e)
		{			
			try {
			ArrayList<String[]>list=db.getAccountByRole(roleCB.getSelectedItem().toString());
			content.removeAll();
			for(int i=0;i<list.size();i++)
			{
				content.add(new accountRecord(list.get(i)));			
			}
			content.updateUI();
			} catch (SQLException e1) {
				e1.printStackTrace();
		}
		}
	}

	
	
	
	
	
	
	
	
	
}
