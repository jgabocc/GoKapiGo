# 15 idiomas en cada juego (dueño 2026-11-12)

De 7 a 15 idiomas, dentro del juego **y** en la ficha de Play. Piloto hecho en **maze-quest** (mírenlo como ejemplo).

| Idioma | Carpeta Android | localeFilters | Selector (clave → nombre) | Ficha de Play |
|---|---|---|---|---|
| Turco | `values-tr` | `tr` | `"tr" to "Türkçe"` | `tr-TR` |
| Indonesio | `values-in` (código viejo de Android) | `in` | `"in" to "Bahasa Indonesia"` | `id` |
| Vietnamita | `values-vi` | `vi` | `"vi" to "Tiếng Việt"` | `vi` |
| Tailandés | `values-th` | `th` | `"th" to "ไทย"` | `th` |
| Hindi | `values-hi` | `hi` | `"hi" to "हिन्दी"` | `hi-IN` |
| Japonés | `values-ja` | `ja` | `"ja" to "日本語"` | `ja-JP` |
| Coreano | `values-ko` | `ko` | `"ko" to "한국어"` | `ko-KR` |
| Chino simplificado | `values-zh-rCN` | `zh-rCN` | `"zh-CN" to "简体中文"` | `zh-CN` |

Los códigos de Play están comprobados en la ayuda de Play Console (lista de idiomas). `play.py` rechaza otra carpeta.
Indonesio: en el emulador `cmd locale set-app-locales <paquete> --locales id` (o `in`) sirve `values-in`; probado.

## Pasos por juego (≈ 1,5–3 h de agente según cuántos textos tenga)

1. **Preparar** (desde `D:\GOOGLE PLAY GAMES`):
   `python GoKapiGo/herramientas/idiomas.py todo <juego>`
   Hace: `filtros` (agrega los 8 códigos a `localeFilters`), `selector` (agrega los 8 al mapa que tenga
   `"ru" to "Русский"`), `home` (copia `componentes/gokapigo-home/*.kt` canónicos, solo los que el juego ya tiene),
   `fuentes`, `estado` y `validar`. Si dice «agrégalo a mano», hacerlo igual que en la tabla.
2. **Traducir** cada archivo de `values/` que tenga textos (lo lista `estado`) a las 8 carpetas. Mismas claves, mismo
   orden; marcadores (`{n}`, `{name}`, `%1$s`…) intactos; `\'` para apóstrofos (el turco los usa: `3 ★\'dan`).
   No se traduce lo que ningún idioma viejo traduce (p. ej. `app_name`: el nombre es igual en todos los países).
   Si el juego genera sus XML con un script (`textos.py`, `textos_chars.py`…), extender el script (ver
   `maze-quest/herramientas/textos_15.py` y `textos_chars.py`). Los textos del ×10: `python textos_x10.py <juego>`.
3. **Calidad:** natural y de juego, no literal. Usar el glosario de abajo para que los 17 juegos digan lo mismo.
   Registro: tailandés cortés-casual, coreano 해요체, japonés de juego (です/ます en avisos, frases cortas en botones),
   chino conciso, hindi con «आप» (los mensajes para compartir, con «तुम»), indonesio con «kamu», turco con «sen»,
   vietnamita con «bạn». Ojo con el orden de palabras: «Nivel {n}» es «{n}단계», «レベル{n}», «第{n}关», «{n}. bölüm».
4. **Validar** hasta que no haya errores: `python GoKapiGo/herramientas/idiomas.py validar <juego>`
   (XML, claves, marcadores, elementos de los arrays, apóstrofos/comillas, y la ficha 30/80/4000 y capturas).
