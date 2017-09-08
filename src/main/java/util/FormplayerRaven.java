package util;

import com.getsentry.raven.Raven;
import com.getsentry.raven.event.Breadcrumb;
import com.getsentry.raven.event.Event;
import com.getsentry.raven.event.EventBuilder;
import com.getsentry.raven.event.User;
import com.getsentry.raven.event.interfaces.ExceptionInterface;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import services.RestoreFactory;

import java.net.URISyntaxException;

/**
 * Created by benrudolph on 4/27/17.
 */
public class FormplayerRaven {

    private static final Log log = LogFactory.getLog(FormplayerRaven.class);

    private final String HQ_HOST_TAG = "HQHost";
    private final String DOMAIN_TAG = "domain";
    private final String AS_USER = "as_user";
    private final String APP_URL_EXTRA = "app_url";
    private final String APP_DOWNLOAD_URL_EXTRA = "app_download";
    private final String USER_SYNC_TOKEN = "sync_token";
    private final String USER_SANDBOX_PATH = "sandbox_path";

    private Raven raven;

    @Value("${commcarehq.environment}")
    private String environment;

    private String domain = "UNKNOWN";
    private String appId = "UNKNOWN";

    @Value("${commcarehq.host}")
    private String host;

    @Autowired
    private RestoreFactory restoreFactory;

    @Autowired(required = false)
    private FormplayerHttpRequest request;

    public FormplayerRaven(Raven raven) {
        this.raven = raven;
    }

    public void recordBreadcrumb(Breadcrumb breadcrumb) {
        if (raven == null) {
            return;
        }
        try {
            raven.getContext().recordBreadcrumb(breadcrumb);
        } catch (Exception e) {
            log.info("Error recording breadcrumb. Ensure that raven is configured. ", e);
        }
    }

    public void setUserContext(String userId, String username, String ipAddress) {
        User user = new User(
                userId,
                username,
                ipAddress,
                null
        );
        try {
            raven.getContext().setUser(user);
        } catch (Exception e) {
            log.info("Error setting user context. Ensure that raven is configured. ", e);
        }
    }

    private String getAppURL() {
        if (domain == null || appId == null) {
            return null;
        }
        return host + "/a/" + domain + "/apps/view/" + appId + "/";
    }

    private String getAppDownloadURL() {
        if (domain == null || appId == null) {
            return null;
        }
        String baseURL = host + "/a/" + domain + "/apps/api/download_ccz/";
        URIBuilder builder;
        try {
            builder = new URIBuilder(baseURL);
        } catch (URISyntaxException e) {
            log.info("Unable to build app download URL");
            return null;
        }
        builder.addParameter("app_id", appId);
        builder.addParameter("latest", Constants.CCZ_LATEST_SAVED);
        return builder.toString();
    }

    private EventBuilder getDefaultBuilder() {
        return (
                new EventBuilder()
                .withEnvironment(environment)
                .withTag(HQ_HOST_TAG, host)
                .withTag(DOMAIN_TAG, domain)
                .withTag(AS_USER, restoreFactory.getAsUsername())
                .withExtra(APP_DOWNLOAD_URL_EXTRA, getAppDownloadURL())
                .withExtra(APP_URL_EXTRA, getAppURL())
                .withExtra(USER_SYNC_TOKEN, restoreFactory == null ? "unknown" : restoreFactory.getSyncToken())
                .withExtra(USER_SANDBOX_PATH, restoreFactory == null ? "unknown" : restoreFactory.getSQLiteDB().getDatabaseFileForDebugPurposes())
        );
    }

    public void sendRavenException(Exception exception) {
        sendRavenException(exception, Event.Level.ERROR);
    }

    public void sendRavenException(Exception exception, Event.Level level) {
        if (request != null) {
            setDomain(request.getDomain());

            if (request.getUserDetails() != null) {
                setUserContext(
                        String.valueOf(request.getUserDetails().getDjangoUserId()),
                        request.getUserDetails().getUsername(),
                        request.getRemoteAddr()
                );
            }
        }

        EventBuilder eventBuilder = getDefaultBuilder()
                .withMessage(exception.getMessage())
                .withLevel(level)
                .withSentryInterface(new ExceptionInterface(exception));

        sendRavenEvent(eventBuilder);
    }

    private void sendRavenEvent(EventBuilder event) {
        if (raven == null) {
            return;
        }
        try {
            raven.sendEvent(event);
        } catch (Exception e) {
            log.info("Error sending event to Sentry. Ensure that raven is configured. ", e);
        }
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }
}
