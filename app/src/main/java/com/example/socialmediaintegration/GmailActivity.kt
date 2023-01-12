@file:Suppress("DEPRECATION")

package com.example.socialmediaintegration

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.socialmediaintegration.databinding.ActivityGmailBinding
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.squareup.picasso.Picasso

class GmailActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener {
    private lateinit var binding: ActivityGmailBinding
    var Pemail_name: String? = null
    var Pemail_profileUrl: String? = null
    var Pemail_email: String? = null
    var googleApiClient: GoogleApiClient? = null
    var progressDialog: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGmailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Loading data...")
        progressDialog!!.show()
        val saveSettings = getSharedPreferences(GMAIL_LOGIN, 0)
        if (saveSettings.getString("gmail_saved", "").toString() == "gmail_saved") {
            val sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
            binding.name.text = sharedPreferences.getString(Emailname, "")
            binding.email.text = sharedPreferences.getString(Emailemail, "")
            Picasso.get().load(sharedPreferences.getString(EmailprofileUrl, ""))
                .placeholder(R.drawable.userprofile).into(binding.profilePic)
            progressDialog!!.dismiss()
        }
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build()
        googleApiClient = GoogleApiClient.Builder(this).enableAutoManage(this, this)
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso).build()
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            Pemail_name = account.displayName
            Pemail_email = account.email
            Pemail_profileUrl = if (account.photoUrl != null) {
                account.photoUrl.toString()
            } else {
                "null"
            }
            Picasso.get().load(Pemail_profileUrl).placeholder(R.drawable.userprofile)
                .into(binding.profilePic)
            binding.name.text = Pemail_name
            binding.email.text = Pemail_email
            progressDialog!!.dismiss()
            sendemailData()
        }
        binding.logOut.setOnClickListener {
            val builderExitbutton = AlertDialog.Builder(this)
            builderExitbutton.setTitle("Really logout?")
                .setMessage("Are you sure ?")
                .setPositiveButton("yes") { _, _ ->
                    Auth.GoogleSignInApi.signOut(googleApiClient!!)
                        .setResultCallback { status ->
                            if (status.isSuccess) {
                                val settings_save = getSharedPreferences(GMAIL_LOGIN, 0)
                                val editor_save = settings_save.edit()
                                editor_save.remove("gmail_saved")
                                editor_save.clear()
                                editor_save.apply()
                                editor_save.apply()
                                val settings = getSharedPreferences(GMAIL_LOGIN, 0)
                                val editor = settings.edit()
                                editor.remove("gmail_logged")
                                editor.clear()
                                editor.apply()
                                editor.apply()
                                Toast.makeText(
                                    this@GmailActivity,
                                    "Gmail Logged out successfully!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                val intent =
                                    Intent(this@GmailActivity, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(
                                    this@GmailActivity,
                                    "Gmail Log out failed!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                }.setNegativeButton("No", null)
            val alertexit = builderExitbutton.create()
            alertexit.show()
        }
    }

    private fun sendemailData() {
        val settings = getSharedPreferences(GMAIL_SAVE, 0)
        val editor_save = settings.edit()
        editor_save.putString("gmail_saved", "gmail_saved")
        editor_save.apply()
        val sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(Emailname, Pemail_name)
        editor.putString(Emailemail, Pemail_email)
        editor.putString(EmailprofileUrl, Pemail_profileUrl)
        editor.apply()
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {}

    companion object {
        const val SHARED_PREFS = "sharedprefs"
        const val EmailprofileUrl = "PemailprofileUrl"
        const val Emailname = "Pemail_name"
        const val Emailemail = "Pemail_email"
        const val GMAIL_LOGIN = "gmail_login"
        const val GMAIL_SAVE = "gmail_save"
    }
}