"""Genera catalog.json, el catálogo de la portada GoKapiGo Home que leen todos los juegos.

- Sale de juegos.json (orden = orden del carrusel). "listed": false lo deja fuera (p. ej. un juego aún sin enviar a Play).
- "public" se comprueba en cada publicación del sitio abriendo la ficha de Google Play (200 = pública, 404 = aún no).
  Si Play no responde, se usa el "public" de juegos.json. Así, cuando Google aprueba un juego, aparece solo en el
  carrusel de todos los demás (la publicación diaria de pages.yml lo recoge).
- Los iconos (landing/iconos/<id>.png, 192 px) son los de Play de cada juego.
"""
import json
import os
import sys
import urllib.error
import urllib.request

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SITIO = "https://www.capibaradev.com"


def publica_en_play(paquete, respaldo):
    url = f"https://play.google.com/store/apps/details?id={paquete}&hl=en"
    req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0 (GoKapiGo catalogo)"})
    try:
        with urllib.request.urlopen(req, timeout=15) as r:
            return r.status == 200
    except urllib.error.HTTPError as e:
        return False if e.code == 404 else respaldo
    except Exception:
        return respaldo


def generar(destino, comprobar=True):
    juegos = json.load(open(os.path.join(ROOT, "juegos.json"), encoding="utf-8"))
    items = []
    for j in juegos:
        if not j.get("listed", True):
            continue
        publica = publica_en_play(j["package"], j.get("public", False)) if comprobar else j.get("public", False)
        items.append({
            "id": j["id"], "type": j.get("type", "game"), "name": j["name"], "package": j["package"],
            "color": j["color"], "icon": f"{SITIO}/iconos/{j['id']}.png", "public": publica,
        })
        print(f"  {j['id']:<18} {'pública' if publica else '—'}")
    os.makedirs(os.path.dirname(destino), exist_ok=True)
    with open(destino, "w", encoding="utf-8", newline="\n") as f:
        json.dump({"version": 1, "items": items}, f, ensure_ascii=False, indent=1)
    return items


if __name__ == "__main__":
    generar(sys.argv[1] if len(sys.argv) > 1 else os.path.join(ROOT, "_site", "catalog.json"))
