package aspects;

import beans.InstallFromSessionRequestBean;
import beans.InstallRequestBean;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import services.FormplayerStorageFactory;
import util.FormplayerSentry;

import java.util.Arrays;

/**
 * Aspect to configure the FormplayerStorageManager
 */
@Aspect
@Order(6)
public class AppInstallAspect {

    @Autowired
    protected FormplayerStorageFactory storageFactory;

    @Autowired
    private FormplayerSentry raven;

    @Before(value = "@annotation(annotations.AppInstall)")
    public void configureStorageFactory(JoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        if (!(args[0] instanceof InstallRequestBean)) {
            throw new RuntimeException("Could not configure StorageFactory with args " + Arrays.toString(args));
        }
        final InstallRequestBean requestBean = (InstallRequestBean) args[0];
        storageFactory.configure(requestBean);

        raven.newBreadcrumb()
                .setData(
                        "appId", requestBean.getAppId(),
                        "installReference", requestBean.getInstallReference(),
                        "locale", requestBean.getLocale()
                )
                .setCategory("app_info")
                .record();
        raven.setAppId(requestBean.getAppId());
    }
}
