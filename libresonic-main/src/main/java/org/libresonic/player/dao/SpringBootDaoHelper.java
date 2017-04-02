package org.libresonic.player.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

public class SpringBootDaoHelper implements DaoHelper {

    @Autowired
    private final JdbcTemplate jdbcTemplate = null;

    @Autowired
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate = null;

    @Autowired
    private final DataSource dataSource = null;


    @Override
    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    @Override
    public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
        return namedParameterJdbcTemplate;
    }

    @Override
    public DataSource getDataSource() {
        return dataSource;
    }
}
