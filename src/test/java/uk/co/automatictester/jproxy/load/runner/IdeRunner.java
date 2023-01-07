package uk.co.automatictester.jproxy.load.runner;

import io.gatling.app.Gatling;
import io.gatling.core.config.GatlingPropertiesBuilder;

public class IdeRunner {

    public static void run(String simulationClass) {
        String projectRootDir = System.getProperty("user.dir");
        String resultsDirectory = projectRootDir + "/target/gatling";

        GatlingPropertiesBuilder props = new GatlingPropertiesBuilder()
                .resultsDirectory(resultsDirectory)
                .simulationClass(simulationClass);

        Gatling.fromMap(props.build());
    }
}
