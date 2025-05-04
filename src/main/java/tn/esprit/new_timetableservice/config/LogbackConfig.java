package tn.esprit.new_timetableservice.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import jakarta.annotation.PostConstruct;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;



@Configuration
public class LogbackConfig {

    @PostConstruct
    public void configureLogback() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.reset(); // Clear any existing configuration

        // Create a console appender
        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
        consoleAppender.setContext(context);
        consoleAppender.setName("CONSOLE");

        // Set encoder with pattern
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("%d{yyyy-MM-dd'T'HH:mm:ss.SSSXXX} %-5level %logger{36} - %msg%n");
        encoder.start();
        consoleAppender.setEncoder(encoder);
        consoleAppender.start();

        // Configure root logger
        Logger rootLogger = context.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(Level.INFO);
        rootLogger.addAppender(consoleAppender);

        // Configure specific loggers
        configureLogger(context, "tn.esprit.new_timetableservice", Level.TRACE, consoleAppender);
        configureLogger(context, "org.springframework.web", Level.TRACE, consoleAppender);
        configureLogger(context, "org.springframework.security", Level.TRACE, consoleAppender);
        configureLogger(context, "org.hibernate", Level.DEBUG, consoleAppender);
        configureLogger(context, "org.hibernate.SQL", Level.DEBUG, consoleAppender);
        configureLogger(context, "org.hibernate.type.descriptor.sql", Level.TRACE, consoleAppender);
        configureLogger(context, "com.netflix.discovery", Level.DEBUG, consoleAppender);
        configureLogger(context, "com.netflix.eureka", Level.DEBUG, consoleAppender);
    }

    private void configureLogger(LoggerContext context, String loggerName, Level level, ConsoleAppender<ILoggingEvent> appender) {
        Logger logger = context.getLogger(loggerName);
        logger.setLevel(level);
        logger.addAppender(appender);
        logger.setAdditive(false); // Prevent log propagation to root logger
    }
}