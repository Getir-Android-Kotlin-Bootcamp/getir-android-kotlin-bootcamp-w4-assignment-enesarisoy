package com.ns.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ns.myapplication.databinding.ActivityMainBinding
import com.ns.myapplication.model.Profile
import com.ns.myapplication.utils.Constants.BASE_URL
import com.ns.myapplication.utils.Constants.USER_EMAIL
import com.ns.myapplication.utils.Constants.USER_PASS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedWriter
import java.io.IOException
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        CoroutineScope(Dispatchers.IO).launch {

            val userId = login()
            if (userId.isNotEmpty()) {
                val profile = getProfile(userId)
                profile?.let {
                    withContext(Dispatchers.Main) {
                        with(binding) {
                            tvName.text = profile.fullName
                            tvEmail.text = profile.email
                            tvEmployer.text = profile.employer
                            tvOccupation.text = profile.occupation
                            tvPhone.text = profile.phoneNumber
                            Toast.makeText(
                                this@MainActivity,
                                "User ID: ${profile.userId}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                }
            } else {
                println("User ID is null or empty.")
            }
        }
    }

    private fun getProfile(userId: String): Profile? {
        val url = URL("$BASE_URL/profile/$userId")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        try {
            val inputStream = BufferedInputStream(connection.inputStream)
            val jsonResponse = inputStream.bufferedReader().use { it.readText() }
            println(jsonResponse)

            val jsonFields = jsonResponse.split(",").associate {
                val splitField = it.split(":")
                val key = splitField[0].replace("\"", "").trim()
                val value = splitField[1].replace("\"", "").trim()
                key to value
            }

            return Profile(
                jsonFields["country"],
                jsonFields["email"] ?: "",
                jsonFields["employer"],
                jsonFields["fullName"] ?: "",
                jsonFields["id"]?.toIntOrNull() ?: 0,
                jsonFields["latitude"]?.toDoubleOrNull() ?: 0.0,
                jsonFields["longitude"]?.toDoubleOrNull() ?: 0.0,
                jsonFields["occupation"],
                jsonFields["password"] ?: "",
                jsonFields["phoneNumber"],
                jsonFields["userId"] ?: ""
            )
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            connection.disconnect()
        }
        return null
    }

    private fun login(): String {
        val url = URL("$BASE_URL/login")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")

        val postData = "{\"email\":\"$USER_EMAIL\",\"password\":\"$USER_PASS\"}"
        connection.doOutput = true
        val outputStream: OutputStream = connection.outputStream
        val writer = BufferedWriter(OutputStreamWriter(outputStream, "UTF-8"))
        writer.write(postData)
        writer.flush()
        writer.close()

        val responseCode = connection.responseCode
        println("Response Code: $responseCode")

        val response = connection.inputStream.bufferedReader().use { it.readText() }
        println("Response: $response")

        connection.disconnect()

        return response
    }

}