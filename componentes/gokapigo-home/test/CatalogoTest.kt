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

    @Test fun recomendadoNoInstaladoSoloJuegosYRota() {
        val l = Catalogo.carrusel(Catalogo.EMBEBIDO.map { it.copy(publica = true) }, "mazequest")
        val juegos = l.drop(1).filter { !it.esApp }.map { it.id }
        assertEquals(juegos[0], Catalogo.recomendado(l, emptySet(), 0)!!.id)
        assertEquals(juegos[1], Catalogo.recomendado(l, emptySet(), 1)!!.id)
        assertEquals(juegos[0], Catalogo.recomendado(l, emptySet(), juegos.size)!!.id)   // da la vuelta
        assertEquals(juegos[1], Catalogo.recomendado(l, setOf(juegos[0]), 0)!!.id)       // el instalado no sale
        assertNull(Catalogo.recomendado(l, juegos.toSet(), 3))                            // los tiene todos: sin ribbon
        assertNull(Catalogo.recomendado(l.take(1), emptySet(), 0))                        // catálogo solo con el propio
        assertNull(Catalogo.recomendado(emptyList(), emptySet(), 5))
        assertTrue((0..40).all { Catalogo.recomendado(l, emptySet(), it)!!.id != "mazequest" })
    }

    @Test fun referrerYColores() {
        assertEquals("utm_source=a&utm_medium=cross_promo&utm_campaign=gokapigo_home&utm_content=b", Catalogo.referrer("a", "b"))
        assertEquals("utm_source=a&utm_medium=cross_promo&utm_campaign=gokapigo_ribbon&utm_content=b", Catalogo.referrer("a", "b", "gokapigo_ribbon"))
        assertEquals(0xFF5C7CFF, Catalogo.colorDe("raro"))
    }

    @Test fun textosEnLos7Idiomas() {
        assertEquals("JUGAR", Textos.de("es-VE").jugar)
        assertEquals("JOGAR", Textos.de("pt-BR").jugar)
        assertEquals("PLAY", Textos.de("ja").jugar)
        listOf("es", "en", "pt", "fr", "de", "pl", "ru").forEach { Textos.de(it).run { assertTrue(instalar.isNotBlank() && tambien.isNotBlank() && gratis.isNotBlank()) } }
    }
}
