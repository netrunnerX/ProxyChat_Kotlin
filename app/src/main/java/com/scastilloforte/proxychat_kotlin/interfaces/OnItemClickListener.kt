package com.scastilloforte.proxychat_kotlin.interfaces

import android.view.View

/**
 * Created by netx on 7/12/17.
 */

/**
 * Interfaz OnItemClickListener: Interfaz para la escucha de clicks en cada item del RecyclerView
 */
interface OnItemClickListener {
    fun onClick(view: View, position:Int)
}