5. **Fuentes (vietnamita).** Las fuentes de los juegos (Rubik, Nunito, Russo, Fredoka…) no traen ạ ơ ư ế…: Android las
   saca de Roboto letra por letra y la palabra sale con dos fuentes. Arreglo: pasar las fuentes propias por
   `com.gokapigo.home.Textos.fuente(fuente, idioma)` (en vietnamita devuelve la del sistema). En maze-quest:
   ```kotlin
   fun bodyStyle(...) = TextStyle(fontFamily = Textos.fuente(Nunito, LocalConfiguration.current.locales[0].toLanguageTag()), ...)
   ```
   y lo mismo en el estilo de títulos; si algo se dibuja en Canvas con `ResourcesCompat.getFont`, usar
   `Typeface.DEFAULT_BOLD` cuando `Textos.codigo(...) == "vi"` (ver `ShareImage.kt`). `idiomas.py fuentes` dice si hace falta.
   Tailandés, hindi, japonés, coreano y chino no necesitan nada: salen enteros de Noto del sistema, sin cajitas.
   Tercer parámetro `sinLetras` = idiomas que le faltan a esa fuente: por defecto `setOf("vi")`; Baloo 2 (Aqua) tampoco
   tiene turco → `setOf("vi", "tr")`; una Nunito completa (Parchís, Aqua) → `emptySet()`. `idiomas.py fuentes` dice cuál.
   Rubik, Nunito recortada y Russo One sí tienen turco.
   **Mayúsculas:** `uppercase()` de Kotlin no sabe turco («Müthiş» → «MÜTHIŞ», mal). Buscar `uppercase()` en etiquetas y
   cambiarlo por `uppercase(LocalConfiguration.current.locales[0])` («MÜTHİŞ»); la portada común ya lo hace.
6. **Selector de idiomas:** 16 filas (Automático + 15). Debe tener scroll (en maze-quest `SheetBox` ya lo tiene).
7. **Compilar y probar** en el emulador cada idioma nuevo: portada, partida, victoria con ×10, tienda, ajustes,
   selector, colección/extra del juego. Buscar cajitas (tofu), textos cortados y botones de una línea que no caben.
   `adb shell cmd locale set-app-locales <paquete> --locales th` (y ko, ja, zh-CN, hi, id, tr, vi; "" para volver).
8. **Ficha de Play** en `store-listing/<código de Play>/`: `title.txt` (≤30), `short_description.txt` (≤80),
   `full_description.txt` (≤4000), `images/phoneScreenshots/1..N.png` en ese idioma (extender `LOCS` y los textos que
   busca `capturas.py`; usar `ADB_SERIAL` si hay varios emuladores), `images/featureGraphic.png` (si la imagen lleva texto,
   traducirlo; para tailandés/CJK/hindi/vietnamita dibujar con una fuente que tenga esas letras, ver
   `maze-quest/herramientas/destacada.py`) e `images/icon.png`. Título: «Nombre: subtítulo» como los demás, con una
   palabra clave de búsqueda del idioma (迷路パズル, 미로 퍼즐, Labirent Bulmaca…). Cambiar en los 7 idiomas viejos la línea
   «Disponible en español, inglés…» por «Disponible en 15 idiomas».
9. **Notas de versión** (`salida/notas-<ver>.txt`): desde ahora con las 17 etiquetas (`<th>…</th>`, `<id>…</id>`…).
   `play.py` sube solo las de idiomas que tenga la ficha y avisa si falta alguna.
10. Commit y push del juego (sin llaves ni `salida/`). No subir a Play sin orden del dueño.

## Glosario (usar estas palabras en todos los juegos)

