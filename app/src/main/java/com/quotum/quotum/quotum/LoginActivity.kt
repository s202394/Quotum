package com.quotum.quotum.quotum

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk.getApplicationContext
import com.facebook.GraphRequest
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.JsonObject
import com.quotum.quotum.quotum.localdatabase.ApplicationConstants
import com.quotum.quotum.quotum.localdatabase.LocalDB
import com.quotum.quotum.quotum.models.*
import com.quotum.quotum.quotum.network.QuotumClient
import com.quotum.quotum.quotum.ui.instagram.AuthenticationListener
import com.quotum.quotum.quotum.ui.instagram.dialog.AuthenticationDialog
import com.quotum.quotum.quotum.utility.Utils
import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*


class LoginActivity : AppCompatActivity(), View.OnClickListener, AuthenticationListener {

    private val TAG = LoginActivity::class.java.simpleName

    private val context: LoginActivity = this@LoginActivity
    var editTextEmail: EditText? = null
    var editTextPassword: EditText? = null
    var editTextEmailSignUp: EditText? = null
    var editTextFirstNameSignUp: EditText? = null
    var editTextLastNameSignUp: EditText? = null
    var editTextMobileSignUp: EditText? = null
    var editTextPasswordSignUp: EditText? = null
    var relativeLayoutLogin: RelativeLayout? = null
    var relativeLayoutSignup: RelativeLayout? = null
    var buttonLoginSwitch: Button? = null
    var buttonSignupSwitch: Button? = null
    private var userToken: String? = null

