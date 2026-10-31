package com.gokapigo.home

import org.json.JSONArray
import org.json.JSONObject

/*
 * GoKapiGo Home: portada común de todos los juegos del estudio, con promoción cruzada.
 * Este paquete (com.gokapigo.home) es IGUAL en todos los juegos: la copia de referencia vive en el repo GoKapiGo
 * (componentes/gokapigo-home). Si se cambia aquí, se cambia allí y en los demás juegos.
 */

/** Un juego o app del catálogo. [publica] = ya se ve en Google Play (las demás no salen en el carrusel). */
data class ItemCatalogo(
    val id: String,
    val tipo: String,          // "game" o "app"
    val nombre: String,
    val paquete: String,
    val color: Long,           // 0xFFRRGGBB
    val icono: String?,        // URL del PNG del icono de Play (se descarga y se guarda)
    val publica: Boolean,
) {
    val esApp get() = tipo == "app"
}

object Catalogo {

    /** El catálogo vivo: https://www.capibaradev.com/catalog.json (lo publica el repo GoKapiGo, carpeta landing/). */
    const val URL = "https://www.capibaradev.com/catalog.json"
    private const val ICONOS = "https://www.capibaradev.com/iconos/"

    private fun item(id: String, tipo: String, nombre: String, color: Long, publica: Boolean) =
        ItemCatalogo(id, tipo, nombre, "com.gokapigo.$id", color, "$ICONOS$id.png", publica)

    /** Respaldo sin internet (el primer arranque). El remoto lo reemplaza en cuanto responde. */
    val EMBEBIDO: List<ItemCatalogo> = listOf(
        item("sandblockpuzzle", "game", "Sand Block Puzzle", 0xFFE0A83A, true),
        item("arrowescape", "game", "Arrow Escape", 0xFF3A6FE0, false),
        item("aquasorting", "game", "Aqua Sorting", 0xFF38B6FF, false),
        item("nutsboltssort", "game", "Nuts & Bolts Sort", 0xFFE5484D, false),
        item("colorblockslide", "game", "Color Block Slide", 0xFF2E9E5B, false),
        item("sudokumaster3000", "game", "Sudoku Master 3000", 0xFF7A8CC4, false),
        item("shapespin", "game", "Shape Spin", 0xFF5C7CFF, false),
        item("parchisparty", "game", "Parchis Party", 0xFF2E9E5B, false),
        item("pipeconnect", "game", "Pipe Connect", 0xFF3478F6, false),
        item("onestroke", "game", "One Stroke", 0xFFF4B544, false),
        item("mazequest", "game", "Maze Quest", 0xFF2E9E5B, false),
        item("shapetiles", "game", "Shape Tiles", 0xFF4A5BD8, false),
        item("solitaireclassic", "game", "Solitaire Classic", 0xFF1F7A45, false),
        item("shapeblast", "game", "Shape Blast", 0xFFFF6B2C, false),
        item("docsarxhive", "app", "docsArxhive", 0xFFF2B705, false),
    )

    /** Lee el JSON del servidor. null si no es válido (entonces se queda el anterior). */
    fun desdeJson(texto: String): List<ItemCatalogo>? = try {
        val arr = JSONObject(texto).getJSONArray("items")
        val r = (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            ItemCatalogo(
                id = o.getString("id"),
                tipo = o.optString("type", "game"),
                nombre = o.getString("name"),
                paquete = o.getString("package"),
                color = colorDe(o.optString("color", "#5C7CFF")),
                icono = o.optString("icon").takeIf { it.startsWith("https://") },
                publica = o.optBoolean("public", true),
            )
        }
        r.takeIf { l -> l.isNotEmpty() && l.all { it.id.isNotBlank() && it.paquete.startsWith(PREFIJO) } }
    } catch (_: Exception) {
        null
    }

    fun aJson(l: List<ItemCatalogo>): String = JSONObject().put("version", 1).put("items", JSONArray(l.map {
        JSONObject().put("id", it.id).put("type", it.tipo).put("name", it.nombre).put("package", it.paquete)
            .put("color", "#%06X".format(it.color and 0xFFFFFF)).put("icon", it.icono ?: "").put("public", it.publica)
    })).toString()

    /** "#RRGGBB" → 0xFFRRGGBB (si no se entiende, azul del estudio). */
    fun colorDe(hex: String): Long =
        hex.removePrefix("#").takeIf { it.length == 6 }?.toLongOrNull(16)?.let { 0xFF000000 or it } ?: 0xFF5C7CFF

    /**
     * Lo que muestra el carrusel: el juego propio primero (al centro) y después los demás públicos en el orden del catálogo.
     * Si el propio no está en el catálogo (juego nuevo), se usa [propioRespaldo].
     */
    fun carrusel(items: List<ItemCatalogo>, propioId: String, incluirApps: Boolean = true, propioRespaldo: ItemCatalogo? = null): List<ItemCatalogo> {
        val propio = items.firstOrNull { it.id == propioId } ?: propioRespaldo ?: EMBEBIDO.firstOrNull { it.id == propioId }
        val otros = items.filter { it.id != propioId && it.publica && (incluirApps || !it.esApp) }
        return listOfNotNull(propio) + otros
    }

    /** Referrer de Play Store para medir qué juego trae instalaciones a cuál. */
    fun referrer(origen: String, destino: String) =
        "utm_source=$origen&utm_medium=cross_promo&utm_campaign=gokapigo_home&utm_content=$destino"

    /** Solo se abren o instalan paquetes del estudio. */
    const val PREFIJO = "com.gokapigo."
}
