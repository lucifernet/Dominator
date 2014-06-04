package tw.com.ischool.dominator.map;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import tw.com.ischool.dominator.R;
import tw.com.ischool.dominator.main.MainActivity;
import tw.com.ischool.dominator.map.GoogleRouteService.OnResultCompleteListener;
import android.app.Activity;
import android.app.FragmentManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapActivity extends Activity {

	public static final String PARAM_ADDRESS = "address";

	private GoogleMap mMap;
	private LocationManager mLocationManager;
	private LatLng mTargetLatLng;
	private LatLng mCurrentLatLng;
	private Marker mCurrentMarker;
	private Polyline mPolyline;
	private String mAddress;
	
	private MapFragment mMapFragment = new MapFragment() {

		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);

			mMap = this.getMap();
			
			// find address		
			Geocoder geo = new Geocoder(MapActivity.this, Locale
					.getDefault());
			
			List<Address> addresses = null;
			try {
				addresses = geo.getFromLocationName(mAddress, 1);
			} catch (IOException e) {
				Log.e(MainActivity.TAG, e.toString());
			}
			
			if (addresses == null || addresses.isEmpty()) {
				Toast.makeText(MapActivity.this, "查詢地址失敗:" + mAddress, Toast.LENGTH_LONG).show();
				MapActivity.this.finish();
				return;
			}
			
			Address address = addresses.get(0);
			mTargetLatLng = new LatLng(address.getLatitude(), address
					.getLongitude());
			
			mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			CameraUpdate update = CameraUpdateFactory.newLatLngZoom(
					mTargetLatLng, 16);
			mMap.animateCamera(update);
			mMap.addMarker(new MarkerOptions().position(mTargetLatLng)
					.title(mAddress)).showInfoWindow();
			
			handleLocation();
		};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		
		mAddress = getIntent().getStringExtra(PARAM_ADDRESS);
		
		FragmentManager fm = this.getFragmentManager();
		fm.beginTransaction().add(R.id.map_container, mMapFragment).commit();

		
		
		//handleLocation();
	}


	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.map, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem itemNavi = menu.findItem(R.id.action_navi);
		MenuItem itemLocation = menu.findItem(R.id.action_my_location);
		MenuItem itemTarget = menu.findItem(R.id.action_target_location);
		
		if(mCurrentLatLng == null) {
			itemNavi.setEnabled(false);
			itemLocation.setEnabled(false);
		} else {
			itemNavi.setEnabled(true);
			itemLocation.setEnabled(true);
		}
		
		if(mTargetLatLng == null){
			itemTarget.setEnabled(false);			
		} else {
			itemTarget.setEnabled(true);
		}
		
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()) {
		case R.id.action_navi:
			routeService();
			return true;
		case R.id.action_my_location:
			cameraFocusOnMe();
			return true;
		case R.id.action_target_location:
			cameraFocusOnTarget();
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	private void handleLocation() {
		mLocationManager = (LocationManager) this
				.getSystemService(LOCATION_SERVICE);

		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);

		String bestProvider = mLocationManager.getBestProvider(criteria, true);
		Location lastLocation = mLocationManager
				.getLastKnownLocation(bestProvider);

		if (lastLocation != null) {
			mCurrentLatLng = new LatLng(lastLocation.getLatitude(),
					lastLocation.getLongitude());
			this.onLocationChanged();
		}

		LocationListener locationListener = new MyLocationListener();
		mLocationManager.requestLocationUpdates(bestProvider, 5000, 5,
				locationListener);
	}

	private void onLocationChanged() {
		if (mMap == null || mCurrentLatLng == null)
			return;

		if (mCurrentMarker == null) {
			mCurrentMarker = mMap.addMarker(new MarkerOptions()
					.position(mCurrentLatLng));
			mCurrentMarker.setTitle("現在位置");
		}

		mCurrentMarker.setPosition(mCurrentLatLng);
		String title = "現在位置(%s)";
		String loc = "Lat:" + mCurrentLatLng.latitude + " Lng:"
				+ mCurrentLatLng.longitude;
		title = String.format(title, loc);
		mCurrentMarker.setTitle(title);
		mCurrentMarker.showInfoWindow();
		
		invalidateOptionsMenu();
	}
	
	private void routeService() {
		GoogleRouteService service = new GoogleRouteService(mCurrentLatLng,
				mTargetLatLng);
		service.setOnResultComplete(new OnResultCompleteListener() {

			@Override
			public void onResultComplete(List<LatLng> route) {
				if (mPolyline != null)
					mPolyline.remove();

				PolylineOptions polylineOpt = new PolylineOptions();
				for (LatLng latlng : route) {
					polylineOpt.add(latlng);
				}

				polylineOpt.color(Color.RED);

				mPolyline = mMap.addPolyline(polylineOpt);
				mPolyline.setWidth(5);

				cameraFocusOnMe();
			}
		});

		service.startService();
	}
	
	private void cameraFocusOnMe() {
		if (mMap == null || mCurrentLatLng == null)
			return;

		CameraPosition camPosition = new CameraPosition.Builder()
				.target(mCurrentLatLng).zoom(16).build();

		mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPosition));
	}
	
	private void cameraFocusOnTarget(){
		if (mMap == null || mTargetLatLng == null)
			return;

		CameraPosition camPosition = new CameraPosition.Builder()
				.target(mTargetLatLng).zoom(16).build();

		mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPosition));
	}
	
	private class MyLocationListener implements LocationListener {

		@Override
		public void onLocationChanged(Location location) {
			mCurrentLatLng = new LatLng(location.getLatitude(),
					location.getLongitude());

			MapActivity.this.onLocationChanged();
		}

		@Override
		public void onProviderDisabled(String provider) {

		}

		@Override
		public void onProviderEnabled(String provider) {

		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	}
}
