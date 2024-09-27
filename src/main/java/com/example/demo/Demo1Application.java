package com.example.demo;

import javax.sql.DataSource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.jdbc.JdbcConnectionDetails;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

import com.zaxxer.hikari.HikariDataSource;

@SpringBootApplication
public class Demo1Application {

	public static void main(String[] args) {
		SpringApplication.run(Demo1Application.class, args).getBean(DataSource.class);
	}

	@Bean
	@ConfigurationProperties(prefix = "spring.datasource.hikari")
	HikariDataSource dataSource(DataSourceProperties properties, JdbcConnectionDetails connectionDetails) {
		HikariDataSource dataSource = createDataSource(connectionDetails, HikariDataSource.class,
				properties.getClassLoader());
		if (StringUtils.hasText(properties.getName())) {
			dataSource.setPoolName(properties.getName());
		}
		return dataSource;
	}

	@Bean
	@ConditionalOnMissingBean(JdbcConnectionDetails.class)
	PropertiesJdbcConnectionDetails jdbcConnectionDetails(DataSourceProperties properties) {
		return new PropertiesJdbcConnectionDetails(properties);
	}

	private static <T> T createDataSource(JdbcConnectionDetails connectionDetails, Class<? extends DataSource> type,
			ClassLoader classLoader) {
		return (T) DataSourceBuilder.create(classLoader).type(type).driverClassName(connectionDetails.getDriverClassName())
				.url(connectionDetails.getJdbcUrl()).username(connectionDetails.getUsername())
				.password(connectionDetails.getPassword()).build();
	}

}
