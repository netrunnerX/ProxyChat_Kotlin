package com.scastilloforte.proxychat_kotlin.activities

import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import com.scastilloforte.proxychat_kotlin.modelos.MeetingPoint
import com.scastilloforte.proxychat_kotlin.modelos.Usuario
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.scastilloforte.proxychat_kotlin.R
import com.scastilloforte.proxychat_kotlin.fragments.MeetingPointChatFragment
import com.scastilloforte.proxychat_kotlin.fragments.MeetingPointUsuariosFragment
import kotlinx.android.synthetic.main.activity_main.*


/**
 * Created by netx on 7/27/17.
 */
/**
 * MeetingPointActivity: actividad que presenta la funcionalidad de un punto de encuentro,
 * se compone de 2 pestañas con un Fragment cada una para facilitar al usuario navegar
 * por las funcionalidades
 */
class MeetingPointActivity : AppCompatActivity() {

    /**
     * The [android.support.v4.view.PagerAdapter] that will provide
     * fragments for each of the sections. We use a
     * [FragmentPagerAdapter] derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * [android.support.v4.app.FragmentStatePagerAdapter].
     */
    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null

    /**
     * The [ViewPager] that will host the section contents.
     */
    private var mViewPager: ViewPager? = null
    private var usuario: Usuario? = null
    private var meetingPoint: MeetingPoint? = null
    private var bundle: Bundle? = null
    private var meetingPointChatFragment: MeetingPointChatFragment? = null
    private var meetingPointUsuariosFragment: MeetingPointUsuariosFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meeting_point)

        //Obtiene el Bundle a traves del Intent
        bundle = intent.extras

        //Obtiene del Bundle el objeto Usuario con los datos del usuario
        usuario = bundle!!.getSerializable("usuario") as Usuario
        //Obtiene del Bundle el objeto MeetingPoint con los datos del punto de encuentro
        meetingPoint = bundle!!.getSerializable("meetingPoint") as MeetingPoint
        //Establece el titulo de la actividad con el nombre del punto de encuentro
        this.title = meetingPoint!!.nombre

        //Obtiene una instancia para el ToolBar
        val toolbar = toolbar
        setSupportActionBar(toolbar)

        //Configura el ActionBar para mostrar el boton de ir atras
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        //Crea el adaptador que devolvera un Fragment para cada una de las pestañas de la actividad
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        //Configura el adaptador con el SectionsPagerAdapter
        mViewPager = container
        mViewPager!!.adapter = mSectionsPagerAdapter

        //Crea un TabLayout y lo configura con el ViewPager
        val tabLayout = tabs
        tabLayout.setupWithViewPager(mViewPager)
        //Establece un icono para cada pestaña del TabLayout
        tabLayout.getTabAt(0)!!.setIcon(R.drawable.ic_sms)
        tabLayout.getTabAt(1)!!.setIcon(R.drawable.ic_people)

    }

    /**
     * Un [FragmentPagerAdapter] que devuelve un Fragment correspondiente a una de las pestañas
     */
    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment? {
            when (position) {
                0 -> {
                    //Devuelve un MeetingPointChatFragment que muestra el chat del punto de encuentro
                    meetingPointChatFragment = MeetingPointChatFragment()
                    meetingPointChatFragment!!.setArguments(bundle)
                    return meetingPointChatFragment
                }
                1 -> {
                    //Devuelve un MeetingPointUsuariosFragment que muestra la lista de usuarios
                    //del punto de encuentro
                    meetingPointUsuariosFragment = MeetingPointUsuariosFragment()
                    meetingPointUsuariosFragment!!.setArguments(bundle)
                    return meetingPointUsuariosFragment
                }
                else -> return null
            }
        }

        /**
         * getCount: devuelve el numero de pestañas
         * @return
         */
        override fun getCount(): Int {
            //Muestra 2 pestañas en total
            return 2
        }

        /**
         * getPageTitle: devuelve el titulo de la pestaña que se encuentra en la posicion pasada por parametro
         * @param position posicion de la pestaña
         * *
         * @return
         */
        override fun getPageTitle(position: Int): CharSequence? {
            when (position) {

            }//return "chat";
            //return "usuarios";
            //Se devuelve siempre null, de este modo no se muestra el titulo y solo aparece el icono en la pestaña
            return null
        }
    }

    /**
     * onOptionsItemSelected: en este metodo se realizan los acciones para cada item de menu cuando estos
     * son seleccionados
     * @param item
     * *
     * @return
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.getItemId()) {
        //Si el item corresponde con el boton de ir atras
            android.R.id.home -> {
                //Termina la actividad
                finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}