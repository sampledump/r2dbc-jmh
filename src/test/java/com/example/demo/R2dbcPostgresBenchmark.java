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
import io.r2dbc.spi.Connection;
import reactor.core.publisher.Flux;

import java.util.concurrent.TimeUnit;

import org.junit.platform.commons.annotation.Testable;
import org.openjdk.jmh.annotations.*;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
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
public class R2dbcPostgresBenchmark {

	@State(value = Scope.Benchmark)
	public static class Resources {

		ConfigurableApplicationContext ctx;
		ConnectionPool pool;
		DatabaseClient client;

		@Setup
		public void setup() {
			ctx = SpringApplication.run(Demo1Application.class);

			pool = ctx.getBean(ConnectionPool.class);
			pool.warmup().block();

			client = ctx.getBean(DatabaseClient.class);
		}

		@TearDown
		public void tearDown() {
			ctx.close();
		}
	}

	@Benchmark
	public Object benchmark(Resources resources) {

		// create table demo (
		// id integer
		// );
		// empty table

		return Flux.usingWhen(resources.pool.create(), connection -> {
			return Flux.from(connection.createStatement("SELECT * FROM demo")
					.execute()).flatMap(it -> it.map(readable -> readable.get(0)));
				},
				Connection::close
		).blockLast();

	}
}
