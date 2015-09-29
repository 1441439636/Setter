import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

//为数据库操作提供方法
//初始化时连接数据库  url password user 先写死
//初始化con
//服务器
public class DatabaseConnect {
	
	Connection con = null;// 创建一个数据库连接
    PreparedStatement pre = null;// 创建预编译语句对象，一般都是用这个而不用Statement
    ResultSet result = null;// 创建一个结果集对象
    
    
    public Boolean connect(String user,String password,String address,String databasename){    	
    	try {
    		Class.forName("oracle.jdbc.driver.OracleDriver");// 加载Oracle驱动程序
    	    String url = "jdbc:oracle:" + "thin:@"+address+"/"+databasename;// 127.0.0.1是本机地址， orcl是你的数据库名        

    	    Properties common = new Properties();
    	    common.put("user", user);
    	    common.put("password",password);

			con=DriverManager.getConnection(url, common);
		
			//没有表 全部删掉 再重建
			if(!con.prepareStatement("select table_name from user_tables where table_name='TABLENAME'").executeQuery().next())
	    	{
				try{			
				dropTable(new String[]{"role","tablename","colname","account"});
	    		createTablenameTable();
	        	createColnameTable();
	        	createAccountTable();
	        	createRoleTable();
				}
				catch(Exception e)
				{
					//don't care;
				}
	    	}
		} catch (Exception e) {
			return false;
		}
    	return true;
    }
    
    
    private void dropTable(String[] s) throws SQLException
    {
    	String sql="drop table ";
    	for(int i=0;i<s.length;i++)
    	{
    		pre=con.prepareStatement(sql+"'"+s[i]+"'");
    		pre.executeQuery();
    		if(pre!=null)pre.close();
    	}
    }
    
  	  //关闭数据库的连接
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
	
  	public Vector<String> getTableNameList() throws Exception
  	{  		
    	result= con.prepareStatement(getTableNameSql).executeQuery();
    	Vector<String>v=new Vector<String>();
    	while(result.next())
    	{
    		v.add(result.getString(1));
    	}
    	return v;
 	}

  	public ResultSet getRecord(String tableName) throws Exception
  	{  		
  		pre=con.prepareStatement(getRecordSql);
  		pre.setString(1, tableName);
    	return pre.executeQuery();
 	}
  	public String getChineseTableName(String tabName) throws SQLException
  	{
  		String str="";
  		pre=con.prepareStatement(getTableNameChineseSql+"'"+tabName+"'");
  		 		
  		ResultSet rs=pre.executeQuery();
  		if(rs.next())
  		{
  			str=rs.getString(1);
  		}
		if(pre!=null)pre.close();
  		return str;
  
  	}
  	
  	public void updateTableName(String name,String chinese) throws SQLException
  	{
  		pre=con.prepareStatement(updateTableName);
  		pre.setString(1, chinese);
  		pre.setString(2, name);
  		pre.executeQuery();
		if(pre!=null)pre.close();
  	}
  	
  	public void updateColName(String chinese,boolean flag,String no,String tablename) throws SQLException
  	{
  		String fla="N";
  		if(flag)fla="Y";
  		int n=no.length()==0?0:Integer.parseInt(no);
  		pre=con.prepareStatement(updateColName);
  		pre.setString(1, chinese);
  		pre.setString(2, fla);
  		pre.setInt(3,n);
  		pre.setString(4, tablename);
  		pre.executeQuery();
		if(pre!=null)pre.close();
  	}

   
    public void createTablenameTable() throws SQLException
    {
    	pre=con.prepareStatement(creatTableNameSql);
    	pre.executeQuery();
    	pre=con.prepareStatement(insertTableNameSql);
    	pre.executeQuery();		
    	if(pre!=null)pre.close();
    	
    }

    public void createColnameTable() throws SQLException
    {
    	con.prepareStatement(creatColNameTableSql).executeQuery();
    	con.prepareStatement(insertColNameTableSql).executeQuery();
    }
    
    public void createRoleTable() throws SQLException
    {
    	con.prepareStatement(creatRoleTableSql).executeQuery();
    	
    }
    public void createAccountTable() throws SQLException
    {
    	con.prepareStatement(creatAccountSql).executeQuery();
    }

    public void addRole(String rolename,Vector<String>v) throws SQLException
    {
    	pre=con.prepareStatement(insertintoRoleTableSql);
    	for(int i=0;i<v.size();i++)
    	{
      		pre.setString(1, rolename);
      		pre.setString(2, v.get(i));
      		pre.executeQuery();
    	}
		if(pre!=null)pre.close();
    }
    
    public void addaccount(String rolename,String name,String password) throws SQLException
    {
    	//补丁 要改
    	pre=con.prepareStatement("select role_name_id from role where role_name="+"'"+rolename+"'"); 	
    	result=pre.executeQuery();
    	String role_name_id = "1";
    	if(result.next())
    	{
    		role_name_id=result.getString(1);
    	
    	pre=con.prepareStatement(insertintoAccountTableSql); 	
      	pre.setString(1, role_name_id);
      	pre.setString(2,name);
      	pre.setString(3,password);
      	pre.executeQuery();
		if(pre!=null)pre.close();
    	}
    }
	public Vector<String> getRole() throws SQLException {
		Vector<String> v=new Vector<String>();
		result=con.prepareStatement(getRoleListSql).executeQuery();
		while(result.next())
		{
			v.add(result.getString(1));
		}
		return v;
	}
	public Vector<String> getRolePermission(String rolename) throws SQLException {
		Vector<String> v=new Vector<String>();
		pre=con.prepareStatement(getRolePermissionSql); 	
      	pre.setString(1, rolename);      
		result=pre.executeQuery();		
		while(result.next())
		{
			v.add(result.getString(1));
		}
		if(pre!=null)pre.close();
		return v;
	}
	public void insertRole(String s) throws SQLException {
		pre=con.prepareStatement(insertintoRoleTableSql);
		pre.setString(1, s);
		pre.executeQuery();
		if(pre!=null)pre.close();
	}


