package study.console;
import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Map;
public interface CatalogMapper {
    @SelectProvider(type=CatalogSql.class, method="select")
    List<Map<String,Object>> rows(@Param("kind") CatalogSql.Kind kind,@Param("filter") Map<String,Object> filter,
        @Param("offset") Integer offset,@Param("limit") Integer limit);
    @InsertProvider(type=CatalogSql.class,method="insert") @Options(useGeneratedKeys=true,keyProperty="row.id")
    int create(@Param("kind") CatalogSql.Kind kind,@Param("row") Map<String,Object> row);
    @UpdateProvider(type=CatalogSql.class,method="update")
    int change(@Param("kind") CatalogSql.Kind kind,@Param("row") Map<String,Object> row);
    @DeleteProvider(type=CatalogSql.class,method="delete")
    int remove(@Param("kind") CatalogSql.Kind kind,@Param("id") long id);
    @InsertProvider(type=CatalogSql.class,method="link")
    int connect(@Param("relation") String relation,@Param("left") long left,@Param("right") long right);
    @DeleteProvider(type=CatalogSql.class,method="unlink")
    int disconnect(@Param("relation") String relation,@Param("left") Long left,@Param("right") Long right);
    @SelectProvider(type=CatalogSql.class,method="links")
    List<Map<String,Object>> relations(@Param("relation") String relation,@Param("left") Long left,@Param("right") Long right);
    @Select("SELECT DISTINCT r.code FROM acl_resource r JOIN acl_grant g ON g.right_id=r.id "
      +"JOIN acl_role a ON a.id=g.left_id JOIN acl_member m ON m.right_id=a.id JOIN acl_user u ON u.id=m.left_id "
      +"WHERE u.id=#{id} AND u.status=1 AND a.status=1 AND r.status=1 ORDER BY r.code")
    List<String> codes(long id);
}
