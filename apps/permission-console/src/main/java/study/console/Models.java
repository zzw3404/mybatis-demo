package study.console;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
public final class Models {
    private Models() { }
    public static class Team {
        public Long deptId;
        public String deptCode, deptName, city, costCenter;
        public List<Person> members = new ArrayList<>();
    }
    public static class Person {
        public Long staffId, deptId, mentorId;
        public String staffNo, staffName, post;
        public LocalDate hiredOn;
        public BigDecimal basePay, meritPay;
        public Integer onJob;
        public Team department;
        public BigDecimal monthlyPay() {
            return (basePay == null ? BigDecimal.ZERO : basePay).add(meritPay == null ? BigDecimal.ZERO : meritPay);
        }
    }
    public static class Vendor {
        public Long supplierId;
        public String supplierName;
        public List<Map<String,Object>> products = new ArrayList<>();
        public List<Map<String,Object>> customers = new ArrayList<>();
        public List<Map<String,Object>> services = new ArrayList<>();
    }
}
