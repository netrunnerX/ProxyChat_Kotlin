package com.scastilloforte.proxychat_kotlin.modelos

import java.io.Serializable

/**
 * Created by netx on 1/07/17.
 */
class Usuario() : Serializable {
    var id : String? = null
    var apodo : String? = null
    var imagenUrl : String? = null

    constructor(id : String? = null,
                apodo : String? = null,
                imagenUrl : String? = null) :this() {
        this.id = id
        this.apodo = apodo
        this.imagenUrl = imagenUrl
    }
}