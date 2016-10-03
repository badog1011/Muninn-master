package studio.bachelor.draft.utility;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * Created by BACHELOR on 2016/04/06.
 */
public class BitmapMD5Encoder implements Runnable {
    final Bitmap bitmap;
    private String MD5 = "";

    public BitmapMD5Encoder(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public void run() {
        if (bitmap != null) {
            try {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] bytes = stream.toByteArray();
                MessageDigest md = MessageDigest.getInstance("MD5");
                md.update(bytes);
                String hash = new BigInteger(1, md.digest()).toString(16);
                MD5 = hash.toUpperCase();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getResult() {
        return MD5;
    }
}
