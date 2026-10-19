package study.userapi;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.userapi.persistence.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:user_storage_test;MODE=MySQL;DB_CLOSE_DELAY=-1","spring.datasource.driver-class-name=org.h2.Driver","spring.datasource.username=sa","spring.datasource.password=","spring.sql.init.mode=always"})
@Transactional
class UserStorageTest {
    @Autowired XmlUsers xml;@Autowired AnnotationUsers annotated;
    private UserRecord draft(String prefix){UserRecord r=new UserRecord();r.username=prefix+UUID.randomUUID();r.password="pass123";r.email=r.username+"@example.com";return r;}
    @Test void xmlListsAndIdMapping(){assertEquals(10,xml.browse().size());assertEquals(10,xml.browseUsingXml().size());assertEquals("demo1",xml.get(1).username);assertNotNull(xml.get(1).createTime);assertNull(xml.get(999));}
    @Test void annotationListsAndIdMapping(){assertEquals(10,annotated.browse().size());assertEquals("demo1",annotated.get(1).username);assertNotNull(annotated.get(1).createTime);assertNull(annotated.get(999));}
    @Test void nameAndLikeHaveEquivalentResults(){assertEquals(xml.named("demo1").id,annotated.named("demo1").id);assertEquals(2,xml.containing("demo1").size());assertEquals(2,annotated.containing("demo1").size());assertNull(xml.named("missing"));}
    @Test void xmlCrudUsesGeneratedKeysAndPartialUpdate(){var x=draft("xml_");assertEquals(1,xml.save(x));assertNotNull(x.id);assertNotNull(xml.get(x.id).createTime);var p=new UserRecord();p.id=x.id;p.email="new_"+x.email;assertEquals(1,xml.patch(p));assertEquals(x.username,xml.get(x.id).username);assertEquals(p.email,xml.get(x.id).email);assertEquals(1,xml.erase(x.id));assertNull(xml.get(x.id));}
    @Test void annotatedCrudUsesGeneratedKeysAndPartialUpdate(){var x=draft("annotation_");assertEquals(1,annotated.save(x));assertNotNull(x.id);var p=new UserRecord();p.id=x.id;p.email="new_"+x.email;assertEquals(1,annotated.patch(p));assertEquals(x.username,annotated.get(x.id).username);assertEquals(p.email,annotated.get(x.id).email);assertEquals(1,annotated.erase(x.id));assertNull(annotated.get(x.id));}
    @Test void annotationLoginAndMapParameters(){assertNotNull(annotated.authenticate("demo1","Demo@123"));assertNull(annotated.authenticate("demo1","bad"));assertNull(annotated.authenticate("demo1",null));assertEquals(1,annotated.matching(Map.of("username","demo1","email","demo1@example.com","password","Demo@123")).size());assertTrue(annotated.matching(Map.of("username","demo1","email","wrong@example.com")).isEmpty());}
    @Test void parametersCannotAlterSql(){assertNull(xml.named("' OR 1=1 --"));assertNull(annotated.authenticate("' OR 1=1 --","x"));assertEquals(10,xml.browse().size());}
}
