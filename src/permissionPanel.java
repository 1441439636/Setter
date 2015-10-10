import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;


public class permissionPanel extends JPanel{

	private DatabaseConnect db;
	private JComboBox<String>roleCB;
	private JButton addRole;
	private JButton deleteRole;
	private JComboBox<String>tableCB;
	private JPanel content;
	private JButton confirm;
	private JButton cancle;

	private final ComboBoxListener com=new ComboBoxListener();

	permissionPanel(DatabaseConnect db)
	{
		this.db=db;
		initComponent();
		addAcctionListener();
	}
	
	private void initComponent()
	{
		setLayout(new BorderLayout());
		JPanel head=new JPanel();
		head.setLayout(new GridLayout(2,1));
		JPanel panel=new JPanel();
		roleCB=new JComboBox<String>();
		roleCB.setPreferredSize(new Dimension(120,20));
		tableCB=new JComboBox<String>();
		tableCB.setPreferredSize(new Dimension(120,20));
		
		addRole=new JButton("新增角色");
		deleteRole=new JButton("删除角色");
		confirm=new JButton("确定");
		cancle=new JButton("取消");
		
		panel.add(new JLabel("选择角色"));
		panel.add(roleCB);
		panel.add(addRole);
		panel.add(deleteRole);
		head.add(panel);
		
		panel=new JPanel();
		panel.add(new JLabel("选择表"));
		panel.add(tableCB);
		head.add(panel);
		add(head,BorderLayout.NORTH);
		content=new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		JScrollPane  jsc=new JScrollPane(content);
		add(jsc,BorderLayout.CENTER);
		panel=new JPanel();
		panel.add(confirm);
		panel.add(cancle);
		add(panel,BorderLayout.SOUTH);
	}
	
	//壮哉我大lisp 这里应该传控件名和方法进去的
	private void loadRolename()
	{
		roleCB.removeAllItems();
		ArrayList<String>list;
		list=db.getRoleList();
		for(int i=0;i<list.size();i++)
		{
			roleCB.addItem(list.get(i));
		}
	}
	private void loadTablename()
	{
		tableCB.removeAllItems();
		ArrayList<String>list;
		list=db.getAdornTablenameList();
		for(int i=0;i<list.size();i++)
		{
			tableCB.addItem(list.get(i));
		}
	}
	
	private void addAcctionListener()
	{
		loadRolename();
		loadTablename();
		
		addRole.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				//弹出一个框 输入角色名
				String rolename = JOptionPane.showInputDialog("输入此角色名");
				if(rolename==null)return;
				if(rolename.equals(""))
				{
					JOptionPane.showMessageDialog(null, "角色名不能为空","Warnning", JOptionPane.ERROR_MESSAGE);
					return;
				}
				db.addRole(rolename);
				loadRolename();
			}
		});
		deleteRole.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				String rolename=roleCB.getSelectedItem().toString();
				db.deleteRole(rolename);
				loadRolename();
			}
		});
		
		tableCB.addItemListener(com);
	
		confirm.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				Component[] com=content.getComponents();
				
				try {
					int role_id = db.getRoleid(roleCB.getSelectedItem().toString());
				
					int table_id=db.getTableid(tableCB.getSelectedItem().toString());
					db.deleteRolePermission(role_id,table_id);
					ArrayList<String>list=new ArrayList<String>();
					for(int i=0;i<com.length;i++)
					{
						permissionRecord record=(permissionRecord)com[i];
						if(record.isSelectd())
						{
							list.add(record.getName());
						}
					}
					db.addRolePermission(role_id,table_id,list);
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
			}
		});
	
	}	
	
	public void refresh()
	{
		System.out.println("refersn");
		tableCB.removeItemListener(com);
		String onselect=tableCB.getSelectedItem().toString();
		loadTablename();
		tableCB.addItemListener(com);
		tableCB.setSelectedItem(onselect);
	}
	//TODO 处理好 table_id 和 role_id 的变量  用id好麻烦啊摔
	class ComboBoxListener implements ItemListener
	{
		public void itemStateChanged(ItemEvent e)
		{
			System.out.println("on lis");
			try {
					content.removeAll();
					String tablename=tableCB.getSelectedItem().toString();
					ArrayList<String>list=db.getAdornColumnList(tablename);
					int role_id = db.getRoleid(roleCB.getSelectedItem().toString());	
					int table_id=db.getTableid(tableCB.getSelectedItem().toString());
				
					for(int i=0;i<list.size();i++)
					{
						permissionRecord record=new permissionRecord("N",list.get(i));
					
						if(db.hasRolePermission(role_id,table_id,list.get(i)))
								record.setSelect(true);
						content.add(record);
					}
					content.updateUI();			
				} catch (SQLException e1)
				{
						e1.printStackTrace();
				}
		}
	}

}
	
