package utils;

import install.FormplayerConfigEngine;
import org.commcare.resources.model.InstallCancelledException;
import org.commcare.resources.model.UnresolvedResourceException;
import org.javarosa.xml.util.UnfullfilledRequirementsException;
import services.InstallService;

import java.io.IOException;

/**
 * Created by benrudolph on 8/21/16.
 */
public class MockInstallServiceImpl implements InstallService {

     private String archivePath;
     public MockInstallServiceImpl(String archivePath) {
         this.archivePath = archivePath;
     }

     @Override
     public FormplayerConfigEngine configureApplication(String reference, String username, String dbPath) throws IOException, InstallCancelledException, UnresolvedResourceException, UnfullfilledRequirementsException {
         FormplayerConfigEngine engine = new FormplayerConfigEngine(username, dbPath);
         engine.initFromArchive(this.archivePath);
         engine.initEnvironment();
         return engine;
     }

    @Override
    public String download(String reference) {
        return this.archivePath;
    }
}
