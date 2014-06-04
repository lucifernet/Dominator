package tw.com.ischool.dominator.map;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import tw.com.ischool.dominator.main.MainActivity;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

public class GoogleRouteService {
	private LatLng mFrom;
	private LatLng mTo;
	private OnResultCompleteListener mListener;

	public GoogleRouteService(LatLng from, LatLng to) {
		mFrom = from;
		mTo = to;
	}

	public void setOnResultComplete(OnResultCompleteListener listener) {
		mListener = listener;
	}

	public void startService() {
		RouteTask task = new RouteTask();
		task.execute();
	}

	public interface OnResultCompleteListener {
		void onResultComplete(List<LatLng> route);
	}

	private class RouteTask extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {
			String mapAPI = "http://maps.google.com/maps/api/directions/json?origin={0}&destination={1}&language=zh-TW&sensor=true";
			String url = MessageFormat.format(mapAPI, getLatLngString(mFrom),
					getLatLngString(mTo));
			HttpGet get = new HttpGet(url);
			String strResult = "";
			try {
				HttpParams httpParameters = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(httpParameters, 3000);
				HttpClient httpClient = new DefaultHttpClient(httpParameters);
				HttpResponse httpResponse = null;
				httpResponse = httpClient.execute(get);
				if (httpResponse.getStatusLine().getStatusCode() == 200) {
					strResult = EntityUtils.toString(httpResponse.getEntity());
					JSONObject jsonObject = new JSONObject(strResult);
					JSONArray routeObject = jsonObject.getJSONArray("routes");

					String polyline = routeObject.getJSONObject(0)
							.getJSONObject("overview_polyline")
							.getString("points");

					if (polyline.length() > 0) {
						return polyline;
					}
				}
			} catch (Exception e) {
				Log.e("map", "MapRoute:" + e.toString());
			}

			return "";
		}

		@Override
		protected void onPostExecute(String poly) {
			super.onPostExecute(poly);

			if (poly == null || poly.length() == 0)
				return;

			ArrayList<LatLng> points = new ArrayList<LatLng>();

			int len = poly.length();
			int index = 0;
			int lat = 0;
			int lng = 0;

			while (index < len) {
				int b, shift = 0, result = 0;

				do {
					b = poly.charAt(index++) - 63;
					result |= (b & 0x1f) << shift;
					shift += 5;
				} while (b >= 0x20);

				int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));

				lat += dlat;
				shift = 0;
				result = 0;
				do {
					b = poly.charAt(index++) - 63;
					result |= (b & 0x1f) << shift;
					shift += 5;
				} while (b >= 0x20);
				int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));

				lng += dlng;
				// GeoPoint p = new GeoPoint((int) (((double) lat / 1E5) * 1E6),
				// (int) (((double) lng / 1E5) * 1E6));
				// LatLng p = new LatLng((double) lat / 1E5) * 1E6, (double) lng
				// /
				// 1E5) * 1E6);
				LatLng p = new LatLng(((double) lat/100000), ((double) lng)/100000); 
				Log.d(MainActivity.TAG, "Lat:" + lat + " Lng:" + lng);

				points.add(p);
			}

			if (mListener != null)
				mListener.onResultComplete(points);
		}
	}

	private String getLatLngString(LatLng location) {
		DecimalFormat df = new DecimalFormat("0.000000");
		return df.format(location.latitude) + ","
				+ df.format(location.longitude);
	}
}
