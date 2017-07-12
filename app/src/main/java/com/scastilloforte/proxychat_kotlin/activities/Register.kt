package com.scastilloforte.proxychat_kotlin.activities

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.scastilloforte.proxychat_kotlin.BuildConfig
import com.scastilloforte.proxychat_kotlin.R
import com.scastilloforte.proxychat_kotlin.models.User
import kotlinx.android.synthetic.main.activity_register.*
import java.io.FileNotFoundException
import java.io.InputStream

class Register : AppCompatActivity() {

    var databaseReference : DatabaseReference? = null
    var progressDialog : ProgressDialog? = null
    var user : User? = null
    var uploadTask : UploadTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        databaseReference = FirebaseDatabase.getInstance().reference

        btSubmitRegister.setOnClickListener { register() }

    }

    fun register() {
        if (etEmailReg.text.toString()
                .matches(Regex("^[\\w-]+(\\.[\\w-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"))) {
            etEmailReg.setError("Debes introducir un email válido")
            return
        }

        if (TextUtils.isEmpty(etPasswordReg.text.toString())) {
            etPasswordReg.setError("Debes introducir una contraseña")
        }

        //Comprueba que el campo de la contraseña por duplicado no este vacio
        if (TextUtils.isEmpty(etPasswordReg2.text.toString())) {
            etPasswordReg2.setError("Debes introducir la contraseña por duplicado");
            return;
        }

        //Comprueba que las dos contraseñas coincidan
        if (etPasswordReg.text.toString() != etPasswordReg2.text.toString()) {
            etPasswordReg.setError("Las contraseñas deben coincidir")
            etPasswordReg2.setError("Las contraseñas deben coincidir")
            return
        }

        //Comprueba que el campo del apodo no este vacio
        if (TextUtils.isEmpty(etNickReg.text.toString())) {
            etNickReg.setError("Debes introducir un nick")
            return
        }
        else {
            //Realiza una consulta a la base de datos para comprobar que el nick no exista
            databaseReference!!.child("usuarios").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError?) {
                }

                override fun onDataChange(p0: DataSnapshot?) {

                    for (usr in p0!!.children) {
                        val u : User? = usr.getValue(User::class.java)

                        if (u?.apodo == etNickReg.text.toString()) {
                            etNickReg.setError("El nick ya existe")
                            return
                        }
                    }

                    startRegistration()
                }

            })
        }
    }

    fun startRegistration() : Unit {
        progressDialog = ProgressDialog(this)
        progressDialog?.setMessage("Por favor, espere")
        progressDialog?.setCancelable(false)
        progressDialog?.show()

        FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(etEmailReg.text.toString(), etPasswordReg.text.toString())
                .addOnCompleteListener {

                    if (!it.isSuccessful) {
                        progressDialog?.dismiss()
                        Toast.makeText(applicationContext,
                                "Ocurrió un error al realizar el registro: ${it.exception}",
                                Toast.LENGTH_LONG).show()
                    }
                    else {
                        val fbUser : FirebaseUser = it.result.user
                        val profileUpdates : UserProfileChangeRequest = UserProfileChangeRequest.Builder()
                                .setDisplayName(etNickReg.text.toString())
                                .build()
                        fbUser.updateProfile(profileUpdates).addOnCompleteListener {
                            if (it.isSuccessful) {
                                user = User(id = fbUser.uid, apodo = fbUser.displayName)

                                val token : String? = FirebaseInstanceId.getInstance().token

                                databaseReference!!.child("tokens").child(user!!.id).child(token).setValue(true)

                                var storageReference : StorageReference = FirebaseStorage.getInstance().reference

                                storageReference = storageReference.child("usuarios")
                                        .child(user!!.id!!)
                                        .child("perfil.jpg")

                                var uri : Uri = Uri.parse("android.resource://${BuildConfig.APPLICATION_ID}/${R.drawable.iconouser}")

                                var inputStream : InputStream
                                try {
                                    inputStream = contentResolver.openInputStream(uri)
                                    uploadTask = storageReference.putStream(inputStream)

                                    uploadTask!!.addOnFailureListener{
                                            Toast.makeText(applicationContext,
                                                    "Ocurrio un error al subir la imagen de perfil al servidor.",
                                                    Toast.LENGTH_LONG).show()
                                    }
                                    uploadTask!!.addOnSuccessListener {
                                        var uri : Uri? = it.downloadUrl

                                        user!!.imagenUrl = uri.toString()

                                        FirebaseDatabase.getInstance().reference
                                                .child("usuarios")
                                                .child(fbUser.uid).setValue(user)

                                        val bundle : Bundle = Bundle()

                                        bundle.putSerializable("user", user)

                                        var i = Intent(this@Register, MainActivity::class.java)
                                        i.putExtras(bundle)

                                        startActivity(i)
                                        finish()
                                        progressDialog?.dismiss()
                                    }
                                }
                                catch (e : FileNotFoundException){
                                    progressDialog?.dismiss()

                                    Toast.makeText(applicationContext,
                                            "No se ha podido cargar la imagen: ${it.exception?.message}",
                                            Toast.LENGTH_LONG).show()
                                }
                            }
                            else {
                                progressDialog?.dismiss()

                                Toast.makeText(applicationContext,
                                        "Ocurrió un error al actualizar la informacion del usuario: ${it.exception?.message}",
                                        Toast.LENGTH_LONG).show()
                            }
                        }
                    }

                }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item?.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}
