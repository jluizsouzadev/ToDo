package com.example.todo

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.Response
import com.example.todo.databinding.FragmentSignInBinding
import com.microsoft.identity.client.*
import com.microsoft.identity.client.exception.MsalException
import org.json.JSONObject

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SignInFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SignInFragment : Fragment() {
    private lateinit var binding: FragmentSignInBinding
    private val myTAG = SignInFragment::class.java.simpleName
    /* Azure AD Variables */
    private var mAccountApp: ISingleAccountPublicClientApplication? = null
    private val msGraphURL: String = "https://graph.microsoft.com/v1.0/me"

     // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        PublicClientApplication.createSingleAccountPublicClientApplication(
                context as Context,
                R.raw.auth_config_single_account,
                object : IPublicClientApplication.ISingleAccountApplicationCreatedListener {
                    override fun onCreated(application: ISingleAccountPublicClientApplication) {
                        mAccountApp = application
                    }

                    override fun onError(exception: MsalException) {
                        Log.d(myTAG, "Error to try to create " +
                                "\"IPublicClientApplication." +
                                "ISingleAccountApplicationCreatedListener\"" +
                                "$exception")
                    }
                }
        )
        binding = FragmentSignInBinding.inflate(inflater, container, false)
        with(binding) {
            buttonSignIn.setOnClickListener {
               if (editTextEmailAddressOrPhone.text.isEmpty()) {
                   val message = "You must enter some email or phone number"
                   Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
               } else {
                   val login: String = editTextEmailAddressOrPhone.text.toString()
                   val scope = arrayOf<String>("User.Read")
                   mAccountApp!!.signIn(activity as Activity, login, scope, getAuthSilentCallback())
               }
            }
            textViewCreateMA.setOnClickListener {
                val site = Uri.parse("https://signup.live.com/signup/")
                val browserIntent = Intent(Intent.ACTION_VIEW, site)
                startActivity(browserIntent)
            }
            return root
        }
    }

    private fun getAuthSilentCallback(): AuthenticationCallback {
        return object : AuthenticationCallback {
            override fun onSuccess(authenticationResult: IAuthenticationResult) {
                /* Successfully got a token, use it to call a protected resource - MSGraph */
                Log.d(myTAG, "Successfully authenticated")
                callGraphAPI(authenticationResult)
            }

            override fun onError(exception: MsalException) {
                /* Failed to acquireToken */
                Log.d(myTAG, "Authentication failed: $exception")
            }

            override fun onCancel() {
                /* User cancelled the authentication */
                Log.d(myTAG, "User cancelled login.")
            }
        }
    }

    /* Make an HTTP request to obtain MSGraph data */
    private fun callGraphAPI(authenticationResult: IAuthenticationResult) {
        MSGraphRequestWrapper.callGraphAPIWithVolley(
            context as Context,
            msGraphURL,
            authenticationResult.accessToken,
            Response.Listener<JSONObject> { response ->
                /* Successfully called graph, process data and send to UI */    
                Log.d(myTAG, "Response: $response")
            },
            Response.ErrorListener { error ->
                Log.d(myTAG, "Error: $error")
            })
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SignInFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                SignInFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}