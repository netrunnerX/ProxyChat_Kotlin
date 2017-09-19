package com.scastilloforte.proxychat_kotlin

import android.app.Application
import com.google.firebase.database.FirebaseDatabase



/**
 * Created by netx on 9/19/17.
 */
/**
 * Aplicacion: clase que representa la aplicacion (vease AndroidManifest.xml)
 */
class Aplicacion : Application() {

    /**
     * onCreate: metodo que se ejecuta cuando la aplicacion se esta iniciando (antes de iniciar ninguna
     * actividad, servicio o receptor excluyendo proveedores de contenido)
     */
    override fun onCreate() {
        super.onCreate()
        //Habilita la persistencia en disco de los datos obtenidos de Firebase, manteniendo el estado de
        //la aplicacion al reiniciar
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
}