/**
 * Created by ZLS on 2017/5/9.
 */
public class Mail {
    public static void main(String[] args) {
        DBMySql db = new DBMySql();
//        for (String s : db.getColumnList("orders")) {
//            System.out.println("-----           " + s);
//        }
        System.out.println(db.getDataType("orders_view","cust_id"));
    }

}
