import io.testproject.examples.sdk.tests.BasicTest;
import io.testproject.examples.sdk.tests.ExtendedTest;
import io.testproject.examples.sdk.tests.ProxyTest;
import io.testproject.java.sdk.v2.Runner;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;


public class Tests {

    private static final String DEV_TOKEN = "YOUR_DEV_TOKEN";
    private static final String DEVICE_UDID = "ANDROID_DEVICE_UDID";
    private static final String PACKAGE_NAME = "io.testproject.demo";
    private static final String ACTIVITY_NAME = ".MainActivity";

    private static Runner runner;

    @BeforeAll
    public static void setup() throws InstantiationException {
        runner = Runner.createAndroid(DEV_TOKEN, DEVICE_UDID, PACKAGE_NAME, ACTIVITY_NAME);
    }

    @AfterAll
    public static void tearDown() throws IOException {
        runner.close();
    }

    @Test
    public void runTest() throws Exception {

        // Create test
        BasicTest test = new BasicTest();

        // Run test
        runner.run(test);

    }

    @Test
    public void runExtendedTest() throws Exception {

        // Create test
        ExtendedTest test = new ExtendedTest();
        test.name = "John Smith";
        test.password = "12345";
        test.country = "Canada";
        test.address = "5000 Highway 7 East";
        test.email = "john.smith@testproject.io";
        test.phone = "+1 (905) 515-2000";

        // Run test
        runner.run(test);
    }

    @Test
    public void runProxyTest() throws Exception {

        // Create test
        ProxyTest test = new ProxyTest();

        test.name = "John Smith";
        test.password = "12345";
        test.country = "Canada";
        test.address = "5000 Highway 7 East";
        test.email = "john.smith@testproject.io";

        // Run test
        runner.run(test);
    }
}