| es | th | ko | ja | zh | hi | id | tr | vi |
|---|---|---|---|---|---|---|---|---|
| Jugar | เล่น | 플레이 | プレイ | 开始游戏 | खेलें | Main | Oyna | Chơi |
| Nivel {n} | ด่าน {n} | {n}단계 | レベル{n} | 第{n}关 | लेवल {n} | Level {n} | Bölüm {n} | Màn {n} |
| estrellas ★ | ดาว | 별 | スター | 星星 | सितारे | bintang | yıldız | sao |
| Pista | คำใบ้ | 힌트 | ヒント | 提示 | संकेत | Petunjuk | İpucu | Gợi ý |
| Reiniciar | เริ่มใหม่ | 다시 시작 | やり直す | 重来 | फिर से शुरू | Ulangi | Yeniden başla | Chơi lại |
| Deshacer | ย้อนกลับ | 되돌리기 | 戻す | 撤销 | पिछली चाल वापस | Urungkan | Geri al | Hoàn tác |
| Regalo diario | ของขวัญรายวัน | 일일 선물 | デイリーギフト | 每日礼物 | रोज़ का तोहफ़ा | Hadiah harian | Günlük hediye | Quà hằng ngày |
| Racha de {d} días | ต่อเนื่อง {d} วัน | {d}일 연속 | {d}日連続 | 连续{d}天 | {d} दिन की स्ट्रीक | {d} hari beruntun | {d} günlük seri | Chuỗi {d} ngày |
| Ver video | ดูวิดีโอ | 동영상 보기 | 動画を見る | 看视频 | वीडियो देखें | Tonton video | Video izle | Xem video |
| Quitar anuncios | ลบโฆษณา | 광고 제거 | 広告を削除 | 移除广告 | विज्ञापन हटाएँ | Hapus iklan | Reklamları kaldır | Gỡ quảng cáo |
| Paquetes de estrellas | แพ็กดาว | 별 패키지 | スターパック | 星星礼包 | सितारों के पैक | Paket bintang | Yıldız paketleri | Gói sao |
| Ajustes | ตั้งค่า | 설정 | 設定 | 设置 | सेटिंग्स | Pengaturan | Ayarlar | Cài đặt |
| Idioma / Automático | ภาษา / อัตโนมัติ | 언어 / 자동 | 言語 / 自動 | 语言 / 自动 | भाषा / अपने आप | Bahasa / Otomatis | Dil / Otomatik | Ngôn ngữ / Tự động |
| Sonido / Vibración | เสียง / การสั่น | 소리 / 진동 | サウンド / バイブレーション | 声音 / 振动 | आवाज़ / वाइब्रेशन | Suara / Getar | Ses / Titreşim | Âm thanh / Rung |
| Modo daltónico | โหมดตาบอดสี | 색약 모드 | 色覚サポートモード | 色盲模式 | कलरब्लाइंड मोड | Mode buta warna | Renk körlüğü modu | Chế độ mù màu |
| Política de privacidad | นโยบายความเป็นส่วนตัว | 개인정보처리방침 | プライバシーポリシー | 隐私政策 | निजता नीति | Kebijakan privasi | Gizlilik politikası | Chính sách quyền riêng tư |
| Cerrar | ปิด | 닫기 | 閉じる | 关闭 | बंद करें | Tutup | Kapat | Đóng |
| Volver al inicio | กลับหน้าแรก | 홈으로 돌아가기 | ホームに戻る | 返回首页 | होम पर वापस जाएँ | Kembali ke beranda | Ana sayfaya dön | Về trang chủ |
| ¡Nivel completado! | ผ่านด่านแล้ว! | 단계 클리어! | レベルクリア！ | 过关！ | लेवल पूरा! | Level selesai! | Bölüm tamam! | Qua màn! |
| movimientos | ก้าว | 이동 | 手数 | 步数 | चालें | langkah | hamle | bước |
| Gratis | ฟรี | 무료 | 無料 | 免费 | मुफ़्त | Gratis | Ücretsiz | Miễn phí |
| Personajes | ตัวละคร | 캐릭터 | キャラクター | 角色 | किरदार | Karakter | Karakterler | Nhân vật |
| Google Play Juegos | Google Play Games | Google Play 게임즈 | Google Play ゲーム | Google Play 游戏 | Google Play Games | Google Play Game | Google Play Games | Google Play Trò chơi |

## Qué no hace la herramienta

No traduce (eso lo escribe el agente, con calidad nativa) y no sube nada a Play.
