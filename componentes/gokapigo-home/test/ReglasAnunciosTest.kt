package com.gokapigo.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Arreglo de anuncios del 2026-11-15: arrancar aunque falle el consentimiento fuera de Europa, reintentos y registro de fallos. */
class ReglasAnunciosTest {

    // ---------- arrancar los anuncios

    @Test fun conPermisoDeGoogleArrancaSiempre() {
        assertTrue(ReglasAnuncios.arrancar(puedePedir = true, forzar = false))
        assertTrue(ReglasAnuncios.arrancar(puedePedir = true, forzar = true))
    }

    @Test fun sinPermisoYSinForzarNoArranca() {
        assertFalse(ReglasAnuncios.arrancar(puedePedir = false, forzar = false))
    }

    /** El caso de Azerbaiyán: la consulta falla, Google dice «no», pero el país no exige consentimiento → arranca. */
    @Test fun falloDeConsentimientoEnAzerbaiyanArranca() {
        val forzar = ReglasAnuncios.forzarTrasFalloUmp("AZ")
        assertTrue(forzar)
        assertTrue(ReglasAnuncios.arrancar(puedePedir = false, forzar = forzar))
    }

    @Test fun falloDeConsentimientoFueraDeEuropaArranca() {
        for (p in listOf("AZ", "IR", "BR", "VE", "MX", "CO", "DO", "US", "RU", "TR", "IN", "CU", "JP", "KR")) {
            assertTrue(p, ReglasAnuncios.forzarTrasFalloUmp(p))
        }
    }

    /** En Europa, Reino Unido y Suiza NUNCA se fuerza: sin consentimiento no hay anuncios (lo exige Google). */
    @Test fun falloDeConsentimientoEnEuropaNoArranca() {
        for (p in ReglasAnuncios.PAISES_RGPD) {
            assertFalse(p, ReglasAnuncios.forzarTrasFalloUmp(p))
            assertFalse(p, ReglasAnuncios.arrancar(puedePedir = false, forzar = ReglasAnuncios.forzarTrasFalloUmp(p)))
        }
    }

    @Test fun paisDesconocidoNoSeFuerza() {
        for (p in listOf(null, "", "  ", "X", "AZE")) assertFalse("«$p»", ReglasAnuncios.fueraDeRgpd(p))
    }

    @Test fun elPaisSeLeeSinImportarMayusculasNiEspacios() {
        assertTrue(ReglasAnuncios.fueraDeRgpd("az"))
        assertTrue(ReglasAnuncios.fueraDeRgpd(" ve "))
        assertFalse(ReglasAnuncios.fueraDeRgpd("es"))
        assertFalse(ReglasAnuncios.fueraDeRgpd("gb"))
    }

    @Test fun zonaRgpdCompleta() {
        val ue = setOf("AT", "BE", "BG", "HR", "CY", "CZ", "DK", "EE", "FI", "FR", "DE", "GR", "HU", "IE", "IT", "LV", "LT", "LU",
            "MT", "NL", "PL", "PT", "RO", "SK", "SI", "ES", "SE")
        assertEquals(27, ue.size)
        assertEquals(ue + setOf("IS", "LI", "NO", "GB", "CH"), ReglasAnuncios.PAISES_RGPD)
    }

    // ---------- reintentos

    @Test fun reintentos30s1m2m5mYSigueCada5m() {
        val r = ReglasAnuncios.Reintentos()
        assertEquals(listOf(30_000L, 60_000L, 120_000L, 300_000L, 300_000L, 300_000L), List(6) { r.fallo("intersticial") })
        assertEquals(6, r.seguidos("intersticial"))
    }

    @Test fun alCargarSeVuelveAEmpezarPor30s() {
        val r = ReglasAnuncios.Reintentos()
        repeat(3) { r.fallo("banner") }
        r.exito("banner")
        assertEquals(0, r.seguidos("banner"))
        assertEquals(30_000L, r.fallo("banner"))
    }

    @Test fun cadaFormatoLlevaSuPropiaCuenta() {
        val r = ReglasAnuncios.Reintentos()
        repeat(3) { r.fallo("banner") }
        assertEquals(30_000L, r.fallo("video"))
        assertEquals(30_000L, r.fallo("intersticial"))
        assertEquals(300_000L, r.fallo("banner"))
    }

    @Test fun esperaNuncaSaleDeLaLista() {
        assertEquals(30_000L, ReglasAnuncios.espera(-5))
        assertEquals(300_000L, ReglasAnuncios.espera(1000))
    }

    // ---------- registro de fallos en Firebase

    @Test fun cadaFalloSeAnotaUnaVezPorSesion() {
        val a = ReglasAnuncios.Anotador()
        assertTrue(a.primeraVez("banner", 2))
        assertFalse(a.primeraVez("banner", 2))
        assertFalse(a.primeraVez("banner", 2))
    }

    @Test fun otroCodigoUOtroFormatoSiSeAnota() {
        val a = ReglasAnuncios.Anotador()
        assertTrue(a.primeraVez("banner", 2))
        assertTrue(a.primeraVez("banner", 3))
        assertTrue(a.primeraVez("video", 2))
        assertTrue(a.primeraVez("intersticial", 2))
        assertFalse(a.primeraVez("video", 2))
    }
}
