package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.locationreminders.RemindersActivity
import kotlin.properties.Delegates

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */

private const val FIREBASE_SIGN_IN_REQUEST_CODE = 1010001
private const val TAG = "AuthenticationActivity"

class AuthenticationActivity : AppCompatActivity() {
    private var loginState: LoginState by Delegates.observable(LoginState.LoggedOut) { _, _, new ->
        when (new) {
            LoginState.LoggedOut -> {
                Toast.makeText(this, R.string.please_sign_in, Toast.LENGTH_LONG).show()
            }
            LoginState.LoggedIn -> navigateToRemindersScreen()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)

        if (!checkIfUserAlreadySignedIn()) {
            Log.i(TAG, "User not already signed in")
            launchSignInFlow()
        } else {
            loginState = LoginState.LoggedIn
        }

//          TODO: a bonus is to customize the sign in flow to look nice using :
        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout
    }

    private fun launchSignInFlow() {
        // Give users the option to sign in / register with their email or Google account.
        // If users choose to register with their email,
        // they will need to create a password as well.
        val signInProviders = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        // Create and launch sign-in intent.
        // We listen to the response of this activity with the
        // FIREBASE_SIGN_IN_REQUEST_CODE
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(signInProviders)
                .build(),
            FIREBASE_SIGN_IN_REQUEST_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FIREBASE_SIGN_IN_REQUEST_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                loginState = LoginState.LoggedIn
                Log.i(
                    TAG,
                    "Successfully signed in user ${FirebaseAuth.getInstance().currentUser?.displayName}!"
                )
            } else {
                loginState = LoginState.LoggedOut
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                Log.i(TAG, "Sign in unsuccessful ${response?.error?.errorCode}")
            }
        }
    }

    private fun checkIfUserAlreadySignedIn(): Boolean {
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        return auth.currentUser != null
    }

    private fun navigateToRemindersScreen() {
        val intent = Intent(this, RemindersActivity::class.java)
        startActivity(intent)
        finish()
    }
}

sealed class LoginState {
    object LoggedIn : LoginState()
    object LoggedOut : LoginState()
}
