# GoKapiGo: base para cada juego

Reglas fijas para todos los juegos del estudio. Cada juego nuevo cumple este archivo; si algo se sale de aquí, se decide a propósito y se anota en el README del juego.

Ponlo en la raíz del repositorio (o cópialo como `CLAUDE.md`) para que Claude Code lo siga en cada juego.

---

## 1. Filosofía
- **Directo a jugar.** El jugador abre la app y en un toque está jugando. Nada se interpone.
- **Cero ventanas emergentes al abrir.** Al arrancar no aparecen ofertas, noticias, "califícanos", "recompensa diaria" ni tutoriales largos.
- **No matar al usuario.** Los anuncios y las ofertas aparecen solo en momentos de pausa natural (entre niveles) y nunca en medio de una jugada.
- **Partidas cortas:** de 1 a 3 minutos por nivel, hechas para esperar el bus.
- **Se explica solo.** Sin textos de reglas: el primer nivel enseña jugando.

## 2. Nombre e identidad
- **Nombre siempre en inglés y el mismo en todos los países** (ej. *Aqua Sorting*).
- En la ficha de la tienda, el subtítulo sí se traduce (ej. "Aqua Sorting – Puzzle de colores").
- Estudio: **GoKapiGo** (antes "Capibara Games" / "KapiGames"). La mascota sigue siendo el capibara; solo cambió el nombre.
- Paquete de los juegos nuevos: `com.gokapigo.<nombreenminusculas>` (ej. `com.gokapigo.shapespin`). Los juegos ya publicados conservan su paquete (Aqua Sorting: `com.capibaragames.aquasorting`).
- Desarrollador en Play Store: **GoKapiGo** (cuenta de organización, D-U-N-S de Capibara Comercializadora C.A.).
- Icono propio por juego: fondo de color sólido o degradado y el objeto principal del juego, sin texto.

## 3. Idiomas
- **7 idiomas obligatorios:** español, inglés, portugués (Brasil), francés, alemán, polaco y ruso.
- **Detección automática** del idioma del teléfono (recursos de Android `res/values-xx`). Si no es ninguno de los 7, el juego sale en **inglés**.
- En Ajustes se puede cambiar a mano (Auto + los 7 idiomas), con el idioma por app de Android (AppCompat `setApplicationLocales`).
- Todos los textos van en `strings.xml` (uno por idioma); nunca texto fijo en el código.
- Las fuentes deben cubrir **latin, latin-ext (polaco) y cyrillic (ruso)**, incrustadas en la app.
- La ficha de Play Store (título, descripción corta, descripción larga y capturas) también va en los 7 idiomas (pt-BR y pt-PT para el portugués).
- Rusia: AdMob no sirve anuncios allá y Google Play no cobra. El ruso se mantiene igual para los rusohablantes de otros países.

## 4. Logo de entrada (splash)
- Al abrir cualquier juego aparece el logo de **GoKapiGo** centrado, como máximo al 60 % del ancho de la pantalla (360 dp en tabletas).
- Logo oficial: `D:\GOOGLE PLAY GAMES\Logos\GoKapiGo_Logo.png` (PNG con fondo transparente). Nunca se enlaza esa ruta desde el código: se copia a cada proyecto optimizado en WebP, 512 px (`res/drawable-xhdpi/gokapigo_logo.webp`) y 1024 px (`res/drawable-xxxhdpi/gokapigo_logo.webp`).
- Fondo **naranja oscuro `#3A1A05`**. No se usa blanco: el texto "KAPI" del logo es color crema y no se vería.
- Entra y sale con fundido, **unos 1,5 s en total** desde el arranque (entrada 0,3 s, salida 0,3 s), y pasa directo al inicio. No se toca para saltarlo, ni lleva sonido ni ventanas emergentes.
- Se muestra **solo en el arranque en frío**: al volver de segundo plano no vuelve a salir.
- Se implementa con la API SplashScreen solo para el fondo (`windowSplashScreenBackground` = `#3A1A05` e icono transparente, sin imagen de marca) y una capa nativa en `MainActivity` (Compose `BrandSplash`) con el logo completo y el fundido.
- **Todo juego nuevo usa este splash.**

