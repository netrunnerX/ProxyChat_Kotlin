package com.scastilloforte.proxychat_kotlin.modelos

import java.io.Serializable

/**
 * Created by netx on 7/12/17.
 */
class Conversacion() : Serializable {
    var contacto:String? = null
    var idContacto:String? = null
    var ultimoMensaje:String? = null

    constructor(contacto:String? = null,
                idContacto:String? = null,
                ultimoMensaje:String? = null) :this() {
        this.contacto = contacto
        this.idContacto = idContacto
        this.ultimoMensaje = ultimoMensaje
    }
}