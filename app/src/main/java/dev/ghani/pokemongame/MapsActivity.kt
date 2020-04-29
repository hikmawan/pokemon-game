package dev.ghani.pokemongame

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        checkPermission()
        LoadPokemon()
    }

    var ACCESSLOCATION=123
    fun checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat
                    .checkSelfPermission(
                        this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), ACCESSLOCATION)
                return
            }
        }
        getUserLocation()
    }

    fun getUserLocation(){
        Toast.makeText(this, "User Location access on", Toast.LENGTH_LONG).show()
        //TODO

        var myLocation = MylocationListener()
        var locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
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
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,3,3f,myLocation)

        var mythread=myThread()
        mythread.start()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            ACCESSLOCATION->{
                if (grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    getUserLocation()
                }else{
                    Toast.makeText(this, "We Cannot access your location", Toast.LENGTH_LONG).show()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
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


    }

    var location:Location?=null
    //Get User Location
    inner class MylocationListener:LocationListener{


        constructor(){
            location= Location("Start")
            location!!.longitude=0.0
            location!!.latitude=0.0
        }
        override fun onLocationChanged(p0: Location?) {
            location=p0
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            TODO("Not yet implemented")
        }

        override fun onProviderEnabled(provider: String?) {
            TODO("Not yet implemented")
        }

        override fun onProviderDisabled(provider: String?) {

        }

    }
    var oldLocation:Location?=null
    inner class myThread:Thread{
        constructor():super(){
            oldLocation= Location("Start")
            oldLocation!!.longitude=0.0
            oldLocation!!.latitude=0.0
        }
        override fun run(){
            while(true){
                try{

                    if(oldLocation!!.distanceTo(location)==0f){
                        continue
                    }

                    oldLocation=location
                    runOnUiThread{
                        mMap!!.clear()
                        //show me
                        val myLoc = LatLng(location!!.latitude, location!!.longitude)
                        val iconCourier = BitmapDescriptorFactory.fromResource(R.drawable.police)
                        mMap!!.addMarker(MarkerOptions()
                            .position(myLoc)
                            .title("Me")
                            .snippet(" Here is my Location " + location!!.latitude + ", "+ location!!.longitude)
                            .icon(iconCourier))
                        mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(myLoc,14f))

                        //show pokemons
                        for(i in 0..listPokemon.size-1){
                            var newPokemon = listPokemon[i]
                            if(newPokemon.isCatch == false){
                                val pokemonLoc = LatLng(newPokemon.location!!.latitude, newPokemon.location!!.longitude)
                                mMap!!.addMarker(MarkerOptions()
                                    .position(pokemonLoc)
                                    .title(newPokemon.name!!)
                                    .snippet(newPokemon.des!! + ", Power: "+newPokemon.power!!)
                                    .icon(BitmapDescriptorFactory.fromResource(newPokemon.image!!)))

                                if (location!!.distanceTo(newPokemon.location)<2){
                                    newPokemon.isCatch=true
                                    listPokemon[i]=newPokemon
                                    playerPower+=newPokemon.power!!
                                    Toast.makeText(applicationContext,"You catch new pokemon, your new power is" + playerPower,Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }

                    sleep(1000)

                } catch (ex:Exception){}
            }
        }
    }

    var playerPower = 0.0
    var listPokemon=ArrayList<Pokemon>()

    fun LoadPokemon(){
        listPokemon.add(Pokemon(R.drawable.bulglar1,"charmander","Here is from Japan",55.0,-6.92377078, 107.68618762))
        listPokemon.add(Pokemon(R.drawable.bulglar2,"Bulbasaur","Here is from Indonesia",90.5,-7.292349,112.7216473))
        listPokemon.add(Pokemon(R.drawable.bulglar3,"Squirtle","Here is from Katapang",33.5,-8.739883,115.180207))
    }
}
