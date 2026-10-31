package com.gokapigo.home

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.launch
import java.text.NumberFormat
import kotlin.math.abs
import kotlin.math.min

/**
 * Portada GoKapiGo Home. El juego propio queda al centro del carrusel; a los lados asoman los demás juegos y apps
 * públicos del estudio. Al centrar otro: ABRIR (instalado) o INSTALAR GRATIS (Play Store con referrer).
 * Sin banner: los anuncios van solo durante la partida (regla del estudio).
 *
 * @param subtitulo nivel o modo actual del juego propio (ya traducido por el juego).
 * @param precioSinAnuncios el precio localizado de Billing; null = solo «Sin anuncios».
 * @param onIdioma null = el botón no sale. @param onCalificar null = abre la ficha del juego en Play.
 * @param extras botones propios del juego (p. ej. modos) bajo el regalo; solo con el juego propio al centro.
 */
@Composable
fun GoKapiGoHome(
    propioId: String,
    estrellas: Int,
    subtitulo: String,
    acento: Color,
    tintaAcento: Color,
    onJugar: () -> Unit,
    onAjustes: () -> Unit,
    onCompartir: () -> Unit,
    modifier: Modifier = Modifier,
    fuenteTitulo: FontFamily = FontFamily.Default,
    fuenteTexto: FontFamily = FontFamily.Default,
    regaloDisponible: Boolean = false,
    diaRacha: Int = 0,
    onRegalo: (() -> Unit)? = null,
    mostrarSinAnuncios: Boolean = false,
    precioSinAnuncios: String? = null,
    onSinAnuncios: () -> Unit = {},
    onIdioma: (() -> Unit)? = null,
    onCalificar: (() -> Unit)? = null,
    onPromo: (ItemCatalogo, String) -> Unit = { _, _ -> },
    incluirApps: Boolean = true,
    extras: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val ctx = LocalContext.current
    val puente = remember { PuenteHome(ctx.applicationContext) }
    val t = Textos.de(LocalConfiguration.current.locales[0].toLanguageTag())
    var catalogo by remember { mutableStateOf(puente.catalogoGuardado()) }
    val lista = remember(catalogo) { Catalogo.carrusel(catalogo, propioId, incluirApps) }
    var instalados by remember { mutableStateOf(emptySet<String>()) }
    val iconos = remember { mutableStateMapOf<String, ImageBitmap>() }
    val alcance = rememberCoroutineScope()

    LaunchedEffect(Unit) { puente.actualizarCatalogo()?.let { catalogo = it } }
    // ABRIR/INSTALAR se recalcula al volver a la app (p. ej. después de instalar otro juego).
    val vida = LocalLifecycleOwner.current
    DisposableEffect(vida, lista) {
        val obs = LifecycleEventObserver { _, e ->
            if (e == Lifecycle.Event.ON_RESUME) instalados = lista.filter { puente.instalado(it.paquete) }.map { it.id }.toSet()
        }
        vida.lifecycle.addObserver(obs)
        onDispose { vida.lifecycle.removeObserver(obs) }
    }
    LaunchedEffect(lista, instalados) {
        lista.forEach { it -> puente.iconoLocal(it)?.let { b -> iconos[it.id] = b } }
        lista.filter { it.id !in iconos }.forEach { it -> puente.descargarIcono(it)?.let { b -> iconos[it.id] = b } }
    }

    val pager = rememberPagerState { lista.size }
    val actual = lista.getOrNull(pager.currentPage) ?: lista.firstOrNull() ?: return
    val propio = lista.first()
    val esPropio = actual.id == propioId
    val colorItem by animateColorAsState(Color(actual.color), tween(350), label = "fondo")
    val fondo = lerp(colorItem, Color.Black, 0.80f)
    val brillo = lerp(colorItem, Color.Black, 0.42f)
    val blanco = Color(0xFFFFFBF5)
    val tenue = blanco.copy(alpha = 0.62f)
    val estilo = { tam: Int, peso: FontWeight, color: Color -> TextStyle(fontFamily = fuenteTexto, fontSize = tam.sp, fontWeight = peso, color = color) }

    Box(
        modifier.fillMaxSize().background(fondo)
            .background(Brush.radialGradient(listOf(brillo.copy(alpha = 0.9f), Color.Transparent), center = Offset.Unspecified, radius = 1400f)),
    ) {
        Column(
            Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Barra superior: estrellas a la izquierda; idioma, calificar, compartir y ajustes a la derecha.
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Row(
                    Modifier.clip(CircleShape).background(Color.Black.copy(alpha = 0.28f)).padding(start = 6.dp, end = 12.dp, top = 5.dp, bottom = 5.dp)
                        .semantics { contentDescription = "$estrellas ${t.estrellas}" },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(24.dp).background(Color(0xFFF5C518), CircleShape), contentAlignment = Alignment.Center) {
                        Image(Iconos.estrella, null, Modifier.size(14.dp), colorFilter = ColorFilter.tint(Color(0xFF3A2600)))
                    }
                    Text(" " + NumberFormat.getInstance(LocalConfiguration.current.locales[0]).format(estrellas), style = estilo(16, FontWeight.ExtraBold, blanco))
                }
                Spacer(Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    onIdioma?.let { BotonIcono(Iconos.idioma, t.idioma, it) }
                    BotonIcono(Iconos.estrella, t.calificar, onCalificar ?: { puente.abrirTienda(ctx.packageName, "") })
                    BotonIcono(Iconos.compartir, t.compartir, onCompartir)
                    BotonIcono(Iconos.ajustes, t.ajustes, onAjustes)
                }
            }

            Spacer(Modifier.weight(1f))

            // Carrusel: el centrado grande y nítido; los vecinos más pequeños y desenfocados.
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val lado = min(maxWidth.value * 0.42f, 172f).dp
                HorizontalPager(
                    pager, Modifier.fillMaxWidth().height(lado + 28.dp),
                    pageSize = PageSize.Fixed(lado), pageSpacing = 14.dp,
                    contentPadding = PaddingValues(horizontal = (maxWidth - lado) / 2),
                    key = { lista[it].id },
                ) { i ->
                    val it = lista[i]
                    val d = min(1f, abs(pager.getOffsetDistanceInPages(i)))
                    Box(
                        Modifier.fillMaxSize()
                            .graphicsLayer { val s = 1f - 0.32f * d; scaleX = s; scaleY = s; alpha = 1f - 0.5f * d }
                            .then(if (Build.VERSION.SDK_INT >= 31 && d > 0.05f) Modifier.blur((5 * d).dp) else Modifier)
                            .clickable(enabled = i != pager.currentPage) { alcance.launch { pager.animateScrollToPage(i) } },
                        contentAlignment = Alignment.Center,
                    ) {
                        IconoItem(it, iconos[it.id], lado, fuenteTitulo)
                        if (it.esApp) {
                            Text(
                                t.app.uppercase(), Modifier.align(Alignment.BottomCenter).clip(CircleShape).background(blanco).padding(horizontal = 10.dp, vertical = 2.dp),
                                style = estilo(11, FontWeight.ExtraBold, Color(0xFF1A1A1A)),
                            )
                        }
                    }
                }
            }

            Text(
                actual.nombre, Modifier.padding(top = 14.dp), maxLines = 2, overflow = TextOverflow.Ellipsis,
                style = TextStyle(fontFamily = fuenteTitulo, fontSize = 30.sp, lineHeight = 33.sp, color = blanco, textAlign = TextAlign.Center),
            )
            Text(
                if (esPropio) subtitulo else if (actual.esApp) t.otraApp else t.otroJuego,
                Modifier.padding(top = 4.dp), style = estilo(15, FontWeight.Bold, tenue),
            )
            if (lista.size > 1) Puntos(lista.size, pager.currentPage, blanco, Modifier.padding(top = 12.dp))

            Spacer(Modifier.height(18.dp))
            val instalado = actual.id in instalados
            val (texto, icono) = when {
                esPropio -> t.jugar to Iconos.jugar
                instalado -> t.abrir to Iconos.abrir
                else -> t.instalar to Iconos.instalar
            }
            BotonGrande(texto, icono, acento, tintaAcento, fuenteTitulo) {
                when {
                    esPropio -> onJugar()
                    instalado -> { onPromo(actual, "open"); puente.abrir(actual.paquete) }
                    else -> { onPromo(actual, "install"); puente.abrirTienda(actual.paquete, Catalogo.referrer(propioId, actual.id)) }
                }
            }

            Box(Modifier.fillMaxWidth().widthIn(max = 360.dp).heightIn(min = 96.dp).padding(top = 12.dp), contentAlignment = Alignment.TopCenter) {
                if (esPropio) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(Modifier.widthIn(max = 360.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (onRegalo != null) {
                                Box(Modifier.weight(1f)) {
                                    BotonChico(
                                        if (regaloDisponible) t.regalo else t.reclamado, Iconos.regalo, Color(0xFFFFF4E6), Color(0xFF3A1D00),
                                        estilo, onRegalo,
                                    )
                                    if (diaRacha > 0) {
                                        Text(
                                            "${t.dia} $diaRacha", Modifier.align(Alignment.TopEnd).offset(x = 4.dp, y = (-9).dp)
                                                .clip(CircleShape).background(Color(0xFFE5484D)).padding(horizontal = 7.dp, vertical = 1.dp),
                                            style = estilo(11, FontWeight.ExtraBold, Color.White),
                                        )
                                    }
                                }
                            }
                            if (mostrarSinAnuncios) {
                                Box(Modifier.weight(1f)) {
                                    BotonChico(
                                        t.sinAnuncios + (precioSinAnuncios?.let { " · $it" } ?: ""), null, Color.Black.copy(alpha = 0.35f), blanco,
                                        estilo, onSinAnuncios, adTachado = true,
                                    )
                                }
                            }
                        }
                        if (onRegalo != null && diaRacha > 0) Racha(diaRacha, acento, blanco)
                        extras?.invoke(this)
                    }
                } else {
                    Text(
                        "← ${t.volver} ${propio.nombre}",
                        Modifier.clip(RoundedCornerShape(12.dp)).clickable { alcance.launch { pager.animateScrollToPage(0) } }.padding(horizontal = 14.dp, vertical = 10.dp),
                        style = estilo(15, FontWeight.ExtraBold, tenue),
                    )
                }
            }
            Spacer(Modifier.weight(1f))
        }
    }
}

