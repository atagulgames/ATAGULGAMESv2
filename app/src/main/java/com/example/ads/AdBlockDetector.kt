package com.example.ads

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.provider.Settings
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.net.InetAddress

object AdBlockDetector {
    private const val TAG = "AdBlockDetector"

    // Primary ad domains required for Start.io and ad networks
    private val AD_HOSTNAMES = listOf(
        "req.startappservice.com",
        "init.startappservice.com",
        "pagead2.googlesyndication.com",
        "googleads.g.doubleclick.net"
    )

    // Control hostnames that should always resolve when device has internet
    private val CONTROL_HOSTNAMES = listOf(
        "google.com",
        "cloudflare.com"
    )

    // Known ad-blocker package identifiers
    private val ADBLOCK_PACKAGES = listOf(
        "com.adguard.android",
        "com.adguard.android.contentblocker",
        "org.blokada.alarm",
        "org.blokada.origin.alarm",
        "org.jak_linux.dns66",
        "de.hpi.sam.adblocker",
        "com.freeadhacker.adblocker",
        "com.adblocker.free"
    )

    /**
     * Performs a comprehensive check for active ad blockers on the device.
     * Returns true if an ad blocker or DNS sinkhole is actively blocking ads.
     */
    suspend fun isAdBlockerActive(context: Context): Boolean = withContext(Dispatchers.IO) {
        try {
            // 1. Check Private DNS configuration (Android 9+)
            if (checkPrivateDnsSettings(context)) {
                Log.w(TAG, "AdBlock detected via Private DNS settings")
                return@withContext true
            }

            // 2. Check /etc/hosts for ad domain sinkholing
            if (checkHostsFile()) {
                Log.w(TAG, "AdBlock detected via Hosts file modification")
                return@withContext true
            }

            // 3. Check installed active ad blocker apps
            if (checkAdBlockerPackages(context)) {
                Log.w(TAG, "AdBlock detected via installed adblocker package + active VPN")
                return@withContext true
            }

            // 4. Check DNS sinkholing (Ad domains resolving to 0.0.0.0 / 127.0.0.1 or blocked)
            if (checkDnsSinkholing()) {
                Log.w(TAG, "AdBlock detected via DNS sinkhole resolution probe")
                return@withContext true
            }

            false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking ad blocker status", e)
            false
        }
    }

    /**
     * Checks if Private DNS is set to a known ad-blocking DNS provider.
     */
    private fun checkPrivateDnsSettings(context: Context): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val privateDnsMode = Settings.Global.getString(
                    context.contentResolver,
                    "private_dns_mode"
                )
                val privateDnsSpecifier = Settings.Global.getString(
                    context.contentResolver,
                    "private_dns_specifier"
                )?.lowercase() ?: ""

                if (!privateDnsSpecifier.isNullOrEmpty()) {
                    val adBlockDnsKeywords = listOf(
                        "adguard",
                        "adblock",
                        "nextdns",
                        "controld",
                        "mullvad",
                        "dnsforge",
                        "ahadns",
                        "block"
                    )
                    return adBlockDnsKeywords.any { privateDnsSpecifier.contains(it) }
                }
            }
            false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Checks if /etc/hosts or /system/etc/hosts contains ad network domains mapped to 127.0.0.1 or 0.0.0.0.
     */
    private fun checkHostsFile(): Boolean {
        val paths = listOf("/system/etc/hosts", "/etc/hosts")
        for (path in paths) {
            val file = File(path)
            if (file.exists() && file.canRead()) {
                try {
                    BufferedReader(FileReader(file)).use { reader ->
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            val l = line?.trim()?.lowercase() ?: continue
                            if (l.startsWith("#") || l.isEmpty()) continue

                            val isSinkholed = l.startsWith("127.0.0.1") || l.startsWith("0.0.0.0") || l.startsWith("::1")
                            if (isSinkholed) {
                                if (l.contains("startapp") || l.contains("doubleclick") || l.contains("googleads") || l.contains("pagead")) {
                                    return true
                                }
                            }
                        }
                    }
                } catch (_: Exception) {}
            }
        }
        return false
    }

    /**
     * Checks if an ad-blocking package is installed AND a VPN/Network transport is currently active.
     */
    private fun checkAdBlockerPackages(context: Context): Boolean {
        try {
            val pm = context.packageManager
            var hasAdBlockApp = false
            for (pkg in ADBLOCK_PACKAGES) {
                try {
                    pm.getPackageInfo(pkg, 0)
                    hasAdBlockApp = true
                    break
                } catch (_: Exception) {}
            }

            if (!hasAdBlockApp) return false

            // Check if VPN is currently active (how local adblockers like AdGuard/Blokada work)
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val activeNetwork = cm?.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(activeNetwork) ?: return false
            return caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Probes DNS resolution for ad domains.
     * If control hostnames resolve normally but ad hostnames are sinkholed to 0.0.0.0 or 127.0.0.1,
     * or ad domains fail to resolve while control domains succeed, an ad blocker is running.
     */
    private fun checkDnsSinkholing(): Boolean {
        try {
            // First check if device has working DNS resolution
            var hasInternetDns = false
            for (controlHost in CONTROL_HOSTNAMES) {
                try {
                    val addresses = InetAddress.getAllByName(controlHost)
                    if (addresses.isNotEmpty() && !addresses.all { isSinkholedAddress(it) }) {
                        hasInternetDns = true
                        break
                    }
                } catch (_: Exception) {}
            }

            // If control domains can't resolve, user is simply offline, don't flag as ad blocker
            if (!hasInternetDns) return false

            // Now probe ad hostnames
            var adDomainsBlockedCount = 0
            for (adHost in AD_HOSTNAMES) {
                try {
                    val addresses = InetAddress.getAllByName(adHost)
                    if (addresses.isEmpty() || addresses.all { isSinkholedAddress(it) }) {
                        adDomainsBlockedCount++
                    }
                } catch (_: Exception) {
                    // UnknownHostException specifically for ad domains when control domain resolved
                    adDomainsBlockedCount++
                }
            }

            // If majority of ad hostnames are sinkholed/blocked while internet is active, ad blocker is active
            return adDomainsBlockedCount >= 2
        } catch (e: Exception) {
            return false
        }
    }

    private fun isSinkholedAddress(address: InetAddress): Boolean {
        val hostAddress = address.hostAddress ?: return false
        return hostAddress == "0.0.0.0" ||
                hostAddress == "127.0.0.1" ||
                hostAddress == "::1" ||
                hostAddress == "::"
    }
}
