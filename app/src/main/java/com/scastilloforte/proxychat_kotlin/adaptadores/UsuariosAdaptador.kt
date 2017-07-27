package com.scastilloforte.proxychat_kotlin.adaptadores

import android.R.attr.onClick
import android.content.Context
import android.graphics.Color
import android.net.Uri
import com.scastilloforte.proxychat_kotlin.R.id.ivFotoPerfil
import android.widget.TextView
import android.support.v7.widget.CardView
import android.view.View.OnLongClickListener
import android.support.v7.widget.RecyclerView
import com.google.firebase.database.DatabaseError
import com.scastilloforte.proxychat_kotlin.R.drawable.iconouser
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.Glide
import com.scastilloforte.proxychat_kotlin.modelos.Usuario
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.scastilloforte.proxychat_kotlin.R
import com.scastilloforte.proxychat_kotlin.interfaces.OnItemClickListener
import com.scastilloforte.proxychat_kotlin.interfaces.OnItemLongClickListener
import kotlinx.android.synthetic.main.card_view_contactos.view.*


/**
 * Created by netx on 7/27/17.
 */
/**
 * UsuariosAdaptador: adaptador utilizado para cargar los usuarios que pertenecen a un punto de encuentro
 * en el RecyclerView de MeetingPointUsuariosFragment
 */
class UsuariosAdaptador (val context: Context,
                         val contactos: List<String>,
                         val idUsuario: String) : RecyclerView.Adapter<UsuariosAdaptador.UsuariosViewHolder>() {

    /**
     * onCreateViewHolder: este metodo se ejecuta a la hora de crear un nuevo ViewHolder.
     * Un ViewHolder es un contenedor para un item del RecyclerView
     * @param parent
     * *
     * @param viewType
     * *
     * @return
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuariosViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.card_view_contactos, parent, false)
        return UsuariosViewHolder(v)
    }

    /**
     * onBindViewHolder: este metodo lo llama el RecyclerView a la hora de mostrar el item
     * en una posicion determinada
     * @param holder ViewHolder con los datos a mostrar
     * *
     * @param position posicion en el RecyclerView donde mostrara el item
     */
    override fun onBindViewHolder(holder: UsuariosViewHolder, position: Int) {

        //Realiza una consulta a la base de datos para obtener los datos de un usuario
        val databaseReference = FirebaseDatabase.getInstance().reference
        databaseReference.child("usuarios").child(contactos[position])
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        //Obtiene un objeto Usuario con los datos del usuario a partir del DataSnapshot
                        val usr = dataSnapshot.getValue(Usuario::class.java)

                        //Configura el texto del TextView que muestra el nombre del usuario
                        holder.tvNombre.text = usr.apodo

                        //Si el usuario es el propio usuario
                        if (usr.id == idUsuario) {
                            //Cambia el color del texto por el color verde
                            holder.tvNombre.setTextColor(Color.rgb(48, 191, 0))
                        }

                        //Crea un objeto Uri a partir de la URL de la imagen del usuario
                        val fotoUri = Uri.parse(usr.imagenUrl)

                        //Descarga la imagen y la carga en el ImageView que muestra la imagen del usuario
                        //utilizando la libreria Glide
                        Glide.with(context)
                                .load(fotoUri)
                                .apply(RequestOptions().placeholder(R.drawable.iconouser).centerCrop())
                                .into(holder.ivFotoContacto)

                    }

                    override fun onCancelled(databaseError: DatabaseError) {

                    }
                })
    }

    /**
     * getItemCount: este metodo devuelve el total de items en el RecyclerView
     * @return
     */
    override fun getItemCount(): Int {

        //Como es equivalente al tamaño de la lista de usuarios, se devuelve este
        return contactos.size
    }

    /**
     * UsuariosViewHolder: clase que define un ViewHolder personalizado de usuarios del punto de encuentro
     */
    class UsuariosViewHolder(v: View) : RecyclerView.ViewHolder(v),
            View.OnClickListener, View.OnLongClickListener {

        var cardView: CardView
        var tvNombre: TextView
        var ivFotoContacto: ImageView

        init {

            //Instancia el CardView
            cardView = v.cardViewContactos
            //Instancia el TextView que muestra el nombre del usuario
            tvNombre = v.tvNombreContacto
            //Instancia el ImageView que muestra la imagen del usuario
            ivFotoContacto = v.ivFotoPerfil

            //Configura un ClickListener para la vista
            v.setOnClickListener(this)
            //Configura un LongClickListener para la vista
            v.setOnLongClickListener(this)

        }

        /**
         * onClick: metodo llamado cuando se hace click sobre la vista del item
         * @param v
         */
        override fun onClick(v: View) {
            if (clickListener != null)
            //Llamamos al metodo onClick del objeto OnItemLongClickListener definido en el adaptador
                clickListener!!.onClick(v, adapterPosition)
        }

        /**
         * onLongClick: metodo llamado cuando se hace una pulsacion larga sobre la vista del item
         * @param v
         * *
         * @return
         */
        override fun onLongClick(v: View): Boolean {

            if (longClickListener != null) {
                //Llamamos al metodo onClick del objeto OnItemLongClickListener definido en el adaptador
                return longClickListener!!.onLongClick(v, adapterPosition)
            }
            return false
        }
    }

    /**
     * setOnItemClickListener: metodo utilizado para configurar el clickListener del adaptador
     * @param clickListener escuchador de clicks
     */
    fun setOnItemClickListener(clickListener: OnItemClickListener) {
        UsuariosAdaptador.clickListener = clickListener
    }

    /**
     * setOnItemLongClickListener: metodo utilizado para configurar el clickListener del adaptador
     * @param longClickListener escuchador de clicks
     */
    fun setOnItemLongClickListener(longClickListener: OnItemLongClickListener) {
        UsuariosAdaptador.longClickListener = longClickListener
    }

    companion object {

        //El atributo estatico clickListener nos permitira manejar eventos
        //de click en cada item desde la clase que crea la instancia del adaptador.
        //La clase que instancia al adaptador debera implementar la interfaz OnItemClickListener
        //y establecerse a si misma como escuchador para poder gestionar
        //los eventos de click en cada item del RecyclerView
        private var clickListener: OnItemClickListener? = null
        private var longClickListener: OnItemLongClickListener? = null
    }

}