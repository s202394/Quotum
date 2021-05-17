package com.quotum.quotum.quotum.ui.searchTrip

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.quotum.quotum.quotum.PostTripActivity
import com.quotum.quotum.quotum.R
import com.quotum.quotum.quotum.localdatabase.LocalDB
import com.quotum.quotum.quotum.models.GetTripLocationResponseModel
import com.quotum.quotum.quotum.network.QuotumClient
import com.quotum.quotum.quotum.utility.PermissionUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class SearchTripFragment : Fragment(), View.OnClickListener {

    private lateinit var searchTripViewModel: SearchTripViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyTripView: LinearLayout
    private lateinit var loadingView: LinearLayout
    private lateinit var postTrip: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        searchTripViewModel = ViewModelProvider(this).get(SearchTripViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        recyclerView = root.findViewById(R.id.rvTrip)
        emptyTripView = root.findViewById(R.id.ll_empty_list)
        loadingView = root.findViewById(R.id.ll_load_trip)
        postTrip = root.findViewById(R.id.post_trip)
        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this.requireActivity())
        recyclerView.layoutManager =
            GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)

        postTrip.setOnClickListener(this)

        hideEmptyListView()
        showLoadingView()
        getNearByTrips()
        return root
    }

    private fun getNearByTrips() {
        if (PermissionUtils.checkLocationPermissions(this.requireContext())) {
            getLocation()
        } else {
            requestLocationPermissions()
        }
    }

    private fun showEmptyListView() {
        emptyTripView.visibility = View.VISIBLE
    }

    private fun hideEmptyListView() {
        emptyTripView.visibility = View.INVISIBLE
    }

    private fun showLoadingView() {
        loadingView.visibility = View.VISIBLE
    }

    private fun hideLoadingView() {
        loadingView.visibility = View.INVISIBLE
    }

    private fun getTrips() {

        val token: String? = context?.let { LocalDB.getUserToken(it) }
        if (token != null) {
            Log.e("token", token)
            QuotumClient.instance.getTripByLocation(latitude, longitude, 100, token)
                .enqueue(object : Callback<GetTripLocationResponseModel> {
                    override fun onFailure(call: Call<GetTripLocationResponseModel>, t: Throwable) {
                        showEmptyListView()
                        hideLoadingView()
                    }

                    override fun onResponse(
                        call: Call<GetTripLocationResponseModel>,
                        response: Response<GetTripLocationResponseModel>
                    ) {
                        if (response.isSuccessful) {
                            val tripLocationResponseModel: GetTripLocationResponseModel =
                                response.body()!!
                            if (tripLocationResponseModel.getResult()?.isEmpty() == true) {
                                hideLoadingView()
                                showEmptyListView()
                            } else {
                                recyclerView.adapter = TripAdapter(tripLocationResponseModel)
                                hideLoadingView()
                                hideEmptyListView()
                            }
                        }
                    }
                })
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 42) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLocation()
            }
        }
    }

    fun getLocation() {
        val manager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showAlertLocation()
        }
        fusedLocationClient =
            activity?.let { LocationServices.getFusedLocationProviderClient(it) }!!
        getLocationUpdates()
    }

    private fun showAlertLocation() {

        val dialog = AlertDialog.Builder(context)
        dialog.setMessage("Your location settings is set to Off, Please enable location to use this application")
        dialog.setPositiveButton("Settings") { _, _ ->
            val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(myIntent)
        }
        dialog.setNegativeButton("Cancel") { _, _ ->

        }
        dialog.setCancelable(false)
        dialog.show()
    }

    private fun getLocationUpdates() {
        try {
            fusedLocationClient =
                context?.let { LocationServices.getFusedLocationProviderClient(it) }!!
            locationRequest = LocationRequest.create()
            locationRequest.interval = 50000
            locationRequest.fastestInterval = 50000
            locationRequest.smallestDisplacement = 170f
            locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    locationResult ?: return
                    if (locationResult.locations.isNotEmpty()) {
                        val addresses: List<Address>?
                        val geoCoder = Geocoder(activity?.applicationContext, Locale.getDefault())
                        latitude = locationResult.lastLocation.latitude
                        longitude = locationResult.lastLocation.longitude

                        if (latitude == 0.0) {
                            return
                        }

                        if(geoCoder==null) {
                            return
                        }
                        addresses = geoCoder.getFromLocation(
                            locationResult.lastLocation.latitude,
                            locationResult.lastLocation.longitude,
                            1
                        )

                        getTrips()
                        if (addresses != null && addresses.isNotEmpty()) {
                            val address: String = addresses[0].getAddressLine(0)
                            val city: String = addresses[0].locality!!
                            val state: String = addresses[0].adminArea!!
                            val country: String = addresses[0].countryName!!
                            val postalCode: String = addresses[0].postalCode!!
                            val knownName: String = addresses[0].featureName!!
                            Log.e(
                                "location",
                                "$latitude $longitude $address $city $state $postalCode $country $knownName"
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Start location updates
    private fun startLocationUpdates() {
        if (context?.let {
                ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            } != PackageManager.PERMISSION_GRANTED && context?.let {
                ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            } != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null /* Looper */
        )
    }

    // Stop location updates
    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    // Stop receiving location update when activity not visible/foreground
    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    // Start receiving location update when activity  visible/foreground
    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    private fun handleDenyPermission() {
        TODO("Not yet implemented")
    }

    companion object {
        private const val PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 100
    }

    fun requestLocationPermissions() {
        if (context?.let {
                ContextCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            }
            != PackageManager.PERMISSION_GRANTED) {
            activity?.let {
                ActivityCompat.requestPermissions(
                    it,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSION_REQUEST_ACCESS_FINE_LOCATION
                )
            }
            return
        }
    }

    override fun onClick(v: View?) {
        if (v!!.id == R.id.post_trip) {
//            val manager: FragmentManager = activity?.supportFragmentManager!!
//            val transaction = manager.beginTransaction()
//            transaction.replace(R.id.container, PostTripFragment(), "POST_TRIP")
//            transaction.addToBackStack(null)
//            transaction.commit()
            val intent = Intent(activity, PostTripActivity::class.java)
            startActivity(intent)
        }
    }
}