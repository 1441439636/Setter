import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;

/*
 * 使用connect 初始化  connect 因为有可能数据库登录连接失败
 * 
 * 
 */

public class DatabaseConnect {
	
	private Connection con = null;
	private PreparedStatement pre = null;
	private ResultSet result = null;
    //全局变量以便使用
    public Boolean connect(String user,String password,String address,String databasename){    	
    	try {
    		Class.forName("oracle.jdbc.driver.OracleDriver");// 加载Oracle驱动程序
    	    String url = "jdbc:oracle:" + "thin:@"+address+"/"+databasename;// 生成连接字符串用来连接数据库      
    	    
    	    Properties common = new Properties();
    	    common.put("user", user);
    	    common.put("password",password);
			con=DriverManager.getConnection(url, common);
			//初始化Connection properties 
				
			if(!hasAllUserTables())
			{
				dropAllUserTables();
				createAlluserTables();
			}
			//保持用户表的完整
			//有些繁琐 需要考虑数据库被恶意操做? 壮哉我大鲁棒性
		} catch (Exception e) {
			return false;
		}
    	return true;
    }
    
    /**
     * 将表转化成视图 
     * @param list
     * @return
     * @throws SQLException 
     */
    private void transTabletoView()
    {
    	try {
    		ResultSet result=con.prepareStatement("select object_name from user_objects where object_type='TABLE' and object_name not in ('ROLE','ACCOUNT','ROLEACCOUNT','ROLEPERMISSION','TABLENAME','COLUMNNAME','QUERYCONDITION') order by object_name").executeQuery();
    	
    	while(result.next())
    	{
    		String name=result.getString(1);
    		if(!hasview(name+"_view"))
    		{
    			createview(name);
    		}
    		
    		
    	}
    	
    	} catch (SQLException e) {
			// TODO Auto-generated catch block
    		System.out.println("here");
			e.printStackTrace();
		}
    }
    
    private void createview(String name) throws SQLException 
    {	
    	pre=con.prepareStatement("create or replace view "+name+"_view"+" as select * from "+name);
		pre.executeQuery();
	}
	private boolean hasview(String name)
    {
    	try {
			pre=con.prepareStatement(hasView);
			pre.setString(1, name);
			ResultSet result=pre.executeQuery();
			if(result.next()&&result.getInt(1)==0)
				return false;
			
    		} catch (SQLException e) {
    			System.out.println("here");
			e.printStackTrace();
			return false;
		}
    	return true;
    }
	private void dropAllUserTables() 
	{	
		for(int i=0;i<usertablename.length;i++)
		{
			try{
			pre=con.prepareStatement("drop table "+usertablename[i]);
	  		pre.executeQuery();
			pre=con.prepareStatement("drop sequence autoadd ");
			pre.executeQuery();
			}
			catch(Exception e){};
		}
	}
    
    private void createAlluserTables() throws SQLException {
  		Statement stmt = con.createStatement(); 
  		for(int i=0;i<createUserTableSql.length;i++)
  		{
  			stmt.addBatch(createUserTableSql[i]);
  		}
  		stmt.executeBatch();
  		stmt.close();
	}

