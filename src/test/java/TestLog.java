import org.slf4j.LoggerFactory;

public class TestLog {

  public static void main(String[] args) {
    var log = LoggerFactory.getLogger(TestLog.class);
    try {
      Integer.parseInt("Not an int");
    } catch (NumberFormatException e) {
      log.error("whoops", e);
    }
  }
}
