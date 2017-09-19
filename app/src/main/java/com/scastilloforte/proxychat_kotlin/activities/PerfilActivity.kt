package com.scastilloforte.proxychat_kotlin.activities

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.scastilloforte.proxychat_kotlin.R
import android.support.design.widget.Snackbar
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.UploadTask
import com.google.android.gms.tasks.OnSuccessListener
import android.support.annotation.NonNull
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import android.content.Intent
import android.net.Uri
import android.view.MenuItem
import android.view.View
import com.scastilloforte.proxychat_kotlin.modelos.Usuario
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_perfil.*
import java.io.FileNotFoundException
import java.io.InputStream


/**
 * PerfilActivity: actividad que muestra el perfil del usuario
 */
class PerfilActivity : AppCompatActivity() {

    private val PICK_IMAGE_REQUEST = 1
    private var usuario: Usuario? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        //Configura el ActionBar para mostrar el boton de ir atras
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        //Obtiene del Bundle contenido en el Intent el objeto Usuario con los datos del usuario
        usuario = intent.extras.getSerializable("usuario") as Usuario
        //Configura el texto del TextView con el apodo del usuario
        tvApodoPerfil.text = usuario!!.apodo

        //Crea un obtiene Uri con la URL de la imagen de perfil del usuario
        val uri = Uri.parse(usuario!!.imagenUrl)

        //Carga la imagen del usuario en el ImageView a partir de la URL, utilizando la libreria Glide
        Glide.with(applicationContext)
                .load(uri)
                .apply(RequestOptions().placeholder(R.drawable.iconouser).centerCrop())
                .into(ivFotoPerfil)

        ivFotoPerfil.setOnClickListener { seleccionarFotoPerfil() }
    }

    /**
     * seleccionarFotoPerfil: metodo que se ejecuta cuando el usuario pulsa sobre la imagen de perfil
     * para seleccionar una imagen de perfil nueva
     */
    fun seleccionarFotoPerfil() {
        //Crea un Intent
        val intent = Intent()
        //con intent.setType("image/*") indicamos que en la nueva actividad solo se mostraran imagenes
        intent.type = "image/*"
        //Muestra contenido que el usuario puede escoger, y que devolvera una URI resultante
        intent.action = Intent.ACTION_GET_CONTENT
        //Inicia una nueva actividad que mostrara el seleccionador de imagenes
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    /**
     * onActivityResult: este metodo se ejecutara una vez finalizada la actividad de seleccion de imagen
     * @param requestCode
     * *
     * @param resultCode
     * *
     * @param data
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {

            //Obtiene la URI de la imagen
            val uri = data.data

            //Declara un InputStream
            var inputStream: InputStream? = null
            try {
                //Inicializa el InputStream utilizando como fuente de datos la URI de la imagen
                inputStream = contentResolver.openInputStream(uri)

                //Obtiene una referencia al almacen de Firebase
                var storageReference = FirebaseStorage.getInstance().reference
                //Situa la referencia en la ruta donde se almacena la imagen de perfil del usuario
                storageReference = storageReference.child("usuarios").child(usuario!!.id!!).child("perfil.png")
                //Crea un objeto UploadTask, utilizado para subir la imagen al almacen de Firebase
                val uploadTask: UploadTask
                //Inicializa el UploadTask haciendo una llamada al metodo putStream del objeto StorageReference
                //y pasandole por parametro el InputStream
                uploadTask = storageReference.putStream(inputStream!!)
                //Añade escuchadores al UploadTask que recibiran los eventos de operacion exitosa u operacion
                //fallida
                uploadTask.addOnFailureListener { e ->
                    /**
                     * onFailure: se ejecuta si la operacion fallo
                     * @param e
                     */
                    //Muestra un Snackbar informando al usuaario del error
                    Snackbar.make(ivFotoPerfil, "Error al subir la imagen al servidor: " + e.message,
                            Snackbar.LENGTH_LONG).show()
                }.addOnSuccessListener { taskSnapshot ->
                    /**
                     * onSuccess: se ejecuta si la operacion se realizo exitosamente
                     * @param taskSnapshot
                     */
                    //Etiqueta para evitar que el IDE se queje de que el metodo getDownloadUrl
                    //solo deberia ser visible por tests o en un ambito private
                    val uri = taskSnapshot.downloadUrl//Obtiene del TaskSnapShot la URI de la imagen
                    //Establece la URL de la imagen de usuario en el objeto Usuario con la nueva URL
                    usuario!!.imagenUrl = uri!!.toString()

                    //Obtiene una referencia a la base de datos
                    val databaseReference = FirebaseDatabase.getInstance().reference
                    //Almacena en la base de datos el objeto Usuario actualizado
                    databaseReference.child("usuarios").child(usuario!!.id!!).setValue(usuario)

                    //Carga la nueva imagen en el ImageView
                    Glide.with(applicationContext)
                            .load(uri)
                            .apply(RequestOptions().placeholder(R.drawable.iconouser).centerCrop())
                            .into(ivFotoPerfil)
                }
            } catch (e: FileNotFoundException) {
                //Si no se encontro la imagen, se muestra un Snackbar informando del error al usuario
                //y termina la ejecucion del metodo mediante return
                Snackbar.make(ivFotoPerfil, "No se ha podido cargar la imagen: " + e.message,
                        Snackbar.LENGTH_LONG).show()

            }

        }
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