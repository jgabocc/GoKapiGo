package com.gokapigo.home

import java.util.Locale

/**
 * Idioma del teléfono sin cierres (2026-11-15). Algunos teléfonos devuelven la lista de idiomas VACÍA y
 * `locales[0]` da null → NullPointerException al abrir el juego (Play Vitals: Aqua, 10 cierres de un jugador).
 * Todos los juegos GoKapiGo leen el idioma con [de]; si no hay ninguno, se usa el del sistema.
 */
object Idioma {
    /** Primer idioma de la lista o, si viene vacía o nula, el del sistema. */
    @JvmStatic
    fun de(lista: android.os.LocaleList?): Locale = elegir(lista == null || lista.isEmpty) { lista!!.get(0) }

    /** La regla, sin Android (para los unit tests): vacía → sistema; primero nulo → sistema; si no, el primero. */
    @JvmStatic
    fun elegir(vacia: Boolean, primero: () -> Locale?): Locale = (if (vacia) null else primero()) ?: Locale.getDefault()
}
