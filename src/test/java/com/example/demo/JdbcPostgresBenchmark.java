/*
 * Copyright 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.demo;

import io.r2dbc.pool.ConnectionPool;
import org.openjdk.jmh.infra.Blackhole;
import reactor.core.publisher.Flux;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.junit.platform.commons.annotation.Testable;
import org.openjdk.jmh.annotations.*;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.r2dbc.core.DatabaseClient;

/**
 * @author Mark Paluch
 */
@Fork(value = 1, warmups = 1)
@Warmup(time = 5, timeUnit = TimeUnit.SECONDS)
@Timeout(time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(time = 5, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@BenchmarkMode(Mode.Throughput)
@Threads(20)
@Testable
public class JdbcPostgresBenchmark {

	@State(value = Scope.Benchmark)
	public static class Resources {

		ConfigurableApplicationContext ctx;
		DataSource pool;
		JdbcTemplate template;

		@Setup
		public void setup() {
			System.setProperty("reactor.netty.ioWorkerCount", "128");

			ctx = SpringApplication.run(Demo1Application.class);

			pool = ctx.getBean(DataSource.class);

			template = ctx.getBean(JdbcTemplate.class);
		}

		@TearDown
		public void tearDown() {
			ctx.close();
		}
	}

	@Benchmark
	public void benchmark(Resources resources, Blackhole bh) throws Exception {

		// create table demo (
		// id integer
		// );
		// empty table

		Connection connection = resources.pool.getConnection();
		Statement statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery("SELECT * from demo");
		while (resultSet.next()) {
			bh.consume(resultSet.getObject(1));
		}

		resultSet.close();
		statement.close();
		connection.close();
	}
}
