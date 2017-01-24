package com.rbkmoney.woody.test_woody;

import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.execution.DockerExecutionException;
import com.palantir.docker.compose.execution.ImmutableDockerComposeExecArgument;
import com.palantir.docker.compose.execution.ImmutableDockerComposeExecOption;
import com.rbkmoney.damsel.payment_processing.EventSinkSrv;
import org.apache.thrift.TException;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * Created by inalarsanukaev on 20.01.17.
 */
@Ignore
public class ShumwayFailTest {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @ClassRule
    public static DockerComposeRule docker = DockerComposeRule.builder()
            .file("src/test/resources/docker-compose-without-shumway.yml")
                .build();

    @Test
    public void testFailShumway() throws TException, InterruptedException, IOException, URISyntaxException {
        //check fail hg -> shumway
        //TODO REMOVE! docker.waitingForService(shumway) NOT working
        Thread.sleep(8000);
        EventSinkSrv.Iface eventSink =  new EventSink().eventSinkSrv();
        try {
            docker.exec(ImmutableDockerComposeExecOption.of(new ArrayList<>()),
                    "inspector",
                    ImmutableDockerComposeExecArgument.of(Arrays.asList("./scripts/dominant/commit-base-fixture.sh", "-v")));
        } catch (DockerExecutionException e) {
            String errMess = e.getLocalizedMessage();
            String[] array = errMess.split("\n");
            long countShumCalls = Arrays.stream(array).filter(s -> s.endsWith("client send: #{url => \"http://shumway:8022/accounter\"}")).count();
            long countShumOk = Arrays.stream(array).filter(s -> s.endsWith("client receive: #{status => error}")).count();
            assertEquals(countShumCalls, countShumOk);
        }
    }
}
