package com.scastilloforte.proxychat_kotlin.fragments

import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.scastilloforte.proxychat_kotlin.modelos.Mensaje
import com.google.firebase.database.ChildEventListener
import android.support.design.widget.Snackbar
import com.scastilloforte.proxychat_kotlin.activities.MainActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.support.v7.widget.LinearLayoutManager
import com.scastilloforte.proxychat_kotlin.adaptadores.MensajeAdaptador
import com.google.firebase.database.FirebaseDatabase
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import com.scastilloforte.proxychat_kotlin.modelos.Usuario
import com.google.firebase.database.DatabaseReference
import com.scastilloforte.proxychat_kotlin.R
import kotlinx.android.synthetic.main.mensajes.*
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by netx on 9/19/17.
 */
/**
 * ProxyFragment: Fragment que muestra el chat por proximidad
 */
class ProxyFragment : Fragment() {

    private var databaseReference: DatabaseReference? = null

    private var maProxy: MensajeAdaptador? = null
    private var mensajesProxy: ArrayList<Mensaje>? = null
    private var usuario: Usuario? = null

    private var usuariosCercanos: ArrayList<String>? = null

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
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                     savedInstanceState: Bundle?): View {
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

        //Obtiene un objeto Usuario con los datos del usuario contenido en la actividad principal
        usuario = (getActivity() as MainActivity).user

        //Obtiene una referencia a la base de datos
        databaseReference = FirebaseDatabase.getInstance().reference

        //Inicializa la lista de mensajes
        mensajesProxy = ArrayList()

        //Crea un adaptador de mensajes
        maProxy = MensajeAdaptador(mensajesProxy!!)

        //Crea un gestor LinearLayout
        val linearLayoutManager = LinearLayoutManager(getContext())
        //setStackFromEnd: cuando el RecyclerView rellena su contenido, empieza desde el final
        //de la vista, asi se muestra la lista desde el final, y cuando abrimos el teclado la
        //lista se ajusta al borde del teclado
        linearLayoutManager.stackFromEnd = true
        //Configura el RecyclerView con el LinearLayoutManager
        recyclerview.layoutManager = linearLayoutManager
        //Configura el RecyclerView con el adaptador de mensajes
        recyclerview.adapter = maProxy

        //Inicia la escucha de mensajes
        iniciarEscuchadorProxy()

        //Configura un escuchador de clicks en el boton
        botonEnviarMensaje.setOnClickListener {

            //Si el campo de texto no esta vacio
            if (!TextUtils.isEmpty(etMensaje!!.text.toString())) {
                //Llama al metodo encargado de enviar el mensaje
                enviarMensajeProxy(etMensaje!!.text.toString())
            }

        }

    }

    /**
     * enviarMensajeProxy: metodo encargado de enviar el mensaje por proximidad
     * @param textoMensaje texto del mensaje
     */
    fun enviarMensajeProxy(textoMensaje: String) {

        //Obtiene la lista de usuarios cercanos contenido en el MapFragment
        //Como solo se necesitan las claves, se obtienen llamando al metodo keySet del Map
        //de usuarios cercanos
        usuariosCercanos = ArrayList((activity as MainActivity).mapFragment!!.usersNearProfile!!.keys)

        //Si la lista no es null
        if (usuariosCercanos != null) {

            //Crea un SimpleDateFormat utilizado para dar formato a la fecha del mensaje
            val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm")

            //Crea un objeto Mensaje con los datos del mensaje a enviar
            val mensaje = Mensaje(usuario!!.apodo!!, usuario!!.id!!,
                    ArrayList<String>(), usuariosCercanos!!, textoMensaje, 0,
                    simpleDateFormat.format(Date()))

            //Almacena el mensaje en la base de datos. El servidor se encargara despues de redireccionarlo
            //a la bandeja de mensajes de cada usuario cercano, asi como la del propio usuario
            databaseReference!!.child("mensajesproxy").push().setValue(mensaje)

            //Vacia el campo de texto
            etMensaje!!.setText("")
        } else {
            //Vacia el campo de texto
            etMensaje!!.setText("")
            //Muestra un Snackbar informando al usuario del error
            Snackbar.make(view!!, "No se ha podido enviar el mensaje: usuariosCercanos null",
                    Snackbar.LENGTH_SHORT).show()
        }//Si la lista es null

    }

    /**
     * iniciarEscuchadorProxy: metodo encargado de realizar una consulta a la base de datos
     * y recibir los mensajes almacenados en el nodo proxy de la bandeja de mensajes
     */
    fun iniciarEscuchadorProxy() {

        //Realiza una consulta a la base de datos para obtener los mensajes
        databaseReference!!.child("mensajes").child("proxy").child(usuario!!.id!!).addChildEventListener(object : ChildEventListener {

            /**
             * onChildAdded: este metodo se ejecuta cuando un nuevo nodo hijo es agregado a la referencia
             * de la base de datos (un nuevo mensaje). Este metodo tambien se ejecuta al crear el
             * escuchador, obteniendo un resultado inicial.
             * @param dataSnapshot
             * *
             * @param s
             */
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {

                //Obtiene un objeto Mensaje con los datos del mensaje a partir del DataSnapshot
                val mensajeTexto = dataSnapshot.getValue(Mensaje::class.java)

                //Si el emisor del mensaje no es el propio usuario
                if (mensajeTexto!!.idEmisor != usuario!!.id)
                //Establece el tipo de mensaje en 1 (mensaje entrante)
                    mensajeTexto.tipoMensaje = 1

                //Añade el mensaje a la lista de mensajes
                mensajesProxy!!.add(mensajeTexto)
                //Notifica al adaptador que el conjunto de datos ha cambiado, de forma que este
                //se actualice
                maProxy!!.notifyDataSetChanged()
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

    /**
     * setScrollBarMensajes: metodo encargado de desplazar la lista al final
     */
    fun setScrollBarMensajes() {
        //Realiza scroll hasta el final de la lista del RecyclerView
        recyclerview.scrollToPosition(maProxy!!.itemCount - 1)
    }

}