	public void insertRolepermission(String s, String name) throws SQLException {
		
		pre=con.prepareStatement(insertintoRolePermissionTableSql);
		pre.setString(1, s);
		pre.setString(2, name);
		pre.executeQuery();
		if(pre!=null)pre.close();
	}


	public void deleteRole(String role) throws SQLException {
		//我记得可以设置主键约束 还是其他的什么  这样删除只要删一个就好了 

		deleteRolePermission(role);
		
		pre=con.prepareStatement(dropRoleAccountSql);
		pre.setString(1, role);
		pre.setString(2, role);
		pre.executeQuery();
		pre=con.prepareStatement(dropRoleSql);
		pre.setString(1, role);
		pre.executeQuery();	
		if(pre!=null)pre.close();
	}
	public void deleteRolePermission(String role) throws SQLException {
		pre=con.prepareStatement(dropRolePermissionSql);
		pre.setString(1, role);
		pre.setString(2, role);
		pre.executeQuery();
		if(pre!=null)pre.close();
	}
    
	private final static String getTableNameSql="select name from tablename";
	
	private final static String getTableNameChineseSql="select chinese from tablename where name=";

	private final static String creatTableNameSql=	"create table tablename (name varchar(30),id number ,chinese varchar(20) default '',flag char(1) default 'N')";
	private final static String insertTableNameSql="insert into tablename(name,id) "
			+"Select OBJECT_NAME,OBJECT_ID  from user_objects WHERE OBJECT_TYPE IN ('TABLE','VIEW') "
			+"AND not OBJECT_NAME  in ('TABLENAME','COLNAME','QUERYCONDITION')";
	
	private final static String creatRoleTableSql="create table role(rol_name varchar(20),role_name_id number)";
	private final static String insertintoRoleTableSql="insert into role values(?,autoadd.nextval)";
	private final static String dropRoleSql="delete from role where role_name=?";
	
	private final static String creatRolePermissionTableSql="create table rolepermission(role_name_id number,table_name_id)";
	private final static String insertintoRolePermissionTableSql="insert into rolepermission(role_name_id,table_name_id) "
			+ "select role.role_name_id,tablename.id "
			+"from role,tablename "
			+"where role.role_name=? and tablename.name=?";
	private final static String dropRolePermissionSql=
			"declare "
			+"i integer; "
			+"begin "
			+"select count(*) into i from role where role_name=?; "
			+"if i > 0 then "
			+"delete from rolepermission where rolepermission.role_name_id in (select role_name_id from role where role.role_name=?); "
			+"end if; "
			+"end;";
		
	private final static String creatAccountSql="create table account(role_name_id number,account varchar(20),password varchar(20))";
	private final static String insertintoAccountTableSql="insert into account (role_name_id,account,password)values(?,?,?)";;
	private final static String creatColNameTableSql="create table colname(col_name varchar(20),table_name varchar(20),chinese varchar(20) default '',type varchar(20),flag char(1) default 'N',no INTEGER default 0 )";
	private final static String insertColNameTableSql="insert into colname(col_name,table_name,type) "
			+"select t.COLUMN_NAME,t.TABLE_NAME,t.DATA_TYPE "
			+"from user_tab_columns t,user_col_comments c  "
			+"where t.table_name = c.table_name and t.column_name = c.column_name and not t.TABLE_NAME  in ('TABLENAME','COLNAME','QUERYCONDITION')";
	private final static String updateColName="update colname set chinese= ?, flag = ? , no= ? where col_name = ?";
	private final static String updateTableName="update tablename set chinese= ? where name= ?";
	private final static String getRecordSql="select flag,col_name,chinese,no from colname where table_name = ?";
	private final static String getRoleListSql="select role_name from role";
	
	private final static String getRolePermissionSql="select tablename.chinese from tablename where id in "
				+"(select ROLEPERMISSION.TABLE_NAME_ID from ROLEPERMISSION where ROLEPERMISSION.Role_Name_Id in"
				+"(select role.role_name_id from role where role.rol_name=?))";
		
	private final static String createAutoAddSql="create sequence autoadd start with 1 in crement by 1";
	private final static String DeleteAutoAddSql="drop sequence autoadd";
    private static 	String createqueryconditionSql="create table querycondition (tablename varchar(20),colname varchar(20),focus char(1),con1 varchar(25),con2 varchar(25),setname varchar(20))";


    private static 	String dropRoleAccountSql=	
    		"declare "
			+"i integer; "
			+"begin "
			+"select count(*) into i from role where role_name=?; "
			+"if i > 0 then "
			+"delete from account where account.role_name_id in (select role_name_id from role where role.role_name=?); "
			+"end if; "
			+"end;";
	











}
