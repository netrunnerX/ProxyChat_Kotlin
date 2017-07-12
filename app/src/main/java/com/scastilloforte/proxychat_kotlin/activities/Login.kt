package com.scastilloforte.proxychat_kotlin.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.AppCompatButton
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import com.scastilloforte.proxychat_kotlin.R
import com.scastilloforte.proxychat_kotlin.models.User
import kotlinx.android.synthetic.main.activity_login.*

class Login : AppCompatActivity() {

    private var firebaseAuth: FirebaseAuth? = null
    private var progressDialog: ProgressDialog? = null
    private var databaseReference: DatabaseReference? = null
    private var userId: String? = null
    private var token: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        progressDialog = ProgressDialog(this)
        progressDialog!!.setMessage("Iniciando sesion, espere")
        progressDialog!!.setCancelable(false)

        firebaseAuth = FirebaseAuth.getInstance()

        if (firebaseAuth?.currentUser != null) {
            progressDialog!!.show()

            val user : FirebaseUser = firebaseAuth!!.currentUser as FirebaseUser

            userId = user.uid

            databaseReference = FirebaseDatabase.getInstance().reference

            token = FirebaseInstanceId.getInstance().token

            databaseReference!!.child("usuarios").child(userId)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(p0: DataSnapshot?) {
                            val usr : User? = p0?.getValue(User::class.java)

                            databaseReference!!.child("tokens").child(userId).child(token).setValue(true)

                            val bundle : Bundle = Bundle()
                            bundle.putSerializable("user", usr)

                            val i = Intent(this@Login, MainActivity::class.java)
                            i.putExtras(bundle)

                            finish()
                            startActivity(i)
                            progressDialog?.dismiss()
                        }

                        override fun onCancelled(p0: DatabaseError?) {

                        }

                    })
        }

        btLogin.setOnClickListener { login() }
        btRegister.setOnClickListener { register() }
    }

    fun login() {
        if (TextUtils.isEmpty(etUser.text)) {
            etUser.setError("Need an email")
            return
        }

        if (TextUtils.isEmpty(etPassword.text)) {
            etPassword.setError("Need a password")
            return
        }

        progressDialog?.show()

        firebaseAuth!!.signInWithEmailAndPassword(etUser.text.toString(), etPassword.text.toString())
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val fbUser : FirebaseUser = firebaseAuth!!.currentUser as FirebaseUser

                        userId = fbUser.uid

                        databaseReference = FirebaseDatabase.getInstance().reference

                        token = FirebaseInstanceId.getInstance().token

                        databaseReference!!.child("usuarios").child(userId)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(p0: DataSnapshot?) {
                                        val usr : User? = p0?.getValue(User::class.java)

                                        databaseReference!!.child("tokens").child(userId).child(token).setValue(true)

                                        val bundle : Bundle = Bundle()
                                        bundle.putSerializable("user", usr)

                                        val i = Intent(this@Login, MainActivity::class.java)
                                        i.putExtras(bundle)

                                        finish()
                                        startActivity(i)
                                        progressDialog?.dismiss()
                                    }

                                    override fun onCancelled(p0: DatabaseError?) {

                                    }

                                })
                    }
                    else {
                        progressDialog?.dismiss()

                        Toast.makeText(applicationContext,
                                it.exception?.message,
                                Toast.LENGTH_LONG).show()
                    }
                }
    }

    fun register() {
        startActivityForResult(Intent(this@Login, Register::class.java), 10)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 10 && resultCode == Activity.RESULT_OK)
            finish()
    }
}
