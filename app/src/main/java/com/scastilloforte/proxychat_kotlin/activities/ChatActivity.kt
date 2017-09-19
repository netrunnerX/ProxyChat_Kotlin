package com.scastilloforte.proxychat_kotlin.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle

import com.scastilloforte.proxychat_kotlin.R
import com.google.firebase.database.DatabaseError
import android.support.design.widget.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener
import com.scastilloforte.proxychat_kotlin.modelos.Mensaje
import com.google.firebase.database.ChildEventListener
import android.text.TextUtils
import com.scastilloforte.proxychat_kotlin.adaptadores.MensajeAdaptador
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.database.FirebaseDatabase
import com.scastilloforte.proxychat_kotlin.modelos.Usuario
import com.google.firebase.database.DatabaseReference
import kotlinx.android.synthetic.main.mensajes.*
import java.text.SimpleDateFormat
import java.util.*


/**
 * ChatActivity: actividad que muestra el chat entre contactos
 */
class ChatActivity : AppCompatActivity() {

    private var contacto: Usuario? = null
    private var usuario: Usuario? = null
    private var mensajes: ArrayList<Mensaje>? = null
    private var databaseReference: DatabaseReference? = null
    private var mensajesAdaptador: MensajeAdaptador? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mensajes)

        //Configura el ActionBar para mostrar el boton de ir atras
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        //Obtiene el bundle del intent
        val bundle = intent.extras
        //Obtiene el objeto Usuario correspondiente al contacto
        contacto = bundle.getSerializable("contacto") as Usuario
        //Establece el titulo de la actividad con el apodo del contacto
        this.title = contacto!!.apodo
        //Obtiene el objecto Usuario del usuario
        usuario = bundle.getSerializable("usuario") as Usuario

        //Inicializa la lista de mensajes
        mensajes = ArrayList()
        //Obtiene una referencia a la base de datos
        databaseReference = FirebaseDatabase.getInstance().reference

        //Crea un gestor LinearLayout
        val linearLayoutManager = LinearLayoutManager(this)
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

            if (!TextUtils.isEmpty(etMensaje!!.text.toString())) {
                //Llama al metodo encargado de enviar el mensaje
                enviarMensaje(etMensaje!!.text.toString())
            }

        }

        habilitarComponentes(false)
        iniciarEscuchadoresBloqueado()

    }

    /**
     * enviarMensaje: metodo encargado de enviar el mensaje
     * @param mensaje texto del mensaje
     */
    fun enviarMensaje(mensaje: String) {

        //Crea un SimpleDateFormat utilizado para dar formato a la fecha del mensaje
        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm")

        //Se introducen en listas el apodo y el id del contacto para incluirlos en el constructor
        //del nuevo mensaje
        val receptores = ArrayList<String>()
        receptores.add(contacto!!.apodo!!)
        val idReceptores = ArrayList<String>()
        idReceptores.add(contacto!!.id!!)

        //Crea un objeto mensaje
        val mensajeTexto = Mensaje(usuario!!.apodo!!, usuario!!.id!!, receptores,
                idReceptores, mensaje, 0, simpleDateFormat.format(Date()))

        //Almacena en la base de datos el mensaje
        databaseReference!!.child("mensajes").child("usuarios")
                .child(usuario!!.id!!)
                .child(contacto!!.id!!).push().setValue(mensajeTexto)

        //Vacia el campo de texto
        etMensaje.setText("")
    }

    /**
     * setScrollBarMensajes: metodo encargado de desplazar la lista al final
     */
    fun setScrollBarMensajes() {
        //Realiza scroll hasta el final de la lista del RecyclerView
        recyclerview.scrollToPosition(mensajesAdaptador!!.itemCount - 1)
    }


    /**
     * iniciarEscuchadorMensajes: metodo encargado de obtener los mensajes de la base de datos
     * a traves de un escuchador
     */
    fun iniciarEscuchadorMensajes() {
        //Establece un escuchador en la referencia de la base de datos donde se almacenan los mensajes
        //entre usuarios, obtiene los ultimos 40 nodos (limitToLast(40))
        databaseReference!!.child("mensajes").child("usuarios")
                .child(usuario!!.id!!)
                .child(contacto!!.id!!).limitToLast(40).addChildEventListener(object : ChildEventListener {

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
                //Si el receptor del mensaje es el usuario
                if (mensajeTexto!!.idReceptores!![0] == usuario!!.id)
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

    fun iniciarEscuchadoresBloqueado() {
        databaseReference!!.child("contactos")
                .child("usuarios")
                .child(usuario!!.id!!)
                .child("bloqueados")
                .child(contacto!!.id!!).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val bBloqueado = dataSnapshot.getValue(Boolean::class.java)

                if (bBloqueado == null) {
                    databaseReference!!.child("contactos")
                            .child("usuarios")
                            .child(contacto!!.id!!)
                            .child("bloqueados")
                            .child(usuario!!.id!!).addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val bBloqueado = dataSnapshot.getValue(Boolean::class.java)

                            if (bBloqueado == null)
                                habilitarComponentes(true)
                            else
                                habilitarComponentes(false)
                        }

                        override fun onCancelled(databaseError: DatabaseError) {

                        }
                    })
                } else
                    habilitarComponentes(false)
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })

    }

    fun habilitarComponentes(estado: Boolean) {
        etMensaje.isEnabled = estado
        botonEnviarMensaje.isEnabled = estado
    }

    /**
     * onCreateOptionsMenu: este metodo se redefine para crear un menu de opciones
     * @param menu
     * *
     * @return
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_chat, menu)
        return true
    }

    /**
     * onOptionsItemSelected: en este metodo se realizan los acciones para cada item de menu cuando estos
     * son seleccionados
     * @param item
     * *
     * @return
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.getItemId()) {
        //Si el item corresponde con el boton de ir atras
            android.R.id.home -> {
                //Termina la actividad
                finish()
                return true
            }

            R.id.action_bloquear -> {
                databaseReference!!.child("contactos")
                        .child("usuarios")
                        .child(usuario!!.id!!)
                        .child("bloqueados")
                        .child(contacto!!.id!!).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val bBloqueado = dataSnapshot.getValue(Boolean::class.java)

                        if (bBloqueado == null) {
                            databaseReference!!.child("contactos")
                                    .child("usuarios")
                                    .child(usuario!!.id!!)
                                    .child("bloqueados")
                                    .child(contacto!!.id!!)
                                    .setValue(true).addOnSuccessListener {
                                Snackbar.make(etMensaje!!,
                                        "Contacto bloqueado",
                                        Snackbar.LENGTH_LONG).show()
                            }.addOnFailureListener {
                                Snackbar.make(etMensaje!!,
                                        "No se ha podido bloquear al contacto",
                                        Snackbar.LENGTH_LONG).show()
                            }
                        } else {
                            Snackbar.make(etMensaje!!,
                                    "El contacto ya se encuentra bloqueado",
                                    Snackbar.LENGTH_LONG).show()
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {

                    }
                })
                return super.onOptionsItemSelected(item)
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }
}
