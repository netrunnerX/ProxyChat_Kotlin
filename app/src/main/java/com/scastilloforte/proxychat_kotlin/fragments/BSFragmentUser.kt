package com.scastilloforte.proxychat_kotlin.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialogFragment
import android.support.design.widget.CoordinatorLayout
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.*
import com.scastilloforte.proxychat_kotlin.R
import com.scastilloforte.proxychat_kotlin.activities.ChatActivity
import com.scastilloforte.proxychat_kotlin.modelos.Usuario
import kotlinx.android.synthetic.main.bs_info_user.*
import com.google.firebase.database.DatabaseError
import android.support.design.widget.Snackbar
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.bs_info_user.view.*


/**
 * Created by netx on 2/07/17.
 */
class BSFragmentUser : BottomSheetDialogFragment() {

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
    var contact : Usuario? = null
    var databaseReference : DatabaseReference? = null

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog?, style: Int) {
        super.setupDialog(dialog, style)

        val contentView : View = View.inflate(context, R.layout.bs_info_user, null)
        dialog?.setContentView(contentView)

        val params = (contentView.parent as View).layoutParams as CoordinatorLayout.LayoutParams
        val behavior = params.behavior

        if (behavior != null && behavior is BottomSheetBehavior) {
            behavior.setBottomSheetCallback(bottomSheetBehaviorCallback)
        }

        user = arguments.getSerializable("user") as Usuario


        databaseReference = FirebaseDatabase.getInstance().reference

        databaseReference!!.child("usuarios").child(arguments.getString("contactid")!!)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        contact = dataSnapshot.getValue(Usuario::class.java)

                        contentView.tvApodoPerfil.text = contact?.apodo

                        val uri = Uri.parse(contact?.imagenUrl)
                        Glide.with(context)
                                .load(uri)
                                .apply(RequestOptions().placeholder(R.drawable.iconouser).centerCrop())
                                .into(contentView.ivFotoPerfil)
                    }

                    override fun onCancelled(databaseError: DatabaseError) {

                    }
                })

        contentView.botonAgregarContacto.setOnClickListener{ v -> addContact(v) }

        contentView.botonEnviarMensajeUsuario.setOnClickListener{ v -> iniciarChat(v) }
    }

    /**
     * agregarContacto: metodo encargado de agregar el contacto a la lista de contactos
     * @param v
     */
    fun addContact(v: View) {

        //Realiza una consulta en la referencia de la base de datos donde se encuentran almacenados
        //los contactos del usuario para comprobar si el contacto ya existe en la lista
        databaseReference!!.child("contactos").child("usuarios").child(user!!.id).child("usuarios")
                .child(contact!!.id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                //Obtiene el valor booleano que contiene el nodo contacto
                val bContacto = dataSnapshot.getValue(Boolean::class.java)

                //Si el valor no es nulo, significa que el nodo del contacto existe en la lista,
                //por lo que no es necesario agregarlo
                if (bContacto != null) {
                    //Muestra un Snackbar informando al usuario de que el contacto ya existe en la lista
                    //de contactos
                    Toast.makeText(context, "El usuario ya existe en la lista de contactos",
                            Toast.LENGTH_LONG).show()
                } else {

                    databaseReference!!.child("invitaciones")
                            .child("usuarios")
                            .child(contact!!.id)
                            .child(user!!.id).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val b = dataSnapshot.getValue(java.lang.Boolean.TYPE)

                            if (b == null) {
                                //Almacena en la base de datos el nuevo contacto
                                databaseReference!!.child("invitaciones")
                                        .child("usuarios")
                                        .child(contact!!.id)
                                        .child(user!!.id)
                                        .setValue(true).addOnSuccessListener {
                                    //Muestra un Snackbar informando al usuario de que el contacto h
                                    // a sido añadido
                                    //a la lista de contactos
                                    Toast.makeText(context, "Petición de contacto enviada",
                                            Toast.LENGTH_LONG).show()
                                }.addOnFailureListener {
                                    //Muestra un Snackbar informando al usuario de que hubo un error en la
                                    //operacion
                                    Toast.makeText(context, "Error al enviar la petición de contacto",
                                            Toast.LENGTH_LONG).show()
                                }
                            } else {
                                Toast.makeText(context, "Ya has enviado una petición de contacto al usuario",
                                        Toast.LENGTH_LONG).show()
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {

                        }
                    })

                }//Si el contacto no existe en la lista
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })

    }

    /**
     * iniciarChat: metodo encargado de iniciar la actividad de chat con el contacto
     * @param v
     */
    fun iniciarChat(v: View) {
        //Crea un bundle
        val bundle = Bundle()
        //Añade al bundle el objeto Usuario con los datos del contacto
        bundle.putSerializable("contacto", contact)
        //Añade al bundle el objeto Usuario con los datos del usuario
        bundle.putSerializable("usuario", user)
        //Crea un Intent utilizado para iniciar la nueva actividad
        val intent = Intent(context, ChatActivity::class.java)
        //Añade el bundle al Intent
        intent.putExtras(bundle)
        //Inicia la actividad de chat a partir del intent
        startActivity(intent)
    }
}