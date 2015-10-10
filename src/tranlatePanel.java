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
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private DatabaseConnect db;

	private JComboBox<String>tableCB;
	private JTextField adorntableNameTF;
	private JPanel content;
	private JButton confirm;
	private JButton refresh;
	private int view_id;
	private final ComboBoxListener comboBoxListener=new ComboBoxListener();
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
	
	public void refersh()
	{
		String tablename=tableCB.getSelectedItem().toString();
		tableCB.removeActionListener(comboBoxListener);
		
		tableCB.removeAllItems();
		int len=tableCB.getItemCount();
		System.out.println(len);
		ArrayList<String> unadronTableNameList;				
		unadronTableNameList = db.getUnadornViewList();
	
		for(int i=0;i<unadronTableNameList.size();i++)
		{
			tableCB.addItem(unadronTableNameList.get(i));
		}
		tableCB.addActionListener(comboBoxListener );
		tableCB.setSelectedItem(tablename);
	}
	
	private void addAcctionListener() 
	{
			//开始时加载未翻译的表
			ArrayList<String> list;
			list = db.getUnadornViewList();
		
			for(int i=0;i<list.size();i++)
			{
				tableCB.addItem(list.get(i));
			}
			
			//当表中选项被选中时 加载列名 查看是否有翻译的表名
			//这里使用内部类 是为了后面更新combobox时暂时卸下监听器方便 
			tableCB.addActionListener(comboBoxListener );
			
			//当刷新重新获取表名    为了加载可能的新添加的表
			//先保存当前选中的表 清空表名下拉框重新加载 然后将选中项设为之前保存的表 如果此时这个表已被删除的话 setSelectedItem 会选中第一项
			//TODO 注意当同时使用其他工具更改tablename 表时 会造成表锁定 这时程序就会卡住
			refresh.addActionListener(new ActionListener() {	
				public void actionPerformed(ActionEvent arg0) {
					refersh();
				}		
			});
			
			//点击确定 更新表名 列名
			//
			confirm.addActionListener(new ActionListener() {				
				public void actionPerformed(ActionEvent arg0) {
					try {
						String tablename=tableCB.getSelectedItem().toString();
						String adorntableName=adorntableNameTF.getText();
					
						db.setTableName(tablename, adorntableName);
						
						Component[]com=content.getComponents();
						for(int i=0;i<com.length;i++)
						{
							record record=(record)com[i];
							db.setColname(tablename,record.getColName(),record.getFlag(),record.getChinese(),record.getxh());
						}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				}
			});
	}
	
	class ComboBoxListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e) {
			String onselecttable=tableCB.getSelectedItem().toString();
			view_id=db.getidByunadornname(onselecttable);
			adorntableNameTF.setText(db.getAdornTableName(onselecttable));
				content.removeAll();
				ArrayList<String> list=db.getColumnList(onselecttable);
				for(int i=0;i<list.size();i++)
				{
					String colname=list.get(i);
					record record;
					ArrayList<String>set=db.getRecord(view_id,colname);
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