    private val loginManager: LoginManager = LoginManager.getInstance()
    private val callbackManager: CallbackManager = CallbackManager.Factory.create()

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_login)

        checkIfUserIsAlreadyLoggedin()

        val buttonFbParent = findViewById<Button>(R.id.fb)
        val buttonInstaParent = findViewById<Button>(R.id.insta)

        buttonFbParent.setOnClickListener(this)
        buttonInstaParent.setOnClickListener(this)

        setVideoBackground()
        //hideSystemUI()

        val bottomSheet = findViewById<LinearLayout>(R.id.bottom_sheet)
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)

        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {
                    }
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                    }
                    BottomSheetBehavior.STATE_HIDDEN -> {
                    }
                    BottomSheetBehavior.STATE_SETTLING -> {
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
        })

        val textViewLogin = findViewById<TextView>(R.id.text_view_login)
        val view = layoutInflater.inflate(R.layout.login_signup_bottom_sheet, null)

        val dialog = BottomSheetDialog(this)

        textViewLogin.setOnClickListener {
            if (view.parent != null) {
                (view.parent as ViewGroup).removeView(view)
            }
            dialog.setContentView(view)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()
        }

        relativeLayoutLogin = view.findViewById(R.id.rl_login) as RelativeLayout
        relativeLayoutSignup = view.findViewById(R.id.rl_signup) as RelativeLayout
        buttonLoginSwitch = view.findViewById(R.id.login_switch) as Button
        buttonSignupSwitch = view.findViewById(R.id.signup_switch) as Button
        val buttonLogin = view.findViewById(R.id.loginButton) as Button
        val buttonSignUp = view.findViewById(R.id.signupBtn) as Button
        val buttonFb = view.findViewById(R.id.login_fb) as Button
        val buttonInsta = view.findViewById(R.id.login_insta) as Button
        val buttonSignUpFb = view.findViewById(R.id.fb) as Button
        val buttonSignUpInsta = view.findViewById(R.id.insta) as Button
        editTextEmail = view.findViewById(R.id.editTextEmail) as EditText
        editTextPassword = view.findViewById(R.id.editTextPassword) as EditText
        editTextEmailSignUp = view.findViewById(R.id.input_email) as EditText
        editTextLastNameSignUp = view.findViewById(R.id.input_last_name) as EditText
        editTextFirstNameSignUp = view.findViewById(R.id.input_first_name) as EditText
        editTextMobileSignUp = view.findViewById(R.id.input_mobile) as EditText
        editTextPasswordSignUp = view.findViewById(R.id.input_password) as EditText

        buttonFb.setOnClickListener(this)
        buttonInsta.setOnClickListener(this)
        buttonLogin.setOnClickListener(this)
        buttonSignUp.setOnClickListener(this)
        buttonSignUpFb.setOnClickListener(this)
        buttonSignUpInsta.setOnClickListener(this)

        relativeLayoutSignup!!.visibility = View.GONE

        buttonLoginSwitch!!.setOnClickListener {
            loginCondition()
        }

        buttonSignupSwitch!!.setOnClickListener {
            signupCondition()
        }
    }

    private fun signupCondition() {
        buttonLoginSwitch!!.setBackgroundResource(R.drawable.login_btton_unpressed_background)
        buttonSignupSwitch!!.setBackgroundResource(R.drawable.login_background)
        buttonLoginSwitch!!.setTextColor(Color.BLACK)
        buttonSignupSwitch!!.setTextColor(Color.WHITE)
        relativeLayoutLogin!!.visibility = View.GONE
        relativeLayoutSignup!!.visibility = View.VISIBLE
    }

    private fun loginCondition() {
        buttonLoginSwitch!!.setBackgroundResource(R.drawable.login_background)
        buttonSignupSwitch!!.setBackgroundResource(R.drawable.login_btton_unpressed_background)
        buttonLoginSwitch!!.setTextColor(Color.WHITE)
        buttonSignupSwitch!!.setTextColor(Color.BLACK)
        relativeLayoutLogin!!.visibility = View.VISIBLE
        relativeLayoutSignup!!.visibility = View.GONE
    }

    private fun checkIfUserIsAlreadyLoggedin() {
        userToken = LocalDB.getUserToken(context)
        val userId = LocalDB.getUserId(context)

        if (userId != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun loginWithEmailAndPassword() {
        val email = editTextEmail?.text.toString().trim()
        val password = editTextPassword?.text.toString().trim()

        if (email.isEmpty()) {
            Toast.makeText(applicationContext, "Enter email address", Toast.LENGTH_SHORT).show()
            return
        }
        if (!Utils.isValidEmail(email)) {
            Toast.makeText(applicationContext, "Invalid email address", Toast.LENGTH_SHORT).show()
            return
        }
        if (password.isEmpty()) {
            Toast.makeText(applicationContext, "Please Enter Password", Toast.LENGTH_SHORT).show()
            return
        }
        if (!Utils.isNetworkConnected(this)) {
            Toast.makeText(
                applicationContext, "Please check your internet connection.", Toast.LENGTH_SHORT
            ).show()
            return
        }
        val loginRequestModel = LoginRequestModel()
        email.also { loginRequestModel.email = it }
        password.also { loginRequestModel.password = it }
        loginRequestModel.deviceToken = LocalDB.getFCMToken(this)
        loginRequestModel.deviceType = ApplicationConstants.DEVICE_ANDROID
        QuotumClient.instance.emailLogin(loginRequestModel)
            .enqueue(object : Callback<LoginResponseModel> {
                override fun onFailure(call: Call<LoginResponseModel>, t: Throwable) {
                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<LoginResponseModel>,
                    response: Response<LoginResponseModel>
                ) {
                    if (response.isSuccessful) {
                        val loginResponseModel: LoginResponseModel = response.body()!!
                        LocalDB.setUserToken(
                            applicationContext, loginResponseModel.result!!.id
                        )
                        LocalDB.setUserId(
                            applicationContext, loginResponseModel.result!!.userId
                        )
                        val intent = Intent(applicationContext, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        when (response.code()) {
                            404 -> Toast.makeText(
                                applicationContext,
                                "User not found. Please SignUp or check your email.",
                                Toast.LENGTH_SHORT
                            ).show()
                            500 -> {
                                val res = JSONObject(response.errorBody().toString())
                                val error = res.optJSONObject("error")
                                var message =
                                    "Server is not responding right now. Please try again later"
                                if (error != null) {
                                    message = error.optString("message", "")
                                }
                                if (message.isEmpty())
                                    message =
                                        "Server is not responding right now. Please try again later"
                                Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT)
                                    .show()
                            }
                            else -> Toast.makeText(
                                applicationContext,
                                "Unknown error. Please try again later",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            })
    }

    private fun hideSystemUI() {
        val decorView = window.decorView
        decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    private fun setVideoBackground() {
        val videoView = findViewById<VideoView>(R.id.videoView)
        val path = "android.resource://" + packageName + "/" + R.raw.beach
        videoView?.setVideoURI(Uri.parse(path))
        videoView.start()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.fb, R.id.login_fb -> {
                loginWithFb()
            }
            R.id.insta, R.id.login_insta -> {
                loginWithInstagram()
            }
            R.id.loginButton -> {
                loginWithEmailAndPassword()
            }
            R.id.signupBtn -> {
                signup()
            }
        }
    }

    private fun signup() {
        val email = editTextEmailSignUp?.text.toString().trim()
        val password = editTextPasswordSignUp?.text.toString().trim()
        val mobile = editTextMobileSignUp?.text.toString().trim()
        val fname = editTextFirstNameSignUp?.text.toString().trim()
        val lname = editTextLastNameSignUp?.text.toString().trim()

        if (email.isEmpty()) {
            Toast.makeText(applicationContext, "Please enter email", Toast.LENGTH_SHORT).show()
            editTextEmailSignUp!!.requestFocus()
            return
        }
        if (!Utils.isValidEmail(email)) {
            Toast.makeText(applicationContext, "Please enter valid email", Toast.LENGTH_SHORT)
                .show()
            editTextEmailSignUp!!.requestFocus()
            return
        }
        if (mobile.isEmpty()) {
            Toast.makeText(applicationContext, "Please enter mobile", Toast.LENGTH_SHORT).show()
            editTextMobileSignUp!!.requestFocus()
            return
        }
        if (fname.isEmpty()) {
            Toast.makeText(applicationContext, "Please enter first name", Toast.LENGTH_SHORT).show()
            editTextFirstNameSignUp!!.requestFocus()
            return
        }
        if (lname.isEmpty()) {
            Toast.makeText(applicationContext, "Please enter last name", Toast.LENGTH_SHORT).show()
            editTextLastNameSignUp!!.requestFocus()
            return
        }
        if (password.isEmpty()) {
            Toast.makeText(applicationContext, "Please enter password", Toast.LENGTH_SHORT).show()
            editTextPasswordSignUp!!.requestFocus()
            return
        }
        if (!Utils.isNetworkConnected(this)) {
            Toast.makeText(
                applicationContext, "Please check your internet connection.", Toast.LENGTH_SHORT
            ).show()
            return
        }
        val model = SignUpRequestModel()
        val userData = SignUpRequestModel.UserData()
        email.also { userData.email = it }
        password.also { userData.password = it }
        fname.also { userData.firstName = it }
        lname.also { userData.lastName = it }
        mobile.also { userData.mobileNumber = it }
        model.deviceToken = LocalDB.getFCMToken(this)
        model.deviceType = ApplicationConstants.DEVICE_ANDROID
        model.userData = userData
        QuotumClient.instance.postEmailSignUp(model)
            .enqueue(object : Callback<JsonObject> {
                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<JsonObject>,
                    response: Response<JsonObject>
                ) {
                    Log.e("sign up response : ", response.errorBody().toString())
                    Log.e("sign up response : ", response.message())
                    Log.e("sign up response : ", response.body().toString())
                    if (response.isSuccessful) {
                        Toast.makeText(
                            applicationContext, "SignUp Successful", Toast.LENGTH_LONG
                        ).show()
                        loginCondition()
                    } else {
                        when (response.code()) {
                            500 -> Toast.makeText(
                                applicationContext,
                                "Server is not responding right now. Please try again later",
                                Toast.LENGTH_SHORT
                            ).show()
                            422 -> Toast.makeText(
                                applicationContext,
                                "User already exist with same email id. Use different email id or login.",
                                Toast.LENGTH_SHORT
                            ).show()
                            else -> Toast.makeText(
                                applicationContext,
                                "Unknown error. Please try again later",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            })
    }

    private fun loginWithInstagram() {
        val authenticationDialog = AuthenticationDialog(this, this)
        authenticationDialog.setCancelable(true)
        authenticationDialog.show()
    }

    private fun loginWithFb() {
        loginManager.logInWithReadPermissions(context, listOf("public_profile"))

        loginManager.registerCallback(
            callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    val request =
                        GraphRequest.newMeRequest(loginResult.accessToken) { `object`, response ->
                            try {
                                if (`object`.has("id")) {
                                    signInWithFacebook(`object`, loginResult.accessToken.token)
                                } else {
                                    Toast.makeText(
                                        applicationContext,
                                        "User not found. Please Signup or check your email.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    val parameters = Bundle()
                    parameters.putString("fields", "id,name,link,birthday,picture,email,gender")
                    request.parameters = parameters
                    request.executeAsync()
                }

                override fun onCancel() {
                    Log.d(TAG, "Facebook Login is Canceled.")
                    Toast.makeText(
                        applicationContext,
                        "Facebook Login is Canceled.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onError(error: FacebookException) {
                    Log.d(
                        TAG,
                        Objects.requireNonNull(error.localizedMessage)
                    )
                    try {
                        val info = packageManager.getPackageInfo(
                            "com.quotum.quotum.quotum",
                            PackageManager.GET_SIGNATURES
                        )
                        for (signature in info.signatures) {
                            val md = MessageDigest.getInstance("SHA")
                            md.update(signature.toByteArray())
                            Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT))
                        }
                    } catch (e: PackageManager.NameNotFoundException) {
                        Log.e("KeyHash:", e.toString())
                    } catch (e: NoSuchAlgorithmException) {
                        Log.e("KeyHash:", e.toString())
                    }
                    Toast.makeText(
                        applicationContext,
                        "Facebook Login Error. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })

    }

    private fun signInWithFacebook(jsonObject: JSONObject, token: String) {
        val socialLoginRequestModel = SocialLoginRequestModel(jsonObject, "facebook")
        QuotumClient.instance.socialLogin(token, socialLoginRequestModel)
            .enqueue(object : Callback<SocialLoginResponseModel> {
                override fun onFailure(call: Call<SocialLoginResponseModel>, t: Throwable) {
                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<SocialLoginResponseModel>,
                    response: Response<SocialLoginResponseModel>
                ) {
                    if (response.isSuccessful) {
                        val loginResponseModel: SocialLoginResponseModel = response.body()!!
                        LocalDB.setUserToken(applicationContext, loginResponseModel.result.id)
                        val intent = Intent(applicationContext, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        when (response.code()) {
                            404 -> Toast.makeText(
                                applicationContext,
                                "User not found. Please Signup or check your email.",
                                Toast.LENGTH_SHORT
                            ).show()
                            500 -> Toast.makeText(
                                applicationContext,
                                "server is not responding right now. Please try again later",
                                Toast.LENGTH_SHORT
                            ).show()
                            else -> Toast.makeText(
                                applicationContext,
                                "Unknown error.Please try again later",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            })
    }

    internal class RequestInstagramAPI(var loginActivity: LoginActivity) :
        AsyncTask<Void?, String?, String?>() {

        override fun onPostExecute(response: String?) {
            super.onPostExecute(response)
            if (response != null) {
                try {
                    val jsonObject = JSONObject(response)
                    Log.e("response", jsonObject.toString())
                    val jsonData = jsonObject.getJSONObject("data")
                    if (jsonData.has("id")) {

                        val intent = Intent(loginActivity, MainActivity::class.java)
                        loginActivity.startActivity(intent)
                        loginActivity.finish()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                val toast =
                    Toast.makeText(
                        getApplicationContext(),
                        "Something went wrong.",
                        Toast.LENGTH_LONG
                    )
                toast.show()
            }
        }

        override fun doInBackground(vararg params: Void?): String? {
            val httpClient: HttpClient = DefaultHttpClient()
            val httpGet = HttpGet(
                "https://api.instagram.com/v1/users/self/?access_token=${
                    LocalDB.getUserToken(loginActivity)
                }"
            )
            try {
                val response: HttpResponse = httpClient.execute(httpGet)
                val httpEntity: HttpEntity = response.entity
                return EntityUtils.toString(httpEntity)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onTokenReceived(auth_token: String?) {
        if (auth_token == null) return
        LocalDB.setUserToken(context, auth_token)
        RequestInstagramAPI(context).execute()
    }
}