package com.gokapigo.home

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.edit
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Lo nativo de la portada: catálogo guardado/remoto, iconos y abrir o instalar otro juego.
 * Para saber si un juego está instalado (Android 11+) su paquete debe estar en <queries> del manifiesto.
 */
class PuenteHome(private val ctx: Context) {

    private val prefs = ctx.getSharedPreferences("gokapigo_home", Context.MODE_PRIVATE)
    private val carpeta = File(ctx.filesDir, "gkg_iconos")

    /** El último catálogo bueno guardado, o el embebido. Nunca falla y no usa internet. */
    fun catalogoGuardado(): List<ItemCatalogo> =
        prefs.getString("catalogo", null)?.let { Catalogo.desdeJson(it) } ?: Catalogo.EMBEBIDO

    /**
     * Descarga el catálogo (máx. una vez cada 12 h, 5 s de espera). null si no cambió nada o no hubo red:
     * la portada sigue con el que tenía.
     */
    suspend fun actualizarCatalogo(forzar: Boolean = false): List<ItemCatalogo>? = withContext(Dispatchers.IO) {
        val ahora = System.currentTimeMillis()
        if (!forzar && ahora - prefs.getLong("catalogo_t", 0) < 12 * 3600_000L) return@withContext null
        val texto = descargar(Catalogo.URL)?.toString(Charsets.UTF_8) ?: return@withContext null
        val nuevo = Catalogo.desdeJson(texto) ?: return@withContext null
        prefs.edit { putString("catalogo", Catalogo.aJson(nuevo)); putLong("catalogo_t", ahora) }
        nuevo
    }

    fun instalado(paquete: String): Boolean =
        paquete.startsWith(Catalogo.PREFIJO) && ctx.packageManager.getLaunchIntentForPackage(paquete) != null

    /**
     * Icono de un elemento, sin red: el propio sale del icono de la app (el mismo que el de Play, regla del estudio);
     * los instalados, de su icono real; los demás, del PNG guardado. null = aún no hay (se pinta un respaldo).
     */
    fun iconoLocal(item: ItemCatalogo): ImageBitmap? {
        if (item.paquete == ctx.packageName || instalado(item.paquete)) {
            return try { cuadrado(ctx.packageManager.getApplicationIcon(item.paquete)).asImageBitmap() } catch (_: Exception) { null }
        }
        val f = File(carpeta, "${item.id}.png")
        return if (f.exists()) BitmapFactory.decodeFile(f.path)?.asImageBitmap() else null
    }

    /** Baja el PNG del icono de Play y lo guarda (se renueva cada 7 días). */
    suspend fun descargarIcono(item: ItemCatalogo): ImageBitmap? = withContext(Dispatchers.IO) {
        val url = item.icono ?: return@withContext null
        val f = File(carpeta, "${item.id}.png")
        if (f.exists() && System.currentTimeMillis() - f.lastModified() < 7 * 86400_000L) return@withContext null
        val bytes = descargar(url) ?: return@withContext null
        val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return@withContext null
        carpeta.mkdirs()
        f.outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bmp.asImageBitmap()
    }

    fun abrir(paquete: String) {
        if (!paquete.startsWith(Catalogo.PREFIJO)) return
        val intent = ctx.packageManager.getLaunchIntentForPackage(paquete)?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (intent != null) ctx.startActivity(intent) else abrirTienda(paquete, "")
    }

    fun abrirTienda(paquete: String, referrer: String) {
        if (!paquete.startsWith(Catalogo.PREFIJO)) return
        val ref = if (referrer.isNotEmpty()) "&referrer=" + Uri.encode(referrer) else ""
        val tienda = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$paquete$ref"))
            .setPackage("com.android.vending").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            ctx.startActivity(tienda)
        } catch (_: ActivityNotFoundException) {
            try {
                ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$paquete$ref"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            } catch (_: ActivityNotFoundException) { }
        }
    }

    /**
     * El icono como en Play: cuadrado completo. Un icono adaptable se dibuja capa por capa (fondo + frente) recortando
     * el área visible (72 de 108 dp), sin la máscara del teléfono (que lo haría círculo, gota…).
     */
    private fun cuadrado(d: Drawable, lado: Int = 192): Bitmap {
        if (Build.VERSION.SDK_INT < 26 || d !is AdaptiveIconDrawable) return d.toBitmap(lado, lado)
        val bmp = Bitmap.createBitmap(lado, lado, Bitmap.Config.ARGB_8888)
        val cv = Canvas(bmp)
        val total = lado * 108 / 72
        val o = -(total - lado) / 2
        listOfNotNull(d.background, d.foreground).forEach { it.setBounds(o, o, o + total, o + total); it.draw(cv) }
        return bmp
    }

    private fun descargar(url: String): ByteArray? = try {
        (URL(url).openConnection() as HttpURLConnection).run {
            connectTimeout = 5000; readTimeout = 5000
            try { if (responseCode == 200) inputStream.use { it.readBytes() } else null } finally { disconnect() }
        }
    } catch (_: Exception) {
        null
    }
}
