package study.userapi;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.Map;
@RestControllerAdvice
public class ApiErrors {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String,String>> badInput(IllegalArgumentException error){return ResponseEntity.badRequest().body(Map.of("error",error.getMessage()));}
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String,String>> conflict(DataIntegrityViolationException error){return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error","用户名或邮箱已存在"));}
}
