import tool.L;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;

/*
 * 使用connect 初始化  connect 因为有可能数据库登录连接失败
 * 
 * 
 */
public class DBOracle implements Database {

    private Connection con = null;


    // 全局变量以便使用


    public Boolean connect(String logname, String logpass) {
        //获取注册表中的存储
        String[] dba = RegistUtil.read();
        String jdbc = "oracle.jdbc.driver.OracleDriver";
        String url = "jdbc:oracle:" + "thin:@" + dba[3] + "/" + dba[4];
        try {
            //获取注册表中的存储
            L.d(Arrays.toString(dba));
            // 初始化Connection properties
            Class.forName(jdbc);// 加载Oracle驱动程序
            Properties common = new Properties();
            common.put("user", dba[1]);
            common.put("password", dba[2]);
            con = DriverManager.getConnection(url, dba[1], dba[2]);
            //检测所有表的完整
            if (!hasAllUserTables()) {
                dropAllUserTables();
                createAlluserTables();
                addRole("root");//增加默认root角色 此角色可以查看所有表 此角色的账号是服务端软件的登录账号
                addAccount("admin", "admin");//增加root账号 这就是默认的登录密码
                addRoleAccount("root", "admin");
            }
            //账号不可用
            if (!serverlog(logname, logpass)) return false;
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public void addRoleAccount(String role, String account) throws SQLException {
        int role_id = getRoleid(role);
        int account_id = getAccountid(account);
        insertRoleAccount(role_id, account_id);
    }

    public int addAccount(String name, String pass) {
        try {
            if (hasAccount(name)) return -1;
            int id = getAutoaddNext();
            PreparedStatement pre = null;
            pre = con.prepareStatement(addAccount);
            pre.setInt(1, id);
            pre.setString(2, name);
            pre.setString(3, pass);
            pre.executeQuery();
            pre.close();
            return id;
        } catch (SQLException e) {
            L.d("添加账号错误");
            e.printStackTrace();
            return -1;
        }

    }

    public boolean hasAccount(String account_name) {
        try {
            PreparedStatement pre = null;
            pre = con.prepareStatement(hasAccount);
            pre.setString(1, account_name);
            ResultSet result = pre.executeQuery();
            if (result.next() && result.getInt(1) == 0) {
                result.close();
                pre.close();

                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 删除账号 删除roleaccount 中的账号 删除 querycondition 中的账号
     *
     * @param account_id
     * @return
     */
    public boolean deleteAccount(int account_id) {
        try {
            PreparedStatement pre = null;
            pre = con.prepareStatement(deleteaccount);
            pre.setInt(1, account_id);
            pre.executeQuery();
            pre.close();
            con.prepareStatement("delete roleaccount where account_id=" + account_id).executeQuery().close();
            con.prepareStatement("delete querycondition where account_id=" + account_id).executeQuery().close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean serverlog(String logname, String logpass) {
        try {
            PreparedStatement pre = null;
            pre = con.prepareStatement(serverlog);
            pre.setString(1, logname);
            pre.setString(2, logpass);
            ResultSet result = pre.executeQuery();
            if (result.next() && result.getInt(1) == 1) {
                result.close();
                pre.close();
                return true;
            }
            result.close();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public void transTabletoView() {
        try {
            PreparedStatement pre = con.prepareStatement(
                    "select object_name from user_objects where object_type='TABLE' and object_name not in ('ROLE','ACCOUNT','ROLEACCOUNT','ROLEPERMISSION','TABLENAME','COLUMNNAME','QUERYCONDITION') order by object_name");
            ResultSet result = pre.executeQuery();
            while (result.next()) {
                String name = result.getString(1);
                if (!hasview(name + "_view")) {
                    createview(name);
                }
            }
            result.close();
            pre.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createview(String name) throws SQLException {
        PreparedStatement pre = null;
        pre = con.prepareStatement("create or replace view " + name + "_view"
                + " as select * from " + name);
        pre.executeQuery();
        pre.close();
    }

    public boolean hasview(String name) {
        try {
            PreparedStatement pre = null;
            pre = con.prepareStatement(hasView);
            pre.setString(1, name);
            ResultSet result = pre.executeQuery();

            if (result.next() && result.getInt(1) == 0) {
                result.close();
                pre.close();
                return false;
            }
            result.close();
            pre.close();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    //**********************************
    public void dropAllUserTables() {
        String[] usertablename = {"QUERYCONDITION", "COLUMNNAME", "TABLENAME", "ROLEPERMISSION", "ACCOUNT", "ROLEACCOUNT", "ROLE"};
        for (int i = 0; i < usertablename.length; i++) {
            try {
                PreparedStatement pre = null;
                pre = con.prepareStatement("drop table " + usertablename[i]);
                pre.executeQuery();
                pre.close();
            } catch (Exception e) {
            }//无视删除表时引起的找不到表的异常
        }
        try {
            PreparedStatement pre = null;
            pre = con.prepareStatement("drop sequence autoadd ");
            pre.executeQuery();
            pre.close();
        } catch (SQLException e) {
        }
    }

    //*****************************************************
    public void createAlluserTables() {
        String[] createUserTableSql = {
                "create table role(role_id number primary key,role_name varchar2(30) not null unique)",//角色表存放角色id 自己分配  角色名 角色名不能为空不能重复
                "create table account(account_id number  primary key,name varchar2(30) not null UNIQUE,password varchar2(30) not null)",//账号表 存放账号 id 自己分配 账号名 账号密码 账号名不能为空不能重复 密码不能为空
                "create table roleaccount(account_id number,role_id number)",// 角色 账号对应表 存放 角色id 账号id
                "create table rolepermission(role_id number ,table_id number not null,column_name varchar2(30) not null)",//角色 权限表 角色id 表id 表的列名
                "create table tablename(table_id number primary key,adorn_name varchar(30) unique,table_name varchar(30),flag char(1) default 'N')",//表名 表id 数据库分配 翻译名  不能相同  表名 是否可见 默认不可见
                "create table columnname(table_id number not null,column_name varchar2(30) not null,datatype varchar(106),adorn_name varchar(30),flag char(1) default 'N',no number default 0)",//列名 表id 列名 翻译列名 数据类型 翻译名 是否可见
                "create table querycondition(account_id number ,table_id number not null,column_name varchar2(30) not null,flag varchar(1),con1 varchar(30),con2 varchar(30),setname varchar(30) not null)",//保存的设置  账号id 表id 列名   是否被选中  查询条件1 查询条件2  设置名
                "CREATE SEQUENCE autoadd INCREMENT BY 1   START WITH 1    NOMAXVALUE  NOCYCLE "//序列 分配用
        };
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            for (int i = 0; i < createUserTableSql.length; i++) {
                stmt.addBatch(createUserTableSql[i]);
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public boolean hasAllUserTables() {
        try {
            PreparedStatement pre = con.prepareStatement(hasAllUserTables);
            ResultSet result = pre
                    .executeQuery();
            if (result.next() && result.getInt(1) == 7) {
                result.close();
                pre.close();
                return true;
            }
            result.close();
            pre.close();
            return false;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {
        DBOracle db = new DBOracle();
        db.connect("admin", "admin");
        db.createAlluserTables();
//        System.out.println(db.getUnadornViewList().size());
//        for (String s : db.getUnadornViewList()) {
//            System.out.println(s);
//        }


    }

    /**
     * 读取视图方法 只读取视图，若无视图则创建视图
     */
    //*******************************
    public ArrayList<String> getUnadornViewList() {

        ArrayList<String> tablename = new ArrayList<String>();
        try {
            transTabletoView();
            PreparedStatement pre = con.prepareStatement("select object_name from user_objects where object_type='VIEW'  order by object_name");
            ResultSet result = pre
                    .executeQuery();
            while (result.next()) {
                tablename.add(result.getString(1));
            }
            result.close();
            pre.close();
            return tablename;
        } catch (SQLException e) {
            e.printStackTrace();
            tablename.clear();
            return tablename;
        }
    }

    public ArrayList<String> getColumnList(String tablename) {
        ArrayList<String> list = new ArrayList<String>();
        try {
            PreparedStatement pre = null;
            pre = con.prepareStatement(getUnadornedColumnListSql);
            pre.setString(1, tablename);
            ResultSet result = pre.executeQuery();
            while (result.next()) {
                list.add(result.getString(1));
            }
            result.close();
            pre.close();

            return list;
        } catch (SQLException e) {
            e.printStackTrace();
            return list;
        }
    }

    // 更新表名 首先看在tablename中是否存在 不存在插入 存在更新
    // 有时候直接写sql语句感觉更方便点

    public boolean hasTable(String name) throws SQLException {
        PreparedStatement pre = null;
        pre = con.prepareStatement(isEixtTableName);
        pre.setString(1, name);
        ResultSet result = pre.executeQuery();
        boolean flag = result.next() && result.getInt(1) == 1;
        result.close();
        pre.close();

        return flag;
    }

    public boolean hasColumn(int table_id, String column_name) throws SQLException {
        PreparedStatement pre = null;
        pre = con.prepareStatement(isEixtColumnName);
        pre.setInt(1, table_id);
        pre.setString(2, column_name);
        ResultSet result = pre.executeQuery();
        boolean flag = result.next() && result.getInt(1) == 1;
        result.close();
        pre.close();

        return flag;
    }

    public void setTableName(String name, String chinese) throws SQLException {

        String haschiese = chinese.equals("") ? "N" : "Y";
        boolean isExit = hasTable(name);
        PreparedStatement pre = null;
        if (isExit) {
            pre = con.prepareStatement(updateTableName);
            pre.setString(1, chinese);
            pre.setString(2, haschiese);
            pre.setString(3, name);
            pre.executeQuery();

        } else {
            pre = con.prepareStatement(insertTableName);
            pre.setString(1, name);
            pre.executeQuery();
            pre = con.prepareStatement(updateTableName);
            pre.setString(1, chinese);
            pre.setString(2, haschiese);
            pre.setString(3, name);
            pre.executeQuery();
        }
        pre.close();
    }

    public String getAdornTableName(String onselecttable) {
        String str = "";
        try {
            PreparedStatement pre = null;

            pre = con.prepareStatement(getAdornTableName);
            pre.setString(1, onselecttable);
            pre.executeQuery();
            ResultSet result = pre.executeQuery();
            if (result.next()) {
                str = result.getString(1);
            }
            result.close();
            pre.close();

        } catch (SQLException e) {
            e.printStackTrace();
            return str;
        }
        return str;
    }

    public String getDateType(String tablename, String column_name) {
        String str = "";
        try {
            PreparedStatement pre = null;

            pre = con.prepareStatement(getDataType);
            pre.setString(1, tablename);
            pre.setString(2, column_name);
            pre.executeQuery();
            ResultSet result = pre.executeQuery();
            if (result.next()) {
                str = result.getString(1);
            }
            result.close();
            pre.close();

        } catch (SQLException e) {
            e.printStackTrace();
            return str;
        }
        return str;

    }

    /*
     * 更新columnname 到底是使用column_id 还是直接用列名 选择了用id 列名虽然不多但 毕竟有三张表使用 然而并没有卵用
     * 还是改成列名
     */
    // 根据表名列名插入tableid columnid datatype
    public void insertColname(String tablename, int table_id,
                              String column_name, String flag, String chinese, int xh) throws SQLException {
        String datatype = getDateType(tablename, column_name);
        PreparedStatement pre = null;
        pre = con.prepareStatement(insertColName);
        pre.setInt(1, table_id);
        pre.setString(2, column_name);
        pre.setString(3, chinese);
        pre.setString(4, datatype);
        pre.setString(5, flag);
        pre.setInt(6, xh);
        pre.executeQuery();
        pre.close();

    }

    public void updateColname(int table_id, String column_name, String flag,
                              String chinese, int xh) throws SQLException {
        PreparedStatement pre = null;

        pre = con.prepareStatement(updateColName);
        pre.setString(1, chinese);
        pre.setString(2, flag);
        pre.setInt(3, xh);
        pre.setInt(4, table_id);
        pre.setString(5, column_name);
        pre.executeQuery();
        pre.close();
    }

    // 插入columnname
    // 首先 获取table_id
    // 检测是否之前已经插入列名
    // 存在 直接插入
    // 不存在先插入
    public void setColname(String tablename, String colName, String flag,
                           String chinese, int xh) throws SQLException {

        int table_id = 0;
        PreparedStatement pre = null;
        pre = con.prepareStatement(getTableid);
        pre.setString(1, tablename);
        ResultSet result = pre.executeQuery();
        if (result.next()) {
            table_id = result.getInt(1);
            result.close();
        } else {
            result.close();
            return;
        }
        pre.close();

        boolean isExit = hasColumn(table_id, colName);
        if (isExit) {
            updateColname(table_id, colName, flag, chinese, xh);
        } else {
            insertColname(tablename, table_id, colName, flag, chinese, xh);
        }

    }

    public ArrayList<String> getRecord(int id, String colname) {
        ArrayList<String> set = new ArrayList<String>();
        try {
            PreparedStatement pre = null;

            pre = con.prepareStatement(getRecordSql);
            pre.setInt(1, id);
            pre.setString(2, colname);
            ResultSet result = pre.executeQuery();
            if (result.next()) {
                set.add(result.getString(1));
                set.add(result.getString(2));
                set.add(result.getString(3));
            }
            result.close();
            pre.close();

        } catch (SQLException e) {
            e.printStackTrace();
            return set;
        }
        return set;
    }

    public ArrayList<String> getAdornTablenameList() {
        ArrayList<String> list = new ArrayList<String>();
        try {
            PreparedStatement pre = null;

            pre = con.prepareStatement(getAdornTablenameList);
            ResultSet result = pre.executeQuery();
            while (result.next()) {
                list.add(result.getString(1));
            }
            pre.close();
            result.close();
            return list;
        } catch (SQLException e) {
            e.printStackTrace();
            return list;
        }
    }

    // 注意到有一些方法的结构类似 ->使用sql 传出一维的list 是否可以将其抽象为一个方法
    public ArrayList<String[]> getRoleList(int type) {

        ArrayList<String[]> list = new ArrayList<String[]>();
        try {
            String s;
            switch (type) {
                case 0:
                    s = getRoleList + " where not role_name='root'";
                    break;
                case 1:
                    s = getRoleList;
                    break;
                default:
                    s = getRoleList;
                    break;
            }
            PreparedStatement pre = null;

            pre = con.prepareStatement(s);
            ResultSet result = pre.executeQuery();
            while (result.next()) {
                String[] kv = {result.getString(1), result.getString(2)};
                list.add(kv);
            }
            result.close();
            pre.close();

            return list;
        } catch (SQLException e) {
            e.printStackTrace();
            return list;
        }
    }

    //添加角色 返回角色id 如果存在角色返回-1 其他错误返回-2
    public int addRole(String rolename) {
        try {
            boolean hasRole = hasRole(rolename);
            if (hasRole) return -1;
            int id = getAutoaddNext();
            PreparedStatement pre = null;
            pre = con.prepareStatement(addRole);
            pre.setInt(1, id);
            pre.setString(2, rolename);
            pre.executeQuery();
            pre.close();
            return id;
        } catch (SQLException e) {
            e.printStackTrace();
            return -2;
        }
    }

    public boolean hasRole(String rolename) {
        try {
            PreparedStatement pre = null;

            pre = con.prepareStatement(hasrole);
            pre.setString(1, rolename);
            ResultSet result = pre.executeQuery();
            if (result.next() && result.getInt(1) == 0) {
                result.close();
                pre.close();
                return false;
            }
            result.close();
            pre.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return true;
    }

    public void deleteRole(String rolename) {
        try {
            PreparedStatement pre = null;

            pre = con.prepareStatement(deleteRole);
            pre.setString(1, rolename);
            pre.executeQuery();
            pre.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除角色 将角色从角色表中删除
     * 将角色账号表中此角色的账号的角色id 设为-1
     *
     * @param role_id
     */
    public void deleteRole(int role_id) {
        try {

            PreparedStatement pre1 = con.prepareStatement("delete role where role_id= " + role_id);
            pre1.executeQuery().close();
            pre1.close();
            PreparedStatement pre2 = con.prepareStatement("update roleaccount set role_id=-1 where role_id= " + role_id);
            pre2.executeQuery().close();
            pre2.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int updateRole(int role_id, String name) {
        try {
            PreparedStatement pre = con.prepareStatement("update role set role_name= '" + name + "' where role_id= " + role_id);
            pre.executeQuery();
            pre.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1;
    }

    public ArrayList<String> getAdornColumnList(String tablename) {
        ArrayList<String> list = new ArrayList<String>();
        try {
            PreparedStatement pre = null;
            pre = con.prepareStatement(getAdornColumnList);
            pre.setString(1, tablename);
            ResultSet result = pre.executeQuery();
            while (result.next()) {
                list.add(result.getString(1));
            }
            result.close();
            pre.close();

            return list;
        } catch (SQLException e) {
            e.printStackTrace();
            return list;
        }
    }

    public void addRolePermission(int role_id, int table_id,
                                  ArrayList<String> list) throws SQLException {
        PreparedStatement pre = null;
        pre = con.prepareStatement(addRolePermission);
        for (int i = 0; i < list.size(); i++) {
            pre.setInt(1, role_id);
            pre.setInt(2, table_id);
            pre.setString(3, list.get(i));
            pre.executeQuery();
        }
        pre.close();
    }

    public int getRoleid(String role_name) {
        try {
            PreparedStatement pre = null;
            pre = con.prepareStatement(getRoleid);
            pre.setString(1, role_name);
            ResultSet result = pre.executeQuery();
            int i = -1;
            if (result.next()) {
                i = result.getInt(1);
            }

            result.close();
            pre.close();
            return i;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int getTableid(String table_name) throws SQLException {
        PreparedStatement pre = null;
        pre = con.prepareStatement(gettableid);
        pre.setString(1, table_name);
        ResultSet result = pre.executeQuery();
        int i = -1;
        if (result.next()) {
            i = result.getInt(1);

        }

        result.close();
        pre.close();
        return i;
    }

    public void deleteRolePermission(int role_id, int table_id) throws SQLException {
        PreparedStatement pre = null;
        pre = con.prepareStatement(deleteRolePermission);
        pre.setInt(1, role_id);
        pre.setInt(2, table_id);
        pre.executeQuery();
        pre.close();
    }

    public boolean hasRolePermission(int role_id, int table_id, String column) throws SQLException {
        PreparedStatement pre = null;
        pre = con.prepareStatement(hasRolePermission);
        pre.setInt(1, role_id);
        pre.setInt(2, table_id);
        pre.setInt(3, table_id);
        pre.setString(4, column);
        ResultSet result = pre.executeQuery();
        boolean flag = result.next() && result.getInt(1) == 1;
        result.close();
        pre.close();
        return flag;
    }

    public void deleteaccount(String role, String text) throws SQLException {
        PreparedStatement pre = null;
        pre = con.prepareStatement(deleteaccount);
        pre.setString(1, role);
        pre.setString(2, text);
        pre.executeQuery();
        pre.close();
    }


    public ArrayList<String[]> getAllAccount() {
        ArrayList<String[]> list = new ArrayList<String[]>();
        String s;
        ResultSet result;
        try {
            PreparedStatement pre = con.prepareStatement(getAllAccount);
            result = pre.executeQuery();
            while (result.next()) {
                String[] acc = new String[3];
                acc[0] = result.getString(1);
                acc[1] = result.getString(2);
                acc[2] = result.getString(3);
                list.add(acc);
            }

            result.close();
            pre.close();
            return list;
        } catch (SQLException e) {
            e.printStackTrace();
            return list;
        }
    }


    public void insertRoleAccount(int role_id, int account_id) throws SQLException {
        PreparedStatement pre = null;
        pre = con.prepareStatement(insertRoleAccount);
        pre.setInt(1, account_id);
        pre.setInt(2, role_id);
        pre.executeQuery();
        pre.close();
    }

    public void updateAccount(int account_id, String name, String pass) {

        try {
            PreparedStatement pre = null;

            pre = con.prepareStatement(updateAccount);

            pre.setString(1, name);
            pre.setString(2, pass);
            pre.setInt(3, account_id);
            pre.executeQuery();
            pre.close();
        } catch (SQLException e) {
            L.d("更新账号名错误");
            e.printStackTrace();
        }
    }

    public HashMap<String, String> getAccountByRole(int role_id) {
        HashMap<String, String> list = new HashMap<String, String>();
        try {
            PreparedStatement pre = null;
            pre = con.prepareStatement(getAccountByRoleid);
            pre.setInt(1, role_id);
            ResultSet result = pre.executeQuery();
            while (result.next()) {

                list.put(result.getString(2), "");
                String[] acc = new String[3];
                acc[0] = result.getString(1);
                acc[1] = result.getString(2);
                acc[2] = result.getString(3);
            }

            result.close();
            pre.close();
            return list;
        } catch (SQLException e) {
            e.printStackTrace();
            return list;
        }

    }

    public int getAccountid(String name) {

        try {
            PreparedStatement pre = null;
            pre = con.prepareStatement(getAccountid);
            pre.setString(1, name);
            ResultSet result = pre.executeQuery();
            int i = -1;
            if (result.next()) {
                i = result.getInt(1);
            }

            result.close();
            pre.close();
            return i;
        } catch (SQLException e) {
            L.d("获取账号id 错误");
            e.printStackTrace();
        }
        return -1;
    }

    public int getidByunadornname(String onselecttable) {

        try {
            PreparedStatement pre = null;

            pre = con.prepareStatement(getidByunadornname);
            pre.setString(1, onselecttable);

            ResultSet result = pre.executeQuery();
            int i = -1;
            if (result.next()) {
                i = result.getInt(1);

            }

            result.close();
            pre.close();
            return i;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public void deleteRoleAccount(int role_id) {
        try {
            PreparedStatement pre = null;
            pre = con.prepareStatement(deleRoleAccount);
            pre.setInt(1, role_id);
            pre.executeQuery();
            pre.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addRoleAccount(int role_id, int accountId) {
        try {
            PreparedStatement pre = null;

            pre = con.prepareStatement(addRoleAccount);
            pre.setInt(1, role_id);
            pre.setInt(2, accountId);
            pre.executeQuery();
            pre.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取序列
     *
     * @return
     * @throws SQLException
     */
    public int getAutoaddNext() throws SQLException {
        PreparedStatement pre = con.prepareStatement(getAutoaddNext);
        ResultSet result = pre.executeQuery();
        result.next();
        int i = result.getInt(1);

        result.close();
        pre.close();

        return i;
    }

    private static final String hasAllUserTables = "select count(*) from user_objects where object_name in ('ROLE','ACCOUNT','ROLEACCOUNT','ROLEPERMISSION','TABLENAME','COLUMNNAME','QUERYCONDITION')";


    private final static String getUnadornedColumnListSql = "select column_name from user_tab_cols where table_name=?";

    private final static String isEixtTableName = "select count(*) from tablename where table_name =?";

    private final static String insertTableName = "insert into tablename(table_id,table_name) select object_id,object_name from user_objects where object_name=?";

    private final static String updateTableName = "update tablename set adorn_name= ?,flag=? where table_name= ?";

    private final static String getAdornTableName = "select adorn_name from tablename where table_name=?";

    private final static String getTableid = "select o.OBJECT_ID from user_objects o where o.OBJECT_NAME=? ";

    private final static String isEixtColumnName = "select count(*)  from columnname c where  c.table_id=? and c.column_name=?";

    private final static String updateColName = "update columnname set adorn_name= ?, flag = ? , no= ? where table_id = ? and column_name=?";

    private final static String insertColName = "insert into columnname(table_id,column_name,adorn_name,datatype,flag,no) values(?,?,?,?,?,?)";

    private final static String getRecordSql = "select c.flag,c.adorn_name,c.no from columnname c where c.table_id =? and c.column_name=?";

    private final static String getAdornTablenameList = "select adorn_name from tablename where flag='Y'";

    private final static String getRoleList = "select role_id,role_name from role";

    private final static String addRole = "insert into role values(?,?)";

    private final static String deleteRole = "delete role where role_name=?";

    private final static String getAdornColumnList = "select adorn_name from columnname where flag='Y' and table_id in (select table_id from tablename where adorn_name=?)";

    private final static String getRoleid = "select role_id from role where role_name=?";

    private final static String hasrole = "select count(*) from role where role_name=?";

    private final static String hasAccount = "select count(*) from account where name=?";

    private final static String deleAccount = "delete from account  where account_id=?";

    private final static String gettableid = "select table_id from tablename where adorn_name=?";

    private final static String addRolePermission = "insert into rolepermission(role_id,table_id,column_name) select r.role_id ,c.table_id,c.column_name from columnname c,role r  where r.role_id=? and  c.table_id=? and  c.adorn_name=?";

    private final static String deleteRolePermission = "delete rolepermission where role_id =? and  table_id=?";

    private final static String hasRolePermission = "select count(*) from rolepermission r where r.role_id=? and r.table_id=? and r.column_name in (select t.column_name from columnname t where t.table_id=? and t.adorn_name=?)";

    private final static String getAllAccount = "select a.account_id, a.name,a.password  from account a";

    private final static String deleteaccount = "delete account where account_id=?";

    private final static String getAccountid = "select account_id from account where name=?";

    private final static String getAutoaddNext = "select autoadd.nextval as id from dual";

    private final static String updateAccount = "update account set name=?,password=? where account_id=?";

    private final static String updateRoleAccount = "update roleaccount set role_id=? where account_id=?";

    private final static String insertRoleAccount = "insert into roleaccount values(?,?)";

    private final static String addAccount = "insert into account(account_id,name,password) values(?,?,?)";

    private final static String getAccountByRoleid = "select a.account_id ,a.name,a.password  from account a where a.account_id in (select account_id from roleaccount where role_id =?)";

    private final static String hasView = "select count(*) from user_objects where object_name=?";

    private final static String createView = "create or replace view ? as select * from ?";

    private final static String getidByunadornname = "select object_id from user_objects where object_name=? ";

    private final static String getDataType = "select data_type from user_tab_cols where table_name=? and column_name=? ";

    private final static String serverlog = "select count(*) from account a,roleaccount ra,role r where a.name=? and a.password=? and a.account_id=ra.account_id and ra.role_id=r.role_id  and r.role_name='root'";

    private final static String deleRoleAccount = "delete roleaccount where role_id=?";

    private final static String addRoleAccount = "insert into roleaccount(role_id,account_id) values(?,?)";


}