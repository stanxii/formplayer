package beans.menus;

import org.commcare.modern.util.Pair;
import org.commcare.suite.model.Detail;
import org.commcare.suite.model.EntityDatum;
import org.commcare.util.screen.EntityDetailSubscreen;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.instance.TreeReference;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

/**
 * Represents one detail tab in a case details page.
 */
public class EntityDetailResponse {
    protected Object[] details;
    private EntityBean[] entities;
    protected Style[] styles;
    protected String[] headers;
    protected String title;
    protected boolean isUseNodeset;

    private CaseTileConfiguration caseTileConfiguration;

    public EntityDetailResponse() {}

    public EntityDetailResponse(EntityDetailSubscreen entityScreen, String title) {
        this.title = title;
        this.details = entityScreen.getData();
        this.headers = entityScreen.getHeaders();
        this.styles = EntityListResponse.processStyles(entityScreen.getDetail());
    }

    // Constructor used for persistent case tile
    public EntityDetailResponse(Detail detail, EvaluationContext ec) {
        this(new EntityDetailSubscreen(0, detail, ec, new String[]{}), "Details");
        processCaseTiles(detail);
        EntityListResponse.processStyles(detail);
    }

    // Constructor used for detail with nodeset
    public EntityDetailResponse(Detail detail,
                                Vector<TreeReference> references,
                                EvaluationContext ec,
                                EntityDatum neededDatum,
                                String title) {
        List<EntityBean> entityList = EntityListResponse.processEntitiesForCaseList(detail, references, ec, null, neededDatum);
        this.entities = new EntityBean[entityList.size()];
        entityList.toArray(this.entities);
        this.title = title;
        this.styles = EntityListResponse.processStyles(detail);
        Pair<String[], int[]> pair = EntityListResponse.processHeader(detail, ec);
        setHeaders(pair.first);
        setUseNodeset(true);
    }

    protected void processCaseTiles(Detail shortDetail) {
        CaseTileConfiguration configuration = CaseTileConfiguration.buildCaseTileConfiguration(shortDetail);
        if (configuration == null) {
            return;
        }
        setCaseTileConfiguration(configuration);
    }

    public Object[] getDetails() {
        return details;
    }

    public void setDetails(Object[] data) {
        this.details = data;
    }

    public Style[] getStyles() {
        return styles;
    }

    public void setStyles(Style[] styles) {
        this.styles = styles;
    }

    public String[] getHeaders() {
        return headers;
    }

    public void setHeaders(String[] headers) {
        this.headers = headers;
    }

    @Override
    public String toString() {
        return "EntityDetailResponse [details=" + Arrays.toString(details)
                + ", styles=" + Arrays.toString(styles)
                + ", headers=" + Arrays.toString(headers) + "]";
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isUseNodeset() {
        return isUseNodeset;
    }

    public void setUseNodeset(boolean useNodeset) {
        isUseNodeset = useNodeset;
    }

    public EntityBean[] getEntities() {
        return entities;
    }

    public void setEntities(EntityBean[] entities) {
        this.entities = entities;
    }

    public CaseTileConfiguration getCaseTileConfiguration() {
        return caseTileConfiguration;
    }

    public void setCaseTileConfiguration(CaseTileConfiguration caseTileConfiguration) {
        this.caseTileConfiguration = caseTileConfiguration;
    }
}
