package com.scastilloforte.proxychat_kotlin.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.support.v4.app.ActivityCompat
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.firebase.database.*
import com.scastilloforte.proxychat_kotlin.activities.MainActivity
import com.scastilloforte.proxychat_kotlin.models.MeetingPoint
import com.scastilloforte.proxychat_kotlin.models.User
import io.nlopez.smartlocation.SmartLocation

/**
 * Created by netx on 1/07/17.
 */
class MapFragment : SupportMapFragment(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener{

    var gMap : GoogleMap? = null
    var usersNearProfile : HashMap<String, User>? = null
    var usersNearMarker : HashMap<String, Marker>? = null
    var mpsNearProfile: HashMap<String, MeetingPoint>? = null
    var mpsNearMarker: HashMap<String, Marker>? = null
    var databaseReference : DatabaseReference? = null
    var user : User? = null

    var geoFireUsers : GeoFire? = null
    var geoFireMeetingPoints : GeoFire? = null
    var geoQueryUsers : GeoQuery? = null
    var geoQueryMeetingPoints : GeoQuery? = null
    var geoLocation : GeoLocation? = null

    var circle : Circle? = null

    val CIRCLE_RADIUS : Double = 10000.0
    val GEOQUERY_RADIUS : Double = 10.0

    override fun onResume() {
        super.onResume()
        setupMapIfNeeded()
    }

    fun setupMapIfNeeded() {
        if (gMap == null) getMapAsync(this)
    }

    override fun onMapReady(p0: GoogleMap?) {
        usersNearMarker = HashMap()
        usersNearProfile = HashMap()
        mpsNearMarker = HashMap()
        mpsNearProfile = HashMap()

        gMap = p0

        //Comprueba los permisos
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }

        gMap?.uiSettings?.isMapToolbarEnabled = true
        gMap?.uiSettings?.isMyLocationButtonEnabled = true
        gMap?.uiSettings?.isZoomControlsEnabled = true
        gMap?.isMyLocationEnabled = true

        databaseReference = FirebaseDatabase.getInstance().reference

        user = (activity as MainActivity).user

        geoFireUsers = GeoFire(databaseReference!!.child("locations").child("usuarios"))
        geoFireMeetingPoints = GeoFire(databaseReference!!.child("locations").child("meeting_points"))

        SmartLocation.with(context).location().start {
            geoLocation = GeoLocation(it.latitude, it.longitude)
            actualizarUbicacion()
        }

        gMap?.setOnMapLongClickListener(this)

        gMap?.setOnMarkerClickListener(object : GoogleMap.OnMarkerClickListener {
            override fun onMarkerClick(p0: Marker?): Boolean {
                val tag : String = p0!!.tag as String

                if (tag.startsWith("u:")) {
                    val bsdf : BottomSheetDialogFragment = BSFragmentUser()
                    val bundle : Bundle = Bundle()
                    bundle.putSerializable("user", user)
                    bundle.putString("contactid", tag.substring(2))
                    bsdf.arguments = bundle
                    bsdf.show(activity.supportFragmentManager, bsdf.tag)
                    return true
                }
                else if (tag.startsWith("p:")) {
                    //Codugo que muestra la informacion del punto de encuentro
                    return true
                }
                return false
            }
        })

        databaseReference!!.child("locations").child(user?.id).onDisconnect().removeValue()
    }

    fun actualizarUbicacion() : Unit {
        geoFireUsers?.setLocation(user?.id, geoLocation)

        drawCircle(geoLocation!!.latitude, geoLocation!!.longitude)

        if (geoQueryUsers != null) geoQueryUsers!!.setCenter(geoLocation)
        else searchNearbyUsers()

        if (geoQueryMeetingPoints != null) geoQueryMeetingPoints!!.setCenter(geoLocation)
        else searchNearbyMeetingPoints()
    }

    fun drawCircle(latitude : Double, longitude : Double) : Unit {
        val latLng : LatLng = LatLng(latitude, longitude)

        val camUpdate : CameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 11f)

        if (circle != null) circle!!.remove()

        val circleOptions : CircleOptions = CircleOptions()
                .center(latLng)
                .radius(CIRCLE_RADIUS)
                .strokeColor(Color.parseColor("#e6faff"))
                .strokeWidth(4f)
                .fillColor(Color.argb(60, 230, 250, 255))

        circle = gMap?.addCircle(circleOptions)

        gMap?.animateCamera(camUpdate)
    }

    fun searchNearbyUsers() {
        geoQueryUsers = geoFireUsers?.queryAtLocation(geoLocation, GEOQUERY_RADIUS)

        geoQueryUsers?.addGeoQueryEventListener(object : GeoQueryEventListener {
            override fun onGeoQueryReady() {

            }

            override fun onKeyEntered(key: String?, location: GeoLocation?) {
                if (key != user?.id) {
                    databaseReference!!.child("usuarios")
                            .child(key)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError?) {

                        }

                        override fun onDataChange(p0: DataSnapshot?) {
                            val usrProxy : User? = p0?.getValue(User::class.java)

                            val marker : Marker? = gMap?.addMarker(MarkerOptions()
                                    .title(usrProxy?.apodo)
                                    .position(LatLng(location!!.latitude, location.longitude))
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)))

                            marker?.tag = "u:${usrProxy?.id}"

                            usersNearProfile?.put(key!!, usrProxy!!)
                            usersNearMarker?.put(key!!, marker!!)
                        }

                    })
                }
            }

            override fun onKeyMoved(key: String?, location: GeoLocation?) {
                if (key != user?.id)
                    usersNearMarker?.get(key)?.position = LatLng(location!!.latitude, location.longitude)
            }

            override fun onKeyExited(key: String?) {
                if (key != user?.id) {
                    usersNearMarker?.get(key)?.remove()
                    usersNearMarker?.remove(key)
                    usersNearProfile?.remove(key)
                }
            }

            override fun onGeoQueryError(error: DatabaseError?) {

            }

        })
    }

    fun searchNearbyMeetingPoints() {

    }

    override fun onMapLongClick(p0: LatLng?) {

    }

}