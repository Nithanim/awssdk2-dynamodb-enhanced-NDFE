import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Map;
import java.util.function.Supplier;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ByteArrayClassLoader;

public class SimpleFixed {
  public static void main(String[] args) throws Throwable {
    // 1. Define child classloader holding a class unknown to this classloader
    ByteArrayClassLoader childClassloader =
        new ByteArrayClassLoader(
            SimpleFixed.class.getClassLoader(),
            false,
            Map.of(),
            ByteArrayClassLoader.PersistenceHandler.MANIFEST);

    ByteBuddy bb = new ByteBuddy();
    var dataClass =
        childClassloader.defineClass(
            "one.Data", bb.subclass(Object.class).name("one.Data").make().getBytes());

    // 2. Create supplier lambda that create instances of that class; i.e. one.Data::new

    var lookup = MethodHandles.privateLookupIn(dataClass, MethodHandles.lookup());

    var methodHandle = lookup.unreflectConstructor(dataClass.getConstructor());
    var callSite =
        LambdaMetafactory.metafactory(
            lookup,
            "get",
            MethodType.methodType(Supplier.class),
            MethodType.methodType(Object.class),
            methodHandle,
            MethodType.methodType(dataClass));
    var lambdaCreator = callSite.getTarget();
    var supplier = (Supplier<?>) lambdaCreator.invoke();

    // 3. Now using the supplier to get a new instance
    Object dataObject = supplier.get();
    /*
    Crashes with:

    Exception in thread "main" java.lang.NoClassDefFoundError: one/Data
        at Main.main(Main.java:39)
    Caused by: java.lang.ClassNotFoundException: one.Data
        at java.base/jdk.internal.loader.BuiltinClassLoader.loadClass(BuiltinClassLoader.java:581)
        at java.base/jdk.internal.loader.ClassLoaders$AppClassLoader.loadClass(ClassLoaders.java:178)
        at java.base/java.lang.ClassLoader.loadClass(ClassLoader.java:522)
        ... 1 more

    Because the lookup from the wrong "context" is used.
     */

    System.out.println(dataObject);
  }
}
