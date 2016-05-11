package beans;

import org.json.JSONArray;
import session.FormSession;

/**
 * Return the current state of the form entry session (tree, languages, title)
 *
 * Created by willpride on 1/20/16.
 */
public class CurrentResponseBean extends SessionBean {
    private JSONArray tree;
    private String title;
    private String[] langs;

    // our JSON-Object mapping lib (Jackson) requires a default constructor
    public CurrentResponseBean(){}

    public CurrentResponseBean(FormSession session){
        tree = session.getFormTree();
        title = session.getTitle();
        langs = session.getLanguages();
        sessionId = session.getSessionId();
    }

    public String getTree() {
        return tree.toString();
    }

    public void setTree(JSONArray tree) {
        this.tree = tree;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String[] getLangs() {
        return langs;
    }
    public void setLangs(String[] langs) {
        this.langs = langs;
    }

    @Override
    public String toString(){
        return "CurrentResponseBean: [sessionId=" + sessionId + "]";
    }
}