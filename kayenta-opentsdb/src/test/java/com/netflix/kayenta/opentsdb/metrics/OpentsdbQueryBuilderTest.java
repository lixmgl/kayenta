/*
 * Copyright (c) 2018 Nike, inc.
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
 *
 */

package com.netflix.kayenta.opentsdb.metrics;

import com.google.common.collect.ImmutableMap;
import com.netflix.kayenta.opentsdb.metrics.TagPair;
import com.netflix.kayenta.opentsdb.canary.OpentsdbCanaryScope;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OpentsdbQueryBuilderTest {

  @Test
  public void test_that_the_program_builder_builds_the_expected_program() {

    String metricName = "request.count";
    String aggregator = "sum";
    String downsample = "";
    boolean rate = false;

    OpentsdbCanaryScope scope = new OpentsdbCanaryScope();
    scope.setScope("1.0.0");
    scope.setScopeKey("version");
    scope.setExtendedScopeParams(ImmutableMap.of(
            "env", "production",
            "_scope_key", "version"));

    OpentsdbQueryBuilder builder = OpentsdbQueryBuilder.create(metricName, aggregator, downsample, rate);

    builder.withTagPair(new TagPair("app", "cms"));
    builder.withTagPair(new TagPair("response_code", "400"));
    builder.withTagPair(new TagPair("uri", "/v2/auth/iam-principal"));
    builder.withScope(scope);

    String expected = "sum:request.count{app=cms,response_code=400,uri=/v2/auth/iam-principal,version=1.0.0,env=production}";

    assertEquals(expected, builder.build());
  }

  @Test
  public void test_that_the_program_builder_builds_the_expected_program_with_rate() {

    String metricName = "request.count";
    String aggregator = "sum";
    String downsample = "";
    boolean rate = true;

    OpentsdbCanaryScope scope = new OpentsdbCanaryScope();
    scope.setScope("1.0.0");
    scope.setScopeKey("version");
    scope.setExtendedScopeParams(ImmutableMap.of(
            "env", "production",
            "_scope_key", "version"));

    OpentsdbQueryBuilder builder = OpentsdbQueryBuilder.create(metricName, aggregator, downsample, rate);

    builder.withTagPair(new TagPair("app", "cms"));
    builder.withTagPair(new TagPair("response_code", "400"));
    builder.withTagPair(new TagPair("uri", "/v2/auth/iam-principal"));
    builder.withScope(scope);

    String expected = "sum:rate:request.count{app=cms,response_code=400,uri=/v2/auth/iam-principal,version=1.0.0,env=production}";

    assertEquals(expected, builder.build());
  }

  @Test
  public void test_that_the_program_builder_builds_the_expected_program_with_downsample() {

    String metricName = "request.count";
    String aggregator = "sum";
    String downsample = "1m-sum";
    boolean rate = true;

    OpentsdbCanaryScope scope = new OpentsdbCanaryScope();
    scope.setScope("1.0.0");
    scope.setScopeKey("version");
    scope.setExtendedScopeParams(ImmutableMap.of(
            "env", "production",
            "_scope_key", "version"));

    OpentsdbQueryBuilder builder = OpentsdbQueryBuilder.create(metricName, aggregator, downsample, rate);

    builder.withTagPair(new TagPair("app", "cms"));
    builder.withTagPair(new TagPair("response_code", "400"));
    builder.withTagPair(new TagPair("uri", "/v2/auth/iam-principal"));
    builder.withScope(scope);

    String expected = "sum:1m-sum:rate:request.count{app=cms,response_code=400,uri=/v2/auth/iam-principal,version=1.0.0,env=production}";

    assertEquals(expected, builder.build());
  }

  @Test
  public void test_that_the_program_builder_builds_the_expected_program_with_extra_scope_qp_pairs() {

    String metricName = "request.count";
    String aggregator = "sum";
    String downsample = "";
    boolean rate = false;

    OpentsdbCanaryScope scope = new OpentsdbCanaryScope();
    scope.setScope("1.0.0");
    scope.setScopeKey("version");
    scope.setExtendedScopeParams(ImmutableMap.of(
            "env", "production",
            "region", "us-west-2",
            "_scope_key", "version"));

    OpentsdbQueryBuilder builder = OpentsdbQueryBuilder
            .create(metricName, aggregator, downsample, rate);

    builder.withTagPair(new TagPair("app", "cms"));
    builder.withTagPair(new TagPair("response_code", "400"));
    builder.withTagPair(new TagPair("uri", "/v2/auth/iam-principal"));
    builder.withScope(scope);

    String expected = "sum:request.count{app=cms,response_code=400,uri=/v2/auth/iam-principal,version=1.0.0,env=production,region=us-west-2}";

    assertEquals(expected, builder.build());
  }

  @Test
  public void test_that_the_program_builder_builds_the_expected_program_1_qp() {

    String metricName = "request.count";
    String aggregator = "sum";
    String downsample = "";
    boolean rate = false;

    OpentsdbCanaryScope scope = new OpentsdbCanaryScope();
    scope.setScope("1.0.0");
    scope.setScopeKey("version");
    scope.setExtendedScopeParams(ImmutableMap.of(
            "env", "production",
            "_scope_key", "version"));

    OpentsdbQueryBuilder builder = OpentsdbQueryBuilder
            .create(metricName, aggregator, downsample, rate);

    builder.withTagPair(new TagPair("app", "cms"));
    builder.withScope(scope);

    String expected = "sum:request.count{app=cms,version=1.0.0,env=production}";

    assertEquals(expected, builder.build());
  }

  @Test
  public void test_that_the_program_builder_builds_the_expected_program_with_no_query_pairs() {

    String metricName = "request.count";
    String aggregator = "sum";
    String downsample = "";
    boolean rate = false;

    OpentsdbCanaryScope scope = new OpentsdbCanaryScope();
    scope.setScope("1.0.0");
    scope.setScopeKey("version");
    scope.setExtendedScopeParams(ImmutableMap.of(
            "env", "production",
            "_scope_key", "version"));

    OpentsdbQueryBuilder builder = OpentsdbQueryBuilder
            .create(metricName, aggregator, downsample, rate);

    builder.withScope(scope);

    String expected = "sum:request.count{version=1.0.0,env=production}";


    assertEquals(expected, builder.build());
  }

  @Test
  public void test_that_when_there_are_extra_scope_query_pairs_that_the_program_builds_as_expected() {
    String metricName = "request.count";
    String aggregator = "sum";
    String downsample = "";
    boolean rate = false;

    OpentsdbCanaryScope scope = new OpentsdbCanaryScope();
    scope.setScope("1.0.0");
    scope.setScopeKey("version");
    scope.setExtendedScopeParams(ImmutableMap.of("_scope_key", "version"));

    OpentsdbQueryBuilder builder = OpentsdbQueryBuilder
            .create(metricName, aggregator, downsample, rate);

    builder.withScope(scope);

    String expected = "sum:request.count{version=1.0.0}";

    assertEquals(expected, builder.build());
  }

}