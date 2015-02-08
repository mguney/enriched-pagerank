package com.gun3y.pagerank.log;

import ch.qos.logback.core.joran.spi.NoAutoStart;
import ch.qos.logback.core.rolling.DefaultTimeBasedFileNamingAndTriggeringPolicy;
import ch.qos.logback.core.rolling.RolloverFailure;

@NoAutoStart
public class StartupTimeBasedTriggeringPolicy<E> extends DefaultTimeBasedFileNamingAndTriggeringPolicy<E> {

    @Override
    public void start() {
        super.start();
        this.nextCheck = 0L;
        this.isTriggeringEvent(null, null);
        try {
            this.tbrp.rollover();
        }
        catch (RolloverFailure e) {
            // Do nothing
        }
    }
}
