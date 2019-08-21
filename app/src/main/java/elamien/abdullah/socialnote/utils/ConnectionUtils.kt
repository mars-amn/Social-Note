package elamien.abdullah.socialnote.utils

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build


class ConnectionUtils(val context : Context) {
	@Suppress("DEPRECATION")
	fun isDeviceNetworkAvailable() : Boolean {
		val connectivityManager =
			context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
		val networkInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			connectivityManager.activeNetwork
		} else {
			connectivityManager.activeNetworkInfo
		}
		return networkInfo != null
	}

	companion object {
		fun getConnectionUtils(context : Context) : ConnectionUtils {
			return ConnectionUtils(context)
		}
	}
}