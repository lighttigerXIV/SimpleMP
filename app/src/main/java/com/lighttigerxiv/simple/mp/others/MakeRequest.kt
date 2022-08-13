package com.lighttigerxiv.simple.mp.others

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import java.nio.charset.StandardCharsets.UTF_8


class MakeRequest(
    val context: Context ?= null,
    val url: String ?= null,
    val responseListener: OnResponseListener?= null
) {


    interface OnResponseListener {
        fun onResponse(responseCode: Int, responseJson: String)
    }


    fun get() {

        val requestQueue = Volley.newRequestQueue(context)

        val stringRequest = object : StringRequest(Method.GET, url, {},
            {
                if (it.toString() == "com.android.volley.TimeoutError")
                    get()
                else
                    Toast.makeText(context, it.toString(), LENGTH_LONG).show()
            }) {

            override fun parseNetworkResponse(response: NetworkResponse): Response<String> {

                val json = String(response.data, UTF_8)

                Handler(Looper.getMainLooper()).post {
                    responseListener?.onResponse(response.statusCode,
                        json)
                }

                return super.parseNetworkResponse(response)
            }
        }

        requestQueue.add(stringRequest)
    }
}