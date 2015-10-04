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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;


public class tranlatePanel extends JPanel
{
	private DatabaseConnect db;

	private JComboBox<String>tableCB;
	private JTextField adorntableNameTF;
	private JPanel content;
	private JButton confirm;
	private JButton refresh;
	
	tranlatePanel(DatabaseConnect db)
	{
		this.db=db;
		initComponent();
		addAcctionListener();
		
		
		
	}
	private void initComponent()
	{
		tableCB=new JComboBox<String>();
		tableCB.setPreferredSize(new Dimension(120,20));
		adorntableNameTF=new JTextField();
		adorntableNameTF.setPreferredSize(new Dimension(120,20));
		content=new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		JScrollPane contentJS=new JScrollPane(content);
		confirm=new JButton("确定");
		refresh=new JButton("刷新");			
		
		setLayout(new BorderLayout());
		JPanel head=new JPanel();
		head.setLayout(new GridLayout(2,1));
		JPanel temppanel=new JPanel();
		temppanel.add(new JLabel("选择表"));
		temppanel.add(tableCB);
		head.add(temppanel);
		temppanel=new JPanel();
		temppanel.add(new JLabel("翻译  "));
		temppanel.add(adorntableNameTF);
		head.add(temppanel);
		add(head,BorderLayout.NORTH);
		add(contentJS,BorderLayout.CENTER);
		temppanel=new JPanel();
		temppanel.add(confirm);
		temppanel.add(refresh);
		add(temppanel,BorderLayout.SOUTH);
	}
	
	private void addAcctionListener()
	{
			//开始时加载未翻译的表
			ArrayList<String> list;
			final ComboBoxListener comboboxlistener=new ComboBoxListener();
			list = db.getUnadornTableNameList();
		
			for(int i=0;i<list.size();i++)
			{
				tableCB.addItem(list.get(i));
			}
			
			//当表中选项被选中时 加载列名 查看是否有翻译的表名
			//TODO 现在使用的是itemstatechange 监听的下拉框中状态的改变 这就导致 选择第一个时不会触发
			tableCB.addItemListener(comboboxlistener );
			
			//当刷新重新获取表名    为了加载可能的新添加的表
			//先保存当前选中的表 清空表名下拉框重新加载 然后将选中项设为之前保存的表 如果此时这个表已被删除的话 setSelectedItem 会选中第一项
			//TODO 这里当会自动触发选择第一个的事件 然后setSelected又触发一次
			//注意当同时使用其他工具更改tablename 表时 会造成表锁定 这时程序就会卡住
			refresh.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent arg0) {
					
					String tablename=tableCB.getSelectedItem().toString();
					tableCB.removeItemListener(comboboxlistener);
					tableCB.removeAllItems();
					int len=tableCB.getItemCount();
					System.out.println(len);
					ArrayList<String> unadronTableNameList;				
					unadronTableNameList = db.getUnadornTableNameList();
				
					for(int i=0;i<unadronTableNameList.size();i++)
					{
						tableCB.addItem(unadronTableNameList.get(i));
					}
					tableCB.addItemListener(comboboxlistener );
					tableCB.setSelectedItem(tablename);
				}		
			});
			
			
			//点击确定 更新表名 列名
			confirm.addActionListener(new ActionListener() {				
				public void actionPerformed(ActionEvent arg0) {
					try {
						String tablename=tableCB.getSelectedItem().toString();
						String adorntableName=adorntableNameTF.getText();
					
						db.updateTableName(tablename, adorntableName);
						
						Component[]com=content.getComponents();
						System.out.println(com.length);
						for(int i=0;i<com.length;i++)
						{
							record record=(record)com[i];
							if(record.getFlag().equals("Y"))
							db.setColname(tablename,record.getColName(),record.getFlag(),record.getChinese(),record.getxh());
						}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				}
			});
			
		
	}
	
	
	class ComboBoxListener implements ItemListener
	{

		public void itemStateChanged(ItemEvent e) {
			String onselecttable=tableCB.getSelectedItem().toString();
			
			adorntableNameTF.setText(db.getAdornTableName(onselecttable));
			if(e.getStateChange() == ItemEvent.SELECTED)
			{
				content.removeAll();
				ArrayList<String> list=db.getColumnList(onselecttable);
				for(int i=0;i<list.size();i++)
				{
					String colname=list.get(i);
					record record;
					ArrayList<String>set=db.getRecord(onselecttable,colname);
					if(set.size()==3)
					{
						record=new record(set.get(0),colname,set.get(1),Integer.parseInt(set.get(2)));
					}
					else
					{
						record=new record(colname);
					}
					content.add(record);
				}
				System.out.println("run itemlistener");
				content.updateUI();
			}	
		}
		
	}
	
	
	
	
	
	
	
	
	

}
