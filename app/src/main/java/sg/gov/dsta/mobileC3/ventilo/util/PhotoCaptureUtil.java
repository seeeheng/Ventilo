package sg.gov.dsta.mobileC3.ventilo.util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore.Images;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import sg.gov.dsta.mobileC3.ventilo.R;

/**
 * Android internals have been modified to store images in the media folder with
 * the correct date meta data
 */
public class PhotoCaptureUtil {

    private static final String TAG = PhotoCaptureUtil.class.getSimpleName();
    private static final int TEMP_STORAGE_SPACE_SIZE = 16 * 1024;
    public static final int PHOTO_GALLERY_REQUEST_CODE = 1000;
    public static final int OPEN_CAMERA_REQUEST_CODE = 2000;
    public static final float MAX_SCALED_WIDTH_OF_DISPLAY_IN_PIXEL = 1024f;

    /**
     * A copy of the Android internals  insertImage method, this method populates the
     * meta data with DATE_ADDED and DATE_TAKEN. This fixes a common problem where media
     * that is inserted manually gets saved at the end of the gallery (because date is not populated).
     *
     * @see android.provider.MediaStore.Images.Media#insertImage(ContentResolver, Bitmap, String, String)
     */
    public static final String insertImage(ContentResolver cr,
                                           Bitmap source,
                                           String title,
                                           String description) {

        ContentValues values = new ContentValues();
        values.put(Images.Media.BUCKET_DISPLAY_NAME, title);
        values.put(Images.Media.DISPLAY_NAME, title);
        values.put(Images.Media.TITLE, title);
        values.put(Images.Media.DESCRIPTION, description);
        values.put(Images.Media.MIME_TYPE, "image/jpeg");
        // Add the date meta data to ensure the image is added at the front of the gallery
        values.put(Images.Media.DATE_ADDED, System.currentTimeMillis());
        values.put(Images.Media.DATE_TAKEN, System.currentTimeMillis());

        Uri url = null;
        String stringUrl = null;    /* value to be returned */

        try {
            url = cr.insert(Images.Media.EXTERNAL_CONTENT_URI, values);

            if (source != null) {
                OutputStream imageOut = cr.openOutputStream(url);
                try {
                    source.compress(Bitmap.CompressFormat.JPEG, 50, imageOut);
                } finally {
                    imageOut.close();
                }

                long id = ContentUris.parseId(url);
                // Wait until MINI_KIND thumbnail is generated.
                Bitmap miniThumb = Images.Thumbnails.getThumbnail(cr, id, Images.Thumbnails.MINI_KIND, null);
                // This is for backward compatibility.
                storeThumbnail(cr, miniThumb, id, 50F, 50F, Images.Thumbnails.MICRO_KIND);
            } else {
                cr.delete(url, null, null);
                url = null;
            }
        } catch (Exception e) {
            if (url != null) {
                cr.delete(url, null, null);
                url = null;
            }
        }

        if (url != null) {
            stringUrl = url.toString();
        }

        return stringUrl;
    }

    /**
     * A copy of the Android internals StoreThumbnail method, it is used with the insertImage to
     * populate the android.provider.MediaStore.Images.Media#insertImage with all the correct
     * meta data. The StoreThumbnail method is private so it must be duplicated here.
     *
     * @see android.provider.MediaStore.Images.Media (StoreThumbnail private method)
     */
    private static final Bitmap storeThumbnail(
            ContentResolver cr,
            Bitmap source,
            long id,
            float width,
            float height,
            int kind) {

        // create the matrix to scale it
        Matrix matrix = new Matrix();

        float scaleX = width / source.getWidth();
        float scaleY = height / source.getHeight();

        matrix.setScale(scaleX, scaleY);

        Bitmap thumb = Bitmap.createBitmap(source, 0, 0,
                source.getWidth(),
                source.getHeight(), matrix,
                true
        );

        ContentValues values = new ContentValues(4);
        values.put(Images.Thumbnails.KIND, kind);
        values.put(Images.Thumbnails.IMAGE_ID, (int) id);
        values.put(Images.Thumbnails.HEIGHT, thumb.getHeight());
        values.put(Images.Thumbnails.WIDTH, thumb.getWidth());

        Uri url = cr.insert(Images.Thumbnails.EXTERNAL_CONTENT_URI, values);

        try {
            OutputStream thumbOut = cr.openOutputStream(url);
            thumb.compress(Bitmap.CompressFormat.JPEG, 100, thumbOut);
            thumbOut.close();
            return thumb;
        } catch (FileNotFoundException ex) {
            return null;
        } catch (IOException ex) {
            return null;
        }
    }

    /**
     * This creates a temporary file for image display of original quality
     *
     * @param context
     * @return
     * @throws IOException
     */
    public static File createImageFile(Context context) throws IOException {
        String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss",
                        Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",     /* suffix */
                storageDir      /* directory */
        );

