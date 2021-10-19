package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootApplication
public class RelationalDataAccessApplication implements CommandLineRunner {
    private static final Logger logger =
            LoggerFactory.getLogger(RelationalDataAccessApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(RelationalDataAccessApplication.class, args);
    }

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        logger.info("Creating tables");

        jdbcTemplate.execute("DROP TABLE customers IF EXISTS");
        jdbcTemplate.execute("CREATE TABLE customers(" +
                "id SERIAL, first_name VARCHAR(255), last_name VARCHAR(255))");

        List<Object[]> splitUpNames = Stream.of("John Woo", "Jeff Dean", "Josh Bloch", "Josh Long")
                .map(name -> name.split(" "))
                .collect(Collectors.toList());

        splitUpNames.forEach(name -> logger.info(String.format(
                "Inserting customer record for %s %s", name[0], name[1]))
        );

        jdbcTemplate.batchUpdate("INSERT INTO customers(first_name, last_name) VALUES " +
                "(?,?)", splitUpNames);

        logger.info("Querying for customer records where first_name = 'Josh':");

        RowMapper<Customer> rowMapper = (rs, rowNum) -> new Customer(
                rs.getLong("id"),
                rs.getString("first_name"),
                rs.getString("last_name")
        );

        jdbcTemplate.query(
                "SELECT id, first_name, last_name FROM customers WHERE first_name = ?",
                new Object[]{"Jeff"}, rowMapper)
                .forEach(customer -> logger.info(customer.toString()));
    }
}
