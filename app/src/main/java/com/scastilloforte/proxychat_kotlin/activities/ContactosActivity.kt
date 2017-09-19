package com.scastilloforte.proxychat_kotlin.activities

import android.support.design.widget.Snackbar
import android.content.DialogInterface
import com.google.firebase.database.DatabaseError
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import com.scastilloforte.proxychat_kotlin.modelos.Usuario
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.FirebaseDatabase
import android.support.v7.widget.LinearLayoutManager
import android.widget.TextView
import android.support.v7.widget.RecyclerView
import com.google.firebase.database.DatabaseReference
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import com.scastilloforte.proxychat_kotlin.R
import com.scastilloforte.proxychat_kotlin.adaptadores.UsuariosAdaptador
import com.scastilloforte.proxychat_kotlin.interfaces.OnItemClickListener
import com.scastilloforte.proxychat_kotlin.interfaces.OnItemLongClickListener


/**
 * Created by netx on 7/27/17.
 */
/**
 * ContactosActivity: Actividad que muestra la lista de contactos
 */
class ContactosActivity : AppCompatActivity(), OnItemClickListener, OnItemLongClickListener {

    private var recyclerView: RecyclerView? = null
    private var contactosAdaptador: UsuariosAdaptador? = null
    private var contactos: MutableList<String>? = null
    private var databaseReference: DatabaseReference? = null
    private var usuario: Usuario? = null
    private var tvNumeroContactos: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contactos)

        //Configura el ActionBar para mostrar el boton de ir atras
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        //Obtiene el objeto Usuario correspondiente al usuario a traves del intent
        usuario = intent.extras.getSerializable("usuario") as Usuario

        //Inicializa el RecyclerView a traves del cual se muestra la lista de contactos
        recyclerView = findViewById<View>(R.id.recyclerviewContactos) as RecyclerView
        //Inicializa el TextView que muestra el numero de contactos
        tvNumeroContactos = findViewById<View>(R.id.tvNumeroContactos) as TextView

        //Crea un gestor LinearLayout
        val linearLayoutManager = LinearLayoutManager(applicationContext)
        //Configura el RecyclerView con el LinearLayoutManager
        recyclerView!!.layoutManager = linearLayoutManager
        //Inicializa la lista de contactos
        contactos = ArrayList()

        //Crea un adaptador de contactos
        contactosAdaptador = UsuariosAdaptador(this, contactos!!, usuario!!.id!!)
        //Configura el RecyclerView con el adaptador de contactos
        recyclerView!!.adapter = contactosAdaptador
        //Establece un escuchador de clicks para el adaptador (el escuchador es la propia actividad)
        contactosAdaptador!!.setOnItemClickListener(this)
        //Establece un escuchador de pulsaciones largas para el adaptador (el escuchador es la propia actividad)
        contactosAdaptador!!.setOnItemLongClickListener(this)

        //Obtiene una referencia a la base de datos
        databaseReference = FirebaseDatabase.getInstance().reference

        //Establece un escuchador en la referencia de la base de datos donde se almacenan los contactos
        //del usuario
        databaseReference!!.child("contactos").child("usuarios").child(usuario!!.id!!).child("usuarios")
                .addChildEventListener(object : ChildEventListener {
                    override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                        //Obtiene el id del contacto a partir del DataSnapShot
                        val keyContacto = dataSnapshot.key

                        //Añade el id del contacto a la lista de contactos
                        contactos!!.add(keyContacto)
                        //Actualiza el numero de contactos
                        tvNumeroContactos!!.text = "Contactos: " + contactos!!.size
                        //Notifica al adaptador que el conjunto de datos ha cambiado, de forma que este
                        //se actualice
                        contactosAdaptador!!.notifyDataSetChanged()
                    }

                    override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

                    }

                    /**
                     * onChildRemoved: este metodo se ejecuta cuando un nodo hijo es borrado de la base de datos
                     * @param dataSnapshot
                     */
                    override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                        //Elimina el id de contacto de la lista de contactos
                        contactos!!.remove(dataSnapshot.key)
                        //Actualiza el numero de contactos
                        tvNumeroContactos!!.text = "Contactos: " + contactos!!.size
                        //Notifica al adaptador que el conjunto de datos ha cambiado, de forma que este
                        //se actualice
                        contactosAdaptador!!.notifyDataSetChanged()
                    }

                    override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

                    }

                    override fun onCancelled(databaseError: DatabaseError) {

                    }
                })

    }

    override fun onStart() {
        super.onStart()

    }

    /**
     * onClick: este metodo se ejecuta cuando el usuario pulsa sobre uno de los items del RecyclerView
     * @param view item que ha sido pulsado
     * *
     * @param position posicion del item dentro del RecyclerView
     */
    override fun onClick(view: View, position: Int) {
        //Obtiene de la lista el id del contacto
        val contacto = contactos!![position]

        //Establece un escuchador para obtener los datos del contacto
        databaseReference!!.child("usuarios").child(contacto).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                //Obtiene un objeto Usuario con los datos del contacto a partir del DataSnapshot
                val usuarioContacto = dataSnapshot.getValue(Usuario::class.java)
                //Crea un bundle
                val bundle = Bundle()
                //Añade al bundle el objeto Usuario del contacto
                bundle.putSerializable("contacto", usuarioContacto)
                //Añade al bundle el objeto Usuario del usuario
                bundle.putSerializable("usuario", usuario)

                //Crea un intent que permitira iniciar la actividad de chat
                val intent = Intent(applicationContext, ChatActivity::class.java)
                //Añade el bundle al intent
                intent.putExtras(bundle)
                //Inicia la actividad de chat a partir del intent
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
        val items = arrayOf<CharSequence>("Eliminar contacto")

        //Crea un constructor de dialogos
        val builder = AlertDialog.Builder(this)

        //Establece el titulo del dialogo
        builder.setTitle("Opciones")
        //Configura el dialogo con los items (opciones) que tendra, tambien se añade un escuchador
        //que recibira los eventos de click en cada una de las opciones del menu contextual
        builder.setItems(items, DialogInterface.OnClickListener { dialog, item ->
            when (item) {
                0 ->
                    //Elimina de la base de datos el nodo correspondiente al contacto en la lista
                    //de contactos del usuario,
                    //se añaden ademas escuchadores que realizaran acciones dependiendo de si la operacion
                    //fue o no un exito
                    databaseReference!!.child("contactos").child("usuarios").child(usuario!!.id!!)
                            .child("usuarios").child(contactos!![position]).removeValue()
                            .addOnSuccessListener {
                                //Muestra un Snackbar informando al usuario de que el contacto se ha eliminado
                                Snackbar.make(view, "Contacto eliminado",
                                        Snackbar.LENGTH_LONG).show()
                            }.addOnFailureListener {
                        //Muestra un Snackbar informando al usuario del error
                        Snackbar.make(view,
                                "No se ha podido eliminar el contacto",
                                Snackbar.LENGTH_LONG).show()
                    }
            }
        })

        //Muestra el dialogo
        builder.show()
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
            else -> return super.onOptionsItemSelected(item)
        }
    }
}
