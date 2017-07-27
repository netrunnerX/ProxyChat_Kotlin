package com.scastilloforte.proxychat_kotlin.adaptadores


import android.content.Context
import android.net.Uri
import android.widget.TextView
import android.support.v7.widget.CardView

import android.support.v7.widget.RecyclerView
import com.google.firebase.database.DatabaseError

import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.Glide
import com.scastilloforte.proxychat_kotlin.modelos.Usuario
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.FirebaseDatabase
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.scastilloforte.proxychat_kotlin.R
import com.scastilloforte.proxychat_kotlin.interfaces.OnItemClickListener
import com.scastilloforte.proxychat_kotlin.interfaces.OnItemLongClickListener
import com.scastilloforte.proxychat_kotlin.modelos.Conversacion
import kotlinx.android.synthetic.main.card_view_conversaciones.view.*


/**
 * Created by netx on 7/27/17.
 */
/**
 * ConversacionesAdaaptador: clase encargada de cargar en el RecyclerView de ConversacionesFragment
 * los items que muestran datos de las conversaciones
 */
class ConversacionesAdaptador (val context: Context,
                               val conversaciones: List<Conversacion>) :
        RecyclerView.Adapter<ConversacionesAdaptador.ConversacionesViewHolder>() {

    /**
     * onCreateViewHolder: este metodo se ejecuta a la hora de crear un nuevo ViewHolder.
     * Un ViewHolder es un contenedor para un item del RecyclerView
     * @param parent
     * *
     * @param viewType
     * *
     * @return
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversacionesViewHolder {
        //Crea un View usando el layout del item
        val v = LayoutInflater.from(parent.context).inflate(R.layout.card_view_conversaciones, parent, false)
        //Crea objeto ConversacionesViewHolder, pasandole la vista como parametro
        return ConversacionesViewHolder(v)
    }

    /**
     * onBindViewHolder: este metodo lo llama el RecyclerView a la hora de mostrar el item
     * en una posicion determinada
     * @param holder ViewHolder con los datos a mostrar
     * *
     * @param position posicion en el RecyclerView donde mostrara el item
     */
    override fun onBindViewHolder(holder: ConversacionesViewHolder, position: Int) {

        //Configura el TextView del nombre de la conversacion con el nombre del contacto
        holder.tvNombre.text = conversaciones[position].contacto
        //Configura el TextView del mensaje con el ultimo mensaje de la conversacion
        holder.tvMensaje.text = conversaciones[position].ultimoMensaje

        //Obtiene una referencia a la base de datos
        val databaseReference = FirebaseDatabase.getInstance().reference
        //Realiza una consulta a la base de datos para obtener la URL de la imagen del contacto
        //de la conversacion
        databaseReference.child("usuarios").child(conversaciones[position].idContacto)
                .addValueEventListener(object : ValueEventListener {
                    /**
                     * onDataChange: este metodo es llamado cuando cambian los datos en la base de datos,
                     * ademas de para obtener un resultado inicial
                     * @param dataSnapshot
                     */
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        //Obtiene un objeto Usuario con los datos del Contacto
                        val usr = dataSnapshot.getValue(Usuario::class.java)
                        //Instancia un objeto Uri con la URL de la imagen del contacto
                        val fotoUri = Uri.parse(usr.imagenUrl)

                        //Descarga la imagen y la añade al ImageView de la imagen del contacto
                        //utilizando la libreria Glide
                        Glide.with(context.getApplicationContext())
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
        //Como es equivalente al tamaño de la lista de conversaciones, se devuelve este
        return conversaciones.size
    }

    /**
     * ConversacionesViewHolder: clase que define un ViewHolder personalizado de conversaciones
     */
    class ConversacionesViewHolder (v: View) : RecyclerView.ViewHolder(v), View.OnClickListener, View.OnLongClickListener {

        var cardView: CardView
        var tvNombre: TextView
        var tvMensaje: TextView
        var ivFotoContacto: ImageView

        init {

            //Instancia el CardView
            cardView = v.cardViewConversaciones
            //Instancia el TextView del nombre de la conversacion
            tvNombre = v.tvNombreC
            //Instancia el TextView del ultimo mensaje
            tvMensaje = v.tvUltimoMensaje
            //Instancia el ImageView de la imagen de contacto
            ivFotoContacto = v.ivFotoConv

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
            //Llamamos al metodo onClick del objeto OnItemClickListener definido en el adaptador
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
        ConversacionesAdaptador.clickListener = clickListener
    }

    /**
     * setOnItemLongClickListener: metodo utilizado para configurar el longClickListener del adaptador
     * @param longClickListener escuchador de clicks largos
     */
    fun setOnItemLongClickListener(longClickListener: OnItemLongClickListener) {
        ConversacionesAdaptador.longClickListener = longClickListener
    }

    companion object {

        //Los atributos estaticos clickListener y longClickListener nos permitira manejar eventos
        //de click en cada item desde la clase que crea la instancia del adaptador.
        //La clase que instancia al adaptador debera implementar las interfaces OnItemClickListener
        //y OnItemLongClickListener, y establecerse a si misma como escuchador para poder gestionar
        //los eventos de click en cada item del RecyclerView
        private var clickListener: OnItemClickListener? = null
        private var longClickListener: OnItemLongClickListener? = null
    }

}
