package com.gun3y.pagerank.crawler.ui;

import java.awt.TextArea;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.encoder.EchoEncoder;
import ch.qos.logback.core.encoder.Encoder;

public class SwingLogAdapter extends AppenderBase<ILoggingEvent> {

    private Encoder<ILoggingEvent> encoder = new EchoEncoder<ILoggingEvent>();

    private ByteArrayOutputStream out = new ByteArrayOutputStream();

    private TextArea textArea;

    public SwingLogAdapter(TextArea textArea) {
        this.textArea = textArea;
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        this.setContext(lc);
        this.start();
        lc.getLogger("ROOT").addAppender(this);
    }

    @Override
    public void start() {
        try {
            this.encoder.init(this.out);
        }
        catch (IOException e) {
        }
        super.start();
    }

    @Override
    protected void append(ILoggingEvent arg0) {
        try {
            this.encoder.doEncode(arg0);
            this.out.flush();
            if (arg0.getLevel() == Level.INFO) {
                this.textArea.append(this.out.toString());
            }
            this.out.reset();
        }
        catch (IOException e) {
        }
    }

}