  	private boolean hasAllUserTables() {
		try {
			result=con.prepareStatement(hasAllUserTables).executeQuery();
			if(result.next()&&result.getInt(1)==7)return true;
			return false;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

  	

  	//先检查有没有没有创建视图的表 创建视图
  	//只读取视图
  	public ArrayList<String> getUnadornViewList() 
  	{  	
  		
  		ArrayList<String>tablename=new ArrayList<String>();
    	try {
    		transTabletoView();
			result=con.prepareStatement(getUnadornedViewListSql).executeQuery();			
	    	while(result.next())
	    	{
	    		tablename.add(result.getString(1));
	    	}
	    	return tablename;
    	}
    	catch (SQLException e) {		
			e.printStackTrace();
			tablename.clear();
			return tablename;
		}
 	}
	public ArrayList<String> getColumnList(String tablename) {
		ArrayList<String>list=new ArrayList<String>();
		try {
			pre=con.prepareStatement(getUnadornedColumnListSql);
			pre.setString(1, tablename);
			result=pre.executeQuery();
			while(result.next())
			{
				list.add(result.getString(1));
			}
			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			return list;
		}
	}
	
	
	//更新表名 首先看在tablename中是否存在 不存在插入 存在更新
  	//有时候直接写sql语句感觉更方便点
	
	private boolean hasTable(String name) throws SQLException
	{
		pre=con.prepareStatement(isEixtTableName);
  		pre.setString(1, name);
		result=pre.executeQuery();
  		return result.next()&&result.getInt(1)==1;
	}

	private boolean hasColumn(int table_id,String column_name) throws SQLException
	{
		pre=con.prepareStatement(isEixtColumnName);
  		pre.setInt(1, table_id);
  		pre.setString(2, column_name);
		result=pre.executeQuery();
		return result.next()&&result.getInt(1)==1;
	}
	public void setTableName(String name,String chinese) throws SQLException
  	{
	
  		String haschiese=chinese.equals("")?"N":"Y";
  		boolean isExit=hasTable(name);
  		
  		if(isExit)
  		{
  			pre=con.prepareStatement(updateTableName);
  			pre.setString(1, chinese);
  			pre.setString(2,haschiese );
  			pre.setString(3, name);
  			pre.executeQuery(); 		
  		}
  		else
  		{
  			pre=con.prepareStatement(insertTableName);
  			pre.setString(1,name);
  			pre.executeQuery();
  			pre=con.prepareStatement(updateTableName);
  			pre.setString(1, chinese);
  			pre.setString(2,haschiese );
  			pre.setString(3, name);
  			pre.executeQuery(); 
  			
  		}  		
  	}		

	public String getAdornTableName(String onselecttable) {
  		String str="";
  		try {
	  		pre=con.prepareStatement(getAdornTableName);
			pre.setString(1, onselecttable);
			pre.executeQuery();
	  		result=pre.executeQuery();
			if(result.next())
	  		{	
				str=result.getString(1);
	  		}
  		} catch (SQLException e) {
			e.printStackTrace();
			return str;
  		}		
  		return str;
	}

	private String getDateType(String tablename,String column_name)
	{
		String str="";
  		try {
	  		pre=con.prepareStatement(getDateType);
			pre.setString(1, tablename);
			pre.setString(2, column_name);
			pre.executeQuery();
	  		result=pre.executeQuery();
			if(result.next())
	  		{	
				str=result.getString(1);
	  		}
  		} catch (SQLException e) {
			e.printStackTrace();
			return str;
  		}		
  		return str;
		
	}
	/*
	 * 更新columnname 到底是使用column_id 还是直接用列名 选择了用id 列名虽然不多但 毕竟有三张表使用 
	 * 然而并没有卵用 还是改成列名
	 * 
	 */
	//根据表名列名插入tableid columnid datatype
	private void insertColname(String tablename,int table_id,String column_name,String flag,String chinese, int xh) throws SQLException
	{
		String datatype=getDateType(tablename,column_name);
		pre=con.prepareStatement(insertColName);
		pre.setInt(1, table_id);
		pre.setString(2,column_name);		
		pre.setString(3, chinese);
		pre.setString(4,datatype);		
		pre.setString(5,flag);		
		pre.setInt(6,xh);
		pre.executeQuery();
		
	}
	private void updateColname(int table_id,String column_name,String flag,String chinese, int xh) throws SQLException
	{
		pre=con.prepareStatement(updateColName);
	//	columnname set adorn_name= ?, flag = ? , no= ? where table_id = ? and column_id=?
		pre.setString(1, chinese);
		pre.setString(2,flag);		
		pre.setInt(3,xh);
		pre.setInt(4,table_id );
		pre.setString(5,column_name );
		pre.executeQuery();
	}
	
	
	//插入columnname
	//首先 获取table_id
	//检测是否之前已经插入列名
	//存在 直接插入
	//不存在先插入
	public void setColname(String tablename, String colName, String flag,
		String chinese, int xh) throws SQLException {
		
		int table_id=0;		
		pre=con.prepareStatement(getTableid);
		pre.setString(1, tablename);
		result=pre.executeQuery();
		if(result.next())
		{
			table_id=result.getInt(1);
		}
		else return;
		
		boolean isExit=hasColumn(table_id,colName);
		if(isExit)
		{
			updateColname(table_id,colName,flag,chinese,xh);
		}
		else 
		{
			insertColname(tablename,table_id,colName,flag,chinese,xh);
		}
		
	}
	
  	public ArrayList<String> getRecord(int id, String colname)
  	{  		
  		ArrayList<String>set=new ArrayList<String>();
  		try {
			pre=con.prepareStatement(getRecordSql);
			pre.setInt(1, id);
			pre.setString(2, colname);
			result=pre.executeQuery();
			if(result.next())
			{
				set.add(result.getString(1));
				set.add(result.getString(2));
				set.add(result.getString(3));
			}	
		} 
  		catch (SQLException e) {
			e.printStackTrace();
			return set;
  		} 	
  		return set;
 	}
	
	public ArrayList<String> getAdornTablenameList() {
		ArrayList<String>list=new ArrayList<String>();
		try {
			pre=con.prepareStatement(getAdornTablenameList);
			result=pre.executeQuery();
			while(result.next())
			{
				list.add(result.getString(1));
			}
			return list;
		} 
		catch (SQLException e) {
			e.printStackTrace();
			return list;
		}
	}
  
	//TODO 注意到有一些方法的结构类似 ->使用sql 传出一维的list 是否可以将其抽象为一个方法
	public ArrayList<String> getRoleList() {
		ArrayList<String>list=new ArrayList<String>();
		try {
			pre=con.prepareStatement(getRoleList);
			result=pre.executeQuery();
			while(result.next())
			{
				list.add(result.getString(1));
			}
			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			return list;
		}
	}
	
	public void addRole(String rolename) {	
		try {
			pre=con.prepareStatement(addRole);
			pre.setString(1, rolename);
			pre.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void deleteRole(String rolename)
	{
		try {
			pre=con.prepareStatement(deleteRole);
			pre.setString(1, rolename);
			pre.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<String> getAdornColumnList(String tablename) {
		ArrayList<String>list=new ArrayList<String>();
		try {
			pre=con.prepareStatement(getAdornColumnList);
			pre.setString(1, tablename);
			result=pre.executeQuery();
			while(result.next())
			{
				list.add(result.getString(1));
			}
			return list;
		} 
		catch (SQLException e) {
			e.printStackTrace();
			return list;
		}
	}

	
	public void addRolePermission(int role_id, int table_id,ArrayList<String> list) throws SQLException
	{
		pre=con.prepareStatement(addRolePermission);
		for(int i=0;i<list.size();i++)
		{
			pre.setInt(1,role_id);
			pre.setInt(2,table_id);
			pre.setString(3,list.get(i));		
			pre.executeQuery();		
		}
	}

	//TODO 异常处理好麻烦 我看到了一个向上的链式结构 
	public int getRoleid(String role_name) throws SQLException
	{
		pre=con.prepareStatement(getRoleid);
		pre.setString(1, role_name);
		result=pre.executeQuery();
		if(result.next())
		{
			return result.getInt(1);
		}
		return -1;	
	}

	public int getTableid(String table_name) throws SQLException
	{
		pre=con.prepareStatement(gettableid);
		pre.setString(1, table_name);
		result=pre.executeQuery();
		if(result.next())
		{
			return result.getInt(1);
		}
		return -1;
	}
	public void deleteRolePermission(int role_id, int table_id) throws SQLException {
		pre=con.prepareStatement(deleteRolePermission);
		pre.setInt(1, role_id);
		pre.setInt(2, table_id);
		pre.executeQuery();
	}
	
	public boolean hasRolePermission(int role_id, int table_id, String column) throws SQLException {
		pre=con.prepareStatement(hasRolePermission);
		pre.setInt(1, role_id);
		pre.setInt(2, table_id);
		pre.setInt(3, table_id);
		pre.setString(4, column);
		result=pre.executeQuery();
		return result.next()&&result.getInt(1)==1;		
	}
	public void deleteaccount(String role, String text) throws SQLException
	{
		pre=con.prepareStatement(deleteaccount);
		pre.setString(1, role);
		pre.setString(2, text);
		pre.executeQuery();
	}	
    public static void main(String[] args) throws SQLException {
    	DatabaseConnect db=new DatabaseConnect();
    	if(db.connect("scott", "tiger", "localhost", "orcl"))System.out.println("db connect");
    	ArrayList<String>list=db.getUnadornViewList();
//    	for(int i=0;i<list.size();i++)
//    	{
//    	//	System.out.println(list.get(i));
//    	}
//    	
 //   	db.createview("ORDERITEMS");
  //  	System.out.println(db.hasview("ORDERITEMS_view"));
    	System.out.println("over");
	}
	public ArrayList<String[]> getAllAccount() throws SQLException {
		ArrayList<String[]> list=new ArrayList<String[]>();
		result=con.prepareStatement(getAllAccount).executeQuery();
		while(result.next())
		{
			String[]acc=new String[3];
			acc[0]=result.getString(1);
			acc[1]=result.getString(2);
			acc[2]=result.getString(3);
			list.add(acc);
		}
		return list;
	}
	//当roleaccount表中没有时 account_id 为-1 name 是唯一的 
	//如果有的话更新roleaccount 和account
	//没有 先获得account_id 在 插入 roleaccount account
	public void setaccount(String role, String name, String pass) throws SQLException
	{
		int role_id=getRoleid(role);
		int account_id=getAccountid(name);
		boolean isExit=account_id!=-1;
		if(isExit)
		{
			updateRoleAccount(role_id,account_id);
			updateAccount(account_id,name,pass);
		}
		else
		{
			int id=getAutoaddNext();
			insertRoleAccount(role_id,id);
			insertAccount(id,name,pass);
		}
	}
	private void insertAccount(int account_id, String name, String pass) throws SQLException {
		pre=con.prepareStatement(insertAccount);
		pre.setInt(1,account_id);
		pre.setString(2, name);
		pre.setString(3, pass);
		pre.executeQuery();
	}
	private void updateRoleAccount(int role_id, int account_id) throws SQLException {
		pre=con.prepareStatement(updateRoleAccount);
		pre.setInt(1, role_id);
		pre.setInt(2, account_id);
		pre.executeQuery();
	}
	private int getAutoaddNext() throws SQLException
	{
		result=con.prepareStatement(getAutoaddNext).executeQuery();
		result.next();
		return result.getInt(1);
	}
	private void insertRoleAccount(int role_id,int account_id) throws SQLException
	{
		pre=con.prepareStatement(insertRoleAccount);
		pre.setInt(1, account_id);		
		pre.setInt(2, role_id);
		pre.executeQuery();
	}
	private void updateAccount(int account_id, String name, String pass) throws SQLException {
		pre=con.prepareStatement(updateAccount);
		pre.setString(1, name);
		pre.setString(2, pass);
		pre.setInt(3, account_id);
		pre.executeQuery();
	}
	
	public ArrayList<String[]> getAccountByRole(String role_name) throws SQLException {
		ArrayList<String[]> list=new ArrayList<String[]>();
		pre=con.prepareStatement(getAccountByRole);
		pre.setString(1, role_name);
		result=pre.executeQuery();
		while(result.next())
		{
			String[]acc=new String[3];
			acc[0]=result.getString(1);
			acc[1]=result.getString(2);
			acc[2]=result.getString(3);
			list.add(acc);
		}
		return list;
	}
	private int getAccountid(String name) throws SQLException {
		pre=con.prepareStatement(getAccountid);
		pre.setString(1, name);
		result=pre.executeQuery();
		if(result.next())
		{
			return result.getInt(1);
		}
		return -1;
	}
	
	public int getidByunadornname(String onselecttable) {
	
		try {
		pre=con.prepareStatement(getidByunadornname);
			pre.setString(1, onselecttable);
		
		result=pre.executeQuery();
		if(result.next())
		{
			return result.getInt(1);
		}
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
		return -1;
	}
	private static final String[] usertablename={"ROLE","ACCOUNT","ROLEACCOUNT","ROLEPERMISSION","TABLENAME","COLUMNNAME","QUERYCONDITION"};

	private static final String hasAllUserTables="select count(*) from user_objects where object_name in ('ROLE','ACCOUNT','ROLEACCOUNT','ROLEPERMISSION','TABLENAME','COLUMNNAME','QUERYCONDITION')";

	private static final String[] createUserTableSql={
			"create table role(role_id number primary key,role_name varchar2(30) not null unique)",
			"create table account(account_id number  primary key,name varchar2(30) not null UNIQUE,password varchar2(30) not null)",
			"create table roleaccount(account_id number primary key,role_id number not null)",
			"create table rolepermission(role_id number ,table_id number not null,column_name varchar2(30) not null)",
			"create table tablename(table_id number primary key,adorn_name varchar(30) unique,table_name varchar(30),flag char(1) default 'N')",
			"create table columnname(table_id number not null,column_name varchar2(30) not null,datatype varchar(106),adorn_name varchar(30),flag char(1) default 'N',no number default 1)",
			"create table querycondition(account_id number ,table_id number not null,column_name varchar2(30) not null,flag char(1) default 'N',con1 varchar(30),con2 varchar(30),setname varchar(30) not null)",
			"CREATE SEQUENCE auto_add INCREMENT BY 1   START WITH 1    NOMAXVALUE  NOCYCLE ",
			"alter table ACCOUNT  add constraint FK_ACCOUNT_ROLEACCOUNT foreign key (ACCOUNT_ID)  references ROLEACCOUNT (ACCOUNT_ID) on　delete　cascade",
			"alter table ROLEACCOUNT  add constraint FK_ROLEACCOUNT_ROLE foreign key (ROLE_ID)  references role (ROLE_ID) on　delete　cascade",
			"alter table ROLEPERMISSION  add constraint FK_PERMISS_ROLE foreign key (ROLE_ID)  references role (ROLE_ID) on　delete　cascade",
			"alter table COLUMNNAME  add constraint FK_COLUMNAME_TABLENAME foreign key (TABLE_ID)  references TABLENAME (TABLE_ID) on　delete　cascade",
			"alter table QUERYCONDITION  add constraint FK_QUERYCONDITION_ACCOUNT foreign key (ACCOUNT_ID)  references ACCOUNT (ACCOUNT_ID) on　delete　cascade",
			"alter table QUERYCONDITION  add constraint FK_QUERYCONDITION_TABLENAME foreign key (TABLE_ID)  references TABLENAME (TABLE_ID) on　delete　cascade"
	};
	
	private final static String getUnadornedTableListSql="select object_name from user_objects where object_type='TABLE' and object_name not in ('ROLE','ACCOUNT','ROLEACCOUNT','ROLEPERMISSION','TABLENAME','COLUMNNAME','QUERYCONDITION') order by object_name";

	private final static String getUnadornedViewListSql="select object_name from user_objects where object_type='VIEW'  order by object_name";

	private final static String getUnadornedColumnListSql="select column_name from user_tab_cols where table_name=?";
	
	private final static String isEixtTableName="select count(*) from tablename where table_name =?";
	
	private final static String insertTableName="insert into tablename(table_id,table_name) select object_id,object_name from user_objects where object_name=?";
	
	private final static String updateTableName="update tablename set adorn_name= ?,flag=? where table_name= ?";

	private final static String getAdornTableName="select adorn_name from tablename where table_name=?";
	
	private final static String getTableid="select o.OBJECT_ID from user_objects o where o.OBJECT_NAME=? ";
	
	private final static String isEixtColumnName="select count(*)  from columnname c where  c.table_id=? and c.column_name=?";
	
	private final static String updateColName="update columnname set adorn_name= ?, flag = ? , no= ? where table_id = ? and column_name=?";
	
	private final static String insertColName="insert into columnname(table_id,column_name,adorn_name,datatype,flag,no) values(?,?,?,?,?,?)";

	private final static String getRecordSql="select c.flag,c.adorn_name,c.no from columnname c where c.table_id =? and c.column_name=?";

	private final static String getAdornTablenameList="select adorn_name from tablename where flag='Y'";

	private final static String getRoleList="select role_name from role";

	private final static String addRole="insert into role values(autoadd.nextval,?)";

	private final static String deleteRole="delete role where role_name=?";

	private final static String getAdornColumnList="select adorn_name from columnname where flag='Y' and table_id in (select table_id from tablename where adorn_name=?)";

	private final static String getRoleid="select role_id from role where role_name=?";
	
	private final static String gettableid="select table_id from tablename where adorn_name=?";
	
	private final static String addRolePermission="insert into rolepermission(role_id,table_id,column_id) select r.role_id ,c.table_id,c.column_id from columnname c,role r where r.role_id=?   and c.table_id=? and  c.adorn_name=?";
	
	private final static String deleteRolePermission="delete rolepermission where role_id =? and  table_id=?";

	private final static String hasRolePermission="select count(*) from rolepermission r where r.role_id=? and r.table_id=? and r.column_id in (select t.column_id from columnname t where t.table_id=? and t.adorn_name=?)";
	
	private final static String getAllAccount="select r.role_name, a.name,a.password  from roleaccount c,account a,role r where a.account_id=c.account_id and r.role_id=c.role_id";

	private final static String deleteaccount="delete  from roleaccount r where r.role_id in (select role_id from role where role_name=?) and r.account_id in (select account_id from account where name=?)";

	private final static String getAccountid="select account_id from account where name=?";
	
	private final static String getAutoaddNext="select autoadd.nextval as id from dual";
	
	private final static String updateAccount="update account set name=?,password=? where account_id=?";
	
	private final static String updateRoleAccount="update roleaccount set role_id=? where account_id=?";
	
	private final static String insertRoleAccount="insert into roleaccount values(?,?)";
		
	private final static String insertAccount="insert into account(account_id,name,password) values(?,?,?)";

	private final static String getAccountByRole="select r.role_name, a.name,a.password  from roleaccount c,account a,role r where a.account_id=c.account_id and r.role_id=c.role_id and r.role_name=?";

	private final static String hasView="select count(*) from user_objects where object_name=?";
	
	private final static String createView="create or replace view ? as select * from ?";
	
	private final static String getidByunadornname="select object_id from user_objects where object_name=? ";
	
	private final static String getDateType="select data_type from user_tab_cols where table_name=? and column_name=? ";
	
	
	
	
}