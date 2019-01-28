package sg.gov.dsta.mobileC3.ventilo.util;

import android.content.res.AssetManager;
import android.graphics.Rect;
import android.util.SparseArray;

import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class CustomMapTileProvider implements TileProvider {
    private static final int TILE_WIDTH = 256;
    private static final int TILE_HEIGHT = 256;
    private static final int BUFFER_SIZE = 16 * 1024;
    private static final SparseArray<Rect> TILE_ZOOMS = new SparseArray<Rect>() {{
        put(2,  new Rect(3,  1,  3,  1 ));
        put(3,  new Rect(6,  3,  6,  3 ));
        put(4,  new Rect(12,  7,  12,  7 ));
        put(5,  new Rect(25,  15,  25,  15 ));
        put(6,  new Rect(50,  31,  50,  31 ));
        put(7,  new Rect(100,  63,  101,  63 ));
        put(8,  new Rect(201,  126,  202,  127 ));
        put(9,  new Rect(403,  253,  404,  254 ));
        put(10, new Rect(806,  507,  809,  508 ));
        put(11, new Rect(1612, 1014, 1619, 1017));
        put(12, new Rect(3224, 2028, 3239, 2034));
        put(13, new Rect(6449, 4057, 6478, 4069));
        put(14, new Rect(12899, 8115, 12957, 8139));
        put(15, new Rect(25798, 16231, 25915, 16278));
        put(16, new Rect(51597, 32463, 51830, 32556));
    }};

    private AssetManager mAssets;

    public CustomMapTileProvider(AssetManager assets) {
        mAssets = assets;
    }

    @Override
    public Tile getTile(int x, int y, int zoom) {
        if (hasTile(x, y, zoom)) {
            byte[] image = readTileImage(x, y, zoom);
            return image == null ? null : new Tile(TILE_WIDTH, TILE_HEIGHT, image);
        } else {
            return NO_TILE;
        }
    }

    private byte[] readTileImage(int x, int y, int zoom) {
        InputStream in = null;
        ByteArrayOutputStream buffer = null;

        try {
            in = mAssets.open(getTileFilename(x, y, zoom));
            buffer = new ByteArrayOutputStream();

            int nRead;
            byte[] data = new byte[BUFFER_SIZE];

            while ((nRead = in.read(data, 0, BUFFER_SIZE)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();

            return buffer.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        } finally {
            if (in != null) try { in.close(); } catch (Exception ignored) {}
            if (buffer != null) try { buffer.close(); } catch (Exception ignored) {}
        }
    }

    private String getTileFilename(int x, int y, int zoom) {
        return "map/" + zoom + '/' + x + '/' + y + ".png";
    }

    private boolean hasTile(int x, int y, int zoom) {
        Rect b = TILE_ZOOMS.get(zoom);
        return b == null ? false : (b.left <= x && x <= b.right && b.top <= y && y <= b.bottom);
    }
}