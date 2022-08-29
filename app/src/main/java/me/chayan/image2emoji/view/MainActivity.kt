package me.chayan.image2emoji.view

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import com.google.android.material.navigation.NavigationView
import me.chayan.image2emoji.R
import me.chayan.image2emoji.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        drawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        navController = findNavController(R.id.nav_host_fragment_content_main)

        appBarConfiguration = AppBarConfiguration(setOf(R.id.nav_home), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        navView.setNavigationItemSelectedListener(this)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val handled = NavigationUI.onNavDestinationSelected(item, navController)

        if (!handled) {
            when (item.itemId) {
                R.id.nav_about -> {
                    /*val unused = Intent(this@MainActivity, about::class.java).also {
                        this@MainActivity.intent = it
                    }
                    this@MainActivity.startActivity(this@MainActivity.intent)*/
                }
                R.id.nav_like -> {
                    /*val intent = Intent(
                        "android.intent.action.VIEW",
                        Uri.parse("market://details?id=" + this@MainActivity.applicationContext.packageName)
                    )
                    if (Build.VERSION.SDK_INT >= 21) {
                        intent.addFlags(1208483840)
                    } else {
                        intent.addFlags(1207959552)
                    }
                    try {
                        this@MainActivity.startActivity(intent)
                    } catch (unused2: ActivityNotFoundException) {
                        val mainActivity = this@MainActivity
                        mainActivity.startActivity(
                            Intent(
                                "android.intent.action.VIEW",
                                Uri.parse("http://play.google.com/store/apps/details?id=" + this@MainActivity.applicationContext.packageName)
                            )
                        )
                    }*/
                }
                R.id.nav_report -> {
                    /*val unused3 = Intent(this@MainActivity, feedback::class.java).also {
                        this@MainActivity.intent = it
                    }
                    this@MainActivity.startActivity(this@MainActivity.intent)*/
                }
                R.id.nav_setting -> {
                    /*val unused4 = Intent(this@MainActivity, Setting::class.java).also {
                        this@MainActivity.intent = it
                    }
                    this@MainActivity.startActivity(this@MainActivity.intent)*/
                }
            }
        }

        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers()
        } else {
            super.onBackPressed()
        }
    }
}