/** Icono de Play del elemento; mientras no hay imagen, un cuadro de su color con las iniciales. */
@Composable
private fun IconoItem(item: ItemCatalogo, img: ImageBitmap?, lado: androidx.compose.ui.unit.Dp, fuente: FontFamily) {
    val forma = RoundedCornerShape(lado * 0.22f)
    Box(
        Modifier.size(lado).shadow(18.dp, forma, ambientColor = Color.Black, spotColor = Color.Black).clip(forma).background(Color(item.color)),
        contentAlignment = Alignment.Center,
    ) {
        if (img != null) {
            Image(img, item.nombre, Modifier.fillMaxSize())
        } else {
            val ini = item.nombre.split(' ', '&').filter { it.isNotBlank() }.take(2).joinToString("") { it.take(1) }.uppercase()
            Text(ini, style = TextStyle(fontFamily = fuente, fontSize = (lado.value * 0.3f).sp, color = Color.White))
        }
    }
}

@Composable
private fun BotonIcono(icono: ImageVector, descripcion: String, onClick: () -> Unit) {
    Box(
        Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(Color.Black.copy(alpha = 0.28f)).clickable(onClickLabel = descripcion, onClick = onClick)
            .semantics { contentDescription = descripcion },
        contentAlignment = Alignment.Center,
    ) { Image(icono, null, Modifier.size(20.dp), colorFilter = ColorFilter.tint(Color.White)) }
}

