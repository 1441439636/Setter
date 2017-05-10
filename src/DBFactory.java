/**
 * Created by ZLS on 2017/5/8.
 */
public class DBFactory {
    public Database createDB(String dbName) {
        switch (dbName) {
            case "SqlServer":
                System.out.println("SqlServer=>");
                return new DBSqlServer();
            case "MySql":
                System.out.println("MySql=>");
                return new DBMySql();
            case "Oracle":
                System.out.println("Oracle=>");
                return new DBOracle();
        }
        return null;
    }
}
