package sg.gov.dsta.mobileC3.ventilo.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;

import com.nutiteq.components.MapTile;
import com.nutiteq.components.TileBitmap;
import com.nutiteq.log.Log;
import com.nutiteq.projections.Projection;
import com.nutiteq.rasterdatasources.StreamRasterDataSource;
import com.nutiteq.utils.Utils;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class CustomRasterDataSource extends StreamRasterDataSource {

    private static final String MAP = "map";
    private static final String VESSEL = "vessel";

    protected Context mContext;

    private int mImageType;

    /**
     *
     * @param projection
     * @param minZoom
     * @param maxZoom
     * @param context
     * @param imageType - 1 represents Map tile images, 2 represents Vessel image
     */
    public CustomRasterDataSource(Projection projection, int minZoom, int maxZoom, Context context, int imageType) {
        super(projection, minZoom, maxZoom);
        this.mContext = context;
        mImageType = imageType;
    }

    protected String buildTilePath(MapTile tile) {
        StringBuilder sb = new StringBuilder();

        if (mImageType == 1) {
            sb.append(MAP);
            sb.append("/");
            sb.append(String.valueOf(tile.zoom));
            sb.append("/");
            sb.append(String.valueOf(tile.x));
            sb.append("/");
            sb.append(String.valueOf(tile.y));
            sb.append(".png");

        } else {
            sb.append(VESSEL);
            sb.append("/");
        }

        return sb.toString();
    }

    public TileBitmap loadTile(MapTile tile) {
        String path = this.buildTilePath(tile);
        Log.info(this.getClass().getName() + ": loading tile " + path);
        InputStream inputStream = null;
        try {
            inputStream = new DataInputStream(mContext.getResources().getAssets().open(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this.readTileBitmap(inputStream);
    }
}
