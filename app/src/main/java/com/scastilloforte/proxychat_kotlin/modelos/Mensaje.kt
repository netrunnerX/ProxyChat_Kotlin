package com.scastilloforte.proxychat_kotlin.modelos

import java.io.Serializable

/**
 * Created by netx on 7/12/17.
 */
class Mensaje() : Serializable {
    var emisor:String? = null
    var idEmisor:String? = null
    var receptores:ArrayList<String>? = null
    var idReceptores:ArrayList<String>? = null
    var mensaje:String? = null
    var tipoMensaje:Int? = null
    var horaMensaje:String? = null

    constructor(emisor:String? = null,
                idEmisor:String? = null,
                receptores:ArrayList<String>? = null,
                idReceptores:ArrayList<String>? = null,
                mensaje:String? = null,
                tipoMensaje:Int? = null,
                horaMensaje:String? = null) :this() {

        this.emisor = emisor
        this.idEmisor = idEmisor
        this.receptores = receptores
        this.idReceptores = idReceptores
        this.mensaje = mensaje
        this.tipoMensaje = tipoMensaje
        this.horaMensaje = horaMensaje
    }
}