        return image;
    }

    /**
     * Displayed image is set at 400dp x 250dp
     *
     * @param context
     * @param filePath
     * @return
     */
    public static Bitmap resizeImage(Context context, String filePath) {

//        String filePath = getRealPathFromURI(context, imageUri); ('imageUri' parameter changed to 'filePath')
        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

        // By setting this field as true, the actual bitmap pixels are not loaded in the memory.
        // Just the bounds are loaded. If you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

        int actualWidth = options.outWidth;
        int actualHeight = options.outHeight;

        // maxHeight and maxWidth values of the compressed image is taken as 400dp x 250dp
        float maxWidth = DimensionUtil.convertDpsToPixel((int) context.getResources().
                getDimension(R.dimen.sitrep_picture_width));
        float maxHeight = DimensionUtil.convertDpsToPixel((int) context.getResources().
                getDimension(R.dimen.sitrep_picture_height));
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

        // Width and height values are set maintaining the aspect ratio of the image
        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;
            }
        }

        // Setting inSampleSize value allows to load a scaled down version of the original image
        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

        // inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

        // These options allow android to claim the bitmap memory if it runs low on memory
        options.inBitmap = bmp;
        options.inTempStorage = new byte[TEMP_STORAGE_SPACE_SIZE];

        // Load the bitmap from its path
        try {
            bmp = BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError exception) {
            Log.d(TAG, "BitmapFactory decodeFile failed. " + exception.toString());

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            Log.d(TAG, "Bitmap createBitmap failed. " + exception.toString());
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

        // Check the rotation of the image and display it properly
        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Log.d(TAG, "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                Log.d(TAG, "Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                Log.d(TAG, "Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                Log.d(TAG, "Exif: " + orientation);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return scaledBitmap;
    }

    public static String storeFileIntoPhoneMemory(Bitmap scaledBitmapToClone, int quality) {
        return storeFileIntoPhoneMemory(scaledBitmapToClone, getFilename(), quality);
    }

    /**
     * Compresses image file without losing quality
     *
     * @param fileName
     * @return
     */
    public static File compressBitmapToFile(String fileName){
        try {

            File file = new File(fileName);
            // BitmapFactory options to downsize the image
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            o.inSampleSize = 6;
            // factor of downsizing the image

            FileInputStream inputStream = new FileInputStream(file);
            //Bitmap selectedBitmap = null;
            BitmapFactory.decodeStream(inputStream, null, o);
            inputStream.close();

            // The new size we want to scale to
            final int REQUIRED_SIZE = 60;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while(o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            inputStream = new FileInputStream(file);

            Bitmap selectedBitmap = BitmapFactory.decodeStream(inputStream, null, o2);
            inputStream.close();

            // Override original image file with compressed version
            file.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(file);

            selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 100 , outputStream);

            return file;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Stores file into internal phone memory of given filename
     *
     * @param scaledBitmapToClone
     * @param filename
     * @param quality
     */
    public static String storeFileIntoPhoneMemory(Bitmap scaledBitmapToClone, String filename, int quality) {
        FileOutputStream out;
        try {
            out = new FileOutputStream(filename);

            // Write the compressed bitmap at the destination specified by filename.
            scaledBitmapToClone.compress(Bitmap.CompressFormat.PNG, quality, out);

        } catch (FileNotFoundException e) {
            Log.d(TAG, "FileOutputStream creation failed. " + e.toString());
        }

        return filename;
    }

    /**
     * Creates file name for storage in default 'Pictures' folder
     *
     * @return
     */
    private static String getFilename() {
        File file = new File(Environment.getExternalStorageDirectory().getPath(), "Pictures");

        if (!file.exists()) {
            file.mkdirs();
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        String uriString = (file.getAbsolutePath() + "/" + timeStamp + ".png");
        return uriString;

    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        System.out.println("inSampleSize: " + inSampleSize);

        return inSampleSize;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for MediaStore Uris
     * and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getRealPathFromURI(Context context, Uri uri,
                                            String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection,
                    selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return "";
    }

    /**
     * Convert bitmap to byte array
     *
     * @param scaledBitmapToStream
     * @param quality
     * @return
     */
    public static byte[] getByteArrayFromImage(Bitmap scaledBitmapToStream, int quality) {
        FileOutputStream out;

        // Convert bitmap to byte array
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        // Write compressed bitmap to the stream destination
        scaledBitmapToStream.compress(Bitmap.CompressFormat.JPEG, quality, stream);

        return stream.toByteArray();
    }

    /**
     * Convert from byte array to bitmap
     * @param image
     * @return
     */
    public static Bitmap getImageFromByteArray(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

}
