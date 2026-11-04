"""Marca GoKapiGo para los anuncios jugables de todos los juegos (regla 15 de GOKAPIGO.md).

Cada playable/build.py llama a envolver(html, "<id del juego>") antes de guardar el archivo de cada red. Agrega:
  1. Al arrancar: pantalla con el logo de GoKapiGo (1,5 s); el juego empieza después (envuelve PLAYABLE.ready).
  2. Al terminar: cuando el juego muestra su #playEnd, se cubre con el final común: logo de GoKapiGo, carrusel con
     los iconos de los juegos y apps del estudio que gira y se detiene en el del juego del anuncio, su nombre,
     el texto del juego ("¡Nivel completado!") y su botón ("Jugar gratis"). Tocar en cualquier parte = PLAYABLE.install()
     (la ficha de Play de ese juego, con la línea de instalar de cada red).
No hace falta tocar la lógica del juego: reutiliza los textos traducidos de su #playEnd.
Iconos: GoKapiGo/landing/iconos/<id>.png (los de Play). Carrusel = juegos de juegos.json ya públicos en Play ("public": true) y, al final, el del anuncio.
"""
import base64
import io
import json
import os

from PIL import Image

ESTUDIO = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
AQUI = os.path.dirname(os.path.abspath(__file__))


def _webp(ruta, lado, calidad=82):
    im = Image.open(ruta).convert("RGBA")
    im.thumbnail((lado, lado), Image.LANCZOS)
    b = io.BytesIO(); im.save(b, "WEBP", quality=calidad, method=6)
    return "data:image/webp;base64," + base64.b64encode(b.getvalue()).decode()


def config(juego_id):
    juegos = json.load(open(os.path.join(ESTUDIO, "juegos.json"), encoding="utf-8"))
    yo = next(j for j in juegos if j["id"] == juego_id)
    otros = [j for j in juegos if j.get("listed", True) and j.get("public") and j["id"] != juego_id]   # solo los que ya están en Play
    icono = lambda j: _webp(os.path.join(ESTUDIO, "landing", "iconos", j["id"] + ".png"), 128)
    return {
        "name": yo["name"], "color": yo.get("color", "#fb7b00"),
        "logo": _webp(os.path.join(ESTUDIO, "landing", "img", "logo.webp"), 320),
        "me": icono(yo), "icons": [icono(j) for j in otros],
        "store": "https://play.google.com/store/apps/details?id=" + yo["package"],
    }


def envolver(html, juego_id):
    """Inserta la marca GoKapiGo justo después de la línea window.PLAYABLE=… (antes de que el juego la lea)."""
    i = html.index("window.PLAYABLE=")
    fin = html.index("</script>", i) + len("</script>")
    css = open(os.path.join(AQUI, "gokapigo_playable.css"), encoding="utf-8").read()
    js = open(os.path.join(AQUI, "gokapigo_playable.js"), encoding="utf-8").read()
    bloque = (f"\n<style>{css}</style>\n<script>window.GKG_CFG={json.dumps(config(juego_id), separators=(',', ':'))};\n{js}</script>")
    return html[:fin] + bloque + html[fin:]
