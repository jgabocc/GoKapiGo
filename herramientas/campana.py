"""Material de una campaña de apps de Google Ads para un juego: imágenes, textos y el anuncio jugable en ZIP.

Sale en <juego>/campanas/google-ads/<idioma>/:
  horizontal.png 1200x628 · cuadrada.png 1200x1200 · vertical.png 1200x1500 · jugable.zip · textos.txt
Uso: python campana.py sand maze aqua        (idiomas: es-419 y pt-BR, para Latinoamérica)
"""
import json
import os
import sys
import zipfile

from PIL import Image, ImageDraw, ImageFilter, ImageFont

RAIZ = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
FUENTE = os.path.join(RAIZ, "maze-quest", "app", "src", "main", "res", "font", "rubik_800.ttf")
NARANJA = (251, 123, 0)

JUEGOS = {
    "sand": {"carpeta": "sand-block-puzzle", "id": "sandblockpuzzle", "captura": 2},
    "maze": {"carpeta": "maze-quest", "id": "mazequest", "captura": 3},
    "solitaire": {"carpeta": "solitaire-classic", "id": "solitaireclassic", "captura": 2},
    "aqua": {"carpeta": "AquaSorting", "id": "aquasorting", "captura": 2},
}
# Google Ads: títulos hasta 30 caracteres, descripciones hasta 90
TEXTOS = {
    "aqua": {
        "es-419": {"gancho": "¡Ordena el agua por colores!", "boton": "Jugar gratis",
                   "titulos": ["Ordena el agua por colores", "Puzzle de agua relajante", "Llena cada tubo de un color", "Juega gratis sin internet", "Aqua Sorting"],
                   "descripciones": ["Vierte el agua de un tubo a otro hasta que cada uno quede de un solo color.",
                                     "Un puzzle relajante para tu mente, con miles de niveles y tubo extra.",
                                     "Gratis y sin internet. ¿Cuántos niveles puedes resolver?"]},
        "pt-BR": {"gancho": "Separe a água por cores!", "boton": "Jogar grátis",
                  "titulos": ["Separe a água por cores", "Puzzle de água relaxante", "Encha cada tubo de uma cor", "Jogue grátis sem internet", "Aqua Sorting"],
                  "descripciones": ["Despeje a água de um tubo para outro até cada um ficar de uma só cor.",
                                    "Um puzzle relaxante para a mente, com milhares de níveis e tubo extra.",
                                    "Grátis e sem internet. Quantos níveis você consegue resolver?"]},
    },
    "sand": {
        "es-419": {"gancho": "¡Bloques que se vuelven arena!", "boton": "Jugar gratis",
                   "titulos": ["Bloques que se vuelven arena", "Puzzle de arena relajante", "Une colores de lado a lado", "Juega gratis sin internet", "Sand Block Puzzle"],
                   "descripciones": ["Suelta bloques, mira cómo se deshacen en arena y une colores de pared a pared.",
                                     "Un puzzle relajante y satisfactorio, con cientos de niveles. Juega sin internet.",
                                     "Gratis. Fácil de aprender y difícil de soltar. ¡Descárgalo ya!"]},
        "pt-BR": {"gancho": "Blocos que viram areia!", "boton": "Jogar grátis",
                  "titulos": ["Blocos que viram areia", "Puzzle de areia relaxante", "Una cores de ponta a ponta", "Jogue grátis sem internet", "Sand Block Puzzle"],
                  "descripciones": ["Solte blocos, veja-os virar areia e una as cores de uma parede à outra.",
                                    "Um puzzle relaxante e satisfatório, com centenas de níveis. Jogue sem internet.",
                                    "Grátis. Fácil de aprender e difícil de largar. Baixe já!"]},
    },
    "maze": {
        "es-419": {"gancho": "¿Encuentras la salida?", "boton": "Jugar gratis",
                   "titulos": ["¿Encuentras la salida?", "Laberintos para relajarte", "Desliza y escapa", "Juega gratis sin internet", "Maze Quest"],
                   "descripciones": ["Desliza para moverte y encuentra la salida. Cada nivel es un reto nuevo.",
                                     "Laberintos que crecen contigo, con pistas y mapa cuando los necesitas.",
                                     "Gratis y sin internet. ¿Hasta qué nivel llegarás?"]},
        "pt-BR": {"gancho": "Você acha a saída?", "boton": "Jogar grátis",
                  "titulos": ["Você acha a saída?", "Labirintos para relaxar", "Deslize e escape", "Jogue grátis sem internet", "Maze Quest"],
                  "descripciones": ["Deslize para se mover e encontre a saída. Cada nível é um novo desafio.",
                                    "Labirintos que crescem com você, com dicas e mapa quando precisar.",
                                    "Grátis e sem internet. Até que nível você chega?"]},
    },
    "solitaire": {
        "es-419": {"gancho": "El solitario de siempre", "boton": "Jugar gratis",
                   "titulos": ["El solitario de siempre", "Solitario clásico gratis", "Cartas grandes y claras", "Juega gratis sin internet", "Solitaire Classic"],
                   "descripciones": ["El Klondike de toda la vida, con cartas grandes, deshacer y pistas.",
                                     "Relájate con el solitario clásico, con regalo diario y sin internet.",
                                     "Gratis. Toca, ordena y gana. ¡Descárgalo ya!"]},
        "pt-BR": {"gancho": "A paciência de sempre", "boton": "Jogar grátis",
                  "titulos": ["A paciência de sempre", "Paciência clássica grátis", "Cartas grandes e claras", "Jogue grátis sem internet", "Solitaire Classic"],
                  "descripciones": ["O Klondike de sempre, com cartas grandes, desfazer e dicas.",
                                    "Relaxe com a paciência clássica, com presente diário e sem internet.",
                                    "Grátis. Toque, organize e vença. Baixe já!"]},
    },
}


