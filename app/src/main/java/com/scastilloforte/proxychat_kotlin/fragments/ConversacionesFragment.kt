package com.scastilloforte.proxychat_kotlin.fragments

import android.support.design.widget.Snackbar
import android.content.DialogInterface
import com.google.firebase.database.DatabaseError
import com.scastilloforte.proxychat_kotlin.activities.ChatActivity
import android.content.Intent
import android.os.Bundle
import com.scastilloforte.proxychat_kotlin.modelos.Usuario
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener
import com.scastilloforte.proxychat_kotlin.modelos.Conversacion
import com.scastilloforte.proxychat_kotlin.modelos.Mensaje
import com.google.firebase.database.ChildEventListener
import com.scastilloforte.proxychat_kotlin.activities.ContactosActivity
import com.scastilloforte.proxychat_kotlin.activities.MainActivity
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import com.google.firebase.database.FirebaseDatabase
import android.support.v7.widget.LinearLayoutManager
import android.widget.TextView
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import com.google.firebase.database.DatabaseReference
import com.scastilloforte.proxychat_kotlin.R
import com.scastilloforte.proxychat_kotlin.adaptadores.ConversacionesAdaptador
import com.scastilloforte.proxychat_kotlin.interfaces.OnItemClickListener
import com.scastilloforte.proxychat_kotlin.interfaces.OnItemLongClickListener
import kotlinx.android.synthetic.main.fragment_conversaciones.view.*


/**
 * Created by netx on 7/27/17.
 */
/**
 * ConversacionesFragment: Fragment que muestra la lista de conversaciones
 */
class ConversacionesFragment : Fragment(), OnItemClickListener, OnItemLongClickListener {

    private var recyclerView: RecyclerView? = null
    private var conversacionesAdaptador: ConversacionesAdaptador? = null
    private var conversaciones: MutableList<Conversacion>? = null
    private var databaseReference: DatabaseReference? = null
    private var floatingActionButton: FloatingActionButton? = null
    private var usuario: Usuario? = null
    private var tvNumeroConversaciones: TextView? = null


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
        //Se crea una nueva vista cargando en esta el layout de conversaciones
        val rootView = inflater.inflate(R.layout.fragment_conversaciones, container, false)
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

        //Obtiene el objeto Usuario contenido en la actividad principal
        usuario = (getActivity() as MainActivity).user
        //Inicializa el RecyclerView
        recyclerView = view.recyclerview
        //Inicializa el TextView que muestra el numero de conversaciones
        tvNumeroConversaciones = view.tvNumeroConversaciones

        //Crea un gestor LinearLayout
        val linearLayoutManager = LinearLayoutManager(getContext())
        //Configura el RecyclerView con el gestor LinearLayout
        recyclerView!!.layoutManager = linearLayoutManager

        //Inicializa la lista de conversaciones
        conversaciones = ArrayList()

        //Inicializa el adaptador de conversaciones
        conversacionesAdaptador = ConversacionesAdaptador(context, conversaciones!!)
        //Configura un OnItemClickListener para el adaptador, pasando como esuchador el propio Fragment
        conversacionesAdaptador!!.setOnItemClickListener(this)
        //Configura un OnItemLongClickListener para el adaptador, pasando como esuchador el propio Fragment
        conversacionesAdaptador!!.setOnItemLongClickListener(this)
        //Configura como adaptador del RecyclerView el adaptador de conversaciones
        recyclerView!!.adapter = conversacionesAdaptador

        //Obtiene una referencia a la base de datos
        databaseReference = FirebaseDatabase.getInstance().reference

        //Inicia el escuchador encargado de obtener los datos de los usuarios
        iniciarEscuchador()

