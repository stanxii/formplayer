package beans.menus;

import org.commcare.modern.util.Pair;
import org.commcare.suite.model.Detail;
import org.commcare.suite.model.DetailField;

/**
 * Created by willpride on 5/3/17.
 */
public class CaseTileConfiguration {
    private int maxWidth;
    private int maxHeight;
    private int numEntitiesPerRow;
    private Tile[] tiles;
    private boolean useUniformUnits;

    protected static CaseTileConfiguration buildCaseTileConfiguration(Detail shortDetail) {
        CaseTileConfiguration configuration = new CaseTileConfiguration();
        DetailField[] fields = shortDetail.getFields();
        if (!shortDetail.usesEntityTileView()) {
            return null;
        }
        Tile[] tiles = new Tile[fields.length];
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].isCaseTileField()) {
                tiles[i] = new Tile(fields[i]);
            } else {
                tiles[i] = null;
            }
        }
        configuration.setTiles(tiles);
        configuration.setNumEntitiesPerRow(shortDetail.getNumEntitiesToDisplayPerRow());
        Pair<Integer, Integer> maxWidthHeight = shortDetail.getMaxWidthHeight();
        configuration.setMaxWidth(maxWidthHeight.first);
        configuration.setMaxHeight(maxWidthHeight.second);
        configuration.setUseUniformUnits(shortDetail.useUniformUnitsInCaseTile());
        return configuration;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    public int getNumEntitiesPerRow() {
        return numEntitiesPerRow;
    }

    public void setNumEntitiesPerRow(int numEntitiesPerRow) {
        this.numEntitiesPerRow = numEntitiesPerRow;
    }

    public Tile[] getTiles() {
        return tiles;
    }

    public void setTiles(Tile[] tiles) {
        this.tiles = tiles;
    }

    public boolean isUseUniformUnits() {
        return useUniformUnits;
    }

    public void setUseUniformUnits(boolean useUniformUnits) {
        this.useUniformUnits = useUniformUnits;
    }
}
