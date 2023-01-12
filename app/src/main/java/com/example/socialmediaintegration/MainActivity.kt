@file:Suppress("DEPRECATION")

package com.example.socialmediaintegration

import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.socialmediaintegration.databinding.ActivityMainBinding
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.tasks.Task
import org.json.JSONException

class MainActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener {

    private lateinit var binding: ActivityMainBinding
    private var callbackManager: CallbackManager? = null
    var name: String? = null
    var id: String? = null
    var profilePicUrl: String? = null
    var email: String? = null

    private var googleApiClient: GoogleApiClient? = null
    var progressDialog: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val fbSettings = getSharedPreferences(FB_LOGIN, 0)
        val emailSettings = getSharedPreferences(GMAIL_LOGIN, 0)
        if (fbSettings.getString("fb_logged", "").toString() == "fb_logged") {
            startActivity(Intent(this@MainActivity, FacebookPageActivity::class.java))
            finish()
        } else if (emailSettings.getString("gmail_logged", "").toString() == "gmail_logged") {
            startActivity(Intent(this@MainActivity, GmailActivity::class.java))
            finish()
        }

        callbackManager = CallbackManager.Factory.create()
        binding.signInButtonFacebook.setOnClickListener {
            LoginManager.getInstance()
                .logInWithReadPermissions(this@MainActivity, listOf("email"))
            LoginManager.getInstance()
                .registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                    override fun onSuccess(result: LoginResult) {
                        binding.signInButtonFacebook.visibility = View.INVISIBLE
                        binding.signInButtongoogle.visibility = View.INVISIBLE
                        progressDialog = ProgressDialog(this@MainActivity)
                        progressDialog!!.setTitle("Loading data...")
                        progressDialog!!.show()
                        val graphRequest: GraphRequest = GraphRequest.newMeRequest(
                            AccessToken.getCurrentAccessToken()
                        ) { `object`, _ ->
                            Log.d("Demo", `object`.toString())
                            try {
                                if (`object` != null) {
                                    name = `object`.getString("name")
                                }
                                id = `object`!!.getString("id")
                                profilePicUrl = `object`.getJSONObject("picture").getJSONObject("data")
                                    .getString("url")
                                email = if (`object`.has("email")) {
                                    `object`.getString("email")
                                } else {
                                    " "
                                }
                                val settings = getSharedPreferences(FB_LOGIN, 0)
                                val editor = settings.edit()
                                editor.putString("fb_logged", "fb_logged")
                                editor.apply()
                                sendfbData()
                                progressDialog!!.dismiss()
                                Toast.makeText(
                                    this@MainActivity,
                                    "Login successful.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                startActivity(
                                    Intent(
                                        this@MainActivity,
                                        FacebookPageActivity::class.java
                                    )
                                )
                                finish()
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }
                        val bundle = Bundle()
                        bundle.putString(
                            "fields",
                            "picture.type(large),gender, name, id, birthday, friends, email"
                        )
                        graphRequest.parameters = bundle
                        graphRequest.executeAsync()
                    }

                    override fun onCancel() {
                        binding.signInButtonFacebook.visibility = View.VISIBLE
                        binding.signInButtongoogle.visibility = View.VISIBLE
                        Toast.makeText(this@MainActivity, "Login unsuccessful.", Toast.LENGTH_SHORT)
                            .show()
                    }

                    override fun onError(error: FacebookException) {
                        binding.signInButtonFacebook.visibility = View.VISIBLE
                        binding.signInButtongoogle.visibility = View.VISIBLE
                        progressDialog!!.dismiss()
                        Toast.makeText(this@MainActivity, "Login error.", Toast.LENGTH_SHORT).show()

                    }
                })
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build()
        googleApiClient = GoogleApiClient.Builder(this).enableAutoManage(this, this)
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso).build()
        binding.signInButtongoogle.setOnClickListener {
            val intent: Intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient!!)
            startActivityForResult(intent, SignIn_value)
        }
    }

    private fun sendfbData() {
        val sharedPreferences: SharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(Fbname, name)
        editor.putString(Fbemail, email)
        editor.putString(FbprofileUrl, profilePicUrl)
        editor.putString(Fbid, id)
        editor.apply()
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {}
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager!!.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SignIn_value) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSigninResult(task)
        }
    }

    private fun handleSigninResult(result: Task<GoogleSignInAccount>) {
        try {
            result.getResult(ApiException::class.java)
            binding.signInButtonFacebook.visibility = View.INVISIBLE
            binding.signInButtongoogle.visibility = View.INVISIBLE
            val settings = getSharedPreferences(GMAIL_LOGIN, 0)
            val editor = settings.edit()
            editor.putString("gmail_logged", "gmail_logged")
            editor.apply()
            Toast.makeText(this@MainActivity, "Login successful.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this@MainActivity,GmailActivity::class.java))
            finish()
        } catch (e: ApiException) {
            binding.signInButtonFacebook.visibility = View.VISIBLE
            binding.signInButtongoogle.visibility = View.VISIBLE
            Toast.makeText(this, "Login failed.", Toast.LENGTH_SHORT).show()
            Log.v("Error", "signInResult:failed code = " + e.statusCode)
        }
    }

    companion object {
        const val SHARED_PREFS = "sharedprefs"
        const val FbprofileUrl = "PfbprofileUrl"
        const val Fbname = "Pfb_name"
        const val Fbemail = "Pfb_email"
        const val Fbid = "Pfb_id"
        const val FB_LOGIN = "fb_login"
        const val GMAIL_LOGIN = "gmail_login"
        const val SignIn_value = 1
    }
}