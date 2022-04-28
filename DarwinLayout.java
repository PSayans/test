package es.santander.libcom.confluent.core.logging;

import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.json.JSONObject;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Class to implement a Layout to be consumed by DARWIN
 */
@Plugin(name = "DarwinLayout", category = "Core", elementType = "layout", printObject = true)
public class DarwinLayout extends AbstractStringLayout {

    public static final String DEFAULT_EOL = "\r\n";
    public static final int UNKNOW_LINE_NUMBER = 0;
    public static final String UNKNOW_METHOD = "unknow-method";
    public static final String UNKNOW_CLASS = "unknow-class";
    public final String finalRegex = "(\")?(%X\\{(.*?)\\})(\")?";

    private String environment;
    private String appKey;
    private String appInit;
    private String application;
    private String extensionType;
    private String system;
    private String subSystem;
    private String subApplication;
    private String paasProject;
    private String paasAppVersion;
    private String component;
    private String customLog;

    protected DarwinLayout(Charset charset,
                           String environment,
                           String appKey,
                           String appInit,
                           String application,
                           String extensionType,
                           String system,
                           String subSystem,
                           String subApplication,
                           String paasProject,
                           String paasAppVersion,
                           String component,
                           String customLog) {
        super(charset);
        this.environment = environment;
        this.appKey = appKey;
        this.appInit = appInit;
        this.application = application;
        this.extensionType = extensionType;
        this.system = system;
        this.subSystem = subSystem;
        this.subApplication = subApplication;
        this.paasProject = paasProject;
        this.paasAppVersion = paasAppVersion;
        this.component = component;
        this.customLog = customLog;
    }

    @PluginFactory
    public static DarwinLayout createLayout(
            @PluginAttribute("environment") String environment,
            @PluginAttribute("appKey") String appKey,
            @PluginAttribute("appInit") String appInit,
            @PluginAttribute("application") String application,
            @PluginAttribute("extensionType") String extensionType,
            @PluginAttribute("system") String system,
            @PluginAttribute("subSystem") String subSystem,
            @PluginAttribute("subApplication") String subApplication,
            @PluginAttribute("paasProject") String paasProject,
            @PluginAttribute("paasAppVersion") String paasAppVersion,
            @PluginAttribute("component") String component,
            @PluginAttribute(value = "customLog", defaultString = "{}") String customLog,
            @PluginAttribute(value = "charset", defaultString = "UTF-8") Charset charset
    ) {


        return new DarwinLayout(charset,
                environment,
                appKey,
                appInit,
                application,
                extensionType,
                system,
                subSystem,
                subApplication,
                paasProject,
                paasAppVersion,
                component,
                customLog);
    }

    @Override
    public String toSerializable(LogEvent logEvent) {

        String interpolatedCustomLog = contextSubstitution(this.customLog);
        JSONObject jsonToLog;

        JSONObject customLogJson = new JSONObject(interpolatedCustomLog);

        jsonToLog = new JSONObject(new HashMap<String, Object>() {{
            put("timeStamp", getTimeStamp(logEvent));
            put("log", (logEvent.getMessage().getFormattedMessage()));
            put("customLog", customLogJson);
            put("codeLine", getLineNumber(logEvent.getSource()));
            put("threadName", logEvent.getThreadName());
            put("methodName", getMethodName(logEvent.getSource()));
            put("logLevel", logEvent.getLevel().getStandardLevel().name());
            put("processId", getClassName(logEvent.getSource()));
            put("machineId", getMachineId());
            put("userId", System.getProperty("user.name"));
            put("environment", environment.toUpperCase(Locale.ROOT));
            put("application", application);
            put("extensionType", extensionType);
            put("system", system);
            put("subSystem", subSystem);
            put("subApplication", subApplication);
            put("component", component);
            put("appKey", appKey);
            put("paasProject", paasProject);
            put("paasApp", System.getenv("APP_NAME"));
            put("paasAppVersion", paasAppVersion);
            put("appInit", appInit);
            put("sessionId", "");
            put("correlationTraceId", "");
            put("correlationSpanId", "");
            put("platformLog", "");
            put("technology", "");
            // New
            put("serverId", System.getenv("HOSTNAME"));
        }});

        return jsonToLog + DEFAULT_EOL;
    }

    /**
     * Method to get the line number where log is produced
     * If source is null returns UNKNOW_LINE_NUMBER constant
     *
     * @param source
     * @return line number
     */
    private int getLineNumber(StackTraceElement source) {
        if (null == source) {
            return UNKNOW_LINE_NUMBER;
        } else {
            return source.getLineNumber();
        }
    }

    /**
     * Method to get the method where log is produced
     * If source is null returns UNKNOW_METHOD constant
     *
     * @param source
     * @return method name
     */
    private String getMethodName(StackTraceElement source) {
        if (null == source) {
            return UNKNOW_METHOD;
        } else {
            return source.getMethodName();
        }
    }

    /**
     * Method to get the class where log is produced
     * If source is null returns UNKNOW_CLASS constant
     *
     * @param source
     * @return class name
     */
    private String getClassName(StackTraceElement source) {
        if (null == source) {
            return UNKNOW_CLASS;
        } else {
            return source.getClassName();
        }
    }

    /**
     * Method which get a TimeStamp String from the Event
     *
     * @param logEvent
     * @return Event's timestamp in an appropiate format
     */
    private String getTimeStamp(LogEvent logEvent) {
        return Instant.ofEpochMilli(logEvent.getTimeMillis()).toString();
    }

    /**
     * Method to get the machine's IP running the Process
     *
     * @return Machine's IP
     */
    private String getMachineId() {
        String machineId = "unknown";

        try {
            InetAddress ip = InetAddress.getLocalHost();
            machineId = ip.getHostName();
        } catch (UnknownHostException e) {
            LOGGER.warn("Hostname can't be set: " + e.getMessage(), e);
        }

        return machineId;
    }

    /**
     * Method that retrieves the thread context and substitutes variables in customLog JSON
     *
     * @param customLog
     * @return Formatted String with substituted context
     */

    private String contextSubstitution(String customLog) {

        if (customLog.isEmpty() || "{}".equals(customLog)) {
            return customLog;
        }

        Map<String, String> context = ThreadContext.getImmutableContext();

        for (Map.Entry<String, String> entry : context.entrySet()) {
            String contextRegex = "%X{" + entry.getKey() + "}";
            customLog = customLog.replace(contextRegex, entry.getValue());
        }

        customLog = customLog.replaceAll(finalRegex, "null");

        return customLog;
    }
}


