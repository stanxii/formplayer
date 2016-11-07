package services;

import application.SQLiteProperties;
import auth.HqAuth;
import beans.CaseBean;
import exceptions.AsyncRetryException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commcare.api.persistence.SqlSandboxUtils;
import org.commcare.api.persistence.SqliteIndexedStorageUtility;
import org.commcare.api.persistence.UserSqlSandbox;
import org.commcare.cases.model.Case;
import org.commcare.core.sandbox.SandboxUtils;
import org.commcare.modern.database.TableBuilder;
import org.commcare.modern.parse.ParseUtilsHelper;
import org.javarosa.core.api.ClassNameHasher;
import org.javarosa.core.model.User;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.services.storage.IStorageIterator;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.xml.util.InvalidStructureException;
import org.javarosa.xml.util.UnfullfilledRequirementsException;
import org.javarosa.xpath.XPathParseTool;
import org.javarosa.xpath.expr.XPathFuncExpr;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParserException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Factory that determines the correct URL endpoint based on domain, host, and username/asUsername,
 * then retrieves and returns the restore XML.
 */
public class RestoreFactory {
    @Value("${commcarehq.host}")
    private String host;

    private String asUsername;
    private String username;
    private String domain;
    private HqAuth hqAuth;

    private String cachedRestore;

    private final Log log = LogFactory.getLog(RestoreFactory.class);

    public String getPath() {
        if (asUsername != null){
            return SQLiteProperties.getDataDir() + domain + "/" + username + "/" + asUsername + ".db";
        }
        return SQLiteProperties.getDataDir() + domain + "/" + username + ".db";
    }

    public UserSqlSandbox getUserSqlSandbox() {
        if(asUsername == null) {
            return new UserSqlSandbox(username, SQLiteProperties.getDataDir() + domain);
        }
        return new UserSqlSandbox(asUsername, SQLiteProperties.getDataDir() + domain + "/" + username);
    }

    public UserSqlSandbox forceRestore() throws Exception {
        new File(getPath()).delete();
        return restoreIfNotExists();
    }

    public UserSqlSandbox restoreIfNotExists() throws Exception{
        File db = new File(getPath());
        if(db.exists()){
            return getUserSqlSandbox();
        } else{
            db.getParentFile().mkdirs();
            return restoreUser();
        }
    }

    public String filterCases(String filterExpression) {
        try {
            String filterPath = "join(',', instance('casedb')/casedb/case" + filterExpression + "/@case_id)";
            UserSqlSandbox mSandbox = restoreIfNotExists();
            EvaluationContext mContext = SandboxUtils.getInstanceContexts(mSandbox, "casedb", "jr://instance/casedb");
            return XPathFuncExpr.toString(XPathParseTool.parseXPath(filterPath).eval(mContext));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "null";
    }

    public static CaseBean getFullCase(String caseId, SqliteIndexedStorageUtility<Case> caseStorage){
        Case cCase = caseStorage.getRecordForValue("case-id", caseId);
        return new CaseBean(cCase);
    }

    public CaseBean[] filterCasesFull(String filterExpression) throws Exception{
        String filterPath = "join(',', instance('casedb')/casedb/case" + filterExpression + "/@case_id)";
        UserSqlSandbox mSandbox = restoreIfNotExists();
        EvaluationContext mContext = SandboxUtils.getInstanceContexts(mSandbox, "casedb", "jr://instance/casedb");
        String filteredCases = XPathFuncExpr.toString(XPathParseTool.parseXPath(filterPath).eval(mContext));
        String[] splitCases = filteredCases.split(",");
        CaseBean[] ret = new CaseBean[splitCases.length];
        SqliteIndexedStorageUtility<Case> caseStorage = mSandbox.getCaseStorage();
        int count = 0;
        for(String cCase: splitCases){
            CaseBean caseBean = getFullCase(cCase, caseStorage);
            ret[count] = caseBean;
            count++;
        }
        return ret;
    }

    private UserSqlSandbox restoreUser() throws
            UnfullfilledRequirementsException, InvalidStructureException, IOException, XmlPullParserException {
        UserSqlSandbox mSandbox = getUserSqlSandbox();
        PrototypeFactory.setStaticHasher(new ClassNameHasher());
        ParseUtilsHelper.parseXMLIntoSandbox(getRestoreXml(), mSandbox);
        // initialize our sandbox's logged in user
        for (IStorageIterator<User> iterator = mSandbox.getUserStorage().iterate(); iterator.hasMore(); ) {
            User u = iterator.nextRecord();
            if (username.equalsIgnoreCase(u.getUsername())) {
                mSandbox    .setLoggedInUser(u);
            }
        }
        return mSandbox;
    }

    public String getRestoreXml() {

        if(cachedRestore != null) {
            return cachedRestore;
        }

        if (domain == null || (username == null && asUsername == null)) {
            throw new RuntimeException("Domain and one of username or asUsername must be non-null. " +
                    " Domain: " + domain +
                    ", username: " + username +
                    ", asUsername: " + asUsername);
        }
        String restoreUrl;
        if (asUsername == null) {
            restoreUrl = getRestoreUrl(host, domain);
        } else {
            restoreUrl = getRestoreUrl(host, domain, asUsername);
        }

        log.info("Restoring from URL " + restoreUrl);
        cachedRestore = getRestoreXmlHelper(restoreUrl, hqAuth);
        return cachedRestore;
    }

    /**
     * Given an async restore xml response, this function throws an AsyncRetryException
     * with meta data about the async restore.
     *
     * @param xml - Async restore response
     * @param headers - HttpHeaders from the restore response
     */
    private void handleAsyncRestoreResponse(String xml, HttpHeaders headers) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        ByteArrayInputStream input;
        Document doc;

        // Create the XML Document builder
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("Unable to instantiate document builder");
        }

