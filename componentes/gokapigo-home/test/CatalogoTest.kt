package com.gokapigo.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogoTest {

    @Test fun embebidoValidoYSoloDelEstudio() {
        assertTrue(Catalogo.EMBEBIDO.all { it.paquete == "com.gokapigo.${it.id}" && it.icono!!.startsWith("https://") })
        assertEquals(Catalogo.EMBEBIDO.size, Catalogo.EMBEBIDO.map { it.id }.toSet().size)
    }

    @Test fun idaYVueltaJson() {
        val vuelta = Catalogo.desdeJson(Catalogo.aJson(Catalogo.EMBEBIDO))
        assertEquals(Catalogo.EMBEBIDO, vuelta)
    }

    @Test fun jsonMaloOAjenoSeRechaza() {
        assertNull(Catalogo.desdeJson("no es json"))
        assertNull(Catalogo.desdeJson("""{"items":[]}"""))
        assertNull(Catalogo.desdeJson("""{"items":[{"id":"x","name":"X","package":"com.otro.x"}]}"""))
        val ok = Catalogo.desdeJson("""{"items":[{"id":"x","name":"X","package":"com.gokapigo.x","color":"#112233"}]}""")
        assertNotNull(ok)
        assertEquals(0xFF112233, ok!![0].color)
        assertTrue(ok[0].publica)   // sin "public" se asume pública
    }

    @Test fun carruselPropioPrimeroYSoloPublicas() {
        val l = Catalogo.carrusel(Catalogo.EMBEBIDO, "mazequest")
        assertEquals("mazequest", l.first().id)          // el propio sale aunque aún no sea público
        assertTrue(l.drop(1).all { it.publica })
        assertTrue(l.drop(1).none { it.id == "mazequest" })
        assertTrue(Catalogo.carrusel(Catalogo.EMBEBIDO.map { it.copy(publica = true) }, "sandblockpuzzle", incluirApps = false).none { it.esApp })
    }

    @Test fun referrerYColores() {
        assertEquals("utm_source=a&utm_medium=cross_promo&utm_campaign=gokapigo_home&utm_content=b", Catalogo.referrer("a", "b"))
        assertEquals(0xFF5C7CFF, Catalogo.colorDe("raro"))
    }

    @Test fun textosEnLos7Idiomas() {
        assertEquals("JUGAR", Textos.de("es-VE").jugar)
        assertEquals("JOGAR", Textos.de("pt-BR").jugar)
        assertEquals("PLAY", Textos.de("ja").jugar)
        listOf("es", "en", "pt", "fr", "de", "pl", "ru").forEach { assertTrue(Textos.de(it).instalar.isNotBlank()) }
    }
}
