import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MainTest {
  @Test
  public void testSimpleCrashing() {
    Assertions.assertThrows(NoClassDefFoundError.class, () -> SimpleCrashing.main(new String[0]));
  }

  @Test
  public void testSimpleWorking() throws Throwable {
    SimpleWorking.main(new String[0]);
  }

  @Test
  public void testAwssdkCrashing() {
    Assertions.assertThrows(NoClassDefFoundError.class, () -> AwssdkCrashing.main(new String[0]));
  }

  @Test
  public void testAwssdkWorking() throws Throwable {
    AwssdkFixed.main(new String[0]);
  }
}