## 5. Pantalla de inicio
- Contiene solo esto:
  1. el logo o ilustración del juego,
  2. el nombre,
  3. un botón grande: **"Jugar nivel N"**,
  4. un enlace pequeño a **Ajustes**,
  5. el botón **Compartir**, discreto.
- Además, mientras no se haya comprado "Quitar anuncios": un botón redondo arriba a la izquierda con el ícono "AD" tachado (círculo rojo con raya). Al tocarlo abre la compra de Google Play; desaparece al comprar. No se muestra con ventanas abiertas ni durante la partida.
- Nada más: sin menús, sin otras tiendas a la vista, sin banners en el inicio.
- **Siempre retoma donde quedó**, nunca empieza de cero.

## 6. Progreso y niveles
- El progreso se guarda en el dispositivo después de cada movimiento.
- **No se pueden saltar niveles** ni elegir un nivel más alto que el alcanzado.
- Los niveles se **generan automáticamente** a partir del número de nivel (siempre sale el mismo nivel) y se verifica que tengan solución.
- La dificultad sube por bloques (cada 10 niveles al principio y cada 100 después del 100), con algo nuevo de vez en cuando para que no se sienta igual.
- Al pasar un nivel se pasa al siguiente aunque cierres la app en la pantalla de victoria.
- El siguiente nivel se genera en segundo plano, sin espera entre niveles.

## 7. Pantalla de juego
- Arriba: nivel, saldo de ★, movimientos y botón de inicio.
- Abajo, máximo 3 botones de ayuda (ej. Deshacer, Reiniciar, Ayuda extra).
- Diseño cómodo con una mano: máximo **2 filas** de piezas; si hay más, se hacen más pequeñas.
- **Colores bien distintos entre sí**, elegidos midiendo la diferencia de color. Los colores parecidos no van en el mismo nivel.
- **Modo daltónico** con símbolos sobre cada color.
- Cualquier pieza se puede tocar aunque haya una animación corriendo (nada de esperas).
- Botón atrás del teléfono: cierra la ventana abierta → vuelve al inicio → minimiza la app.

## 8. Estrellas (moneda del juego)
- Cada nivel ganado da **1 a 3 ★**, según los movimientos frente a la meta que calcula el solucionador.
- Las ★ se acumulan en una billetera y se canjean por ayudas. Precios base (se ajustan por juego):
  - recargar ayudas tipo "deshacer": **50 ★**,
  - ayuda grande tipo "¼ de tubo extra": **100 ★**.
- La primera ayuda grande de cada nivel es gratis.
- Si no le alcanza, el botón dice cuántas ★ le faltan, y se ofrece "Ver video" como alternativa.

## 9. Monetización
### Anuncios (AdMob)
- **Intersticial (pantalla completa):** cada **2 niveles ganados**, solo entre niveles. **Nunca antes del nivel 5** ni en la primera sesión.
- **Video con recompensa:** siempre opcional, como alternativa a gastar ★ en una ayuda.
- **Banner adaptable abajo, solo durante la partida** (nunca en el inicio ni en las ventanas). Se carga después del consentimiento UMP y desaparece con "Quitar anuncios".
- **Sin internet se juega igual:** si un anuncio no carga, no se espera ni se bloquea nada. El intersticial se salta, el banner no deja hueco, y si el video no llega (o se cierra antes de tiempo) se avisa y la ventana de origen vuelve con sus otras opciones (★ o reintentar).
- IDs por juego en `app/build.gradle.kts`: ID de app, intersticial, bonificado y banner (los de prueba de Google hasta publicar).
- Consentimiento **GDPR con UMP** antes de cargar anuncios (obligatorio en Europa).

### Compras (Google Play Billing)
- **Quitar anuncios: $2.99, compra única. Precio fijo para todos los juegos.**
  - Quita los intersticiales y el banner. Los videos con recompensa siguen disponibles porque el jugador los elige.
  - Se restaura automáticamente con la misma cuenta de Google.
