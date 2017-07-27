package com.scastilloforte.proxychat_kotlin.modelos

/**
 * Created by netx on 7/12/17.
 */
data class Mensaje(var emisor:String,
                   var idEmisor:String,
                   var receptores:ArrayList<String>,
                   var idReceptores:ArrayList<String>,
                   var mensaje:String,
                   var tipoMensaje:Int,
                   var horaMensaje:String)