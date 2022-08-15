package com.example.lugares

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.lugares.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    companion object {
        private const val RC_SIGN_IN  = 9001
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        FirebaseApp.initializeApp(this)
        auth = Firebase.auth

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        binding.btGoogleLogin.setOnClickListener {  googleSignIn() }

        //Se define el metodo para el login
        binding.btLogin.setOnClickListener{
            haceLogin()
        }

        //Se define el metodo para el registro
        binding.btRegister.setOnClickListener{
            haceRegister()
        }
    }

    private fun haceRegister() {
        val email = binding.etEmail.text.toString()
        val clave = binding.etClave.text.toString()

        //Se hace el registro
        auth.createUserWithEmailAndPassword(email, clave)
            .addOnCompleteListener(this) { task ->
                if(task.isSuccessful){
                    Log.d("Creando usuario", "Registrado")
                    val user = auth.currentUser
                    actualiza(user)
                }else{
                    Log.d("Creando usuario", "Fallo")
                    Toast.makeText(baseContext, "Fallo", Toast.LENGTH_LONG).show()
                    actualiza(null)
                }
            }
    }

    private fun haceLogin(){
        val email = binding.etEmail.text.toString()
        val clave = binding.etClave.text.toString()

        //Se hace el login
        auth.signInWithEmailAndPassword(email, clave)
            .addOnCompleteListener(this) { task ->
                if(task.isSuccessful){
                    Log.d("Autenticado", "Autenticado")
                    val user = auth.currentUser
                    actualiza(user)
                }else{
                    Log.d("Autenticado", "Fallo")
                    Toast.makeText(baseContext, "Fallo", Toast.LENGTH_LONG).show()
                    actualiza(null)
                }
            }
    }

    private fun googleSignIn(){
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun firebaseAuthWithGoogle(idToken: String){
        val credentiales = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credentiales)
            .addOnCompleteListener(this){ task ->
                if(task.isSuccessful){
                    val usuario = auth.currentUser
                    actualiza(usuario)
                }else{
                    actualiza(null)
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == RC_SIGN_IN){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            }catch (e: ApiException){

            }
        }
    }

    private fun actualiza(user: FirebaseUser?){
        if(user != null){
            val intent = Intent(this, Principal::class.java)
            startActivity(intent)
        }
    }

    //Esto hara que una vez autenticado... no pida mas a menos que se cierre la sesion
    public override fun onStart(){
        super.onStart()
        val usuario = auth.currentUser
        actualiza(usuario)
    }
}