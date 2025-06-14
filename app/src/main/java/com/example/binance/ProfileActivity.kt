package com.example.binance

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.example.binance.models.UserProfile
import com.example.binance.network.RetrofitClient
import com.example.binance.network.UpdateProfileRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.bottomnavigation.BottomNavigationView

class ProfileActivity : AppCompatActivity() {

    // 1. Views do layout
    private lateinit var ivAvatar: ImageView
    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView


    // Label e botão “change” de Personal Details
    private lateinit var tvLabelPersonalDetails: TextView
    private lateinit var tvChangePersonalDetails: TextView

    // 2. CardViews “botões” de FAQ, Help, Language
    private lateinit var cardFaq: CardView
    private lateinit var cardHelp: CardView
    private lateinit var cardLanguage: CardView
    private lateinit var cardTutorials: CardView
    private lateinit var bottomNav: BottomNavigationView

    // 3. Launcher para escolher avatar da galeria
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>

    // 4. SharedPreferences (“APP_PREFS”)
    private lateinit var prefs: SharedPreferences

    // 5. Armazenar token e userId localmente
    private var savedUserId: String? = null
    private var savedAuthToken: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Vincular as Views
        ivAvatar                 = findViewById(R.id.ivAvatar)
        tvUserName               = findViewById(R.id.tvUserName)
        tvUserEmail              = findViewById(R.id.tvUserEmail)
        tvLabelPersonalDetails   = findViewById(R.id.tvLabelPersonalDetails)
        tvChangePersonalDetails  = findViewById(R.id.tvChangePersonalDetails)


        // Vincular CardViews
        cardFaq      = findViewById(R.id.cardFaq)
        cardHelp     = findViewById(R.id.cardHelp)
        cardLanguage = findViewById(R.id.cardLanguage)
        cardTutorials = findViewById(R.id.cardTutorials)
        bottomNav = findViewById(R.id.bottom_nav)

        // Inicializar SharedPreferences
        prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        savedUserId    = prefs.getString("USER_ID", null)
        savedAuthToken = prefs.getString("AUTH_TOKEN", null)
        val savedUsername = prefs.getString("USERNAME", null)

        // Mostrar username que veio do login (se existir)
        if (!savedUsername.isNullOrBlank()) {
            tvUserName.text = savedUsername
        } else {
            tvUserName.text = "..."
        }

