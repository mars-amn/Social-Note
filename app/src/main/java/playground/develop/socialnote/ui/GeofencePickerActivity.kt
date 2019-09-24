package playground.develop.socialnote.ui

import android.animation.IntEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
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
import org.jetbrains.anko.toast
import playground.develop.socialnote.R
import playground.develop.socialnote.databinding.ActivityLocationMapBinding
import playground.develop.socialnote.utils.ConnectionUtils
import playground.develop.socialnote.utils.Constants
import playground.develop.socialnote.utils.Constants.Companion.GEOFENCE_REMINDER_MAP_RADIUS
import java.io.IOException
import java.util.*

class GeofencePickerActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var geofencingClient: GeofencingClient
    var mGeofenceLocation: LatLng? = null
    lateinit var mBinding: ActivityLocationMapBinding
    lateinit var mMapFragment: SupportMapFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFullScreen()
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_location_map)
        mBinding.handlers = this
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        if (intent != null && intent.hasExtra(Constants.NOTE_GEOFENCE_REMINDER_LATLNG_INTENT_KEY)) {
            mGeofenceLocation = intent.getParcelableExtra(Constants.NOTE_GEOFENCE_REMINDER_LATLNG_INTENT_KEY)
        }
        mMapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mMapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        geofencingClient = LocationServices.getGeofencingClient(this)
        checkForConnection()
    }

    private fun checkForConnection() {
        if (ConnectionUtils.getConnectionUtils(this@GeofencePickerActivity).isDeviceNetworkAvailable()) {
            showMap()
        } else {
            hideMap()
        }
    }

    private fun showMap() {
        mMapFragment.view?.visibility = View.VISIBLE
        mBinding.networkStateGroup.visibility = View.GONE
    }

    private fun hideMap() {
        mBinding.networkStateGroup.visibility = View.VISIBLE
        mMapFragment.view?.visibility = View.GONE

    }

    fun onRetryButtonClick(view: View) {
        checkForConnection()
    }

    private fun setFullScreen() {
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.isMyLocationEnabled = true
        getUserLocation()
        mMap.setOnMapClickListener { latLng ->
            if (latLng != null) {
                mGeofenceLocation = latLng
                addMarker(latLng)

                try {
                    val addresses: List<Address> = getNearAddresses(latLng)
                    showPlaceDialogConfirm(addresses, latLng)
                } catch (e: IOException) {
                    e.printStackTrace()
                    toast(getString(R.string.error_msg))
                } catch (e: IllegalArgumentException) {
                    e.printStackTrace()
                    toast(getString(R.string.error_msg))
                }

            }
        }
    }

    private fun getNearAddresses(latLng: LatLng): List<Address> {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses: List<Address>
        addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
        return addresses
    }

    private fun addMarker(latLng: LatLng) {
        mMap.clear()
        mMap.addMarker(MarkerOptions().position(latLng).icon(getMapMarker()))

        val locationCircle = mMap.addCircle(CircleOptions().center(latLng).radius(
            GEOFENCE_REMINDER_MAP_RADIUS).strokeColor(ContextCompat.getColor(this@GeofencePickerActivity,
                                                                             R.color.circle_stroke_color)).fillColor(
            ContextCompat.getColor(this@GeofencePickerActivity, R.color.circle_map_color)))
        val valueAnimator = ValueAnimator()
        valueAnimator.repeatCount = ValueAnimator.INFINITE
        valueAnimator.repeatMode = ValueAnimator.RESTART
        valueAnimator.setIntValues(0, 100)
        valueAnimator.duration = 2000
        valueAnimator.setEvaluator(IntEvaluator())
        valueAnimator.interpolator = AccelerateDecelerateInterpolator()
        valueAnimator.addUpdateListener { animator ->
            locationCircle.radius = animator.animatedFraction * GEOFENCE_REMINDER_MAP_RADIUS
        }
        valueAnimator.start()
    }

    private fun getMapMarker(): BitmapDescriptor {
        val markerDrawable = ContextCompat.getDrawable(this@GeofencePickerActivity,
                                                       R.drawable.ic_marker)
        markerDrawable?.setBounds(0,
                                  0,
                                  markerDrawable.intrinsicWidth,
                                  markerDrawable.intrinsicHeight)
        val markerBitmap = Bitmap.createBitmap(markerDrawable?.intrinsicWidth!!,
                                               markerDrawable.intrinsicHeight,
                                               Bitmap.Config.ARGB_8888)
        val canvas = Canvas(markerBitmap)
        markerDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(markerBitmap)
    }

    private fun showPlaceDialogConfirm(addresses: List<Address>, latLng: LatLng) {
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
                message(null,
                        "${getString(R.string.place_picker_dialog_message)} $subAdminArea , $featureName ?")
                cornerRadius(resources.getInteger(R.integer.place_picker_dialog_corner_radius).toFloat())
                negativeButton(null,
                               getString(R.string.place_picker_dialog_button_negative_label),
                               object : DialogCallback {
                                   override fun invoke(dialog: MaterialDialog) {
                                       dialog.dismiss()
                                   }

                               })
                positiveButton(null,
                               getString(R.string.place_picker_dialog_button_positive_label),
                               object : DialogCallback {
                                   override fun invoke(dialog: MaterialDialog) {
                                       dialog.dismiss()
                                       getUserPreferredLocation(latLng)
                                   }

                               })
            }
        }
    }

    private fun getUserPreferredLocation(latLng: LatLng) {
        val intent = intent.putExtra(Constants.NOTE_GEOFENCE_REMINDER_LATLNG_INTENT_KEY, latLng)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    @SuppressLint("MissingPermission")
    private fun getUserLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                if (mGeofenceLocation != null) {
                    val cameraUpdate = CameraUpdateFactory.newLatLngZoom(mGeofenceLocation, 15f)
                    mMap.animateCamera(cameraUpdate)
                    addMarker(mGeofenceLocation!!)
                } else {
                    val latLng = LatLng(location.latitude, location.longitude)
                    val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15f)
                    mMap.animateCamera(cameraUpdate)
                }
            } else if (mGeofenceLocation != null) {
                val cameraUpdate = CameraUpdateFactory.newLatLngZoom(mGeofenceLocation, 15f)
                mMap.animateCamera(cameraUpdate)
                addMarker(mGeofenceLocation!!)
            }
        }
    }

}
