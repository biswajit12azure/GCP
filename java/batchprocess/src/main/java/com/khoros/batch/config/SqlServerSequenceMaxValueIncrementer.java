package com.khoros.batch.config;


import javax.sql.DataSource;

import org.springframework.jdbc.support.incrementer.AbstractSequenceMaxValueIncrementer;


public class SqlServerSequenceMaxValueIncrementer extends AbstractSequenceMaxValueIncrementer {

    public SqlServerSequenceMaxValueIncrementer(DataSource dataSource, String incrementerName) {
        super(dataSource, incrementerName);
    }

    @Override
    protected String getSequenceQuery() {
        return "select next value for " + getIncrementerName();
    }
}