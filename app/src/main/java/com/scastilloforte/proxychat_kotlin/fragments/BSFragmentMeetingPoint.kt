package com.scastilloforte.proxychat_kotlin.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialogFragment
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.*
import com.scastilloforte.proxychat_kotlin.R
import com.scastilloforte.proxychat_kotlin.activities.MeetingPointActivity
import com.scastilloforte.proxychat_kotlin.modelos.MeetingPoint
import com.scastilloforte.proxychat_kotlin.modelos.Usuario
import kotlinx.android.synthetic.main.bs_info_meetingpoint.*

/**
 * Created by netx on 7/12/17.
 */
class BSFragmentMeetingPoint : BottomSheetDialogFragment() {

    private val bottomSheetBehaviorCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss()
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {

        }
    }

    var user : Usuario? = null
    var meetingPoint: MeetingPoint? = null
    var databaseReference : DatabaseReference? = null

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog?, style: Int) {
        super.setupDialog(dialog, style)

        val contentView : View = View.inflate(context, R.layout.bs_info_meetingpoint, null)
        dialog?.setContentView(contentView)

        val params = (contentView.parent as View).layoutParams as CoordinatorLayout.LayoutParams
        val behavior = params.behavior

        if (behavior != null && behavior is BottomSheetBehavior) {
            behavior.setBottomSheetCallback(bottomSheetBehaviorCallback)
        }

        user = arguments.getSerializable("user") as Usuario

        databaseReference = FirebaseDatabase.getInstance().reference

        databaseReference!!.child("meeting_points").child(arguments.getString("meetingpointid")!!)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        meetingPoint = dataSnapshot.getValue(MeetingPoint::class.java)

                        tvNombreMeetingPoint.text = meetingPoint?.nombre
                        tvDescMeetingPoint.text = meetingPoint?.descripcion
                    }

                    override fun onCancelled(databaseError: DatabaseError) {

                    }
                })

        botonUnirseMeetingPoint.setOnClickListener { addMeetingPoint() }

    }

    /**
     * agregarContacto: metodo encargado de agregar el punto de encuentro a la lista de puntos del usuario
     */
    fun addMeetingPoint() {

        databaseReference!!.child("contactos").child("usuarios").child(user?.id).child("meeting_points")
                .child(meetingPoint?.id)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        //Obtiene el valor booleano que contiene el nodo meeting point
                        val bMeetingPoint = dataSnapshot.getValue(Boolean::class.java)

                        //Si el valor no es nulo, el punto de encuentro ya existe en la lista
                        if (bMeetingPoint != null) {

                            Snackbar.make(view!!, "El punto de encuentro ya existe en la lista",
                                    Snackbar.LENGTH_LONG).show()
                        } else {
                            databaseReference!!.child("contactos").child("usuarios").child(user?.id).child("meeting_points")
                                    .child(meetingPoint?.id).setValue(true).addOnSuccessListener {
                                //Muestra un Snackbar informando al usuario de que el punto de encuentro
                                //ha sido añadido a la lista
                                Snackbar.make(view!!, "Punto de encuentro agregado", Toast.LENGTH_LONG).show()
                                iniciarMeetingPoint()
                            }.addOnFailureListener {
                                //Muestra un Snackbar informando al usuario de que hubo un error en la
                                //operacion
                                Snackbar.make(view!!, "Error al agregar el contacto", Toast.LENGTH_LONG).show()
                            }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {

                    }
                })

    }

    /**
     * iniciarChat: metodo encargado de iniciar la actividad del punto de encuentro
     */
    fun iniciarMeetingPoint() {
        val bundle = Bundle()
        //Añade el objeto MeetingPoint con los datos del punto de encuentro
        //al Bundle
        bundle.putSerializable("meetingPoint", meetingPoint)
        //Añade el objeto Usuario con los datos del usuario al Bundle
        bundle.putSerializable("usuario", user)

        //Crea un Intent utilizado para iniciar la actividad del punto de encuentro
        val intent = Intent(context, MeetingPointActivity::class.java)
        //Añade el Bundle al Intent
        intent.putExtras(bundle)
        //Inicia la actividad
        startActivity(intent)
    }
}