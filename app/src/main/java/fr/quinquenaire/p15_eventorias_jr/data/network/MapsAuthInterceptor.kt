package fr.quinquenaire.p15_eventorias_jr.data.network

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Interceptor
import okhttp3.Response
import java.security.MessageDigest
import javax.inject.Inject

class MapsAuthInterceptor @Inject constructor(
    @ApplicationContext private val context: Context
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        if (request.url.host != "maps.googleapis.com") {
            return chain.proceed(request)
        }

        val newRequest = request.newBuilder()
            .addHeader("X-Android-Package", context.packageName)
            .addHeader("X-Android-Cert", getSigningCertSha1())
            .build()
        val response = chain.proceed(newRequest)
        return response
    }

    private fun getSigningCertSha1(): String {
        val signature = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val packageInfo = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNING_CERTIFICATES
            )
            packageInfo.signingInfo!!.apkContentsSigners[0]
        } else {
            @Suppress("DEPRECATION")
            val packageInfo = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNATURES
            )
            @Suppress("DEPRECATION")
            packageInfo.signatures!![0]
        }

        val digest = MessageDigest.getInstance("SHA-1").digest(signature.toByteArray())
        return digest.joinToString(":") { "%02X".format(it) }
    }
}
