package com.etl.report.business;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogTester {

    private static final Logger logger = LogManager.getLogger(LogTester.class);
    static int getNumber() {
        return 5;
    }

    public static void main(String[] args) {
        logger.debug("Hello from Log4j 2");
        logger.debug("{}", () -> getNumber());

    }
}
