package com.scastilloforte.proxychat_kotlin.interfaces

import android.view.View

/**
 * Created by netx on 7/12/17.
 */

/**
 * Interfaz OnItemLongClickListener: Interfaz para la escucha de clicks largos en cada item del RecyclerView
 */
interface OnItemLongClickListener {
    fun onLongClick(view: View, position:Int):Boolean
}