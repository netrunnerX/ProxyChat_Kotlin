package com.scastilloforte.proxychat_kotlin.activities

import android.support.v7.app.AppCompatActivity

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem

import com.scastilloforte.proxychat_kotlin.R
import com.scastilloforte.proxychat_kotlin.fragments.ConversacionesFragment
import com.scastilloforte.proxychat_kotlin.fragments.MapFragment
import com.scastilloforte.proxychat_kotlin.fragments.MeetingPointsFragment
import com.scastilloforte.proxychat_kotlin.fragments.ProxyFragment
import com.scastilloforte.proxychat_kotlin.modelos.Usuario
import kotlinx.android.synthetic.main.activity_main.*
import com.google.firebase.database.DatabaseError
import android.content.Intent
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.FirebaseDatabase


class MainActivity : AppCompatActivity() {

    var user : Usuario? = null
    var mapFragment : MapFragment? = null
    /**
     * The [android.support.v4.view.PagerAdapter] that will provide
     * fragments for each of the sections. We use a
     * [FragmentPagerAdapter] derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * [android.support.v4.app.FragmentStatePagerAdapter].
     */
    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        user = intent.extras.getSerializable("user") as Usuario

        setSupportActionBar(toolbar)
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)


        container.offscreenPageLimit = 3
        container.adapter = mSectionsPagerAdapter

        tabs.setupWithViewPager(container)
        tabs.getTabAt(0)?.setIcon(R.drawable.ic_map)
        tabs.getTabAt(1)?.setIcon(R.drawable.ic_location_on)
        tabs.getTabAt(2)?.setIcon(R.drawable.ic_rss_feed)
        tabs.getTabAt(3)?.setIcon(R.drawable.ic_message)


    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        when (id) {
            R.id.action_perfil -> {
                //Obtiene una referencia a la base de datos
                val databaseReference = FirebaseDatabase.getInstance().reference
                //Realiza una consulta a la base de datos para obtener los datos del usuario
                databaseReference.child("usuarios")
                        .child(user!!.id).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val usr = dataSnapshot.getValue(Usuario::class.java)

                        //Crea un bundle
                        val bundle = Bundle()
                        //Añade el objeto Usuario con los datos del usuario al bundle
                        bundle.putSerializable("usuario", usr)
                        //Crea un Intent utilizado para iniciar la actividad de Perfil
                        val intent = Intent(this@MainActivity, PerfilActivity::class.java)
                        //Añade el bundle al Intent
                        intent.putExtras(bundle)
                        //Inicia la actividad
                        startActivity(intent)

                        //La consulta es realizada para poder iniciar la actividad pasandole
                        //un objeto Usuario actualizado, de esta forma se podra ver la imagen
                        //de perfil actual cuando se muestra el perfil del usuario

                    }

                    override fun onCancelled(databaseError: DatabaseError) {

                    }
                })

                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            when (position) {
                0 -> {
                    mapFragment = MapFragment()
                    return mapFragment!!
                }
                1 -> return MeetingPointsFragment()
                2 -> return ProxyFragment()
                3 -> return ConversacionesFragment()
                else -> return Fragment()
            }
        }

        override fun getCount(): Int {
            // Show 4 total pages.
            return 4
        }

        override fun getPageTitle(position: Int): CharSequence? {
            when (position) {
                0 -> return null
                1 -> return null
                2 -> return null
                3 -> return null
            }
            return null
        }
    }
}
