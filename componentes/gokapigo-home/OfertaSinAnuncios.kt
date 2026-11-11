package com.gokapigo.home

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Oferta «mira un video y no verás anuncios a pantalla completa durante 20 minutos» (el banner sigue).
 * Sale después de cerrar un intersticial, abajo, y se queda hasta que el jugador ve el video o la cierra con ✕.
 * Al terminar los 20 minutos vuelve a salir sola: se puede repetir todas las veces que quiera.
 * El estado vive en el AdsManager de cada juego; aquí solo el dibujo y los textos (igual en todos los juegos).
 */
class OfertaSinAnuncios(context: Context) {
    private val prefs = context.getSharedPreferences("gokapigo_oferta", Context.MODE_PRIVATE)

    /** Hasta cuándo no se muestran intersticiales (epoch ms). */
    var hasta: Long = prefs.getLong("hasta", 0L)
        private set

    var visible by mutableStateOf(false)
    var ganada by mutableStateOf(false)

    /** Acciones (las pone el AdsManager). [hayVideo] = hay un video con recompensa cargado y no compró «quitar anuncios». */
    var onVer: () -> Unit = {}
    var hayVideo: () -> Boolean = { false }

    /** Para volver a ofrecerla una sola vez al terminar cada periodo de 20 minutos. */
    private var reofrecida = true

    fun activa(ahora: Long = System.currentTimeMillis()) = ahora < hasta

    fun ganar(ahora: Long = System.currentTimeMillis()) {
        hasta = ahora + MINUTOS * 60_000L
        prefs.edit().putLong("hasta", hasta).apply()
        reofrecida = false
    }

    /** Se llama cada pocos segundos: terminó el periodo sin intersticiales → se vuelve a ofrecer. */
    fun revisar(ahora: Long = System.currentTimeMillis()) {
        if (!reofrecida && !activa(ahora) && hayVideo()) { reofrecida = true; visible = true }
    }

    companion object { const val MINUTOS = 20 }
}

private class TextosOferta(val titulo: String, val sub: String, val boton: String, val listo: String)

private fun textos(idioma: String) = when (idioma.take(2).lowercase()) {
    "es" -> TextosOferta("¿20 minutos sin anuncios?", "Mira un video y no verás anuncios a pantalla completa", "Ver video", "¡Listo! 20 minutos sin anuncios")
    "pt" -> TextosOferta("20 minutos sem anúncios?", "Assista a um vídeo e fique sem anúncios em tela cheia", "Ver vídeo", "Pronto! 20 minutos sem anúncios")
    "fr" -> TextosOferta("20 minutes sans pub ?", "Regarde une vidéo et évite les pubs plein écran", "Voir la vidéo", "C'est fait ! 20 minutes sans pub")
    "de" -> TextosOferta("20 Minuten ohne Werbung?", "Sieh dir ein Video an – keine Vollbildwerbung", "Video ansehen", "Fertig! 20 Minuten ohne Werbung")
    "pl" -> TextosOferta("20 minut bez reklam?", "Obejrzyj film i pomiń reklamy pełnoekranowe", "Obejrzyj film", "Gotowe! 20 minut bez reklam")
    "ru" -> TextosOferta("20 минут без рекламы?", "Посмотрите видео — и никакой полноэкранной рекламы", "Смотреть видео", "Готово! 20 минут без рекламы")
    else -> TextosOferta("20 minutes without ads?", "Watch a video and skip full-screen ads", "Watch video", "Done! 20 minutes without ads")
}

/** Tarjeta de la oferta y aviso de «listo». Ponerla sobre la pantalla del juego, alineada abajo. */
@Composable
fun TarjetaOfertaSinAnuncios(o: OfertaSinAnuncios, modifier: Modifier = Modifier) {
    val t = textos(LocalConfiguration.current.locales[0].language)
    val naranja = Color(0xFFFB7B00)
    val crema = Color(0xFFFFFBF5)
    LaunchedEffect(o) { while (true) { delay(15_000); o.revisar() } }      // al acabar los 20 min, vuelve a ofrecerla
    LaunchedEffect(o.ganada) { if (o.ganada) { delay(3_000); o.ganada = false } }
    Box(modifier.fillMaxWidth().navigationBarsPadding().padding(start = 12.dp, end = 12.dp, bottom = 74.dp), contentAlignment = Alignment.BottomCenter) {
        AnimatedVisibility(o.visible, enter = slideInVertically { it } + fadeIn(), exit = slideOutVertically { it } + fadeOut()) {
            val forma = RoundedCornerShape(18.dp)
            Row(
                Modifier.widthIn(max = 460.dp).fillMaxWidth().clip(forma)
                    .background(Brush.horizontalGradient(listOf(Color(0xFF2A1A0C), Color(0xFF3A2210))))
                    .border(1.5.dp, naranja, forma).padding(start = 14.dp, end = 6.dp, top = 10.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text("🎬", style = TextStyle(fontSize = 26.sp))
                Column(Modifier.weight(1f)) {
                    Text(t.titulo, style = TextStyle(color = crema, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold))
                    Text(t.sub, style = TextStyle(color = crema.copy(alpha = .75f), fontSize = 12.sp, lineHeight = 15.sp))
                }
                Text(
                    t.boton, Modifier.clip(CircleShape).background(naranja).clickable { o.visible = false; o.onVer() }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    style = TextStyle(color = Color(0xFF1A0D00), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold),
                )
                Box(Modifier.size(32.dp).clip(CircleShape).clickable { o.visible = false }, contentAlignment = Alignment.Center) {
                    Text("✕", style = TextStyle(color = crema.copy(alpha = .7f), fontSize = 15.sp, fontWeight = FontWeight.Bold))
                }
            }
        }
        AnimatedVisibility(o.ganada, enter = fadeIn(), exit = fadeOut()) {
            Text(
                "✓  " + t.listo, Modifier.clip(CircleShape).background(Color(0xFF1E7A4C)).padding(horizontal = 16.dp, vertical = 10.dp),
                style = TextStyle(color = crema, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold),
            )
        }
    }
}
