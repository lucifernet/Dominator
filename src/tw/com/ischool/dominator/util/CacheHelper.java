package tw.com.ischool.dominator.util;

import java.io.File;
import java.io.FileOutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class CacheHelper {
	public static Bitmap loadImage(Context context, String filename) {
		File file = getCacheFullFileName(context, filename);

		if (file.exists()) {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;
			Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(),
					options);
			return bitmap;
		}
		return null;
	}

	public static void cacheImage(Context context, String filename,
			Bitmap bitmap) {
		File file = getCacheFullFileName(context, filename);
		try {
			FileOutputStream out = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
			out.close();
		} catch (Exception e) {

		}
	}

	public static File getCacheFullFileName(Context context, String filename) {
		File dir = context.getExternalCacheDir();
		File imgDir = new File(dir, "images");
		imgDir.mkdirs();
		return new File(imgDir, filename);
	}
}
