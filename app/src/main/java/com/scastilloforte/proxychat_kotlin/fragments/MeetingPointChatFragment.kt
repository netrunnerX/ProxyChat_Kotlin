package com.scastilloforte.proxychat_kotlin.fragments

import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.scastilloforte.proxychat_kotlin.modelos.Mensaje
import com.google.firebase.database.ChildEventListener
import android.text.TextUtils
import android.widget.ImageButton
import android.widget.EditText
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.scastilloforte.proxychat_kotlin.modelos.Usuario
import com.scastilloforte.proxychat_kotlin.modelos.MeetingPoint
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import com.google.firebase.database.DatabaseReference
import com.scastilloforte.proxychat_kotlin.R
import com.scastilloforte.proxychat_kotlin.adaptadores.MensajeAdaptador
import kotlinx.android.synthetic.main.mensajes.*
import kotlinx.android.synthetic.main.mensajes.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


/**
 * Created by netx on 7/27/17.
 */


/**
 * MeetinPointChatFragment: Fragment que muestra el chat del punto de encuentro
 */
class MeetingPointChatFragment : Fragment() {

    private var meetingPoint: MeetingPoint? = null
    private var usuario: Usuario? = null
    private var mensajes: ArrayList<Mensaje>? = null
    private var databaseReference: DatabaseReference? = null
    private var mensajesAdaptador: MensajeAdaptador? = null

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
        val rootView = inflater.inflate(R.layout.mensajes, container, false)
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

        //Obtiene el Bundle pasado al Fragment cuando se creo
        val bundle = getArguments()

        //Obtiene un objeto MeetingPoint con los datos del punto de encuentro a partir del Bundle
        meetingPoint = bundle.getSerializable("meetingPoint") as MeetingPoint
        //Obtiene un objeto Usuario con los datos del usuario a partir del Bundle
        usuario = bundle.getSerializable("usuario") as Usuario

        //Inicializa la lista de mensajes
        mensajes = ArrayList()

        //Obtiene una referencia a la base de datos
        databaseReference = FirebaseDatabase.getInstance().reference

        //Crea un gestor LinearLayout
        val linearLayoutManager = LinearLayoutManager(context)
        //setStackFromEnd: cuando el RecyclerView rellena su contenido, empieza desde el final
        //de la vista, asi se muestra la lista desde el final, y cuando abrimos el teclado la
        //lista se ajusta al borde del teclado
        linearLayoutManager.stackFromEnd = true
        //Configura el RecyclerView con el LinearLayoutManager
        recyclerview.layoutManager = linearLayoutManager
        //Crea un adaptador de mensajes
        mensajesAdaptador = MensajeAdaptador(mensajes!!)
        //Configura el RecyclerView con el adaptador de mensajes
        recyclerview.adapter = mensajesAdaptador

        //Inicia la escucha de mensajes
        iniciarEscuchadorMensajes()

        //Configura un escuchador de clicks en el boton
        botonEnviarMensaje.setOnClickListener {
            //Si el campo de texto no esta vacio
            if (!TextUtils.isEmpty(etMensaje.text.toString())) {
                //Llama al metodo encargado de enviar el mensaje
                enviarMensaje(etMensaje.text.toString())
            }
        }
    }

    /**
     * enviarMensaje: metodo encargado de enviar el mensaje
     * @param mensaje texto del mensaje
     */
    fun enviarMensaje(mensaje: String) {

        //Crea un SimpleDateFormat utilizado para dar formato a la fecha del mensaje
        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm")

        //Se introducen en listas el apodo y el id del punto de encuentro para incluirlos en el constructor
        //del nuevo mensaje
        val receptores = ArrayList<String>()
        receptores.add(meetingPoint!!.nombre!!)
        val idReceptores = ArrayList<String>()
        idReceptores.add(meetingPoint!!.id!!)

        //Crea un objeto mensaje
        val mensajeTexto = Mensaje(usuario!!.apodo!!, usuario!!.id!!, receptores,
                idReceptores, mensaje, 0, simpleDateFormat.format(Date()))

        //Almacena en la base de datos el mensaje
        databaseReference!!.child("mensajes").child("meeting_points")
                .child(meetingPoint!!.id!!).push().setValue(mensajeTexto)

        //Vacia el campo de texto
        etMensaje!!.setText("")
    }

    /**
     * setScrollBarMensajes: metodo encargado de desplazar la lista al final
     */
    fun setScrollBarMensajes() {
        //Realiza scroll hasta el final de la lista del RecyclerView
        recyclerview.scrollToPosition(mensajesAdaptador!!.getItemCount() - 1)
    }


    /**
     * iniciarEscuchadorMensajes: metodo encargado de obtener los mensajes de la base de datos
     * a traves de un escuchador
     */
    fun iniciarEscuchadorMensajes() {
        //Establece un escuchador en la referencia de la base de datos donde se almacenan los mensajes
        //del punto de encuentro, obtiene los ultimos 40 nodos (limitToLast(40))
        databaseReference!!.child("mensajes").child("meeting_points")
                .child(meetingPoint!!.id!!).limitToLast(40).addChildEventListener(object : ChildEventListener {

            /**
             * onChildAdded: este metodo se ejecuta cuando un nuevo nodo hijo es agregado a la referencia
             * de la base de datos (un nuevo mensaje). Este metodo tambien se ejecuta al crear el
             * escuchador, obteniendo un resultado inicial.
             * @param dataSnapshot
             * *
             * @param s
             */
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {

                //Obtiene el mensaje a partir del DataSnapShot
                val mensajeTexto = dataSnapshot.getValue(Mensaje::class.java)
                //Si el emisor del mensaje es el usuario
                if (mensajeTexto!!.idEmisor != usuario!!.id)
                //Establece el tipo de mensaje en 1 (mensaje entrante)
                    mensajeTexto.tipoMensaje = 1

                //Añade el mensaje a la lista de mensajes
                mensajes!!.add(mensajeTexto)
                //Notifica al adaptador que el conjunto de datos ha cambiado, de forma que este
                //se actualice
                mensajesAdaptador!!.notifyDataSetChanged()
                //Realiza scroll hasta el final de la lista en el RecyclerView
                setScrollBarMensajes()

            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {

            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })

    }
}