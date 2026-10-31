# GoKapiGo Home (nativo)

Portada común de todos los juegos del estudio, con promoción cruzada. Kotlin/Compose, paquete `com.gokapigo.home`,
**igual en todos los juegos**: esta carpeta es la copia de referencia. Si se cambia, se copia a todos los juegos.

Nació de la prueba de concepto web (`GoKapiGo_Home_v1_1.zip`), reescrita en nativo (ley del estudio: nada de WebView).

## Qué hace

- **Carrusel:** el juego propio al centro; a los lados asoman los demás juegos y apps **públicos** en Play, más pequeños
  y desenfocados. Al deslizar, el fondo toma el color del elemento centrado.
- **Botón principal:** JUGAR (propio) · ABRIR (otro instalado) · INSTALAR GRATIS (Play Store con referrer
  `utm_source=<origen>&utm_medium=cross_promo&utm_campaign=gokapigo_home&utm_content=<destino>`).
- Con el propio al centro: regalo diario (botón, nunca ventana) con la racha de 7 días y «Sin anuncios · precio» con el
  AD tachado. Con otro al centro: «← Volver a …».
- Barra superior: estrellas · idioma (opcional) · calificar (por defecto abre la ficha en Play) · compartir · ajustes.
- **Sin banner** en la portada (los anuncios van solo durante la partida).
- 7 idiomas (es, en, pt, fr, de, pl, ru) según el idioma de la app; si no, inglés.
- **Icono:** el propio sale del icono de la app (capas del icono adaptable, cuadrado, sin máscara) = el de Play.
  Los instalados, de su icono real; los demás, del PNG de Play en `capibaradev.com/iconos/<id>.png` (guardado 7 días).
  Ningún juego vuelve a dibujar su logo aparte.

## Catálogo

- Embebido en `Catalogo.EMBEBIDO` (respaldo sin internet) + remoto `https://www.capibaradev.com/catalog.json`
  (máx. una vez cada 12 h, 5 s de espera, se guarda).
- El remoto lo genera `herramientas/catalogo.py` del repo GoKapiGo desde `juegos.json` en cada publicación del sitio
  (y una vez al día): mira en Google Play qué juegos ya son públicos. **Un juego nuevo aprobado aparece solo** en el
  carrusel de todos los demás, sin actualizar nada.
- Para que salga ABRIR en vez de INSTALAR, su paquete debe estar en `<queries>` del manifiesto (va en la siguiente
  versión de cada juego).

## Integrar en un juego

1. Copiar `Catalogo.kt`, `GoKapiGoHome.kt`, `Puente.kt`, `Textos.kt` a `app/src/main/java/com/gokapigo/home/` y
   `test/CatalogoTest.kt` a `app/src/test/java/com/gokapigo/home/` (necesita `testImplementation("org.json:json:20240303")`).
   Requiere Compose foundation (pager), `androidx.lifecycle:lifecycle-runtime-compose` y `androidx.core:core-ktx`.
2. Manifiesto: `<uses-permission android:name="android.permission.INTERNET" />` y el bloque `<queries>` con todos los
   paquetes `com.gokapigo.*` del catálogo (ver Sand Block Puzzle).
3. Reemplazar el contenido de la pantalla de inicio por:

```kotlin
GoKapiGoHome(
    propioId = "sandblockpuzzle",                       // id del juego en el catálogo
    estrellas = m.p.wallet,
    subtitulo = tx(R.string.level, "n" to m.p.level),   // nivel o modo, ya traducido
    acento = c.accent, tintaAcento = c.accentInk,       // colores del juego
    fuenteTitulo = Russo, fuenteTexto = Nunito,         // fuentes del juego
    regaloDisponible = avail, diaRacha = …,             // 1..7
    onRegalo = { m.open(Sheet.Daily) },                 // la ventana de regalo diario que ya tiene el juego
    mostrarSinAnuncios = !m.p.noAds, precioSinAnuncios = m.price("remove_ads"), onSinAnuncios = { m.buyRemoveAds() },
    onIdioma = { m.open(Sheet.Languages) },             // null si el juego no tiene selector de idioma
    onJugar = { m.startLevel(m.p.level) },
    onAjustes = { m.open(Sheet.Settings) },
    onCompartir = { m.share() },
    onPromo = { item, accion -> analytics("cross_promo", item.id, accion) },
    extras = { /* botones propios del juego (modos, dificultad…), solo con el propio al centro */ },
)
```

4. Quitar el botón flotante «AD tachado» del inicio (ahora va dentro de «Sin anuncios») y cualquier logo dibujado a mano
   que se use como icono (inicio, imagen de compartir…): usar el icono de la app.
5. Probar: icono igual al de Play, JUGAR, regalo, Sin anuncios, idioma, ajustes, compartir; con otro juego del estudio
   instalado → ABRIR; sin instalar → INSTALAR GRATIS abre Play.
