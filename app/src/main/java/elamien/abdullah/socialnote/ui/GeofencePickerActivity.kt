package elamien.abdullah.socialnote.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import elamien.abdullah.socialnote.R
import elamien.abdullah.socialnote.utils.Constants

class GeofencePickerActivity : AppCompatActivity(), OnMapReadyCallback {
	private lateinit var mMap : GoogleMap
	private lateinit var fusedLocationClient : FusedLocationProviderClient
	lateinit var geofencingClient : GeofencingClient

	override fun onCreate(savedInstanceState : Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_location_map)
		val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
		mapFragment.getMapAsync(this)
		fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
		geofencingClient = LocationServices.getGeofencingClient(this)
	}

	override fun onMapReady(googleMap : GoogleMap) {
		mMap = googleMap
		getUserLocation()
		mMap.setOnMapClickListener { latLng ->
			if (latLng != null) {
				mMap.clear()
				mMap.addMarker(MarkerOptions().position(latLng))
				mMap.addCircle(CircleOptions().center(latLng).radius(Constants.GEOFENCE_REMINDER_MAP_RADIUS).strokeColor(
						ContextCompat.getColor(this@GeofencePickerActivity, R.color.circle_stroke_color)).fillColor(
						ContextCompat.getColor(this@GeofencePickerActivity, R.color.circle_map_color)))
				getUserPreferredLocation(latLng)
			}
		}
	}

	private fun getUserPreferredLocation(it : LatLng) {
		val intent = intent.putExtra(Constants.NOTE_GEOFENCE_REMINDER_LATLNG_INTENT_KEY, it)
		setResult(Activity.RESULT_OK, intent)
		finish()
	}

	@SuppressLint("MissingPermission")
	private fun getUserLocation() {
		fusedLocationClient.lastLocation.addOnSuccessListener {
			if (it != null) {
				val latLng = LatLng(it.latitude, it.longitude)
				val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 10f)
				mMap.animateCamera(cameraUpdate)
			}
		}
	}

}
