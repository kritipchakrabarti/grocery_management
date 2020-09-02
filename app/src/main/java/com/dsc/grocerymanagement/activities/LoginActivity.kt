package com.dsc.grocerymanagement.activities

import android.app.PendingIntent.getActivity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.dsc.grocerymanagement.R
import com.dsc.grocerymanagement.util.ConnectionManager
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit


class LoginActivity : AppCompatActivity() {
    private lateinit var emailTextField: EditText
    private lateinit var passFieldForEmail: EditText
    private lateinit var MobileNumber: EditText
    private lateinit var enterOtpMobile: EditText
    private lateinit var forgotPassEmail: TextView
    private lateinit var registerEmail: TextView
    private lateinit var btnLoginEmail: Button
    private lateinit var btnLoginMobile: Button
    private lateinit var btnRequestOtp: Button
    private lateinit var layoutForEmailSignIn: LinearLayout
    private lateinit var layoutForMobileSignIn: LinearLayout
    private lateinit var loginOptionGoogle: Button
    private lateinit var loginOptionEmail: Button
    private lateinit var loginOptionMobile: Button
    private var storedVerificationId: String = "0"
    private var mAuth: FirebaseAuth? = null
    private var pAuth: PhoneAuthProvider? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        emailTextField = findViewById(R.id.emailTextField)
        MobileNumber = findViewById(R.id.MobileNumber)
        enterOtpMobile = findViewById(R.id.enterOtpMobile)
        passFieldForEmail = findViewById(R.id.passFieldForEmail)
        forgotPassEmail = findViewById(R.id.forgotPassEmail)
        registerEmail = findViewById(R.id.registerEmail)
        btnLoginEmail = findViewById(R.id.btnLoginEmail)
        btnLoginMobile = findViewById(R.id.btnLoginMobile)
        btnRequestOtp = findViewById(R.id.btnRequestOtp)
        layoutForEmailSignIn = findViewById(R.id.layoutForEmailSignIn)
        layoutForMobileSignIn = findViewById(R.id.layoutForMobileSignIn)
        mAuth = FirebaseAuth.getInstance()
        pAuth = PhoneAuthProvider.getInstance()
        layoutForMobileSignIn.visibility = View.GONE
        layoutForEmailSignIn.visibility = View.GONE
        btnLoginMobile.visibility = View.GONE
        enterOtpMobile.visibility = View.GONE
        val customLayout: View = layoutInflater.inflate(R.layout.signin_options, null)
        loginOptionGoogle = customLayout.findViewById(R.id.loginOptionGoogle)
        loginOptionEmail = customLayout.findViewById(R.id.loginOptionEmail)
        loginOptionMobile = customLayout.findViewById(R.id.loginOptionMobile)
        val loginOption = AlertDialog.Builder(this@LoginActivity, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen)
        //val loginOption = AlertDialog.Builder(this@LoginActivity)
        loginOption.setTitle("Login Options")
                .setMessage("Select your desired mode for login: ")
                .setView(customLayout)
                /*.setPositiveButton("Continue") { _, _ ->

                }*/
                .setNegativeButton("Cancel") { _, _ ->
                    finishAffinity()
                }
                .setOnCancelListener {
                    finishAffinity()
                }
        val dialog: AlertDialog = loginOption.create()
        dialog.show()
        loginOptionMobile.setOnClickListener {
            layoutForEmailSignIn.visibility = View.GONE
            layoutForMobileSignIn.visibility = View.VISIBLE
            dialog.dismiss()
        }
        loginOptionEmail.setOnClickListener {
            layoutForEmailSignIn.visibility = View.VISIBLE
            layoutForMobileSignIn.visibility = View.GONE
            dialog.dismiss()
        }
        loginOptionGoogle.setOnClickListener {
            layoutForEmailSignIn.visibility = View.GONE
            layoutForMobileSignIn.visibility = View.GONE
            dialog.dismiss()
        }
        emailTextField.setOnFocusChangeListener { _, _ ->
            if (!Patterns.EMAIL_ADDRESS.matcher(emailTextField.text.toString().trim()).matches()) {
                emailTextField.error = "Incorrect Email Address"
                //emailTextField.requestFocus()
            }
        }
        passFieldForEmail.setOnFocusChangeListener { _, _ ->
            if (!(passFieldForEmail.text.toString().length >= 6)) {
                passFieldForEmail.error = "At least 6 digits"
                //passFieldForEmail.requestFocus()
            }
        }
        btnLoginEmail.setOnClickListener {
            val email = emailTextField.text.toString().trim()
            val password = passFieldForEmail.text.toString().trim()
            if (ConnectionManager().checkConnectivity(this@LoginActivity)) {
                if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    if (password.length >= 6) {
                        mAuth!!.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener(this) { task ->
                                    when {
                                        task.isSuccessful -> {
                                            Toast.makeText(this@LoginActivity, "Signed In successfully",
                                                    Toast.LENGTH_SHORT).show()
                                            // Sign in success, update UI with the signed-in user's information
                                            val user = mAuth!!.currentUser
                                            //updateUI(user)
                                            val name = user?.displayName
                                            println("check $name")
                                        }
                                        task.exception is FirebaseAuthInvalidUserException -> {
                                            // If sign in fails, display a message to the user.
                                            Toast.makeText(this@LoginActivity, "User not registered\nPlease register first!",
                                                    Toast.LENGTH_SHORT).show()
                                            //updateUI(null)
                                        }
                                        task.exception is FirebaseAuthInvalidCredentialsException -> {
                                            Toast.makeText(this@LoginActivity, "Invalid Credentials!\nPlease check your password",
                                                    Toast.LENGTH_SHORT).show()
                                        }
                                        else -> {
                                            Toast.makeText(this@LoginActivity, "Failed to sign you In",
                                                    Toast.LENGTH_SHORT).show()
                                        }
                                    }

                                    // ...
                                }
                    } else {
                        passFieldForEmail.error = "Password should be at least 6 digits"
                        passFieldForEmail.requestFocus()
                    }
                } else {
                    emailTextField.error = "Invalid Email address"
                    emailTextField.requestFocus()
                }
            } else {
                val dialogNet = android.app.AlertDialog.Builder(this@LoginActivity)
                dialogNet.setTitle("Error")
                dialogNet.setMessage("Internet Connection is not Found")
                dialogNet.setPositiveButton("Open Settings") { _, _ ->
                    val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                    startActivity(settingsIntent)
                    finish()
                }
                dialogNet.setNegativeButton("Exit") { _, _ ->
                    finishAffinity()
                }
                        .setOnCancelListener {
                            finishAffinity()
                        }
                dialogNet.create()
                dialogNet.show()
            }
        }
        registerEmail.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterEmailActivity::class.java)
            startActivity(intent)
        }
        forgotPassEmail.setOnClickListener {
            val email = emailTextField.text.toString().trim()
            if (ConnectionManager().checkConnectivity(this@LoginActivity)) {
                if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    mAuth!!.sendPasswordResetEmail(email)
                            .addOnCompleteListener(this) { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(this@LoginActivity, "A mail has been sent with the password reset link",
                                            Toast.LENGTH_SHORT).show()
                                } else if (task.exception is FirebaseAuthInvalidUserException) {
                                    Toast.makeText(this@LoginActivity, "User not registered\nPlease register first!",
                                            Toast.LENGTH_SHORT).show()
                                }
                            }
                } else {
                    emailTextField.error = "Invalid Email address"
                    emailTextField.requestFocus()
                }
            } else {
                val dialogNet = android.app.AlertDialog.Builder(this@LoginActivity)
                dialogNet.setTitle("Error")
                dialogNet.setMessage("Internet Connection is not Found")
                dialogNet.setPositiveButton("Open Settings") { _, _ ->
                    val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                    startActivity(settingsIntent)
                    finish()
                }
                dialogNet.setNegativeButton("Exit") { _, _ ->
                    finishAffinity()
                }
                        .setOnCancelListener {
                            finishAffinity()
                        }
                dialogNet.create()
                dialogNet.show()
            }
        }
        btnRequestOtp.setOnClickListener {
            val mobile = MobileNumber.text.toString()
            if (mobile.length >= 10) {
                pAuth!!.verifyPhoneNumber(
                        mobile, // Phone number to verify
                        60, // Timeout duration
                        TimeUnit.SECONDS, // Unit of timeout
                        this, // Activity (for callback binding)
                        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                                Toast.makeText(this@LoginActivity, "You have been successfully verified!",
                                        Toast.LENGTH_SHORT).show()
                            }

                            override fun onVerificationFailed(e: FirebaseException) {
                                Toast.makeText(this@LoginActivity, e.toString(),
                                        Toast.LENGTH_SHORT).show()
                                if (e is FirebaseAuthInvalidCredentialsException) {
                                    Toast.makeText(this@LoginActivity, "Invalid phone number",
                                            Toast.LENGTH_SHORT).show()
                                } else if (e is FirebaseTooManyRequestsException) {
                                    Toast.makeText(this@LoginActivity, "The SMS quota for the project has been exceeded",
                                            Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(this@LoginActivity, "Some error occurred",
                                            Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onCodeSent(
                                    verificationId: String,
                                    forceResendingToken: PhoneAuthProvider.ForceResendingToken
                            ) {
                                Toast.makeText(this@LoginActivity, "OTP has been sent to your mobile!",
                                        Toast.LENGTH_SHORT).show()
                                storedVerificationId = verificationId
                                this@LoginActivity.enableUserManuallyInputCode()
                            }
                        }) // OnVerificationStateChangedCallbacks

            } else {
                MobileNumber.error = "Number should be of 10 digits"
                MobileNumber.requestFocus()
            }
        }
        btnLoginMobile.setOnClickListener {
            val otp = enterOtpMobile.text.toString().trim()
            if (otp.length == 6) {
                val credential = PhoneAuthProvider.getCredential(storedVerificationId, otp)
                mAuth!!.signInWithCredential(credential)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                // Sign in success, update UI with the signed-in user's information
                                Toast.makeText(this@LoginActivity, "Logged In successfully",
                                        Toast.LENGTH_SHORT).show()
                                val user = task.result?.user
                                // ...
                            } else if (task.exception is FirebaseAuthInvalidCredentialsException) {
                                // Sign in failed, display a message and update the UI
                                Toast.makeText(this@LoginActivity, "Incorrect OTP!",
                                        Toast.LENGTH_SHORT).show()
                                    // The verification code entered was invalid
                                }
                            else{
                                Toast.makeText(this@LoginActivity, "Some error occurred!!",
                                        Toast.LENGTH_SHORT).show()
                            }
                        }
            } else {
                enterOtpMobile.error = "Invalid OTP"
                enterOtpMobile.requestFocus()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = mAuth!!.currentUser
        //updateUI(currentUser)
    }

    fun enableUserManuallyInputCode() {
        btnLoginMobile.visibility = View.VISIBLE
        enterOtpMobile.visibility = View.VISIBLE
        btnRequestOtp.text = getString(R.string.Resend_OTP)
    }
}