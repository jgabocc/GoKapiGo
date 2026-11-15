package com.gokapigo.home

/**
 * Reglas de cuándo y cómo se piden los anuncios (dueño 2026-11-15), sin nada de Android para poder probarlas.
 * Mismo archivo en todos los juegos GoKapiGo; lo usa el AdsManager de cada uno.
 *
 * Por qué existe: si la consulta de consentimiento de Google (UMP) fallaba (la red bloquea ese servidor, mala conexión),
 * canRequestAds() quedaba en falso y el juego no pedía NINGÚN anuncio en toda la sesión. En Azerbaiyán: 0 pedidos en
 * AdMob con jugadores de 45 min al día. Y un anuncio que fallaba no se volvía a pedir hasta salir y volver a la app.
 */
object ReglasAnuncios {
    /** Donde Google exige consentimiento antes de mostrar anuncios: UE (27) + EEE (Islandia, Liechtenstein, Noruega) + Reino Unido + Suiza. */
    val PAISES_RGPD = setOf(
        "AT", "BE", "BG", "HR", "CY", "CZ", "DK", "EE", "FI", "FR", "DE", "GR", "HU", "IE", "IT", "LV", "LT", "LU",
        "MT", "NL", "PL", "PT", "RO", "SK", "SI", "ES", "SE", "IS", "LI", "NO", "GB", "CH",
    )

    /** Esperas antes de volver a pedir un anuncio que falló: 30 s, 1, 2 y 5 min (y sigue cada 5 min). */
    val ESPERAS_MS = longArrayOf(30_000, 60_000, 120_000, 300_000)

    /** País conocido (código ISO de red, SIM o idioma) y fuera de la zona donde se exige consentimiento. País vacío = no se sabe = no. */
    fun fueraDeRgpd(pais: String?): Boolean {
        val p = pais?.trim()?.uppercase().orEmpty()
        return p.length == 2 && p !in PAISES_RGPD
    }

    /** ¿Se pueden pedir anuncios? Sí si Google lo permite, o si se forzó tras fallar la consulta (solo fuera de Europa). */
    fun arrancar(puedePedir: Boolean, forzar: Boolean): Boolean = puedePedir || forzar

    /** La consulta de consentimiento falló: se fuerza el arranque solo si el jugador está fuera de la zona RGPD. */
    fun forzarTrasFalloUmp(pais: String?): Boolean = fueraDeRgpd(pais)

    /** Espera del intento número [intento] (0 = primer fallo). */
    fun espera(intento: Int): Long = ESPERAS_MS[intento.coerceIn(0, ESPERAS_MS.size - 1)]

    /** Cuenta los fallos seguidos de cada formato (banner, intersticial, video) para espaciar los reintentos. */
    class Reintentos {
        private val seguidos = mutableMapOf<String, Int>()
        /** Falló: devuelve cuánto esperar antes de volver a pedirlo. */
        fun fallo(formato: String): Long {
            val k = seguidos.getOrDefault(formato, 0)
            seguidos[formato] = k + 1
            return espera(k)
        }
        /** Cargó: la próxima vez que falle se empieza otra vez por 30 s. */
        fun exito(formato: String) { seguidos.remove(formato) }
        fun seguidos(formato: String) = seguidos.getOrDefault(formato, 0)
    }

    /** Para no llenar Firebase: cada fallo (formato + código) se anota una sola vez por sesión. */
    class Anotador {
        private val vistos = mutableSetOf<String>()
        fun primeraVez(formato: String, codigo: Int): Boolean = vistos.add("$formato:$codigo")
    }
}
