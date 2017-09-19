package com.scastilloforte.proxychat_kotlin.fragments

import android.support.design.widget.Snackbar
import android.support.annotation.NonNull
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import android.content.DialogInterface
import com.google.firebase.database.DatabaseError
import android.support.v4.content.ContextCompat.startActivity
import com.scastilloforte.proxychat_kotlin.activities.MeetingPointActivity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import com.scastilloforte.proxychat_kotlin.modelos.MeetingPoint
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.FirebaseDatabase
import com.scastilloforte.proxychat_kotlin.adaptadores.MeetingPointsAdaptador
import android.support.v7.widget.LinearLayoutManager
import android.widget.TextView
import android.support.v7.widget.RecyclerView
import com.scastilloforte.proxychat_kotlin.activities.MainActivity
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import com.google.firebase.database.DatabaseReference
import com.scastilloforte.proxychat_kotlin.R
import com.scastilloforte.proxychat_kotlin.interfaces.OnItemClickListener
import com.scastilloforte.proxychat_kotlin.interfaces.OnItemLongClickListener
import com.scastilloforte.proxychat_kotlin.modelos.Usuario
import kotlinx.android.synthetic.main.fragment_meetingpoints.*


/**
 * Created by netx on 9/19/17.
 */
/**
 * MeetingPointsFragment: Fragment que muestra la lista de puntos de encuentro en los que participa el usuario
 */
class MeetingPointsFragment : Fragment(), OnItemClickListener, OnItemLongClickListener {

    private var usuario: Usuario? = null
    private var meetingPoints: ArrayList<String>? = null
    private var meetingPointsAdaptador: MeetingPointsAdaptador? = null
    private var databaseReference: DatabaseReference? = null


    /**
     * onCreateView: metodo que es llamado a la hora de crear la vista del Fragment
     * @param inflater
     * *
     * @param container
     * *
     * @param savedInstanceState
     * *
     * @return
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val rootView = inflater.inflate(R.layout.fragment_meetingpoints, container, false)
        return rootView
    }

    /**
     * onViewCreated: este metodo es llamado una vez que la vista ha sido creada
     * @param view
     * *
     * @param savedInstanceState
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Obtiene un objeto Usuario con los datos del usuario contenido en la actividad principal
        usuario = (getActivity() as MainActivity).user

        //Crea un gestor LinearLayout
        val linearLayoutManager = LinearLayoutManager(getContext())
        //Configura el RecyclerView con el LinearLayoutManager
        recyclerviewMeetingPoints.layoutManager = linearLayoutManager

        //Inicializa la lista de puntos de encuentro
        meetingPoints = ArrayList()
        //Crea un adaptador de puntos de encuentro
        meetingPointsAdaptador = MeetingPointsAdaptador(meetingPoints!!)
        //Configura un escuchador de clicks para el adaptador, el esuchador es el propio Fragment
        meetingPointsAdaptador!!.setOnItemClickListener(this)
        //Configura un escuchador de clicks largos para el adaptador, el esuchador es el propio Fragment
        meetingPointsAdaptador!!.setOnItemLongClickListener(this)
        //Configura el RecyclerView con el adaptador de puntos de encuentro
        recyclerviewMeetingPoints.adapter = meetingPointsAdaptador

        //Obtiene una referencia a la base de datos
        databaseReference = FirebaseDatabase.getInstance().reference

        //Inicia la escucha de puntos de encuentro
        iniciarEscuchador()

    }

    /**
     * iniciarEscuchador: metodo encargado de realizar una consulta a la base de datos
     * y obtener la lista de puntos de encuentro del usuario a traves del escuchador
     */
    fun iniciarEscuchador() {

        //Realiza una consulta a la base de datos para obtener los puntos de encuentro del usuario
        databaseReference!!.child("contactos").child("usuarios").child(usuario!!.id!!).child("meeting_points")
                .addChildEventListener(object : ChildEventListener {

                    /**
                     * onChildAdded: este metodo se ejecuta cuando un nuevo nodo hijo es agregado a la referencia
                     * de la base de datos (un punto de encuentro agregado). Este metodo tambien se ejecuta al crear el
                     * escuchador, obteniendo un resultado inicial.
                     * @param dataSnapshot
                     * *
                     * @param s
                     */
                    override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                        //Obtiene la clave del nodo, este es el id del punto de encuentro
                        val keyMeetingPoint = dataSnapshot.key
                        //Añade el id a la lista de puntos de encuentro
                        meetingPoints!!.add(keyMeetingPoint)
                        //Notifica al adaptador que el conjunto de datos ha cambiado, de forma que este
                        //se actualice
                        meetingPointsAdaptador!!.notifyDataSetChanged()
                        //Actualiza el TextView que muestra el numero de puntos de encuentro
                        tvNumeroMeetingPoints!!.text = "Puntos de encuentro: " + meetingPoints!!.size
                    }

                    override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

                    }

                    override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                        //Elimina el id del punto de encuentro de la lista de puntos de encuentro
                        meetingPoints!!.remove(dataSnapshot.key)
                        //Actualiza el numero de puntos de encuentro
                        tvNumeroMeetingPoints!!.text = "Puntos de encuentro: " + meetingPoints!!.size
                        //Notifica al adaptador que el conjunto de datos ha cambiado, de forma que este
                        //se actualice
                        meetingPointsAdaptador!!.notifyDataSetChanged()
                    }

