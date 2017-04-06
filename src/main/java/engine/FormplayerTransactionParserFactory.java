package engine;

import org.commcare.xml.FixtureXmlParser;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.services.storage.IStorageUtilityIndexed;
import sandbox.UserSqlSandbox;
import org.commcare.cases.model.Case;
import org.commcare.core.parse.CommCareTransactionParserFactory;
import org.commcare.data.xml.TransactionParser;
import org.commcare.data.xml.TransactionParserFactory;
import org.commcare.xml.CaseXmlParser;
import org.kxml2.io.KXmlParser;
import parsers.FormplayerCaseXmlParser;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Created by willpride on 3/6/17.
 */
public class FormplayerTransactionParserFactory extends CommCareTransactionParserFactory {

    //final private Context context;
    //final private HttpRequestEndpoints generator;
    final private ArrayList<String> createdAndUpdatedCases = new ArrayList<>();

    private TransactionParserFactory formInstanceParser;
    private boolean caseIndexesWereDisrupted = false;

    /**
     * A mapping from an installed form's namespace its install path.
     */
    private Hashtable<String, String> formInstanceNamespaces;

    public FormplayerTransactionParserFactory(UserSqlSandbox sandbox) {
        super(sandbox);
    }

    @Override
    public TransactionParser getParser(KXmlParser parser) {
        String namespace = parser.getNamespace();
        if (namespace != null && formInstanceNamespaces != null && formInstanceNamespaces.containsKey(namespace)) {
            req();
            return formInstanceParser.getParser(parser);
        }
        return super.getParser(parser);
    }

    public void initFixtureParser() {
        fixtureParser = new TransactionParserFactory() {
            FixtureXmlParser created = null;

            @Override
            public TransactionParser getParser(KXmlParser parser) {
                if (created == null) {
                    created = new FixtureXmlParser(parser, true,
                            ((UserSqlSandbox)sandbox).getUserFixtureStorage()) {
                        private IStorageUtilityIndexed<FormInstance> fixtureStorage;

                        @Override
                        public IStorageUtilityIndexed<FormInstance> storage() {
                            if (fixtureStorage == null) {
                                fixtureStorage = sandbox.getUserFixtureStorage();
                            }
                            return fixtureStorage;
                        }
                    };
                }

                return created;
            }
        };
    }

    @Override
    public void initCaseParser() {
        caseParser = new TransactionParserFactory() {
            CaseXmlParser created = null;

            @Override
            public TransactionParser<Case> getParser(KXmlParser parser) {
                if (created == null) {
                    created = new FormplayerCaseXmlParser(parser, true, (UserSqlSandbox) sandbox) {

                        @Override
                        public void onIndexDisrupted(String caseId) {
                            caseIndexesWereDisrupted = true;
                        }

                        @Override
                        protected void onCaseCreateUpdate(String caseId) {
                            createdAndUpdatedCases.add(caseId);
                        }
                    };
                }

                return created;
            }
        };
    }

    public ArrayList<String> getCreatedAndUpdatedCases() {
        return createdAndUpdatedCases;
    }

    public boolean wereCaseIndexesDisrupted() {
        return caseIndexesWereDisrupted;
    }
}
