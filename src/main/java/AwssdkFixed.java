import java.util.Map;
import java.util.function.Supplier;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ByteArrayClassLoader;

public class AwssdkFixed {
  public static void main(String[] args) throws Throwable {
    // 1. Define child classloader holding a class unknown to this classloader
    ByteArrayClassLoader childClassloader =
        new ByteArrayClassLoader(
            AwssdkFixed.class.getClassLoader(),
            false,
            Map.of(),
            ByteArrayClassLoader.PersistenceHandler.MANIFEST);

    ByteBuddy bb = new ByteBuddy();
    var dataClass =
        childClassloader.defineClass(
            "one.Data", bb.subclass(Object.class).name("one.Data").make().getBytes());

    // 2. Create supplier lambda that create instances of that class; i.e. one.Data::new

    Supplier<?> supplier =
        LambdaToMethodBridgeBuilderFixed.create(Supplier.class)
            .lambdaMethodName("get")
            .runtimeLambdaSignature(Object.class)
            .compileTimeLambdaSignature(dataClass)
            .targetMethod(dataClass.getConstructor())
            .build();

    // 3. Now using the supplier to get a new instance
    Object dataObject = supplier.get();
    System.out.println(dataObject);
  }
}
