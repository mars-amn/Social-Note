package elamien.abdullah.socialnote.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.afollestad.materialdialogs.DialogCallback
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import elamien.abdullah.socialnote.R
import elamien.abdullah.socialnote.utils.Constants
import java.io.IOException
import java.util.*

class GeofencePickerActivity : AppCompatActivity(), OnMapReadyCallback {
	private lateinit var mMap : GoogleMap
	private lateinit var fusedLocationClient : FusedLocationProviderClient
	lateinit var geofencingClient : GeofencingClient
	override fun onCreate(savedInstanceState : Bundle?) {
		super.onCreate(savedInstanceState)
		setFullScreen()
		setContentView(R.layout.activity_location_map)
		val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
		mapFragment.getMapAsync(this)
		fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
		geofencingClient = LocationServices.getGeofencingClient(this)
	}

	private fun setFullScreen() {
		window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
	}

	@SuppressLint("MissingPermission")
	override fun onMapReady(googleMap : GoogleMap) {
		mMap = googleMap
		mMap.isMyLocationEnabled = true
		getUserLocation()
		mMap.setOnMapClickListener { latLng ->
			if (latLng != null) {
				addMarker(latLng)

				try {
					val addresses : List<Address> = getNearAddresses(latLng)
					showPlaceDialogConfirm(addresses, latLng)
				} catch (e : IOException) {
					e.printStackTrace()
					Toast.makeText(this@GeofencePickerActivity, getString(R.string.error_msg), Toast.LENGTH_LONG).show()
				} catch (e : IllegalArgumentException) {
					e.printStackTrace()
					Toast.makeText(this@GeofencePickerActivity, getString(R.string.error_msg), Toast.LENGTH_LONG).show()
				}

			}
		}
	}

	private fun getNearAddresses(latLng : LatLng) : List<Address> {
		val geocoder = Geocoder(this, Locale.getDefault())
		val addresses : List<Address>
		addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
		return addresses
	}

	private fun addMarker(latLng : LatLng) {
		mMap.clear()
		mMap.addMarker(MarkerOptions().position(latLng).icon(getMapMarker()))

		mMap.addCircle(CircleOptions().center(latLng).radius(Constants.GEOFENCE_REMINDER_MAP_RADIUS).strokeColor(
				ContextCompat.getColor(this@GeofencePickerActivity, R.color.circle_stroke_color)).fillColor(
				ContextCompat.getColor(this@GeofencePickerActivity, R.color.circle_map_color)))
	}

	private fun getMapMarker() : BitmapDescriptor {
		val markerDrawable = ContextCompat.getDrawable(this@GeofencePickerActivity, R.drawable.ic_marker)
		markerDrawable?.setBounds(0, 0, markerDrawable.intrinsicWidth, markerDrawable.intrinsicHeight)
		val markerBitmap = Bitmap.createBitmap(markerDrawable?.intrinsicWidth!!,
				markerDrawable.intrinsicHeight,
				Bitmap.Config.ARGB_8888)
		val canvas = Canvas(markerBitmap)
		markerDrawable.draw(canvas)
		return BitmapDescriptorFactory.fromBitmap(markerBitmap)
	}

	private fun showPlaceDialogConfirm(addresses : List<Address>, latLng : LatLng) {
		addresses.forEach { location ->

			MaterialDialog(this, BottomSheet(LayoutMode.WRAP_CONTENT)).show {
				title(R.string.place_picker_dialog_title, null)
				val subAdminArea = if (location.subAdminArea != null) {
					location.subAdminArea
				} else {
					""
				}
				val featureName = if (location.featureName != null) {
					location.featureName
				} else {
					""
				}
				message(null, "${getString(R.string.place_picker_dialog_message)} $subAdminArea , $featureName ?")
				cornerRadius(resources.getInteger(R.integer.place_picker_dialog_corner_radius).toFloat())
				negativeButton(null, getString(R.string.place_picker_dialog_button_negative_label), object : DialogCallback {
					override fun invoke(dialog : MaterialDialog) {
						dialog.dismiss()
					}

				})
				positiveButton(null, getString(R.string.place_picker_dialog_button_positive_label), object : DialogCallback {
					override fun invoke(dialog : MaterialDialog) {
						dialog.dismiss()
						getUserPreferredLocation(latLng)
					}

				})
			}
		}
	}

	private fun getUserPreferredLocation(latLng : LatLng) {
		val intent = intent.putExtra(Constants.NOTE_GEOFENCE_REMINDER_LATLNG_INTENT_KEY, latLng)
		setResult(Activity.RESULT_OK, intent)
		finish()
	}

	@SuppressLint("MissingPermission")
	private fun getUserLocation() {
		fusedLocationClient.lastLocation.addOnSuccessListener {
			if (it != null) {
				val latLng = LatLng(it.latitude, it.longitude)
				val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15f)
				mMap.animateCamera(cameraUpdate)
			}
		}
	}

}
