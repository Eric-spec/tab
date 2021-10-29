package com.rickie.mytabs

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.JsonHttpResponseHandler
import com.rickie.mytabs.databinding.ActivityMapsBinding
import cz.msebera.android.httpclient.Header
import org.json.JSONArray

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        //GPS - Global Positioning System
        //GPS works with satellites
        //GPS uses lat and long
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        GPS()

        // kotlin to access python at this point
        //retrieve all latitudes and longitudes from database
        // server will run at 127.0.0.1:8080/locations
        //we can host the api online



        val client = AsyncHttpClient(true,80,443)
        client.get("http://modcom.pythonanywhere.com/api/all", object : JsonHttpResponseHandler()
        {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONArray
            ) {
                //we use a for loop
                for(i in 0 until response.length()){
                    val jsonObject = response.getJSONObject(i)
                    val lat = jsonObject.optString("lat").toDouble()
                    val lon = jsonObject.optString("lon").toDouble()
                    val name = jsonObject.optString("name").toString()


                    val Aboretum = LatLng(lat, lon)
                    mMap.addMarker(MarkerOptions().position(Aboretum).title(name).icon(
                        BitmapDescriptorFactory.fromResource(R.drawable.car)
                    ))

                }
            } // end success

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?,
                throwable: Throwable?
            ) {
                super.onFailure(statusCode, headers, responseString, throwable)
            }
        }
        )

        // we need internet permissions added to manifest
        //for android 10,11 we need to allow cleartext
        //above ARE DONE IN MANIFEST FILE


        // Add a marker in Aboretum and move the camera
        //val Aboretum = LatLng(-1.2780722675271095, 36.80117057121729)
        //mMap.addMarker(MarkerOptions().position(Aboretum).title("Aboretum"))


    }

    //GPS function
    fun GPS(){
        //check permissions
        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),1)
        }

        //get user position
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener(this) {
            location->
            if (location!=null) {
                val currentLocation = LatLng(location.latitude,location.longitude)
                mMap.addMarker(
                    MarkerOptions()
                        .position(currentLocation)
                        .title("I'm Here")
                        .icon(
                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)
                ))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
            }
            else{
                Toast.makeText(applicationContext,"No Location, Activate GPS", Toast.LENGTH_LONG).show()
            }
        }
    }
}