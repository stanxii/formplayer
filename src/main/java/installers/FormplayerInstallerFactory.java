package installers;

import org.commcare.resources.model.InstallerFactory;
import org.commcare.resources.model.ResourceInstaller;
import org.commcare.resources.model.installers.LocaleFileInstaller;
import org.commcare.resources.model.installers.LoginImageInstaller;
import org.commcare.resources.model.installers.MediaInstaller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import services.FormplayerStorageFactory;

/**
 * Overrides InstallerFactory to point to Formplayer's own installer classes.
 * The primary difference is that Formplayer overrides the storage() call to point to its own storage factory class
 */
@Component
public class FormplayerInstallerFactory extends InstallerFactory {

    @Autowired
    FormplayerStorageFactory storageFactory;

    public ResourceInstaller getProfileInstaller(boolean forceInstall) {
        return new FormplayerProfileInstaller(forceInstall);
    }

    @Override
    public ResourceInstaller getXFormInstaller() {
        return new FormplayerXFormInstaller();
    }

    public ResourceInstaller getUserRestoreInstaller() {
        return new FormplayerOfflineUserRestoreInstaller();
    }

    public ResourceInstaller getSuiteInstaller() {
        return new FormplayerSuiteInstaller();
    }

    public ResourceInstaller getLocaleFileInstaller(String locale) {
        return new LocaleFileInstaller(locale);
    }

    public ResourceInstaller getLoginImageInstaller() {
        return new LoginImageInstaller();
    }

    public ResourceInstaller getMediaInstaller(String path) {
        return new MediaInstaller();
    }
}