        // Parse the xml into a utf-8 byte array
        try {
            input = new ByteArrayInputStream(xml.getBytes("utf-8") );
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unable to parse async restore response.");
        }

        // Build an XML document
        try {
            doc = builder.parse(input);
        } catch (SAXException e) {
            throw new RuntimeException("Unable to parse into XML Document");
        } catch (IOException e) {
            throw new RuntimeException("Unable to parse into XML Document");
        }

        NodeList messageNodes = doc.getElementsByTagName("message");
        NodeList progressNodes = doc.getElementsByTagName("progress");

        assert messageNodes.getLength() == 1;
        assert progressNodes.getLength() == 1;

        String message = messageNodes.item(0).getTextContent();
        Node progressNode = progressNodes.item(0);
        NamedNodeMap attributes = progressNode.getAttributes();

        throw new AsyncRetryException(
                message,
                Integer.parseInt(attributes.getNamedItem("done").getTextContent()),
                Integer.parseInt(attributes.getNamedItem("total").getTextContent()),
                Integer.parseInt(headers.get("retry-after").get(0))
        );
    }

    private String getRestoreXmlHelper(String restoreUrl, HqAuth auth) {
        RestTemplate restTemplate = new RestTemplate();
        log.info("Restoring at domain: " + domain + " with auth: " + auth);
        HttpHeaders headers = auth.getAuthHeaders();
        headers.add("x-openrosa-version",  "2.0");
        ResponseEntity<String> response = restTemplate.exchange(
                restoreUrl,
                HttpMethod.GET,
                new HttpEntity<String>(headers),
                String.class
        );
        if (response.getStatusCode().value() == 202) {
            handleAsyncRestoreResponse(response.getBody(), response.getHeaders());
        }
        return response.getBody();
    }

    public static String getRestoreUrl(String host, String domain){
        return host + "/a/" + domain + "/phone/restore/?version=2.0";
    }

    public String getRestoreUrl(String host, String domain, String username) {
        return host + "/a/" + domain + "/phone/restore/?as=" + username + "@" +
                domain + "&version=2.0";
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = TableBuilder.scrubName(username);
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public HqAuth getHqAuth() {
        return hqAuth;
    }

    public void setHqAuth(HqAuth hqAuth) {
        this.hqAuth = hqAuth;
    }

    public String getAsUsername() {
        return asUsername;
    }

    public void setAsUsername(String asUsername) {
        this.asUsername = asUsername;
    }

    public String getCachedRestore() {
        return cachedRestore;
    }

    public void setCachedRestore(String cachedRestore) {
        this.cachedRestore = cachedRestore;
    }
}
