package sg.gov.dh.utils;

import android.content.Context;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class FileSaver {

    String TAG = "FILESAVER";
//    PrintWriter out;
    FileOutputStream out;
    public FileSaver(Context context, String filepath) throws IOException {
        File path = context.getFilesDir();
        Log.d(TAG,"Abs path" + path.getAbsolutePath());
        File file = new File(path, filepath);
        out = new FileOutputStream(file, true);
//        this.out = new PrintWriter(new BufferedWriter(new FileWriter(filepath, true)));
    }

    public void close() throws IOException {
        this.out.close();
    }

    public void write(String text) throws IOException {
//        if (this.out!=null)
//        {
//            this.out.println(text);
//            Log.d(TAG,"FileSaver not initialised");
//        }

        if (this.out!=null)
        {

            out.write(text.getBytes());
            out.write("\n".getBytes());
        }


    }
}