/** Botón principal con relieve (sombra sólida debajo), como el PLAY de la portada. */
@Composable
private fun BotonGrande(texto: String, icono: ImageVector, fondo: Color, tinta: Color, fuente: FontFamily, onClick: () -> Unit) {
    val hondo = lerp(fondo, Color.Black, 0.28f)
    Row(
        Modifier.widthIn(max = 360.dp).fillMaxWidth()
            .drawBehind { drawRoundRect(hondo, Offset(0f, 6.dp.toPx()), size, CornerRadius(22.dp.toPx())) }
            .clip(RoundedCornerShape(22.dp)).background(fondo).clickable(onClick = onClick).padding(vertical = 17.dp),
        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(icono, null, Modifier.size(22.dp), colorFilter = ColorFilter.tint(tinta))
        Spacer(Modifier.width(10.dp))
        Text(texto, maxLines = 1, style = TextStyle(fontFamily = fuente, fontSize = 22.sp, color = tinta))
    }
}

@Composable
private fun BotonChico(
    texto: String, icono: ImageVector?, fondo: Color, tinta: Color,
    estilo: (Int, FontWeight, Color) -> TextStyle, onClick: () -> Unit, adTachado: Boolean = false,
) {
    Row(
        Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(14.dp)).background(fondo).clickable(onClick = onClick).padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icono != null) {
            Image(icono, null, Modifier.size(17.dp), colorFilter = ColorFilter.tint(tinta))
            Spacer(Modifier.width(6.dp))
        }
        if (adTachado) {
            AdTachado()
            Spacer(Modifier.width(7.dp))
        }
        Text(texto, maxLines = 1, overflow = TextOverflow.Ellipsis, style = estilo(14, FontWeight.ExtraBold, tinta))
    }
}

