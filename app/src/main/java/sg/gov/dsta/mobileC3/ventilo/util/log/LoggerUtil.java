package sg.gov.dsta.mobileC3.ventilo.util.log;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.html.HTMLLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.util.StatusPrinter;
import ch.qos.logback.core.util.FileSize;
import sg.gov.dsta.mobileC3.ventilo.BuildConfig;

import timber.log.Timber;

public class LoggerUtil {
    public static class DebugLogTree extends Timber.DebugTree {

        @Override
        protected void log(int priority, String tag, String message, Throwable t) {
//            // Workaround for devices that doesn't show lower priority logs
//            if (Build.MANUFACTURER.equals("HUAWEI") || Build.MANUFACTURER.equals("samsung")) {
//                if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO)
//                    priority = Log.ERROR;
//            }
            super.log(priority, tag, message, t);
        }

        @Override
        protected String createStackElementTag(StackTraceElement element) {
            // Add log statements line number to the log
            return super.createStackElementTag(element) + " - " + element.getLineNumber();
        }
    }

    public static class FileLoggingTree extends Timber.DebugTree {
        private static Logger mLogger = LoggerFactory.getLogger(FileLoggingTree.class);
        private static final String LOG_PREFIX = "ventilo-log";

        public FileLoggingTree() {
            final String logDirectory = generatePath("logs").toString();
            configureLogger(logDirectory);
        }

        private void configureLogger(String logDirectory) {
            // reset the default context (which may already have been initialized)
            // since we want to reconfigure it
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            loggerContext.reset();

            RollingFileAppender<ILoggingEvent> rollingFileAppender = new RollingFileAppender<>();
            rollingFileAppender.setContext(loggerContext);
            rollingFileAppender.setAppend(true);
            rollingFileAppender.setFile(logDirectory + "/" + LOG_PREFIX + "-latest.html");

            SizeAndTimeBasedFNATP<ILoggingEvent> fileNamingPolicy = new SizeAndTimeBasedFNATP<>();
            fileNamingPolicy.setContext(loggerContext);
            fileNamingPolicy.setMaxFileSize(FileSize.valueOf("1MB"));

            TimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new TimeBasedRollingPolicy<>();
            rollingPolicy.setContext(loggerContext);
            rollingPolicy.setFileNamePattern(logDirectory + "/" + LOG_PREFIX + ".%d{yyyy-MM-dd}.%i.html");
            rollingPolicy.setMaxHistory(5);
            rollingPolicy.setTimeBasedFileNamingAndTriggeringPolicy(fileNamingPolicy);
            rollingPolicy.setParent(rollingFileAppender);  // parent and context required!
            rollingPolicy.start();

            HTMLLayout htmlLayout = new HTMLLayout();
            htmlLayout.setContext(loggerContext);
            htmlLayout.setPattern("%d{HH:mm:ss.SSS}%level%thread%msg");
            htmlLayout.start();

            LayoutWrappingEncoder<ILoggingEvent> encoder = new LayoutWrappingEncoder<>();
            encoder.setContext(loggerContext);
            encoder.setLayout(htmlLayout);
            encoder.start();

            // Alternative text encoder - very clean pattern, takes up less space
//        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
//        encoder.setContext(loggerContext);
//        encoder.setCharset(Charset.forName("UTF-8"));
//        encoder.setPattern("%date %level [%thread] %msg%n");
//        encoder.start();

            rollingFileAppender.setRollingPolicy(rollingPolicy);
            rollingFileAppender.setEncoder(encoder);
            rollingFileAppender.start();

            // add the newly created appenders to the root logger;
            // qualify Logger to disambiguate from org.slf4j.Logger
            ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
            root.setLevel(Level.DEBUG);
            root.addAppender(rollingFileAppender);

            // print any status messages (warnings, etc) encountered in logback config
            StatusPrinter.print(loggerContext);
        }

        @Override
        protected void log(int priority, String tag, String message, Throwable t) {
            if (priority == Log.VERBOSE) {
                return;
            }

            String logMessage = tag + ": " + message;
            switch (priority) {
                case Log.DEBUG:
                    mLogger.debug(logMessage);
                    break;
                case Log.INFO:
                    mLogger.info(logMessage);
                    break;
                case Log.WARN:
                    mLogger.warn(logMessage);
                    break;
                case Log.ERROR:
                    mLogger.error(logMessage);
                    break;
            }
        }
    }


    /*  Helper method to create file*/
    @Nullable
    private static File generatePath(@NonNull String path) {
        File root = null;
        if (isExternalStorageAvailable()) {
            root = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),
                    BuildConfig.APPLICATION_ID + File.separator + path);

//            boolean dirExists = true;

            if (!root.exists()) {
                root.mkdirs();
            }

//            if (dirExists) {
//                file = new File(root, fileName);
//            }
        }
        return root;
    }

    /* Helper method to determine if external storage is available*/
    private static boolean isExternalStorageAvailable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }
}
