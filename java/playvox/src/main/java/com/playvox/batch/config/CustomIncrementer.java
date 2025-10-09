package com.playvox.batch.config;


import org.springframework.batch.item.database.support.DataFieldMaxValueIncrementerFactory;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;

import javax.sql.DataSource;

public class CustomIncrementer implements DataFieldMaxValueIncrementerFactory {

    private final DataSource dataSource;

    public CustomIncrementer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public DataFieldMaxValueIncrementer getIncrementer(String databaseType, String incrementerName) {
        return new SqlServerSequenceMaxValueIncrementer(dataSource, incrementerName);
    }

    @Override
    public boolean isSupportedIncrementerType(String databaseType) {
        return true;
    }

    @Override
    public String[] getSupportedIncrementerTypes() {
        return null; // method should not get called anyway
    }
}