        //Inicializa el FloatingActionButon
        floatingActionButton = view.fab
        //Establece un escuchador de clicks para el boton
        floatingActionButton!!.setOnClickListener {
            //Crea un Bundle
            val bundle = Bundle()
            //Añade el objeto Usuario con los datos de nuestro usuario al Bundle
            bundle.putSerializable("usuario", (getActivity() as MainActivity).user)
            //Crea un Intent utilizado para iniciar la actividad de contactos
            val intent = Intent(getContext(), ContactosActivity::class.java)
            //Añade el Bundle al Intent
            intent.putExtras(bundle)

            //Inicia la actividad
            startActivity(intent)
        }


    }

    /**
     * iniciarEscuchador: este metodo se encarga de realizar una consulta a la base de datos para obtener
     * los datos de cada conversacion
     */
    fun iniciarEscuchador() {
        //Realiza una consulta a la referencia donde se encuentra la bandeja de mensajes privados del usuario
        databaseReference!!.child("mensajes").child("usuarios")
                .child(usuario!!.id!!).addChildEventListener(object : ChildEventListener {
            /**
             * onChildAdded: este metodo se ejecuta para obtener un resultado inicial, y despues
             * se ejecutara cada vez que se añade un nuevo nodo (por ejemplo en el caso de que se almacene
             * un mensaje de un contanto que no se encuentre previamente en la bandeja de mensajes del usuario)
             * @param dataSnapshot
             * *
             * @param s
             */
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                //Declara un objeto Mensaje que contendra el ultimo mensaje
                var ultimoMensaje: Mensaje? = null

                //Obtiene un iterador con los nodos hijos del Snapshot, estos nodos hijos son los
                //mensajes de uno de los contactos
                val dataSnaps = dataSnapshot.children.iterator()

                //Recorre los nodos asignando el mensaje a ultimoMensaje en cada iteracion,
                //de esta forma cuando termina el bucle dispondremos del ultimo mensaje
                while (dataSnaps.hasNext()) {
                    ultimoMensaje = dataSnaps.next().getValue(Mensaje::class.java)
                }

                //Obtiene el id del emisor
                val idEmisor = ultimoMensaje!!.idEmisor
                //Obtiene el nombre del emisor
                val emisor = ultimoMensaje.emisor
                //Obtiene el id del receptor
                val idReceptor = ultimoMensaje.idReceptores!![0]
                //Obtiene el nombre del receptor
                val receptor = ultimoMensaje.receptores!![0]

                //Declara una variable para almacenar el nombre del contacto de la conversacion
                val contacto: String
                //Declara una variable para almacenar el id del contacto de la conversacion
                val idContacto: String

                //Si el receptor del mensaje no es el propio usuario
                if (idReceptor != usuario!!.id) {
                    //Establece como contacto el receptor del mensaje
                    contacto = receptor
                    idContacto = idReceptor
                } else {
                    //Establece como contacto el emisor del mensaje
                    contacto = emisor!!
                    idContacto = idEmisor!!
                }//Por otra parte, si el receptor del mensaje es el usuario

                //Obtiene el texto del mensaje
                var ultMensaje = ultimoMensaje.mensaje

                //Si la longitud del texto del mensaaje es mayor de 25 caracteres
                if (ultMensaje!!.length > 25) {
                    //Acota el mensaje a 25 caracteres y le concatena puntos suspensivos
                    ultMensaje = ultMensaje.substring(0, 25) + "..."
                }

                //Crea una conversacion con los datos obtenidos
                val conversacion = Conversacion(contacto, idContacto, ultMensaje)

                //Añade la conversacion a la lista
                conversaciones!!.add(conversacion)
                //Notifica al adaptador que hubo cambios en el conjunto de datos, de forma
                //que este actualice el RecyclerView
                conversacionesAdaptador!!.notifyDataSetChanged()
                //Actualiza el TextView que muestra el numero de conversaciones
                tvNumeroConversaciones!!.text = "Conversaciones: " + conversaciones!!.size

            }

            /**
             * onChildChanged: este metodo se ejecuta cuando el valor que contiene uno de los nodos
             * cambia, esto es en el caso de que sea almacenado un nuevo mensaje de un contacto
             * que ya se encuentra presente en la bandeja de mensajes del usuario
             * @param dataSnapshot
             * *
             * @param s
             */
            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
                //Declara un objeto Mensaje que contendra el ultimo mensaje
                var ultimoMensaje: Mensaje? = null

                //Obtiene un iterador con los nodos hijos del Snapshot, estos nodos hijos son los
                //mensajes de uno de los contactos
                val dataSnaps = dataSnapshot.children.iterator()

                //Recorre los nodos asignando el mensaje a ultimoMensaje en cada iteracion,
                //de esta forma cuando termina el bucle dispondremos del ultimo mensaje
                while (dataSnaps.hasNext()) {
                    ultimoMensaje = dataSnaps.next().getValue(Mensaje::class.java)
                }

                //Obtiene el nombre del emisor
                val emisor = ultimoMensaje!!.emisor
                //Obtiene el id del emisor
                val idEmisor = ultimoMensaje!!.idEmisor
                //Obtiene el nombre del receptor
                val receptor = ultimoMensaje!!.receptores!![0]
                //Obtiene el id del receptor
                val idReceptor = ultimoMensaje!!.idReceptores!![0]

                //Declara una variable para almacenar el nombre del contacto de la conversacion
                val contacto: String
                //Declara una variable para almacenar el id del contacto de la conversacion
                val idContacto: String

                //Si el receptor del mensaje no es el propio usuario
                if (idReceptor != usuario!!.id) {
                    //Establece como contacto el receptor del mensaje
                    contacto = receptor
                    idContacto = idReceptor
                } else {
                    //Establece como contacto el emisor del mensaje
                    contacto = emisor!!
                    idContacto = idEmisor!!
                }//Por otra parte, si el receptor del mensaje es el usuario

                //Obtiene el texto del mensaje
                var ultMensaje = ultimoMensaje!!.mensaje

                //Si la longitud del texto del mensaaje es mayor de 25 caracteres
                if (ultMensaje!!.length > 25) {
                    //Acota el mensaje a 25 caracteres y le concatena puntos suspensivos
                    ultMensaje = ultMensaje!!.substring(0, 25) + "..."
                }

                //Crea una conversacion con los datos del mensaje
                val conversacion = Conversacion(contacto, idContacto, ultMensaje)

                //Recorre la lista de conversaciones
                for (i in conversaciones!!.indices) {
                    //Si encuentra en la lista la conversacion cuyo id es el id del contacto
                    if (conversaciones!![i].idContacto == conversacion.idContacto) {
                        //Actualiza el ultimo mensaje de la conversacion
                        conversaciones!![i].ultimoMensaje = conversacion.ultimoMensaje
                        //Notifica al adaptador que hubo cambios en el conjunto de datos, de forma
                        //que este actualice el RecyclerView
                        conversacionesAdaptador!!.notifyDataSetChanged()
                        //Sale del bucle
                        break
                    }
                }

            }

            /**
             * onChildRemoved: metodo que se ejecuta cuando un nodo es eliminado de la base de datos,
             * este es el caso en el que el usuario elimina una conversacion de la lista de conversaciones,
             * lo que hace que el nodo que hace referencia al contacto en la bandeja de mensajes del usuario
             * sea eliminado
             * @param dataSnapshot
             */
            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                //Obtiene el id del contacto, que corresponde con la clave del nodo
                val key = dataSnapshot.key

                //Recorre la lista de conversaciones
                for (i in conversaciones!!.indices) {
                    //Si el id de la conversacion coincide con el id del contacto
                    if (conversaciones!![i].idContacto == key) {
                        //Elimina de la lista la conversacion
                        conversaciones!!.removeAt(i)

                        //Notifica al adaptador que hubo cambios en el conjunto de datos, de forma
                        //que este actualice el RecyclerView
                        conversacionesAdaptador!!.notifyDataSetChanged()
                        //Actualiza el TextView que muestra el numero de conversaciones
                        tvNumeroConversaciones!!.text = "Conversaciones: " + conversaciones!!.size
                        break
                    }
                }
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

        //Obtiene el id de contacto a partir de la lista de conversaciones
        val idContacto = conversaciones!![position].idContacto

        //Realiza una consulta a la base de datos para obtener los datos del contacto
        databaseReference!!.child("usuarios").child(idContacto).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                //Obtiene un objeto Usuario con los datos del contacto a partir del DataSnaphot
                val contacto = dataSnapshot.getValue(Usuario::class.java)
                //Crea un Bundle
                val bundle = Bundle()
                //Añade al Bundle el objeto Usuario con los datos del contacto
                bundle.putSerializable("contacto", contacto)
                //Añade al Bundle el objeto Usuario con los datos del usuario
                bundle.putSerializable("usuario", usuario)

                //Crea un Intent utilizado para iniciar la actividad de chat
                val intent = Intent(getContext(), ChatActivity::class.java)
                //Añade el Bundle al Intent
                intent.putExtras(bundle)
                //Inicia la actividad
                startActivity(intent)
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })

    }

    /**
     * onLongClick: metodo que se ejecuta cuando el usuario realiza una pulsacion larga sobre uno de
     * los items del RecyclerView
     * @param view item pulsado
     * *
     * @param position posicion del item dentro del RecyclerView
     * *
     * @return
     */
    override fun onLongClick(view: View, position: Int): Boolean {

        //Inicializa un array CharSequence que contiene la descripcion para cada opcion del menu contextual
        val items = arrayOf<CharSequence>("Eliminar Conversación")

        //Crea un constructor de dialogos
        val builder = AlertDialog.Builder(getContext())

        //Establece el titulo del dialogo
        builder.setTitle("Opciones")
        //Configura el dialogo con los items (opciones) que tendra, tambien se añade un escuchador
        //que recibira los eventos de click en cada una de las opciones del menu contextual
        builder.setItems(items, DialogInterface.OnClickListener { dialog, item ->
            when (item) {
                0 ->
                    //Elimina de la base de datos el nodo correspondiente al contacto de la conversacion
                    //en la bandeja de mensajes del usuario
                    //se añaden ademas escuchadores que realizaran acciones dependiendo de si la operacion
                    //fue o no un exito
                    databaseReference!!.child("mensajes").child("usuarios").child(usuario!!.id!!)
                            .child(conversaciones!![position].idContacto).removeValue()
                            .addOnSuccessListener {
                                //Muestra un Snackbar informando al usuario de que la conversacion se ha eliminado
                                Snackbar.make(view, "Conversacion eliminada",
                                        Snackbar.LENGTH_LONG).show()
                            }.addOnFailureListener {
                        //Muestra un Snackbar informando al usuario del error
                        Snackbar.make(view, "No se ha podido eliminar la conversacion",
                                Snackbar.LENGTH_LONG).show()
                    }
            }
        })

        //Muestra el dialogo
        builder.show()
        return true
    }
}