- **Paquetes de ★ (consumibles), opcionales:** 200★ $0.99 · 600★ $1.99 · 1500★ $3.99.
- El botón "Quitar anuncios" está en Ajustes y aparece una sola vez en la pantalla de victoria, justo después del primer intersticial. **Nunca como ventana al abrir.**

## 10. Compartir
- Botón **"Comparte <Nombre>"** en el inicio y en la pantalla de victoria cada 5 niveles.
- Usa el menú nativo de Android (WhatsApp, Telegram, Instagram, SMS…).
- El mensaje va traducido, incluye el nivel del jugador como reto ("Voy por el nivel {n}, ¿me pasas?") y el link de Play Store con UTM:
  `https://play.google.com/store/apps/details?id=<paquete>&referrer=utm_source%3Dshare`
- Lleva imagen: una captura del nivel actual con el logo.
- Premio: **+20 ★ la primera vez que comparte cada día** (máximo 1 vez al día).

## 11. Calificaciones (políticas de Google Play)
- Solo con la **Play In-App Review API**, el cuadro oficial de Google.
- **Prohibido:** pantalla propia de estrellas, preguntar "¿te gusta?" antes, mandar a Play solo a quien puntúa alto (filtrado de reseñas) y dar premios por calificar.
- Cuándo se pide: en la pantalla de victoria tras ganar con 3★, en los niveles **15, 40 y 100**. Máximo 3 veces en total, nunca en las primeras 24 h.
- En Ajustes:
  - "Califica <Nombre>": abre la ficha de Play Store.
  - Interruptor **"No pedirme calificar"** (opt-out).

## 12. Ajustes (siempre los mismos)
Modo daltónico · Sonido · Vibración · Fondo · Idioma · Quitar anuncios · Califícanos · No pedirme calificar · Política de privacidad.

## 13. Técnica
- **Juego nativo en Android: Kotlin + Jetpack Compose.** Nada de HTML ni WebView en el juego (es ley del estudio).
  Única excepción: el anuncio jugable (sección 15), porque las redes de anuncios solo aceptan HTML5.
  (Aqua Sorting quedó publicado en HTML/WebView y no se migra.)
- minSdk 24, targetSdk el que exija Play, solo vertical. Funciona **sin internet**.
- Servicios comunes para todos los juegos (mismas clases): `AdsManager` (UMP + AdMob), `BillingManager` (Play Billing),
  `PlayGamesManager` (Play Juegos), `Analytics` (Firebase), `Share` / `ShareReceiver` (compartir con UTM por app).
- La lógica del juego habla con Android a través de una interfaz `Platform` (anuncios, compras, compartir, reseña, sonido, vibración).
- Botón atrás: ventana abierta → inicio → minimizar la app (`moveTaskToBack`).
- Se entrega **.aab firmado** para Play Store y **APK de prueba**.
- Cada juego tiene su propia llave de firma, guardada fuera del repositorio y con respaldo.
- Versión: `versionCode` sube en cada entrega.

## 13b. Privacidad, actualizaciones y publicación
- **Política de privacidad común** para todos los juegos (válida en EE. UU., Europa y América Latina, 7 idiomas), en el repo del estudio `jgabocc/GoKapiGo`, publicada con GitHub Pages por GitHub Actions: `https://jgabocc.github.io/GoKapiGo/privacy/` (elige el idioma del teléfono). Es el `PRIVACY_URL` de cada juego y la URL que va en Play Console. Contacto: `ayuda@capibaradev.com`.
- **Actualizaciones dentro de la app** (Play In-App Updates): si hay versión nueva, en el inicio aparece "Actualización disponible"; al tocarla se descarga en segundo plano y luego "Reiniciar para actualizar". Nunca como ventana al abrir. Si la versión se publica con **prioridad 5**, la actualización es obligatoria (pantalla de Google).
- **Un repo por juego** en GitHub. GitHub Actions compila y prueba cada push (`CI`) y, al crear un tag `vX.Y.Z`, compila el `.aab` firmado y lo sube a la **prueba interna** de Play (`Publicar en Google Play`). `versionCode` = número de commits (siempre sube); `versionName` = el tag.
- Secrets de cada repo: `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`, `PLAY_SERVICE_ACCOUNT_JSON` y, cuando exista, `GOOGLE_SERVICES_JSON`.

