package com.scastilloforte.proxychat_kotlin.adaptadores

import android.R.attr.onClick
import android.widget.TextView
import android.support.v7.widget.CardView
import android.view.View.OnLongClickListener
import android.support.v7.widget.RecyclerView
import com.google.firebase.database.DatabaseError
import com.scastilloforte.proxychat_kotlin.modelos.MeetingPoint
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.scastilloforte.proxychat_kotlin.R
import com.scastilloforte.proxychat_kotlin.interfaces.OnItemClickListener
import com.scastilloforte.proxychat_kotlin.interfaces.OnItemLongClickListener
import kotlinx.android.synthetic.main.card_view_meeting_points.view.*


/**
 * Created by netx on 9/19/17.
 */
/**
 * MeetingPointsAdaptador: adaptador utilizado para cargar los items con los datos de cada punto de encuentro
 * en el RecyclerView de MeetingPointsFragment
 */
class MeetingPointsAdaptador(val meetingPoints: List<String>) : RecyclerView.Adapter<MeetingPointsAdaptador.MeetingPointsViewHolder>() {

    //Los atributos clickListener y longClickListener nos permitira manejar eventos
    //de click en cada item desde la clase que crea la instancia del adaptador.
    //La clase que instancia al adaptador debera implementar las interfaces OnItemClickListener
    //y OnItemLongClickListener, y establecerse a si misma como escuchador para poder gestionar
    //los eventos de click en cada item del RecyclerView
    private var clickListener: OnItemClickListener? = null
    private var longClickListener: OnItemLongClickListener? = null

    /**
     * onCreateViewHolder: este metodo se ejecuta a la hora de crear un nuevo ViewHolder.
     * Un ViewHolder es un contenedor para un item del RecyclerView
     * @param parent
     * *
     * @param viewType
     * *
     * @return
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeetingPointsAdaptador.MeetingPointsViewHolder {
        //Crea un View usando el layout del item
        val v = LayoutInflater.from(parent.context).inflate(R.layout.card_view_meeting_points, parent, false)
        //Crea objeto ConversacionesViewHolder, pasandole la vista como parametro
        return MeetingPointsViewHolder(v)
    }

    /**
     * onBindViewHolder: este metodo lo llama el RecyclerView a la hora de mostrar el item
     * en una posicion determinada
     * @param holder ViewHolder con los datos a mostrar
     * *
     * @param position posicion en el RecyclerView donde mostrara el item
     */
    override fun onBindViewHolder(holder: MeetingPointsAdaptador.MeetingPointsViewHolder, position: Int) {

        //Obtiene una referencia a la base de datos
        val databaseReference = FirebaseDatabase.getInstance().reference
        //Realiza una consulta para obtener los datos del punto de encuentro
        databaseReference.child("meeting_points").child(meetingPoints[position])
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        //Obtiene un objeto MeetingPoint con los datos del punto de encuentro a partir
                        //del DataSnapshot
                        val meetingPoint = dataSnapshot.getValue(MeetingPoint::class.java)

                        //Configura el texto del TextView que muestra el nombre del punto de encuentro con
                        //el nombre del punto de encuentro
                        holder.tvNombre.text = meetingPoint!!.nombre
                        //Configura el texto del TextView que muestra la descripcion del punto de encuentro con
                        //la descripcion del punto de encuentro
                        holder.tvDescripcion.text = meetingPoint!!.descripcion
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

        //Como es equivalente al tamaño de la lista de puntos de encuentro, se devuelve este
        return meetingPoints.size
    }

    /**
     * MeetingPointsViewHolder: clase que define un ViewHolder personalizado de puntos de encuentro
     */
    inner class MeetingPointsViewHolder (v: View) : RecyclerView.ViewHolder(v), View.OnClickListener, View.OnLongClickListener {

        var cardView: CardView
        var tvNombre: TextView
        var tvDescripcion: TextView

        init {

            //Instancia el CardView
            cardView = v.cardViewMeetingPoints
            //Instancia el TextView que muestra el nombre del punto de encuentro
            tvNombre = v.tvNombreMp
            //Instancia el TextView que muestra la descripcion del puunto de encuentro
            tvDescripcion = v.tvDescripcionMp

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
        this.clickListener = clickListener
    }

    /**
     * setOnItemLongClickListener: metodo utilizado para configurar el longClickListener del adaptador
     * @param longClickListener escuchador de clicks largos
     */
    fun setOnItemLongClickListener(longClickListener: OnItemLongClickListener) {
        this.longClickListener = longClickListener
    }

}