package tw.com.ischool.dominator.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Base64;

public class StringUtil {
	public static final String EMPTY = "";
	public static final String WHITESPACE = " ";

	public static Boolean isNullOrWhitespace(String string) {
		if (string == null)
			return true;

		if (string.equalsIgnoreCase("null"))
			return true;

		String trimed = string.trim();
		return TextUtils.isEmpty(trimed);
	}

	public static String getExceptionMessage(Throwable ex) {
		StringBuilder sb = new StringBuilder(ex.getClass().getSimpleName())
				.append(":");
		if (ex.getMessage() != null) {
			sb.append(ex.getMessage()).append("\n");
		}

		for (StackTraceElement element : ex.getStackTrace()) {
			sb.append(element.toString()).append("-")
					.append(element.getLineNumber()).append("\n");
		}

		if (ex.getCause() != null) {
			String inner = getExceptionMessage(ex.getCause());
			sb.append(inner);
		}

		sb.append("\n");
		return sb.toString();
	}

	public static Bitmap toBitmap(String base64String) {
		if (isNullOrWhitespace(base64String))
			return null;

		byte[] decodedString = Base64.decode(base64String, Base64.DEFAULT);
		Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0,
				decodedString.length);
		return decodedByte;
	}

	public static Date parseToDate(String dateString) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd",
				Locale.getDefault());
		try {
			return format.parse(dateString);
		} catch (ParseException e) {
			return new Date();
		}
	}

	public static String toDateString(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd",
				Locale.getDefault());

		return format.format(date);
	}

	public static String toDateString(Calendar calendar) {
		if (calendar == null)
			return EMPTY;

		return toDateString(calendar.getTime());
	}

	public static Calendar parseToCalendar(String dateString) {
		if (isNullOrWhitespace(dateString))
			return null;

		Date date = parseToDate(dateString);
		Calendar cal = Calendar.getInstance(Locale.getDefault());
		cal.setTime(date);

		return cal;
	}

	public static int convertToInt(String value, int defaultValue) {
		if (isNullOrWhitespace(value))
			return defaultValue;

		try {
			return Integer.valueOf(value);
		} catch (Exception e) {
			return defaultValue;
		}
	}
	
	public static int convertToInt(String value) {
		return convertToInt(value, 0);
	}
	
	public static double convertToDouble(String value, double defaultValue) {
		if (isNullOrWhitespace(value))
			return defaultValue;
		
		try {
			return Double.valueOf(value);
		} catch (Exception e) {
			return defaultValue;
		}
	}
	
	public static double convertToDouble(String value) {
		return convertToDouble(value, 0);
	}
}
