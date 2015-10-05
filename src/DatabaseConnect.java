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
	private void dropAllUserTables() 
	{	
		for(int i=0;i<usertablename.length;i++)
		{
			try{
			pre=con.prepareStatement("drop table "+usertablename[i]);
	  		pre.executeQuery();}
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

  	
  	public void close(){
  		try{  
  			if (result != null)
  				result.close();
  			if (pre != null)
  				pre.close();
  			if (con != null)
  				con.close();
  	   System.out.println("数据库连接已关闭！");
  		}
  	  catch (Exception e)
	    {
	    	System.out.println("数据库关闭异常");
	        e.printStackTrace();
	    }
  	}
	
  	public ArrayList<String> getUnadornTableNameList()
  	{  	
  		ArrayList<String>tablename=new ArrayList<String>();
    	try {
			result=con.prepareStatement(getUnadornedTableNameListSql).executeQuery();
		
	    	while(result.next())
	    	{
	    		tablename.add(result.getString(1));
	    	}
	    	return tablename;
    	} 
    	
    	catch (SQLException e) {		
			e.printStackTrace();
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

	private boolean hasColumn(int table_id,int column_id) throws SQLException
	{
		pre=con.prepareStatement(isEixtColumnName);
  		pre.setInt(1, table_id);
  		pre.setInt(2, column_id);
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


	/*
	 * 更新columnname 到底是使用column_id 还是直接用列名 选择了用id 列名虽然不多但 毕竟有三张表使用 
	 * 
	 * 
	 */
	//根据表名列名插入tableid columnid datatype
	private void insertColname(String tablename,String columnname) throws SQLException
	{
		pre=con.prepareStatement(insertColName);
		pre.setString(1, tablename);
		pre.setString(2, columnname);
		pre.executeQuery();
	}
	private void updateColname(int table_id,int column_id,String flag,String chinese, int xh) throws SQLException
	{
		pre=con.prepareStatement(updateColName);
	//	columnname set adorn_name= ?, flag = ? , no= ? where table_id = ? and column_id=?
		pre.setString(1, chinese);
		pre.setString(2,flag);		
		pre.setInt(3,xh);
		pre.setInt(4,table_id );
		pre.setInt(5,column_id );
		pre.executeQuery();
	}
	
	public void setColname(String tablename, String colName, String flag,
		String chinese, int xh) throws SQLException {
		int table_id=0;
		int column_id=0;
		System.out.println(tablename+"  "+colName+" "+flag+" "+chinese+" "+xh);
		pre=con.prepareStatement(getTable_idColumn_id);
		pre.setString(1, tablename);
		pre.setString(2, colName);
		result=pre.executeQuery();
		if(result.next())
		{
			table_id=result.getInt(1);
			column_id=result.getInt(2);	
		System.out.println(table_id+"  "+column_id);
		}
		else return;

		boolean isExit=hasColumn(table_id,column_id);
		if(isExit)
		{
			updateColname(table_id,column_id,flag,chinese,xh);
		}
		else 
		{
			insertColname(tablename,colName);
			updateColname(table_id,column_id,flag,chinese,xh);
		}
		
	}
	
  	public ArrayList<String> getRecord(String tableName, String colname)
  	{  		
  		ArrayList<String>set=new ArrayList<String>();
  		try {
			pre=con.prepareStatement(getRecordSql);
			pre.setString(1, tableName);
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
	//TODO 注意当用plsql 直接插入一个角色是 在这里是读不了的 只有关了plsql? 才可以
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
	//	pre=con.prepareStatement("insert into rolepermission(role_id,table_id,column_id) select r.role_id ,c.table_id,c.column_id from columnname c,role r where c.table_id=74569  and r.role_id=33 and  c.adorn_name='顾客id'");
	//	pre.executeQuery();
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
	
    public static void main(String[] args) throws SQLException {
    	DatabaseConnect db=new DatabaseConnect();
    	if(db.connect("scott", "tiger", "localhost", "orcl"))System.out.println("db connect");
   // 	db.addRolePermission(33, 64569, null);
    	System.out.println("over");
	}
   
	private static final String[] usertablename={"ROLE","ACCOUNT","ROLEACCOUNT","ROLEPERMISSION","TABLENAME","COLUMNNAME","QUERYCONDITION"};

	private static final String hasAllUserTables="select count(*) from user_objects where object_name in ('ROLE','ACCOUNT','ROLEACCOUNT','ROLEPERMISSION','TABLENAME','COLUMNNAME','QUERYCONDITION')";

	private static final String[] createUserTableSql={
			"create table role(role_id number primary key,role_name varchar2(30) not null unique)",
			"create table account(account_id number  primary key,name varchar2(30) not null UNIQUE,password varchar2(30) not null)",
			"create table roleaccount(account_id number primary key,role_id number not null)",
			"create table rolepermission(role_id number ,table_id number not null,column_id number not null)",
			"create table tablename(table_id number primary key,adorn_name varchar(30) unique,table_name varchar(30),flag char(1) default 'N')",
			"create table columnname(table_id number not null,column_id number not null,datatype varchar(106),adorn_name varchar(30),flag char(1) default 'N',no number default 1)",
			"create table querycondition(account_id number ,table_id number not null,column_id number not null,con1 varchar(30),con2 varchar(30),setname varchar(30) not null)",
			"CREATE SEQUENCE auto_add INCREMENT BY 1   START WITH 1    NOMAXVALUE  NOCYCLE ",
			"alter table columnname  add constraint FK_COLUMN_TABNAME_TABID foreign key (TABLE_ID)  references TABLENAME (TABLE_ID) on　delete　cascade"	
			};
	
	private final static String getUnadornedTableNameListSql="select object_name from user_objects where object_type in ('TABLE','VIEW') and object_name not in ('ROLE','ACCOUNT','ROLEACCOUNT','ROLEPERMISSION','TABLENAME','COLUMNNAME','QUERYCONDITION') order by object_name";

	private final static String getUnadornedColumnListSql="select column_name from user_tab_cols where table_name=?";
	
	private final static String isEixtTableName="select count(*) from tablename where table_name =?";
	
	private final static String insertTableName="insert into tablename(table_id,table_name) select object_id,object_name from user_objects where object_name=?";
	
	private final static String updateTableName="update tablename set adorn_name= ?,flag=? where table_name= ?";

	private final static String getAdornTableName="select adorn_name from tablename where table_name=?";
	
	private final static String getTable_idColumn_id="select o.OBJECT_ID,t.COLUMN_ID  from user_objects o,user_tab_cols t where o.OBJECT_NAME=? and t.COLUMN_NAME=? and o.OBJECT_NAME=t.TABLE_NAME";
	
	private final static String isEixtColumnName="select count(*)  from columnname c where  c.table_id=? and c.column_id=?";
	
	private final static String updateColName="update columnname set adorn_name= ?, flag = ? , no= ? where table_id = ? and column_id=?";
	
	private final static String insertColName=
			"insert into columnname(table_id,column_id,datatype)"
			+" select o.OBJECT_ID,t.COLUMN_ID,t.DATA_TYPE  from user_objects o,user_tab_cols t "
			+"where o.OBJECT_NAME=? and t.COLUMN_NAME=? and o.OBJECT_NAME=t.TABLE_NAME";

	private final static String getRecordSql="select c.flag,c.adorn_name,c.no from columnname c,user_objects o,user_tab_cols t where o.OBJECT_NAME=? and t.TABLE_NAME=o.OBJECT_NAME and t.COLUMN_NAME=? and c.table_id =o.OBJECT_ID and c.COLUMN_ID=t.COLUMN_ID";

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

}
