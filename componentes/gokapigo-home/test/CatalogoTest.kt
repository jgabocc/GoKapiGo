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

    @Test fun textosEnLos15Idiomas() {
        assertEquals("JUGAR", Textos.de("es-VE").jugar)
        assertEquals("JOGAR", Textos.de("pt-BR").jugar)
        assertEquals("プレイ", Textos.de("ja-JP").jugar)
        assertEquals("PLAY", Textos.de("sv").jugar)                     // idioma sin traducir → inglés
        // indonesio con el código viejo de Android y el nuevo; chino: simplificado sí, tradicional va en inglés (sin recursos)
        assertEquals("MAIN", Textos.de("in").jugar); assertEquals("MAIN", Textos.de("id-ID").jugar)
        assertEquals("开始游戏", Textos.de("zh-CN").jugar); assertEquals("开始游戏", Textos.de("zh-Hans-CN").jugar)
        assertEquals("PLAY", Textos.de("zh-TW").jugar); assertEquals("PLAY", Textos.de("zh-Hant-HK").jugar)
        // orden de palabras: plantillas {x} y {n}
        assertEquals("Volver a Maze Quest", Textos.de("es").volverA("Maze Quest"))
        assertEquals("Maze Questに戻る", Textos.de("ja").volverA("Maze Quest"))
        assertEquals("3日目", Textos.de("ja").diaN(3)); assertEquals("第3天", Textos.de("zh-CN").diaN(3)); assertEquals("Día 3", Textos.de("es").diaN(3))
        listOf("es", "en", "pt", "fr", "de", "pl", "ru", "th", "ko", "ja", "zh", "hi", "id", "tr", "vi").forEach {
            Textos.de(it).run { assertTrue(it, instalar.isNotBlank() && tambien.isNotBlank() && gratis.isNotBlank() && desliza.isNotBlank()) }
        }
    }
}