                    override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

                    }

                    override fun onCancelled(databaseError: DatabaseError) {

                    }
                })
    }

    /**
     * onClick: metodo que se ejecuta cuando el usuario pulsa sobre uno de los items del RecyclerView
     * @param view item pulsado
     * *
     * @param position posicion del item dentro del RecyclerView
     */
    override fun onClick(view: View, position: Int) {

        //Realiza una consulta a la base de datos para obtener los datos del punto de encuentro
        databaseReference!!.child("meeting_points").child(meetingPoints!![position])
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        //Obtiene un objeto MeetingPoint con los datos del punto de encuentro
                        //a partir del DataSnapshot
                        val meetingPoint = dataSnapshot.getValue(MeetingPoint::class.java)

                        //Si el objeto MeetingPoint no es nulo
                        if (meetingPoint != null) {
                            //Crea un Bundle
                            val bundle = Bundle()
                            //Añade el objeto MeetingPoint con los datos del punto de encuentro
                            //al Bundle
                            bundle.putSerializable("meetingPoint", meetingPoint)
                            //Añade el objeto Usuario con los datos del usuario al Bundle
                            bundle.putSerializable("usuario", usuario)

                            //Crea un Intent utilizado para iniciar la actividad del punto de encuentro
                            val intent = Intent(context, MeetingPointActivity::class.java)
                            //Añade el Bundle al Intent
                            intent.putExtras(bundle)
                            //Inicia la actividad
                            startActivity(intent)
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {

                    }
                })
    }

    /**
     * onLongClick: metodo que se ejecuta cuando el usuario realiza una pulsacion larga
     * sobre uno de los items del Recyclerview
     * @param view item pulsado
     * *
     * @param position posicion del item dentro del RecyclerView
     * *
     * @return
     */
    override fun onLongClick(view: View, position: Int): Boolean {

        //Inicializa un array CharSequence que contiene la descripcion para cada opcion del menu contextual
        val items = arrayOf<CharSequence>("Eliminar Punto de Encuentro")

        //Crea un constructor de dialogos
        val builder = AlertDialog.Builder(context)

        //Establece el titulo del dialogo
        builder.setTitle("Opciones")
        //Configura el dialogo con los items (opciones) que tendra, tambien se añade un escuchador
        //que recibira los eventos de click en cada una de las opciones del menu contextual
        builder.setItems(items, { dialog, item ->
            when (item) {
                0 ->
                    //Elimina de la base de datos el nodo correspondiente al punto de encuentro en la lista
                    //de puntos de encuentro del usuario,
                    //se añaden ademas escuchadores que realizaran acciones dependiendo de si la operacion
                    //fue o no un exito
                    databaseReference!!.child("contactos").child("usuarios").child(usuario!!.id!!)
                            .child("meeting_points").child(meetingPoints!![position]).removeValue()
                            .addOnSuccessListener {
                                //Muestra un Snackbar informando al usuario de que el
                                //punto de encuentro ha sido eliminado
                                Snackbar.make(view, "Punto de encuentro eliminado",
                                        Snackbar.LENGTH_LONG).show()
                            }.addOnFailureListener {
                        //Muestra un Snackbar informando al usuario del error
                        Snackbar.make(view,
                                "No se ha podido eliminar el punto de encuentro",
                                Snackbar.LENGTH_LONG).show()
                    }
            }
        })

        //Muestra el dialogo
        builder.show()
        return true
    }
}