package com.rbkmoney.woody.test_woody;

import com.palantir.docker.compose.DockerComposeRule;
import com.rbkmoney.damsel.payment_processing.EventRange;
import com.rbkmoney.damsel.payment_processing.EventSinkSrv;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;

/**
 * Created by inalarsanukaev on 20.01.17.
 */
@Ignore
public class HGFailTest {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @ClassRule
    public static DockerComposeRule docker = DockerComposeRule.builder()
            .file("src/test/resources/docker-compose-only-hg.yml")
                .build();

    @Test
    public void testFailHG() throws TException, InterruptedException, IOException, URISyntaxException {
        EventSinkSrv.Iface eventSink = new EventSink().eventSinkSrv();
        EventRange eventRange = new EventRange();
        eventRange.setLimit(10);
        log.debug("Start polling events after= {}, limit={}", eventRange.getAfter(), eventRange.getLimit());
        try {
            eventSink.getEvents(eventRange);
        } catch (TTransportException e) {
            assertEquals("HTTP Response code: 500", e.getLocalizedMessage() );
        }
    }
}
