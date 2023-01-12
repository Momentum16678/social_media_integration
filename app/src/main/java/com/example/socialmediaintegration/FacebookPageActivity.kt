package com.example.socialmediaintegration

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.socialmediaintegration.databinding.ActivityFacebookPageBinding
import com.facebook.AccessToken
import com.facebook.AccessTokenTracker
import com.squareup.picasso.Picasso

class FacebookPageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFacebookPageBinding
     var Pfb_id: String? = null
     var Pfb_name: String? = null
     var Pfb_profileUrl: String? = null
     var Pfb_email: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFacebookPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
        Pfb_name = sharedPreferences.getString(Fbname, "")
        Pfb_email = sharedPreferences.getString(Fbemail, "")
        Pfb_profileUrl = sharedPreferences.getString(FbprofileUrl, "")
        Pfb_id = sharedPreferences.getString(Fbid, "")
        Picasso.get().load(Pfb_profileUrl).placeholder(R.drawable.userprofile).into(binding.profilePic)
        binding.name.text = Pfb_name
        binding.email.text = Pfb_email
        binding.logOut.setOnClickListener {
            val builderExitbutton = AlertDialog.Builder(this)
            builderExitbutton.setTitle("Really Logout?")
                .setMessage("Are you sure?")
                .setPositiveButton("yes") { _, _ ->
                    val settings = getSharedPreferences(FB_LOGIN, 0)
                    val editor = settings.edit()
                    editor.remove("fb_logged")
                    editor.clear()
                    editor.apply()
                    val editor1 = sharedPreferences.edit()
                    editor1.clear()
                    editor1.apply()
                    Toast.makeText(
                        this,
                        "Facebook Logged out successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }.setNegativeButton("No", null)
            val alertexit = builderExitbutton.create()
            alertexit.show()
        }
    }

    private var accessTokenTracker = object : AccessTokenTracker() {
        override fun onCurrentAccessTokenChanged(
            oldAccessToken: AccessToken?,
            currentAccessToken: AccessToken?
        ) {
            if (currentAccessToken == null) {
                binding.profilePic.setImageResource(0)
                binding.name.text = " "
                binding.email.text = " "
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        accessTokenTracker.stopTracking()
    }

    companion object {
        const val SHARED_PREFS = "sharedprefs"
        const val FbprofileUrl = "PfbprofileUrl"
        const val Fbname = "Pfb_name"
        const val Fbemail = "Pfb_email"
        const val Fbid = "Pfb_id"
        const val FB_LOGIN = "fb_login"
    }
}