## 14. Medición (Firebase Analytics)
Eventos mínimos: `level_start`, `level_win` (nivel, movimientos, estrellas), `level_restart`, `help_used`, `stars_spent`, `ad_shown`, `ad_rewarded`, `purchase`, `share` (canal), `review_asked`.
Métricas que se miran cada semana: retención día 1 (meta ≥ 35 %), día 7, nivel promedio alcanzado, ingreso por usuario por día.

## 15. Anuncio jugable (playable ad)
Cada juego trae su **anuncio jugable**: una mini versión del juego, con **la misma vista, colores y animaciones**, que se usa como anuncio en las campañas (Google Ads, AppLovin, Unity, Meta, etc.).
- **Un solo archivo HTML** con todo dentro (JS, CSS, imágenes en base64, sonidos). Sin pedir nada a internet y **menos de 5 MB** (mejor si pesa menos de 2 MB).
- Como el juego es nativo, el jugable es una versión HTML con **el mismo diseño, colores, animaciones y reglas** (se parte del prototipo HTML del juego cuando lo hay).
- **Duración: 15–30 s.** 1 o 2 niveles fáciles y fijos (no generados), pensados para ganarse rápido y sentirse bien.
- **Primeros 3 s:** una mano animada muestra qué tocar (sin texto).
- **Al terminar** (gane o se agote el tiempo): pantalla final con logo y nombre del juego, "¡Nivel completado!", botón grande **"Instalar"/"Jugar gratis"** y, junto a él, el logo pequeño de **GoKapiGo**.
- Si a los 10 s el usuario no ha tocado nada, el juego hace una jugada solo, para que el anuncio nunca se quede quieto.
- **Botón de instalar según la red:**
  - Google Ads: `ExitApi.exit()`.
  - Redes con MRAID (AppLovin, Unity, ironSource): `mraid.open(urlDePlayStore)`.
  - Se arma una versión del archivo por red, cambiando solo esa línea.
- Funciona en vertical **y** horizontal y se adapta a cualquier tamaño de pantalla.
- **Idioma del teléfono**, con los mismos 7 idiomas del juego. Muy poco texto: el anuncio debe entenderse sin leer.
- Sonido apagado hasta el primer toque (los navegadores lo exigen).
- Se guarda en `/playable/<red>/index.html` dentro del repo del juego.
- Antes de subirlo, se revisa con la herramienta de vista previa de cada red. Los límites de tamaño y de formato cambian con el tiempo, así que se confirman en la documentación de cada red antes de cada campaña.

## 16. Checklist antes de publicar
- [ ] Logo de entrada de GoKapiGo (~1,5 s con fundido), y luego directo al inicio sin ventanas emergentes.
- [ ] Los 7 idiomas se ven bien (textos que no se cortan, fuentes con ñ, ã, ç, ś, ż, cirílico).
- [ ] Retoma la partida tras cerrar y reabrir.
- [ ] Botón atrás correcto en cada pantalla.
- [ ] Intersticial cada 2 niveles y nunca antes del nivel 5; banner solo en la partida; videos y compras con IDs de prueba verificados.
- [ ] En modo avión se puede jugar todo: nada se queda esperando un anuncio.
- [ ] "Quitar anuncios" a $2.99 funciona y se restaura.
- [ ] Compartir abre el menú y el link lleva a la ficha.
- [ ] La calificación cumple la sección 11.
- [ ] Política de privacidad común enlazada en Ajustes y en Play Console: `https://jgabocc.github.io/GoKapiGo/privacy/`.
- [ ] Ficha de Play Store en los 7 idiomas, con capturas e icono de 512 px.
- [ ] Anuncio jugable listo (sección 15): un archivo por red, menos de 5 MB, probado en la vista previa de cada red.