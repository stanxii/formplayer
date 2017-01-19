package installers;

import org.commcare.api.persistence.SqliteIndexedStorageUtility;
import org.commcare.resources.model.*;
import org.commcare.resources.model.installers.SuiteInstaller;
import org.commcare.suite.model.Suite;
import org.commcare.util.CommCarePlatform;
import org.commcare.xml.SuiteParser;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.reference.Reference;
import org.javarosa.core.services.storage.IStorageUtility;
import org.javarosa.core.services.storage.StorageFullException;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.xml.util.InvalidStructureException;
import org.javarosa.xml.util.UnfullfilledRequirementsException;
import org.xmlpull.v1.XmlPullParserException;
import services.FormplayerStorageFactory;
import services.RestoreFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by willpride on 12/1/16.
 */
public class FormplayerSuiteInstaller extends SuiteInstaller {

    FormplayerStorageFactory storageFactory;
    String dbPath;

    public FormplayerSuiteInstaller(){}

    public FormplayerSuiteInstaller(FormplayerStorageFactory storageFactory, RestoreFactory restoreFactory) {
        this.storageFactory = storageFactory;
        this.dbPath = restoreFactory.getDbPath();
    }

    @Override
    protected IStorageUtility<Suite> storage() {
        if (cacheStorage == null) {
            cacheStorage = storageFactory.newStorage(Suite.STORAGE_KEY, Suite.class);
        }
        return cacheStorage;
    }

    @Override
    public boolean install(Resource r, ResourceLocation location, Reference ref,
                           ResourceTable table, CommCarePlatform instance,
                           boolean upgrade) throws UnresolvedResourceException, UnfullfilledRequirementsException {
        if (location.getAuthority() == Resource.RESOURCE_AUTHORITY_CACHE) {
            //If it's in the cache, we should just get it from there
            return false;
        } else {
            InputStream incoming = null;
            try {
                incoming = ref.getStream();
                SuiteParser parser = new SuiteParser(incoming,
                        table,
                        r.getRecordGuid(),
                        new SqliteIndexedStorageUtility<>(FormInstance.class, storageFactory.getUsername(), "AppFixture", dbPath));
                if (location.getAuthority() == Resource.RESOURCE_AUTHORITY_REMOTE) {
                    parser.setMaximumAuthority(Resource.RESOURCE_AUTHORITY_REMOTE);
                }
                Suite s = parser.parse();
                storage().write(s);
                cacheLocation = s.getID();

                table.commitCompoundResource(r, Resource.RESOURCE_STATUS_INSTALLED);

                // TODO: Add a resource location for r for its cache location
                // so it can be uninstalled appropriately.
                return true;
            } catch (InvalidStructureException e) {
                throw new UnresolvedResourceException(r, e.getMessage(), true);
            } catch (StorageFullException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                throw new UnreliableSourceException(r, e.getMessage());
            } catch (XmlPullParserException e) {
                e.printStackTrace();
                return false;
            } finally {
                try {
                    if (incoming != null) {
                        incoming.close();
                    }
                } catch (IOException e) {
                }
            }
        }
    }

    @Override
    public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
        super.readExternal(in, pf);
        String username = ExtUtil.nullIfEmpty(ExtUtil.readString(in));
        String domain = ExtUtil.nullIfEmpty(ExtUtil.readString(in));
        String appId = ExtUtil.nullIfEmpty(ExtUtil.readString(in));
        storageFactory = new FormplayerStorageFactory();
        storageFactory.configure(username, domain, appId);
        dbPath = ExtUtil.nullIfEmpty(ExtUtil.readString(in));
    }

    @Override
    public void writeExternal(DataOutputStream out) throws IOException {
        super.writeExternal(out);
        ExtUtil.writeString(out, ExtUtil.emptyIfNull(storageFactory.getUsername()));
        ExtUtil.writeString(out, ExtUtil.emptyIfNull(storageFactory.getDomain()));
        ExtUtil.writeString(out, ExtUtil.emptyIfNull(storageFactory.getAppId()));
        ExtUtil.writeString(out, ExtUtil.emptyIfNull(dbPath));
    }
}
