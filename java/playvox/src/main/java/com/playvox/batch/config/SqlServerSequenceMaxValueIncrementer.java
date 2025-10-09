package com.playvox.batch.config;


import org.springframework.jdbc.support.incrementer.AbstractSequenceMaxValueIncrementer;

import javax.sql.DataSource;


public class SqlServerSequenceMaxValueIncrementer extends AbstractSequenceMaxValueIncrementer {

    public SqlServerSequenceMaxValueIncrementer(DataSource dataSource, String incrementerName) {
        super(dataSource, incrementerName);
    }

    @Override
    protected String getSequenceQuery() {
        return "select next value for " + getIncrementerName();
    }
}