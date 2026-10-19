package study.console;
import org.apache.ibatis.annotations.Param;
import java.util.*;
import study.console.Models.*;
public interface DirectoryMapper {
    Team team(@Param("id") long id);
    Team teamJoin(@Param("id") long id);
    Team brief(@Param("id") long id);
    List<Team> teams(Map<String,Object> filters);
    List<Person> members(@Param("id") long id);
    Person person(@Param("id") long id);
    Person personJoin(@Param("id") long id);
    List<Person> search(Map<String,Object> filters);
    List<Map<String,Object>> totals();
    Map<String,Object> payroll();
    int addTeam(Team row);
    int editTeam(Team row);
    int dropTeam(long id);
    int addPerson(Person row);
    int editPerson(Person row);
    int transfer(@Param("id") long id,@Param("team") long team);
    int leave(long id);
}
