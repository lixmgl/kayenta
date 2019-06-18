/*
 * Copyright 2018 Snap Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.kayenta.config;

import static com.netflix.kayenta.opentsdb.E2EIntegrationTest.CANARY_WINDOW_IN_MINUTES;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import lombok.extern.slf4j.Slf4j;

@TestConfiguration
@Slf4j
public class OpentsdbIntegrationTestConfig {
    public static final String CONTROL_SCOPE_NAME = "control";
    public static final String EXPERIMENT_SCOPE_HEALTHY = "test-healthy";
    public static final String EXPERIMENT_SCOPE_UNHEALTHY = "test-unhealthy";

    private static final String LOCAL_OPENTSDB_HOST = "localhost";
    private static final String TEST_METRIC = "test.server.request";
    private static final String TEST_TAGS = "response_code=400";
    private static final int MOCK_SERVICE_REPORTING_INTERVAL_IN_MILLISECONDS = 1000;
    public static final int[] HEALTHY_SERVER_METRICS = {0, 10};
    public static final int[] UNHEALTHY_SERVER_METRICS = {10, 20};

    private final ExecutorService executorService;

    private Instant metricsReportingStartTime;

    public OpentsdbIntegrationTestConfig() {
        this.executorService = Executors.newFixedThreadPool(3);
    }

    @Bean
    public Instant metricsReportingStartTime() {
        return metricsReportingStartTime;
    }

    @PostConstruct
    public void start() {
        metricsReportingStartTime = Instant.now();
        executorService.submit(createMetricReportingMockService(
            getOpentsdbMetricProvider(
                CONTROL_SCOPE_NAME,
                HEALTHY_SERVER_METRICS[0],
                HEALTHY_SERVER_METRICS[1])));
        executorService.submit(createMetricReportingMockService(
            getOpentsdbMetricProvider(
                EXPERIMENT_SCOPE_HEALTHY,
                HEALTHY_SERVER_METRICS[0],
                HEALTHY_SERVER_METRICS[1])));
        executorService.submit(createMetricReportingMockService(
            getOpentsdbMetricProvider(
                EXPERIMENT_SCOPE_UNHEALTHY,
                UNHEALTHY_SERVER_METRICS[0],
                UNHEALTHY_SERVER_METRICS[1])));
        metricsReportingStartTime = Instant.now();
        try {
            long pause = TimeUnit.MINUTES.toMillis(CANARY_WINDOW_IN_MINUTES) + TimeUnit.SECONDS.toMillis(10);
            log.info("Waiting for {} milliseconds for mock data to flow through opentsdb, before letting the " +
                "integration" +
                " tests run", pause);
            // TODO restore to pause
            Thread.sleep(pause);
        } catch (InterruptedException e) {
            log.error("Failed to wait to send metrics", e);
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    public void stop() {
        executorService.shutdownNow();
    }

    private Runnable createMetricReportingMockService(OpentsdbMetricProvider opentsdbMetricProvider) {
        int opentsdbQueryPort = Integer.parseInt(System.getProperty("opentsdb.queryPort"));
        return () -> {
            while (!Thread.currentThread().isInterrupted()) {
                try (Socket socket = new Socket(LOCAL_OPENTSDB_HOST, opentsdbQueryPort)) {
                    OutputStream outputStream = socket.getOutputStream();
                    PrintWriter out = new PrintWriter(outputStream);
                    out.println(opentsdbMetricProvider.getRandomMetricWithinRange());
                    out.flush();
                    out.close();
                    Thread.sleep(MOCK_SERVICE_REPORTING_INTERVAL_IN_MILLISECONDS);
                } catch (UnknownHostException e) {
                    log.error("UNABLE TO FIND HOST", e);
                } catch (IOException e) {
                    log.error("CONNECTION ERROR", e);
                } catch (InterruptedException e) {
                    log.debug("Thread interrupted", e);
                }
            }
        };
    }

    private OpentsdbMetricProvider getOpentsdbMetricProvider(String scope, int min, int max) {
        String tags = String.format("scope=%s %s", scope, TEST_TAGS);
        return new OpentsdbMetricProvider(min, max, TEST_METRIC, tags);
    }
}