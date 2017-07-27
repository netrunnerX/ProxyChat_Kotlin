package com.scastilloforte.proxychat_kotlin.adaptadores

import com.scastilloforte.proxychat_kotlin.R.id.tvHora
import android.widget.TextView
import com.scastilloforte.proxychat_kotlin.R.id.tvMensaje
import com.scastilloforte.proxychat_kotlin.R.id.tvNombre
import com.scastilloforte.proxychat_kotlin.R.id.cardViewMensajes
import android.support.v7.widget.CardView
import com.scastilloforte.proxychat_kotlin.R.id.fondoMensaje
import android.widget.LinearLayout
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.R.attr.gravity
import android.widget.RelativeLayout
import com.scastilloforte.proxychat_kotlin.R.drawable.globoazul
import com.scastilloforte.proxychat_kotlin.R.drawable.globoverde
import android.widget.FrameLayout
import com.scastilloforte.proxychat_kotlin.R.layout.card_view_mensajes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.scastilloforte.proxychat_kotlin.R
import com.scastilloforte.proxychat_kotlin.modelos.Mensaje
import kotlinx.android.synthetic.main.card_view_mensajes.*
import kotlinx.android.synthetic.main.card_view_mensajes.view.*


/**
 * Created by netx on 7/27/17.
 */
/**
 * MensajeAdaptador: adaptador utilizado para cargar mensajes en el RecyclerView de
 * ChatActivity, ProxyFragment y MeetingPointChatFragment
 */

class MensajeAdaptador(val mensajes : ArrayList<Mensaje>) : RecyclerView.Adapter<MensajeAdaptador.MensajesViewHolder>() {

    /**
     * onCreateViewHolder: este metodo se ejecuta a la hora de crear un nuevo ViewHolder.
     * Un ViewHolder es un contenedor para un item del RecyclerView
     * @param parent
     * *
     * @param viewType
     * *
     * @return
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MensajesViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.card_view_mensajes, parent, false)
        return MensajesViewHolder(v)
    }

    /**
     * onBindViewHolder: este metodo lo llama el RecyclerView a la hora de mostrar el item
     * en una posicion determinada
     * @param holder ViewHolder con los datos a mostrar
     * *
     * @param position posicion en el RecyclerView donde mostrara el item
     */
    override fun onBindViewHolder(holder: MensajesViewHolder, position: Int) {
        //Obtiene el tipo de mensaje
        val tipoMensaje = mensajes[position].tipoMensaje

        //Obtiene un objeto RelativeLayout.LayoutParams del cardView, que se utiliza para configurar
        //los parametros del layout
        val layoutParams = holder.cardView.layoutParams as RelativeLayout.LayoutParams

        //Obtiene un objeto FrameLayout.LayoutParams del LinearLayout del fondo del mensaje,
        //que se utiliza para configurar los parametros del layout
        val flParams = holder.fondoMensaje.layoutParams as FrameLayout.LayoutParams

        //Obtiene un objeto LinearLayout.LayoutParams del TextView de la hora del mensaje,
        //que se utiliza para configurar los parametros del layout
        val llHoraParams = holder.tvHora.layoutParams as LinearLayout.LayoutParams

        //Obtiene un objeto LinearLayout.LayoutParams del TextView del texto del mensaje,
        //que se utiliza para configurar los parametros del layout
        val llMensajeParams = holder.tvMensaje.layoutParams as LinearLayout.LayoutParams

        //Obtiene un objeto LinearLayout.LayoutParams del TextView del nombre del emisor,
        //que se utiliza para configurar los parametros del layout
        val llNombreParams = holder.tvNombre.layoutParams as LinearLayout.LayoutParams

        //Si el valor del tipo de mensaje es 0, es un mensaje saliente
        if (tipoMensaje == 0) {
            //Establece el fondo del mensaje con la imagen del globo verde
            holder.fondoMensaje.setBackgroundResource(R.drawable.globoverde)

            //Alinea los elementos para que aparezcan a la derecha
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0)
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
            flParams.gravity = Gravity.RIGHT
            llHoraParams.gravity = Gravity.RIGHT
            llNombreParams.gravity = Gravity.RIGHT
            llMensajeParams.gravity = Gravity.RIGHT
            holder.tvMensaje.gravity = Gravity.RIGHT
        } else {

            //Establece el fondo del mensaje con la imagen del globo verde
            holder.fondoMensaje.setBackgroundResource(R.drawable.globoazul)

            //Alinea los elementos para que aparezcan a la izquierda
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0)
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
            flParams.gravity = Gravity.LEFT
            llHoraParams.gravity = Gravity.LEFT
            llNombreParams.gravity = Gravity.LEFT
            llMensajeParams.gravity = Gravity.LEFT
            holder.tvMensaje.gravity = Gravity.LEFT
        }//Por otra parte, si es 1, es un mensaje entrante

        //Actualiza los parametros de layout de los elementos del ViewHolder
        holder.cardView.layoutParams = layoutParams
        holder.fondoMensaje.layoutParams = flParams
        holder.tvHora.layoutParams = llHoraParams
        holder.tvMensaje.layoutParams = llMensajeParams
        holder.tvNombre.layoutParams = llNombreParams

        //Actualiza el texto del TextView que muestra el nombre del emisor
        holder.tvNombre.text = mensajes[position].emisor
        //Actualiza el texto del TextView que muestra el texto del mensaje
        holder.tvMensaje.text = mensajes[position].mensaje
        //Actualiza el texto del TextView que muestra la hora del mensaje
        holder.tvHora.text = mensajes[position].horaMensaje
    }

    /**
     * getItemCount: este metodo devuelve el total de items en el RecyclerView
     * @return
     */
    override fun getItemCount(): Int {

        //Como es equivalente al tamaño de la lista de mensajes, se devuelve este
        return mensajes.size
    }

    /**
     * MensajesViewHolder: clase que define un ViewHolder personalizado de mensajes
     */
    class MensajesViewHolder(v: View) : RecyclerView.ViewHolder(v) {

        var cardView: CardView
        var tvNombre: TextView
        var tvMensaje: TextView
        var tvHora: TextView
        var fondoMensaje: LinearLayout

        init {

            //Instancia el LinearLayout que hace de fondo de mensaje
            fondoMensaje = v.fondoMensaje
            //Instancia el CardView
            cardView = v.cardViewMensajes
            //Instancia el TextView que muestra el nombre del emisor
            tvNombre = v.tvNombre
            //Instancia el TextView que muestra el texto del mensaje
            tvMensaje = v.tvMensaje
            //Instancia el TextView que muestra la hora del mensaje
            tvHora = v.tvHora
        }
    }
}