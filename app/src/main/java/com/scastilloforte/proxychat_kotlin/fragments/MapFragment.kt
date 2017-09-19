package com.scastilloforte.proxychat_kotlin.fragments

import android.Manifest
import android.app.ProgressDialog
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
import com.scastilloforte.proxychat_kotlin.modelos.MeetingPoint
import com.scastilloforte.proxychat_kotlin.modelos.Usuario
import io.nlopez.smartlocation.SmartLocation
import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.support.design.widget.Snackbar
import com.google.firebase.database.DatabaseReference
import com.google.android.gms.maps.model.LatLng
import com.scastilloforte.proxychat_kotlin.R
import kotlinx.android.synthetic.main.dialogo_meetingpoint.view.*
import com.google.android.gms.maps.model.Marker




/**
 * Created by netx on 1/07/17.
 */
class MapFragment : SupportMapFragment(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener{

    var gMap : GoogleMap? = null
    var usersNearProfile : HashMap<String, Usuario>? = null
    var usersNearMarker : HashMap<String, Marker>? = null
    var mpsNearbyProfile: HashMap<String, MeetingPoint>? = null
    var mpsNearbyMarker: HashMap<String, Marker>? = null
    var databaseReference : DatabaseReference? = null
    var user : Usuario? = null

    var geoFireUsers : GeoFire? = null
    var geoFireMeetingPoints : GeoFire? = null
    var geoQueryUsers : GeoQuery? = null
    var geoQueryMeetingPoints : GeoQuery? = null
    var geoLocation : GeoLocation? = null

    var circle : Circle? = null

    val CIRCLE_RADIUS : Double = 10000.0
    val GEOQUERY_RADIUS : Double = 10.0

    var invisible = false

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
        mpsNearbyMarker = HashMap()
        mpsNearbyProfile = HashMap()

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
                    //Codigo que muestra la informacion del punto de encuentro
                    val bsdf : BottomSheetDialogFragment = BSFragmentMeetingPoint()
                    val bundle : Bundle = Bundle()
                    bundle.putSerializable("user", user)
                    bundle.putString("meetingpointid", tag.substring(2))
                    bsdf.arguments = bundle
                    bsdf.show(activity.supportFragmentManager, bsdf.tag)
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
                            val usrProxy : Usuario? = p0?.getValue(Usuario::class.java)

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
        //Crea una nueva consulta por localizacion
        geoQueryMeetingPoints = geoFireMeetingPoints!!.queryAtLocation(geoLocation, GEOQUERY_RADIUS)
        //Añade a la consulta un escuchador GeoQuery
        geoQueryMeetingPoints!!.addGeoQueryEventListener(object : GeoQueryEventListener {

            /**
             * onKeyEntered: metodo que se ejecuta cuando un punto de encuentro entra en el radio de alcance de la consulta
             * @param key clave que identifica al punto de encuentro (meeting point id)
             * *
             * @param location localizacion del punto de encuentro
             */
            override fun onKeyEntered(key: String, location: GeoLocation) {

                //Realiza una consulta para obtener los datos del punto de encuentro
                databaseReference!!.child("meeting_points").child(key)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                //Obtiene un objeto MeetingPoint con los datos del punto de encuentro
                                //a partir del DataSnapshot
                                val meetingPoint = dataSnapshot.getValue(MeetingPoint::class.java)

                                //Añade al mapa un marcador ubicado en la localizacion del punto de encuentro
                                val marker = gMap!!.addMarker(MarkerOptions().title(meetingPoint!!.nombre)
                                        .position(LatLng(location.latitude, location.longitude))
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)))
                                marker.tag = "p:" + meetingPoint.id

                                //Añade el objeto MeetingPoint al map de puntos de encuentro cercanos
                                mpsNearbyProfile!!.put(key, meetingPoint)
                                //Añade el marcador al map de marcadores de puntos de encuentro cercanos
                                mpsNearbyMarker!!.put(key, marker)

                            }

                            override fun onCancelled(databaseError: DatabaseError) {

                            }
                })
            }

            /**
             * onKeyExited: metodo que se ejecuta cuando un punto de encuentro sale del radio de alcance de la consulta
             * @param key clave que identifica al punto de encuentro (meeting point id)
             */
            override fun onKeyExited(key: String) {
                //Elimina el marcador del mapa
                mpsNearbyMarker?.get(key)?.remove()
                //Elimina el marcador de la lista de marcadores
                mpsNearbyMarker?.remove(key)
                //Elimina el marcador de la lista de puntos de encuentro
                mpsNearbyProfile?.remove(key)
            }

            /**
             * onKeyMoved: este metodo se ejecuta cuando un punto de encuentro cambia su localizacion dentro del
             * radio de alcance
             * @param key clave del marcador, identifica al punto de encuentro
             * *
             * @param location nueva localizacion
             */
            override fun onKeyMoved(key: String, location: GeoLocation) {
                //Actualiza la ubicacion en el mapa del marcador que representa al punto de encuentro
                mpsNearbyMarker?.get(key)?.position = LatLng(location.latitude, location.longitude)
            }

            override fun onGeoQueryReady() {

            }

            override fun onGeoQueryError(error: DatabaseError) {

            }
        })
    }

    override fun onMapLongClick(p0: LatLng?) {
        //Inicializa un array CharSequence que contiene la descripcion para cada opcion del menu contextual
        val items = arrayOfNulls<CharSequence>(2)

        items[0] = "Crear punto de encuentro"
        if (invisible) {
            items[1] = "Desactivar modo invisible"
        } else {
            items[1] = "Activar modo invisible"
        }

        //Crea un constructor de dialogos
        val builder = AlertDialog.Builder(context)

        //Establece el titulo del dialogo
        builder.setTitle("Opciones")
        //Configura el dialogo con los items (opciones) que tendra, tambien se añade un escuchador
        //que recibira los eventos de click en cada una de las opciones del menu contextual
        builder.setItems(items, DialogInterface.OnClickListener { dialog, item ->
            when (item) {
                0 -> crearMeetingPoint(p0!!)
                1 -> cambiarModoInvisible()
            }
        })

        //Muestra el dialogo
        builder.show()
    }

    fun crearMeetingPoint(latLng: LatLng) {
        //**********************************************
        //Crea una vista cargando en esta el layout del dialogo para crear un punto de encuentro
        val v = activity.layoutInflater.inflate(R.layout.dialogo_meetingpoint, null)

        //Crea un objeto GeoLocation con las coordenadas donde se realizo la pulsacion
        val mpGeoLocation = GeoLocation(latLng.latitude, latLng.longitude)

        //Instancia el campo de texto del dialogo que recibe el nombre del punto
        val etNombrePoint = v.etNombrePointDialog
        //Instancia el campo de texto del dialogo que recibe la descripcion del punto

        //Crea el dialogo
        val alertDialog = AlertDialog.Builder(context)
                .setView(v)
                .setTitle("Nuevo punto de encuentro")
                .setPositiveButton("Aceptar", null)
                .setNegativeButton("Cancelar", null)
                .create()

        //Configura un escuchador que se disparara cuando se muestre el dialogo
        alertDialog.setOnShowListener(DialogInterface.OnShowListener //en el metodo onShow, obtenemos el boton de aceptar y le configuramos un escuchador
        //View.OnclickListener
        //De esta forma podemos validar el texto introducido por el usuario,
        //controlando cuando cerrar el dialogo
        {
            //Obtiene una instancia del boton de aceptar del dialogo
            val botonAceptar = alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)
            //Configura un escuchador de clicks para el boton de aceptar
            botonAceptar.setOnClickListener {
                //Crea un dialogo para informar al usuario de que la creacion del punto
                //se esta llevando a cabo
                val progressDialog = ProgressDialog(context)
                //Establece el mensaje del dialogo de carga
                progressDialog.setMessage("Creando punto de encuentro, por favor espere...")
                //Establece el dialogo de carga como modal
                progressDialog.setCancelable(false)
                //Muestra el dialogo de carga
                progressDialog.show()

                //si el EditText del nombre del punto no esta vacio
                if (etNombrePoint.text.toString() != "") {

                    //Obtiene una referencia a la base de datos donde se almacenara el punto de encuentro
                    val meetingPointDataRef = databaseReference!!.child("meeting_points").push()

                    //Crea un objeto MeetingPoint con los datos del punto de encuentro
                    val meetingPoint = MeetingPoint(meetingPointDataRef.key,
                            user!!.id, etNombrePoint.text.toString(), v.etDescripcionDialog.text.toString())

                    //Almacena el punto de encuentro en la base de datos
                    meetingPointDataRef.setValue(meetingPoint).addOnSuccessListener {
                        //Almacena la localizacion del punto de encuentro en la base de datos
                        geoFireMeetingPoints!!.setLocation(meetingPoint.id, mpGeoLocation)

                        //Añade el punto de encuentro a la lista de puntos de encuentro del usuario
                        //en la base de datos
                        databaseReference!!.child("contactos")
                                .child("usuarios").child(user!!.id)
                                .child("meeting_points")
                                .child(meetingPoint.id).setValue(true)

                        //Cierra el dialogo
                        alertDialog.dismiss()

                        //Cierra el dialogo de carga
                        progressDialog.dismiss()

                        //Muestra un Snackbar informando al usuario de que el punto ha sido creado
                        Snackbar.make(view!!,
                                "Punto creado y agregado a la lista de puntos",
                                Snackbar.LENGTH_LONG).show()
                    }.addOnFailureListener {
                        //Cierra el dialogo
                        alertDialog.dismiss()

                        //Cierra el dialogo de carga
                        progressDialog.dismiss()

                        //Muestra un Snackbar al usuario informando del error
                        Snackbar.make(view!!, "Error al crear el punto de encuentro",
                                Snackbar.LENGTH_LONG).show()
                    }

                } else {
                    //Cierra el dialogo de carga
                    progressDialog.dismiss()

                    //muestra un mensaje tip informando del error
                    etNombrePoint.error = "Debes introducir un nombre para el punto de encuentro"
                }//si el EditText del nombre del punto esta vacio
            }
        })

        //Muestra el dialogo
        alertDialog.show()
    }

    fun cambiarModoInvisible() {
        if (invisible) {
            invisible = false
            //Itera sobre el mapa de marcadores mostrandolos en el mapa
            for (entry in usersNearMarker!!.entries) {
                entry.value.isVisible = true
            }
            Snackbar.make(view!!, "Modo invisible desactivado",
                    Snackbar.LENGTH_LONG).show()
        } else {
            invisible = true

            //Elimina la localizacion del usuario de la base de datos
            databaseReference!!.child("locations")
                    .child("usuarios")
                    .child(user!!.id).removeValue()
                    .addOnSuccessListener {
                        //Itera sobre el mapa de marcadores ocultandolos del mapa
                        for (entry in usersNearMarker!!.entries) {
                            entry.value.isVisible = false
                        }
                        Snackbar.make(view!!, "Modo invisible activado",
                                Snackbar.LENGTH_LONG).show()
                    }.addOnFailureListener {
                        invisible = false

                        Snackbar.make(view!!, "No se ha podido activar el modo invisible",
                                Snackbar.LENGTH_LONG).show()
            }

        }
    }
}