def fuente(t):
    return ImageFont.truetype(FUENTE, t)


def fondo(w, h, color):
    """Degradado vertical del color del juego a casi negro."""
    c = tuple(int(color[i:i + 2], 16) for i in (1, 3, 5))
    im = Image.new("RGB", (w, h))
    d = ImageDraw.Draw(im)
    for y in range(h):
        k = y / h
        d.line([(0, y), (w, y)], fill=tuple(int(c[i] * (1 - k) * .55 + 12 * k) for i in range(3)))
    return im


def redondear(im, r):
    m = Image.new("L", im.size, 0); ImageDraw.Draw(m).rounded_rectangle((0, 0, *im.size), r, fill=255)
    im = im.convert("RGBA"); im.putalpha(m); return im


def texto_centrado(d, w, y, txt, f, color=(255, 255, 255), maximo=None):
    while maximo and d.textlength(txt, font=f) > maximo:
        f = fuente(f.size - 4)
    d.text(((w - d.textlength(txt, font=f)) / 2, y), txt, font=f, fill=color)
    return f


def pieza(w, h, juego, loc, cfg, color):
    """Cuadrada o vertical: gancho arriba, captura del juego al centro, icono + nombre + botón abajo."""
    base = os.path.join(RAIZ, cfg["carpeta"], "store-listing", loc, "images")
    im = fondo(w, h, color); d = ImageDraw.Draw(im)
    t = TEXTOS[juego][loc]
    texto_centrado(d, w, int(h * .045), t["gancho"], fuente(int(w * .07)), maximo=w * .9)
    # campanas/google-ads/fuente/<idioma>.png, si existe, manda sobre la captura de la ficha
    propia = os.path.join(RAIZ, cfg["carpeta"], "campanas", "google-ads", "fuente", f"{loc}.png")
    cap = Image.open(propia if os.path.exists(propia) else os.path.join(base, "phoneScreenshots", f"{cfg['captura']}.png")).convert("RGB")
    alto = int(h * .62); ancho = int(alto * cap.width / cap.height)
    cap = redondear(cap.resize((ancho, alto), Image.LANCZOS), int(ancho * .07))
    x, y = (w - ancho) // 2, int(h * .15)
    sombra = Image.new("RGBA", im.size, (0, 0, 0, 0))
    ImageDraw.Draw(sombra).rounded_rectangle((x + 10, y + 18, x + ancho + 10, y + alto + 18), int(ancho * .07), fill=(0, 0, 0, 150))
    im.paste(sombra.filter(ImageFilter.GaussianBlur(18)), (0, 0), sombra.filter(ImageFilter.GaussianBlur(18)))
    im.paste(cap, (x, y), cap)
    # abajo: icono, nombre y botón
    ic = redondear(Image.open(os.path.join(base, "icon.png")).convert("RGB").resize((int(h * .085),) * 2, Image.LANCZOS), int(h * .019))
    nombre = json.load(open(os.path.join(RAIZ, "GoKapiGo", "juegos.json"), encoding="utf-8"))
    nombre = next(j["name"] for j in nombre if j["id"] == cfg["id"])
    yb = int(h * .785); fn = fuente(int(h * .04))
    tw = ic.width + 24 + d.textlength(nombre, font=fn)
    x0 = int((w - tw) / 2)
    im.paste(ic, (x0, yb), ic)
    d.text((x0 + ic.width + 24, yb + (ic.height - fn.size) / 2 - 4), nombre, font=fn, fill=(255, 255, 255))
    fb = fuente(int(h * .036)); bw = d.textlength(t["boton"], font=fb) + 90; bh = int(h * .065)
    bx, by = (w - bw) / 2, int(h * .915) - bh // 2
    d.rounded_rectangle((bx, by + 8, bx + bw, by + bh + 8), bh // 2, fill=(155, 76, 0))
    d.rounded_rectangle((bx, by, bx + bw, by + bh), bh // 2, fill=NARANJA)
    d.text((bx + 45, by + (bh - fb.size) / 2 - 4), t["boton"], font=fb, fill=(26, 13, 0))
    return im


def generar(juego):
    cfg = JUEGOS[juego]
    color = next(j["color"] for j in json.load(open(os.path.join(RAIZ, "GoKapiGo", "juegos.json"), encoding="utf-8")) if j["id"] == cfg["id"])
    for loc, t in TEXTOS[juego].items():
        assert all(len(x) <= 30 for x in t["titulos"]), [x for x in t["titulos"] if len(x) > 30]
        assert all(len(x) <= 90 for x in t["descripciones"]), [x for x in t["descripciones"] if len(x) > 90]
        sal = os.path.join(RAIZ, cfg["carpeta"], "campanas", "google-ads", loc); os.makedirs(sal, exist_ok=True)
        fg = Image.open(os.path.join(RAIZ, cfg["carpeta"], "store-listing", loc, "images", "featureGraphic.png")).convert("RGB")
        ancho = int(fg.height * 1200 / 628); x0 = (fg.width - ancho) // 2
        fg.crop((x0, 0, x0 + ancho, fg.height)).resize((1200, 628), Image.LANCZOS).save(os.path.join(sal, "horizontal.png"))
        pieza(1200, 1200, juego, loc, cfg, color).save(os.path.join(sal, "cuadrada.png"))
        pieza(1200, 1500, juego, loc, cfg, color).save(os.path.join(sal, "vertical.png"))
        with zipfile.ZipFile(os.path.join(sal, "jugable.zip"), "w", zipfile.ZIP_DEFLATED) as z:
            z.write(os.path.join(RAIZ, cfg["carpeta"], "playable", "google", "index.html"), "index.html")
        with open(os.path.join(sal, "textos.txt"), "w", encoding="utf-8", newline="\n") as f:
            f.write("TÍTULOS (máx. 30)\n" + "\n".join(t["titulos"]) + "\n\nDESCRIPCIONES (máx. 90)\n" + "\n".join(t["descripciones"]) + "\n")
        print(f"✓ {juego} {loc}: {sal}")


if __name__ == "__main__":
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")
    for j in sys.argv[1:] or JUEGOS:
        generar(j)
