package com.example.apnashop.utils

import android.util.Log
import okhttp3.*
import org.json.JSONArray
import java.io.IOException

object PincodeUtils {

    fun fetchDistrictState(
        pincode: String,
        onResult: (district: String?, state: String?) -> Unit
    ) {

        val client = OkHttpClient()

        val request = Request.Builder()
            .url("https://api.postalpincode.in/pincode/$pincode")
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                Log.e("PINCODE_API", "API Failed: ${e.message}")
                onResult(null, null)
            }

            override fun onResponse(call: Call, response: Response) {

                val body = response.body?.string()

                Log.d("PINCODE_API", "Response: $body")

                try {

                    val jsonArray = JSONArray(body)
                    val postOfficeArray =
                        jsonArray.getJSONObject(0).getJSONArray("PostOffice")

                    val postOffice = postOfficeArray.getJSONObject(0)

                    val district = postOffice.getString("District")
                    val state = postOffice.getString("State")

                    Log.d("PINCODE_API", "District: $district  State: $state")

                    onResult(district, state)

                } catch (e: Exception) {
                    Log.e("PINCODE_API", "Parsing Error: ${e.message}")
                    onResult(null, null)
                }
            }
        })
    }
}