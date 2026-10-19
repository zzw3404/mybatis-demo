package study.console;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.*;
import java.io.PrintStream;
/** Human-oriented tables and nested relationship sections, shared by menu and one-shot commands. */
public final class ChineseDisplay {
    private final PrintStream out;
    private static final Map<String,String> WORDS=new HashMap<>();
    static {
        String[][] entries={
            {"id","编号"},{"username","用户名"},{"password","密码"},{"realName","姓名"},{"email","邮箱"},{"phone","电话"},{"status","状态"},
            {"name","名称"},{"description","说明"},{"parentId","父权限编号"},{"code","权限代码"},{"type","权限类型"},{"url","接口路径"},{"sort","排序"},{"children","下级权限"},{"roles","所属角色"},{"permissions","拥有权限"},
            {"deptId","部门编号"},{"deptName","部门名称"},{"deptCode","部门代码"},{"city","城市"},{"costCenter","成本中心"},{"members","部门员工"},{"staffCount","员工人数"},{"totalPay","薪酬合计"},{"accountCount","账号数量"},
            {"staffId","员工编号"},{"staffNo","工号"},{"staffName","员工姓名"},{"post","岗位"},{"mentorId","上级编号"},{"hiredOn","入职日期"},{"basePay","基本工资（sal）"},{"meritPay","佣金（comm）"},{"onJob","在职状态"},{"department","所属部门"},
            {"accountId","账号编号"},{"loginName","登录名"},{"secret","口令"},{"mailbox","联系邮箱"},{"state","状态"},{"createdAt","创建时间"},{"updatedAt","更新时间"},
            {"supplierId","供应商编号"},{"supplierName","供应商名称"},{"productId","商品编号"},{"productName","商品名称"},{"customerId","客户编号"},{"customerName","客户名称"},{"saleId","销售编号"},{"itemId","实物编号"},{"serialNo","设备序列号"},{"salePrice","成交金额"},{"soldOn","销售日期"},{"serviceDate","服务日期"},{"quantity","采购数量"},{"products","已售商品"},{"customers","购买客户"},{"services","售后服务"},{"title","商品名称"},{"label","名称"},{"price","单价"},
            {"name","姓名或名称"},{"age","年龄"},{"major","专业"},{"score","分数"},{"version","版本号"},{"deleted","删除标记"},{"createTime","创建时间"},{"updateTime","更新时间"},{"minimum","最低分"},{"maximum","最高分"},{"average","平均分"},
            {"records","本页学生"},{"total","总数量"},{"size","每页条数"},{"current","当前页"},{"pages","总页数"},{"orders","排序规则"},{"sql","生成的SQL"},{"data","查询结果"},
            {"allowed","是否有权限"},{"authenticated","登录是否成功"},{"equal","查询结果是否一致"},{"columnQuery","字符串列名查询"},{"lambdaQuery","Lambda查询"},{"columns","字符串列名查询"},{"lambda","Lambda查询"},{"slim","部分字段查询"},{"maps","Map结果"},{"groups","专业统计"},
            {"staff_total","员工总数"},{"on_job_total","在职人数"},{"pay_total","薪酬合计"},{"pay_avg","平均薪酬"},{"pay_min","最低薪酬"},{"pay_max","最高薪酬"},
            {"inserted","新增行数"},{"updated","更新行数"},{"removed","删除行数"},{"rolledBack","是否已回滚"},{"staleUpdated","过期版本更新行数"},{"deletedFlag","删除标记"},{"generatedTime","自动填充时间"},{"matched","批量更新行数"},{"preservedScore","空值策略保留分数"},{"explicitNull","显式置空是否生效"},{"duplicateRejected","重名单条查询是否被拒绝"},{"count","匹配数量"},{"result","匹配结果"},
            {"organization","组织人事"},{"sales","销售关联"},{"students","学生信息"},{"suppliers","供应商"},{"error","错误"},{"httpStatus","HTTP状态码"},{"body","接口结果"}
        };for(String[] pair:entries){WORDS.put(pair[0],pair[1]);WORDS.put(pair[0].toLowerCase(Locale.ROOT),pair[1]);}
    }
    public ChineseDisplay(PrintStream out){this.out=out;}
    public void show(String title,Object value){out.println("\n【"+title+"】");if(value==null){out.println("未找到记录。");return;}if(value instanceof String text){out.println(text);return;}render(JsonCodec.MAPPER.valueToTree(value),"");}
    static String label(String key){return WORDS.getOrDefault(key,WORDS.getOrDefault(key.toLowerCase(Locale.ROOT),key));}
    private static final Set<String> HIDDEN=Set.of("orders","optimizeCountSql","searchCount","optimizeJoinOfCountSql","countId","maxLimit");
    private void render(JsonNode node,String prefix){
        if(node==null||node.isNull()){out.println(prefix+"无记录");return;}
        if(node.isArray()){
            if(node.isEmpty()){out.println(prefix+"没有符合条件的数据。");return;}
            if(node.get(0).isObject())table(node,prefix);else{for(JsonNode item:node)out.println(prefix+"  · "+cell(item,""));}
            return;
        }
        if(node.isObject()){
            node.fields().forEachRemaining(entry->{if(!HIDDEN.contains(entry.getKey())&&!entry.getValue().isContainerNode())out.println(prefix+label(entry.getKey())+"："+cell(entry.getValue(),entry.getKey()));});
            node.fields().forEachRemaining(entry->{if(!HIDDEN.contains(entry.getKey())&&entry.getValue().isContainerNode() && !entry.getValue().isEmpty()){
                out.println(prefix+"── "+label(entry.getKey())+" ──");render(entry.getValue(),prefix+"  ");}});return;
        }
        out.println(prefix+cell(node,""));
    }
    private void table(JsonNode rows,String prefix){
        LinkedHashSet<String> keys=new LinkedHashSet<>();for(JsonNode row:rows)row.fieldNames().forEachRemaining(k->{if(!row.get(k).isContainerNode())keys.add(k);});
        List<String> order=List.of("saleId","itemId","staffId","deptId","accountId","id","serialNo","staffNo","staffName","name","username","loginName","deptName","productId","productName","supplierId","supplierName","customerId","customerName","post","age","major","score","salePrice","soldOn");
        List<String> columns=new ArrayList<>(keys);columns.sort(Comparator.comparingInt(k->{int index=order.indexOf(k);return index<0?100:index;}));if(columns.isEmpty()){for(JsonNode row:rows)render(row,prefix);return;}
        int[] widths=new int[columns.size()];for(int i=0;i<widths.length;i++){String key=columns.get(i);widths[i]=displayWidth(label(key));for(JsonNode row:rows)widths[i]=Math.min(28,Math.max(widths[i],displayWidth(cell(row.get(key),key))));}
        List<List<Integer>> groups=new ArrayList<>();List<Integer> group=new ArrayList<>();group.add(0);int used=widths[0]+5;
        for(int i=1;i<columns.size();i++){
            if(used+widths[i]+3>110 && group.size()>1){groups.add(group);group=new ArrayList<>();group.add(0);used=widths[0]+5;}
            group.add(i);used+=widths[i]+3;
        }
        groups.add(group);
        for(int g=0;g<groups.size();g++){
            List<Integer> positions=groups.get(g);List<String> visible=positions.stream().map(columns::get).toList();int[] sizes=positions.stream().mapToInt(i->widths[i]).toArray();
            if(groups.size()>1)out.println(prefix+"字段组 "+(g+1)+" / "+groups.size()+"（按"+label(columns.get(0))+"对应同一记录）");
            line(prefix,visible.stream().map(ChineseDisplay::label).toList(),sizes);
            line(prefix,Arrays.stream(sizes).mapToObj(w->"─".repeat(w)).toList(),sizes);
            for(JsonNode row:rows)line(prefix,visible.stream().map(k->cell(row.get(k),k)).toList(),sizes);
        }
        out.println(prefix+"共 "+rows.size()+" 条记录。");
        for(JsonNode row:rows)row.fields().forEachRemaining(entry->{if(entry.getValue().isContainerNode()&&!entry.getValue().isEmpty()){
            String identity=row.has("staffName")?row.get("staffName").asText():row.has("name")?row.get("name").asText():row.has("id")?row.get("id").asText():"";
            out.println(prefix+"── "+identity+" 的"+label(entry.getKey())+" ──");render(entry.getValue(),prefix+"  ");}});
    }
    private void line(String prefix,List<String> cells,int[] widths){StringBuilder row=new StringBuilder(prefix+"| ");for(int i=0;i<cells.size();i++){String text=cut(cells.get(i),widths[i]);row.append(text).append(" ".repeat(Math.max(0,widths[i]-displayWidth(text)))).append(" | ");}out.println(row);}
    private static String cell(JsonNode n,String key){if(n==null||n.isNull())return "—";if(key.equals("password")||key.equals("secret"))return "******";if(n.isBoolean())return n.asBoolean()?"是":"否";
        if(n.isNumber()){
            if(Set.of("salePrice","price","basePay","meritPay","totalPay","pay_total","pay_avg","pay_min","pay_max").contains(key))return n.decimalValue().setScale(2,java.math.RoundingMode.HALF_UP).toPlainString();
            return n.decimalValue().stripTrailingZeros().toPlainString();
        }
        return n.asText().replace('\n',' ');}
    static int displayWidth(String text){return text.codePoints().map(c->c>=0x2E80?2:1).sum();}
    private static String cut(String text,int width){if(displayWidth(text)<=width)return text;StringBuilder b=new StringBuilder();int used=0;for(int c:text.codePoints().toArray()){int size=c>=0x2E80?2:1;if(used+size>width-1)break;b.appendCodePoint(c);used+=size;}return b+"…";}
}
