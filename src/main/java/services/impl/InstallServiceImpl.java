package services.impl;

import install.FormplayerConfigEngine;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commcare.resources.model.InstallCancelledException;
import org.commcare.resources.model.UnresolvedResourceException;
import org.javarosa.core.io.BufferedInputStream;
import org.javarosa.core.io.StreamsUtil;
import org.javarosa.xml.util.UnfullfilledRequirementsException;
import services.InstallService;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by willpride on 2/25/16.
 */
public class InstallServiceImpl implements InstallService {

    private final Log log = LogFactory.getLog(InstallServiceImpl.class);

    private final String host;

    public InstallServiceImpl(String host){
        this.host = host;
    }

    @Override
    public FormplayerConfigEngine configureApplication(String cczFilePath, String username, String dbPath) throws IOException, InstallCancelledException, UnresolvedResourceException, UnfullfilledRequirementsException {
        log.info("Configuring application with ccz " + cczFilePath + " and dbPath: " + dbPath + ".");
        FormplayerConfigEngine engine = new FormplayerConfigEngine(username, dbPath);
        engine.initFromArchive(cczFilePath);
        engine.initEnvironment();
        return engine;
    }

    @Override
    public String download(String reference) {
        try {
            URL url = new URL(reference);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setInstanceFollowRedirects(true);  //you still need to handle redirect manully.
            HttpURLConnection.setFollowRedirects(true);

            File file = File.createTempFile("commcare_", ".ccz");

            FileOutputStream fos = new FileOutputStream(file);
            StreamsUtil.writeFromInputToOutput(new BufferedInputStream(conn.getInputStream()), fos);
            return file.getAbsolutePath();
        } catch (IOException e) {
            log.error("Issue downloading or create stream for " + reference);
            e.printStackTrace();
            System.exit(-1);
            return null;
        }
    }
}
