package com.scastilloforte.proxychat_kotlin.models

import java.io.Serializable

/**
 * Created by netx on 2/07/17.
 */
data class MeetingPoint(var id : String? = null,
                        var idPropietario : String? = null,
                        var nombre : String? = null,
                        var descripcion : String? = null) : Serializable