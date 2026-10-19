package study.console;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import java.sql.*;
import java.io.Reader;
import java.util.*;
/** Initialization only seeds tables which did not exist before startup. Existing rows are never reset. */
final class ProjectData {
    private ProjectData() { }
    static Set<String> existing(PooledDataSource source)throws SQLException{
        Set<String> tables=new HashSet<>();try(Connection c=source.getConnection();ResultSet r=c.getMetaData().getTables(c.getCatalog(),null,"%",new String[]{"TABLE"})){while(r.next())tables.add(r.getString("TABLE_NAME").toLowerCase(Locale.ROOT));}return tables;
    }
    static void initialize(Database db,PooledDataSource source,Set<String> before)throws Exception{
        if(!before.contains("acl_user"))db.runScript("database/project-access-seed.sql");
        try(Connection c=source.getConnection()){
            c.setAutoCommit(false);
            if(!before.contains("dept")){
                String[] names={"财务管理部","研发技术部","市场运营部","综合保障部"};
                try(PreparedStatement p=c.prepareStatement("INSERT INTO dept(deptno,dname,loc,deleted) VALUES(?,?,?,0)")){
                    for(int i=0;i<names.length;i++){p.setInt(1,i+1);p.setString(2,names[i]);p.setString(3,new String[]{"上海","杭州","深圳","成都"}[i]);p.executeUpdate();}
                }
            }
            if(!before.contains("emp")){
                List<Integer> departments=new ArrayList<>();try(Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT deptno FROM dept WHERE deleted=0 ORDER BY deptno")){while(r.next())departments.add(r.getInt(1));}
                if(!departments.isEmpty())try(PreparedStatement p=c.prepareStatement("INSERT INTO emp(empno,ename,job,hiredate,sal,comm,deptno,deleted) VALUES(?,?,?,CURRENT_DATE,?,?,?,0)")){
                    String[] names={"顾清和","沈雨桐","江亦辰","许言希","苏予安","林书瑶","程墨","陆嘉宁","唐以诺","叶知夏","陈沐白","温景澄","邵安然","季晨曦"};
                    for(int i=0;i<names.length;i++){p.setInt(1,i+1);p.setString(2,names[i]);p.setString(3,i%3==0?"开发工程师":"业务专员");p.setBigDecimal(4,java.math.BigDecimal.valueOf(8000+i*300));p.setBigDecimal(5,java.math.BigDecimal.ZERO);p.setInt(6,departments.get(i%departments.size()));p.executeUpdate();}
                }
            }
            if(!before.contains("login_account")){
                try(Statement s=c.createStatement();ResultSet rows=s.executeQuery("SELECT empno FROM emp WHERE deleted=0 ORDER BY empno LIMIT 10");PreparedStatement p=c.prepareStatement("INSERT INTO login_account(login_name,secret,mailbox,staff_id,state) VALUES(?,?,?,?,1)")){
                    while(rows.next()){int id=rows.getInt(1);p.setString(1,"staff"+id);p.setString(2,"Demo@123");p.setString(3,"staff"+id+"@example.com");p.setInt(4,id);p.executeUpdate();}
                }
            }
            if(!before.contains("supplier")&&!before.contains("product")&&!before.contains("customer")){
                try(Statement s=c.createStatement()){
                    s.executeUpdate("INSERT INTO supplier(supplier_id,supplier_name) VALUES(1,'晨曦设备'),(2,'远山电子'),(3,'青禾服务')");
                    s.executeUpdate("INSERT INTO product(product_id,product_name,unit_price) VALUES(1,'开发工作站',6800),(2,'办公显示器',1800),(3,'网络设备',900)");
                    s.executeUpdate("INSERT INTO customer(customer_id,customer_name,address) VALUES(1,'东湖研究室','武汉'),(2,'西岭设计院','成都'),(3,'海湾培训中心','宁波')");
                    s.executeUpdate("INSERT INTO supplier_product(supplier_id,product_id,supply_price) VALUES(1,1,6000),(1,2,1500),(2,1,6100),(2,3,700),(3,2,1600)");
                    s.executeUpdate("INSERT INTO purchase(purchase_id,customer_id,supplier_id,product_id,quantity,unit_price) VALUES(1,1,1,1,2,6500),(2,2,2,1,1,6700),(3,2,1,2,2,1700)");
                    s.executeUpdate("INSERT INTO product_service(service_id,purchase_id,service_type,service_note) VALUES(1,1,'设备配置','协助设置网络'),(2,2,'现场升级','完成系统更新')");
                }
            }
            c.commit();
        }
        if(!before.contains("mp_student"))db.runScript("database/project-students-seed.sql");
        syncItems(source);
    }
    private static void syncItems(PooledDataSource source)throws SQLException{
        try(Connection c=source.getConnection();Statement query=c.createStatement();ResultSet rows=query.executeQuery("SELECT purchase_id,product_id,supplier_id,quantity FROM purchase");PreparedStatement p=c.prepareStatement("INSERT INTO console_product_item(product_id,supplier_id,serial_no,purchase_id) SELECT ?,?,?,? WHERE NOT EXISTS(SELECT 1 FROM console_product_item WHERE serial_no=?)")){
            c.setAutoCommit(false);
            while(rows.next())for(int item=1;item<=rows.getInt("quantity");item++){
                String serial="P"+rows.getInt("purchase_id")+"-"+item;
                p.setInt(1,rows.getInt("product_id"));p.setInt(2,rows.getInt("supplier_id"));p.setString(3,serial);p.setInt(4,rows.getInt("purchase_id"));p.setString(5,serial);p.executeUpdate();
            }c.commit();
        }
    }
}