/** El «AD» tachado del estudio (círculo rojo con raya): la marca de «Quitar anuncios». */
@Composable
private fun AdTachado() {
    val rojo = Color(0xFFE53935)
    Box(Modifier.size(24.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
        Text("AD", style = TextStyle(fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color(0xFF1F1F1F)))
        Canvas(Modifier.size(21.dp)) {
            val w = 2.dp.toPx(); val r = size.minDimension / 2 - w / 2; val d = r * .7071f
            drawCircle(rojo, r, style = Stroke(w))
            drawLine(rojo, Offset(center.x - d, center.y - d), Offset(center.x + d, center.y + d), w)
        }
    }
}

/** 7 rayas de la racha del regalo diario (las cumplidas, del color del acento). */
@Composable
private fun Racha(dia: Int, acento: Color, blanco: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        repeat(7) { i -> Box(Modifier.size(22.dp, 5.dp).clip(CircleShape).background(if (i < dia) acento else blanco.copy(alpha = 0.18f))) }
    }
}

@Composable
private fun Puntos(n: Int, sel: Int, color: Color, modifier: Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(n) { i -> Box(Modifier.size(if (i == sel) 16.dp else 6.dp, 6.dp).clip(CircleShape).background(color.copy(alpha = if (i == sel) 0.95f else 0.3f))) }
    }
}

/** Iconos de la portada (trazos SVG de 24×24), sin depender de librerías de iconos del juego. */
private object Iconos {
    private fun svg(nombre: String, d: String, trazo: Boolean = true) = ImageVector.Builder(nombre, 24.dp, 24.dp, 24f, 24f).apply {
        val nodos = PathParser().parsePathString(d).toNodes()
        if (trazo) addPath(nodos, stroke = SolidColor(Color.White), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
        else addPath(nodos, fill = SolidColor(Color.White))
    }.build()

    val idioma = svg("idioma", "M12 3a9 9 0 1 0 0 18a9 9 0 1 0 0-18M3 12h18M12 3c3 3 3 15 0 18M12 3c-3 3-3 15 0 18")
    val estrella = svg("estrella", "M12 3l2.7 5.6 6.1.9-4.4 4.3 1 6.1L12 17l-5.4 2.9 1-6.1L3.2 9.5l6.1-.9z", trazo = false)
    val compartir = svg("compartir", "M6 9.5a2.5 2.5 0 1 0 0 5a2.5 2.5 0 1 0 0-5M18 3.5a2.5 2.5 0 1 0 0 5a2.5 2.5 0 1 0 0-5M18 15.5a2.5 2.5 0 1 0 0 5a2.5 2.5 0 1 0 0-5M8.2 11l7.6-3.8M8.2 13l7.6 3.8")
    val ajustes = svg("ajustes", "M12 9a3 3 0 1 0 0 6a3 3 0 1 0 0-6M12 2v3M12 19v3M2 12h3M19 12h3M4.9 4.9l2.1 2.1M17 17l2.1 2.1M4.9 19.1L7 17M17 7l2.1-2.1")
    val jugar = svg("jugar", "M7 4l13 8-13 8z", trazo = false)
    val abrir = svg("abrir", "M14 4h6v6M20 4l-9 9M18 14v5a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h5")
    val instalar = svg("instalar", "M12 4v11M7 10l5 5 5-5M5 20h14")
    val regalo = svg("regalo", "M3 9h18v4H3zM5 13h14v8H5zM11 9h2v12h-2zM12 9C9 9 7 4 9.5 4S12 9 12 9zm0 0c3 0 5-5 2.5-5S12 9 12 9z", trazo = false)
}
