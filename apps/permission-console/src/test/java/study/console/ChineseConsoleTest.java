package study.console;
import org.junit.Test;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import static org.junit.Assert.*;
public class ChineseConsoleTest {
    private String play(String script)throws Exception{
        try(Database db=Database.demo();Commands commands=new Commands(db)){
            ByteArrayOutputStream bytes=new ByteArrayOutputStream();
            try(PrintStream out=new PrintStream(bytes,true,StandardCharsets.UTF_8)){
                new ChineseMenus(commands,new BufferedReader(new StringReader(script)),out).run();
            }return bytes.toString(StandardCharsets.UTF_8);
        }
    }
    @Test public void multiTableSalesHasChineseJoinedColumns()throws Exception{
        String screen=play("1\n7\n\n\n\n0\n0\n");
        assertTrue(screen.contains("多表联查"));assertTrue(screen.contains("关联链"));
        for(String word:List.of("销售编号","商品名称","供应商名称","客户名称","晨曦设备","东湖研究室"))assertTrue(word,screen.contains(word));
        assertFalse(screen,screen.contains("操作失败"));assertFalse(screen.contains("\"saleId\""));
    }
    @Test public void departmentDetailDisplaysMembers()throws Exception{
        String screen=play("1\n1\n1101\n\n0\n0\n");
        for(String word:List.of("技术开发组","部门员工","员工姓名","基本工资","顾清和"))assertTrue(word,screen.contains(word));assertFalse(screen,screen.contains("操作失败"));
    }
    @Test public void everyBusinessMenuIsReachable()throws Exception{
        String screen=play("2\n0\n3\n0\n4\n0\n5\n0\n6\n0\n7\n0\n8\n0\n0\n");
        for(String word:List.of("新增用户","给用户分配角色","注册账号","供应商的售后客户","录入学生","按条件批量调整分数","通过用户接口新增用户","重置销售演示数据"))assertTrue(word,screen.contains(word));assertFalse(screen,screen.contains("操作失败"));
    }
    @Test public void renderingMasksSecretsAndAlignsCjk(){
        var bytes=new ByteArrayOutputStream();var out=new PrintStream(bytes,true,StandardCharsets.UTF_8);
        new ChineseDisplay(out).show("账号",List.of(Map.of("loginName","测试账号","secret","NeverShow")));
        String text=bytes.toString(StandardCharsets.UTF_8);assertTrue(text.contains("登录名"));assertTrue(text.contains("******"));assertFalse(text.contains("NeverShow"));assertEquals(8,ChineseDisplay.displayWidth("测试账号"));
        assertEquals(List.of("org-hire","两 字姓名","1101"),Launcher.split("org-hire '两 字姓名' 1101"));
        assertThrows(IllegalArgumentException.class,()->Launcher.split("org-hire '未闭合"));
    }
    @Test public void liveIdsExplainMissingDepartmentAndEmployee()throws Exception{
        String screen=play("1\n2\n1\n4\n10\n2\n1101\n4\n7001\n0\n0\n");
        assertTrue(screen.contains("菜单序号不是数据编号"));
        assertTrue(screen.contains("1101（技术开发组）"));assertTrue(screen.contains("7001（顾清和）"));
        assertTrue(screen.contains("未找到部门编号 1 对应的记录"));assertTrue(screen.contains("未找到员工编号 10 对应的记录"));
        assertTrue(screen.contains("部门员工"));assertFalse(screen,screen.contains("操作失败"));
    }
}
