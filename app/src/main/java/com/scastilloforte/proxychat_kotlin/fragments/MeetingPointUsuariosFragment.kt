package com.scastilloforte.proxychat_kotlin.fragments

import com.google.firebase.database.DatabaseError
import android.support.v4.content.ContextCompat.startActivity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.support.v4.app.Fragment
import com.scastilloforte.proxychat_kotlin.modelos.Usuario
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.FirebaseDatabase
import android.support.v7.widget.LinearLayoutManager
import android.widget.TextView
import android.support.v7.widget.RecyclerView
import com.scastilloforte.proxychat_kotlin.modelos.MeetingPoint
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import com.google.firebase.database.DatabaseReference
import com.scastilloforte.proxychat_kotlin.R
import com.scastilloforte.proxychat_kotlin.adaptadores.UsuariosAdaptador
import com.scastilloforte.proxychat_kotlin.interfaces.OnItemClickListener
import kotlinx.android.synthetic.main.fragment_meetingpoint_usuarios.view.*


/**
 * Created by netx on 7/27/17.
 */


/**
 * MeetingPointUsuariosFragment: Fragment que muestra los usuarios que participan en un punto de encuentro
 */
class MeetingPointUsuariosFragment : Fragment(), OnItemClickListener {

    private var recyclerView: RecyclerView? = null
    private var usuariosAdaptador: UsuariosAdaptador? = null
    private var usuarios: MutableList<String>? = null
    private var databaseReference: DatabaseReference? = null
    private var usuario: Usuario? = null
    private var meetingPoint: MeetingPoint? = null
    private var tvNumeroUsuarios: TextView? = null

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
        val rootView = inflater.inflate(R.layout.fragment_meetingpoint_usuarios, container, false)
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

        //Obtiene el Bundle pasado al Fragment a la hora de crearlo
        val bundle = getArguments()
        //Obtiene un objeto Usuario con los datos del usuario a partir del Bundle
        usuario = bundle.getSerializable("usuario") as Usuario
        //Obtiene un objeto MeetingPoint con los datos del punto de encuentro a partir del Bundle
        meetingPoint = bundle.getSerializable("meetingPoint") as MeetingPoint

        //Inicializa el RecyclerView
        recyclerView = view.recyclerviewContactos

        //Inicializa el TextView que muestra el numero de usuarios
        tvNumeroUsuarios = view.tvNumeroContactos

        //Crea un gestor LinearLayout
        val linearLayoutManager = LinearLayoutManager(context)
        //Configura el RecyclerView con el LinearLayoutManager
        recyclerView!!.layoutManager = linearLayoutManager
        //Inicializa la lista de usuarios
        usuarios = ArrayList()

        //Crea un adaptador de usuarios
        usuariosAdaptador = UsuariosAdaptador(context, usuarios!!, usuario!!.id!!)
        //Configura el RecyclerView con el adaptador de usuarios
        recyclerView!!.adapter = usuariosAdaptador
        //Establece un escuchador de clicks para el adaptador, el esuchador es el propio Fragment
        usuariosAdaptador!!.setOnItemClickListener(this)

        //Obtiene una referencia a la base de datos
        databaseReference = FirebaseDatabase.getInstance().reference

        //Realiza una consulta a la base de datos para obtener los usuarios que participan en el
        //punto de encuentro
        databaseReference!!.child("contactos").child("meeting_points").child(meetingPoint!!.id!!)
                .addChildEventListener(object : ChildEventListener {

                    /**
                     * onChildAdded: este metodo se ejecuta cuando un nuevo nodo hijo es agregado a la referencia
                     * de la base de datos (un nuevo usuario agregado al punto de encuentro).
                     * Este metodo tambien se ejecuta al crear el
                     * escuchador, obteniendo un resultado inicial.
                     * @param dataSnapshot
                     * *
                     * @param s
                     */
                    override fun onChildAdded(dataSnapshot: DataSnapshot, s: String) {
                        //Obtiene la clave que identifica al usuario a partir del DataSnapshot
                        val keyContacto = dataSnapshot.key

                        //Añade el id del usuario a la lista de usuarios
                        usuarios!!.add(keyContacto)
                        //Actualiza el TextView que muestra el numero de usuarios
                        tvNumeroUsuarios!!.text = "Usuarios: " + usuarios!!.size
                        //Notifica al adaptador que el conjunto de datos ha cambiado, de forma que este
                        //se actualice
                        usuariosAdaptador!!.notifyDataSetChanged()
                    }

                    override fun onChildChanged(dataSnapshot: DataSnapshot, s: String) {

                    }

                    override fun onChildRemoved(dataSnapshot: DataSnapshot) {

                    }

                    override fun onChildMoved(dataSnapshot: DataSnapshot, s: String) {

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
        //Obtiene el id del usuario que corresponde con el item pulsado
        val keyUsuario = usuarios!![position]

        //Si el id del usuario no es el del propio usuario
        if (keyUsuario != usuario!!.id) {
            //Realiza una consulta a la base de datos para obtener los datos del usuario
            databaseReference!!.child("usuarios").child(keyUsuario).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    //Obtiene un objeto Usuario con los datos del usuario a partir del DataSnapshot
                    val usuarioContacto = dataSnapshot.getValue(Usuario::class.java)

                    //Crea un BottomSheetDialogFragment con los datos del usuario
                    val bsdf : BottomSheetDialogFragment = BSFragmentUser()
                    val bundle : Bundle = Bundle()
                    bundle.putSerializable("contacto", usuarioContacto)
                    bundle.putString("usuario", tag.substring(2))
                    bsdf.arguments = bundle
                    bsdf.show(activity.supportFragmentManager, bsdf.tag)
                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            })
        }
    }
}