        // Registrar launcher para escolher avatar da galeria
        pickImageLauncher = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let { selectedUri ->
                Glide.with(this)
                    .load(selectedUri)
                    .circleCrop()
                    .placeholder(R.drawable.ic_person_placeholder)
                    .into(ivAvatar)
                // Opcional: salve a URI para enviar ao servidor mais tarde
            }
        }
        ivAvatar.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // Configurar cliques nos CardViews
        cardFaq.setOnClickListener {
            Toast.makeText(this, "Abrir FAQ (implementar depois)", Toast.LENGTH_SHORT).show()
        }
        cardHelp.setOnClickListener {
            Toast.makeText(this, "Abrir Help (implementar depois)", Toast.LENGTH_SHORT).show()
        }
        cardLanguage.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        // Configurar clique no CardView de Tutoriais tem de levar os dados do utilizador para depois abrir a HomeActivity
        cardTutorials.setOnClickListener {
            val username = prefs.getString("USERNAME", null)
            val email = prefs.getString("EMAIL", null)

            if (!savedUserId.isNullOrBlank() && !username.isNullOrBlank() && !email.isNullOrBlank()) {
                val intent = Intent(this, OnboardingActivity::class.java).apply {
                    putExtra("USER_ID", savedUserId)
                    putExtra("USERNAME", username)
                    putExtra("EMAIL", email)
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "Dados de utilizador incompletos", Toast.LENGTH_SHORT).show()
            }
        }


        // Configurar clique em “change” para abrir diálogo de edição
        tvChangePersonalDetails.setOnClickListener {
            if (!savedUserId.isNullOrBlank() && !savedAuthToken.isNullOrBlank()) {
                showEditProfileDialog(savedUserId!!, savedAuthToken!!)
            } else {
                Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show()
            }
        }

        // Após configurar o diálogo, chamar fetchUserProfile para carregar email/username atual
        if (!savedUserId.isNullOrBlank() && !savedAuthToken.isNullOrBlank()) {
            fetchUserProfile(savedUserId!!, savedAuthToken!!)
        } else {
            Toast.makeText(this,
                "Erro: usuário não autenticado. Faça login novamente.",
                Toast.LENGTH_LONG
            ).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val switchTheme = findViewById<Switch>(R.id.switchTheme)

        // Verifica o tema salvo
        val isDarkMode = prefs.getBoolean("DARK_MODE", false)
        switchTheme.isChecked = isDarkMode

        // Define comportamento do switch
        switchTheme.setOnCheckedChangeListener { _, isChecked ->
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
            prefs.edit().putBoolean("DARK_MODE", isChecked).apply()
        }

        //Navigation Bottom Menu
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this@ProfileActivity, HomeActivity::class.java))
                    true
                }
                R.id.nav_refresh -> {
                    startActivity(Intent(this@ProfileActivity, TradeHistoryActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this@ProfileActivity, ProfileActivity::class.java))
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this@ProfileActivity, BotSettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }

    }

    /**
     *  Mostra um AlertDialog com dois EditText (username/email), já preenchidos
     *  com os valores atuais, e um botão “Salvar” que dispara a chamada PUT.
     */
    private fun showEditProfileDialog(userId: String, authToken: String) {
        // 1) Infla um layout customizado para o diálogo
        val dialogView = LayoutInflater.from(this)
            .inflate(R.layout.dialog_edit_profile, null, false)

        val etNewUsername = dialogView.findViewById<EditText>(R.id.etNewUsername)
        val etNewEmail    = dialogView.findViewById<EditText>(R.id.etNewEmail)

        // Pré-preenche com os valores atuais da tela
        etNewUsername.setText(tvUserName.text.toString())
        etNewEmail.setText(tvUserEmail.text.toString())

        // 2) Constrói o AlertDialog
        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.edit_profile))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.save), null)   // clicaremos manualmente abaixo
            .setNegativeButton(getString(R.string.cancel)) { dlg, _ -> dlg.dismiss()
            }.create()

        // 3) Ao mostrar o diálogo, sobrepomos o OnClick do botão “Salvar” para evitar fechar automático
        dialog.setOnShowListener {
            val btnSalvar = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            btnSalvar.setOnClickListener {
                val newUsername = etNewUsername.text.toString().trim()
                val newEmail    = etNewEmail.text.toString().trim()

                // Validação simples
                if (newUsername.isEmpty()) {
                    etNewUsername.error = getString(R.string.error_username_required)
                    return@setOnClickListener
                }
                if (newEmail.isEmpty()) {
                    etNewEmail.error = getString(R.string.error_email_required)
                    return@setOnClickListener
                }
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                    etNewEmail.error = getString(R.string.error_invalid_email)
                    return@setOnClickListener
                }

                // 4) Chama o Retrofit para atualizar no back-end
                val updateReq = UpdateProfileRequest(newUsername, newEmail)
                RetrofitClient.apiService
                    .updateUserProfile(userId, authToken, updateReq)
                    .enqueue(object : Callback<UserProfile> {
                        override fun onResponse(
                            call: Call<UserProfile>,
                            response: Response<UserProfile>
                        ) {
                            if (response.isSuccessful) {
                                // 5) Se deu certo, atualiza a UI e SharedPreferences, fecha diálogo
                                response.body()?.let { updated ->
                                    tvUserName.text  = updated.username
                                    tvUserEmail.text = updated.email

                                    // Atualiza também no SharedPreferences
                                    prefs.edit()
                                        .putString("USERNAME", updated.username)
                                        .apply()
                                }
                                Toast.makeText(
                                    this@ProfileActivity,
                                    getString(R.string.profile_updated_successfully),
                                    Toast.LENGTH_SHORT
                                ).show()
                                dialog.dismiss()
                            } else {
                                // Tratar erros de validação enviados pelo servidor (409 conflit, etc.)
                                if (response.code() == 409) {
                                    val erroMap = response.errorBody()?.string().orEmpty()
                                    Toast.makeText(
                                        this@ProfileActivity,
                                        getString(R.string.error_update_conflict) +"$erroMap",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this@ProfileActivity,
                                        getString(R.string.error_update_conflict) +"${response.code()}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }

                        override fun onFailure(call: Call<UserProfile>, t: Throwable) {
                            Toast.makeText(
                                this@ProfileActivity,
                                 getString(R.string.network_error)+"${t.localizedMessage}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    })
            }
        }

        dialog.show()
    }

    /**
     *  Carrega username e e-mail atuais do servidor (GET /api/users/{id}).
     */
    private fun fetchUserProfile(userId: String, authToken: String) {
        RetrofitClient.apiService
            .getUserProfile(userId, authToken)
            .enqueue(object : Callback<UserProfile> {
                override fun onResponse(
                    call: Call<UserProfile>,
                    response: Response<UserProfile>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let { perfil ->
                            tvUserName.text  = perfil.username
                            tvUserEmail.text = perfil.email
                        }
                    } else {
                        Toast.makeText(
                            this@ProfileActivity,
                            getString(R.string.error_getting_profile) +"${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<UserProfile>, t: Throwable) {
                    Toast.makeText(
                        this@ProfileActivity,
                        getString(R.string.network_error) + "${t.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }
}
