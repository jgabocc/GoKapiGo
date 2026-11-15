package com.gokapigo.home

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

/** Arreglo del 2026-11-15: lista de idiomas vacía en algunos teléfonos (cierre al abrir). */
class IdiomaTest {
    @Test fun listaVaciaUsaElDelSistema() {
        assertEquals(Locale.getDefault(), Idioma.elegir(vacia = true) { error("no se debe leer una lista vacía") })
    }

    @Test fun primeroNuloUsaElDelSistema() {
        assertEquals(Locale.getDefault(), Idioma.elegir(vacia = false) { null })
    }

    @Test fun conIdiomaUsaEse() {
        assertEquals(Locale("az", "AZ"), Idioma.elegir(vacia = false) { Locale("az", "AZ") })
    }
}
