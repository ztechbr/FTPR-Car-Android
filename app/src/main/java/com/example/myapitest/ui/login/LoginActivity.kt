package com.example.myapitest.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapitest.R
import com.example.myapitest.ui.main.MainActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit

// RZ - LoginActivity: A porta de entrada do app.
// Aqui usamos o Firebase Auth para garantir que só usuários autenticados acessem os dados.
// O fluxo é simples: envia o código para o telefone e valida o que o usuário digita.

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var verificationId: String? = null

    private lateinit var etPhoneNumber: EditText
    private lateinit var etVerificationCode: EditText
    private lateinit var btnSendCode: Button
    private lateinit var btnLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        etPhoneNumber = findViewById(R.id.etPhoneNumber)
        etVerificationCode = findViewById(R.id.etVerificationCode)
        btnSendCode = findViewById(R.id.btnSendCode)
        btnLogin = findViewById(R.id.btnLogin)

        // RZ - Passo 1: Solicitar o código de verificação
        btnSendCode.setOnClickListener {
            val phoneNumber = etPhoneNumber.text.toString()
            if (phoneNumber.isNotEmpty()) {
                startPhoneNumberVerification(phoneNumber)
            } else {
                Toast.makeText(this, "Digite o número", Toast.LENGTH_SHORT).show()
            }
        }

        // RZ - Passo 2: Validar o código recebido e logar
        btnLogin.setOnClickListener {
            val code = etVerificationCode.text.toString()
            if (code.isNotEmpty() && verificationId != null) {
                signInWithPhoneAuthCredential(PhoneAuthProvider.getCredential(verificationId!!, code))
            }
        }
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // RZ - Em alguns casos o login é automático se o Firebase detectar o SMS
                    signInWithPhoneAuthCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Toast.makeText(this@LoginActivity, "Falha: ${e.message}", Toast.LENGTH_LONG).show()
                }

                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    this@LoginActivity.verificationId = verificationId
                    // RZ - Mostra os campos para digitar o código
                    etVerificationCode.visibility = View.VISIBLE
                    btnLogin.visibility = View.VISIBLE
                    btnSendCode.visibility = View.GONE
                    Toast.makeText(this@LoginActivity, "Código enviado!", Toast.LENGTH_SHORT).show()
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // RZ - Sucesso! Vai para a tela principal
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Código inválido", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
