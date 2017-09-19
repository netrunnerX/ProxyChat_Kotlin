package com.scastilloforte.proxychat_kotlin.modelos

import java.io.Serializable

/**
 * Created by netx on 2/07/17.
 */
class MeetingPoint() : Serializable {
    var id : String? = null
    var idPropietario : String? = null
    var nombre : String? = null
    var descripcion : String? = null

    constructor(id : String? = null,
                idPropietario : String? = null,
                nombre : String? = null,
                descripcion : String? = null) :this() {
        this.id = id
        this.idPropietario = idPropietario
        this.nombre = nombre
        this.descripcion = descripcion
    }
}