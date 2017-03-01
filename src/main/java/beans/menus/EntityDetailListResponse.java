package beans.menus;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import org.commcare.cases.entity.Entity;
import org.commcare.cases.entity.NodeEntityFactory;
import org.commcare.modern.util.Pair;
import org.commcare.suite.model.Detail;
import org.commcare.util.screen.EntityDetailSubscreen;
import org.commcare.util.screen.EntityScreen;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.instance.TreeReference;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by willpride on 1/4/17.
 */
public class EntityDetailListResponse {

    private EntityDetailResponse[] entityDetailList;

    public EntityDetailListResponse() {}

    public EntityDetailListResponse(EntityScreen screen, EvaluationContext ec, TreeReference treeReference) {
        EntityDetailSubscreen[] subscreens = processDetails(screen, ec, treeReference);
        if (subscreens != null) {
            EntityDetailResponse[] responses = new EntityDetailResponse[subscreens.length];
            for (int j = 0; j < subscreens.length; j++) {
                responses[j] = new EntityDetailResponse(subscreens[j]);
                responses[j].setTitle(subscreens[j].getTitles()[j]);
            }
            entityDetailList = responses;
        } else {
            entityDetailList = null;
        }
    }

    private EntityDetailSubscreen[] processDetails(EntityScreen screen, EvaluationContext ec, TreeReference ref) {
        Detail[] detailList = screen.getLongDetailList();
        if (detailList == null || !(detailList.length > 0)) {
            // No details, just return null
            return null;
        }
        EvaluationContext subContext = new EvaluationContext(ec, ref);
        EntityDetailSubscreen[] ret = new EntityDetailSubscreen[detailList.length];
        for (int i = 0; i < detailList.length; i++) {

            if (detailList[i].getNodeset() != null) {
                TreeReference subReference = detailList[i].getNodeset().contextualize(ref);
                EvaluationContext subSubContext = new EvaluationContext(subContext, subReference);
                ret[i] = new EntityDetailSubscreen(i, detailList[i], subSubContext, screen.getDetailListTitles(subSubContext));
            } else {
                ret[i] = new EntityDetailSubscreen(i, detailList[i], subContext, screen.getDetailListTitles(subContext));
            }
        }
        return ret;
    }

    public Pair<List<Entity<TreeReference>>, List<TreeReference>> handleSubnodeDetail(Detail childDetail, TreeReference childReference) {
        NodeEntityFactory factory = new NodeEntityFactory(childDetail, this.getFactoryContext(childReference));
        List<TreeReference> references = factory.expandReferenceList(childReference);
        List<Entity<TreeReference>> full = new ArrayList<>();
        for (TreeReference ref : references) {
            Entity<TreeReference> e = factory.getEntity(ref);
            if (e != null) {
                full.add(e);
            }
        }
        factory.prepareEntities();
        return new Pair<>(full, references);
    }

    @JsonGetter(value = "details")
    public EntityDetailResponse[] getEntityDetailList() {
        return entityDetailList;
    }

    @JsonSetter(value = "details")
    public void setEntityDetailList(EntityDetailResponse[] entityDetailList) {
        this.entityDetailList = entityDetailList;
    }
}
