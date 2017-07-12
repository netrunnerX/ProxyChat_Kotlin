package com.scastilloforte.proxychat_kotlin.models

import java.io.Serializable

/**
 * Created by netx on 1/07/17.
 */
data class Usuario(val id : String? = null,
                   var apodo : String? = null,
                   var imagenUrl : String? = null) : Serializable