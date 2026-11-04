"""Subidas a Google Play desde esta PC con la cuenta de servicio del estudio (API de Google Play Developer).

Las llaves de firma no salen de cada juego: el AAB se compila y firma en local (compilar.ps1) y aquí solo se sube.
La credencial es el JSON de la cuenta de servicio, fuera de todo repo:  D:\\GOOGLE PLAY GAMES\\_play\\cuenta.json
(o la ruta en la variable PLAY_CUENTA).

Uso:
  python play.py estado                          # versión publicada y en revisión de cada juego
  python play.py publicar sand 1.1               # sube salida/*-1.1.aab + salida/notas-1.1.txt a producción y lo envía a revisión
  python play.py publicar sand 1.1 --ficha       # además sube la ficha (textos e imágenes) de store-listing/ en los 9 idiomas
  python play.py publicar sand 1.1 --pista internal --borrador    # a la prueba interna, como borrador (no se envía)
  python play.py ficha sand                      # solo la ficha (textos e imágenes de los 9 idiomas)
  python play.py todos 1.1 [--ficha]             # publicar en los juegos ya públicos (PUBLICADOS) que tengan el AAB
  --prueba   prepara todo en Play pero NO confirma (valida AAB, notas y ficha sin cambiar nada)
"""
import argparse
import glob
import os
import re
import sys

from google.oauth2 import service_account
from googleapiclient.discovery import build
from googleapiclient.errors import HttpError
from googleapiclient.http import MediaFileUpload

sys.stdout.reconfigure(encoding="utf-8", errors="replace")     # la consola de Windows no imprime ✓ ✗ →
RAIZ = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
CUENTA = os.environ.get("PLAY_CUENTA", os.path.join(RAIZ, "_play", "cuenta.json"))

# id corto → (carpeta, paquete)
JUEGOS = {
    "sand": ("sand-block-puzzle", "com.gokapigo.sandblockpuzzle"),
    "aqua": ("AquaSorting", "com.gokapigo.aquasorting"),
    "nuts": ("nuts-bolts-sort", "com.gokapigo.nutsboltssort"),
    "color": ("color-block-slide", "com.gokapigo.colorblockslide"),
    "sudoku": ("SUDOKUMaster3000", "com.gokapigo.sudokumaster3000"),
    "shapespin": ("ShapeSpin", "com.gokapigo.shapespin"),
    "parchis": ("parchis-party", "com.gokapigo.parchisparty"),
    "pipe": ("pipe-connect", "com.gokapigo.pipeconnect"),
    "onestroke": ("one-stroke", "com.gokapigo.onestroke"),
    "maze": ("maze-quest", "com.gokapigo.mazequest"),
    "shapetiles": ("shape-tiles", "com.gokapigo.shapetiles"),
    "solitaire": ("solitaire-classic", "com.gokapigo.solitaireclassic"),
    "arrow": ("arrow-escape", "com.gokapigo.arrowescape"),
    "shapeblast": ("shape-blast", "com.gokapigo.shapeblast"),
    "docs": ("docs-arxhive", "com.gokapigo.docsarxhive"),
    "laberinto": ("minotaurs Maze", "com.gokapigo.laberinto"),
}
# los que ya están públicos en Play (los que toca "todos")
PUBLICADOS = ["sand", "aqua", "nuts", "color", "sudoku", "shapespin", "parchis", "pipe", "onestroke", "maze", "shapetiles", "solitaire"]
IMAGENES = {"icon.png": "icon", "featureGraphic.png": "featureGraphic"}


def servicio():
    if not os.path.exists(CUENTA):
        sys.exit(f"Falta la credencial de la cuenta de servicio: {CUENTA}")
    cred = service_account.Credentials.from_service_account_file(CUENTA, scopes=["https://www.googleapis.com/auth/androidpublisher"])
    return build("androidpublisher", "v3", credentials=cred, cache_discovery=False)


def notas(archivo):
    """<es-419>texto</es-419>… → [{language, text}]"""
    if not os.path.exists(archivo):
        return []
    s = open(archivo, encoding="utf-8").read()
    return [{"language": l, "text": t.strip()} for l, t in re.findall(r"<([\w-]+)>\s*(.*?)\s*</\1>", s, flags=re.S)]


def subir_ficha(ed, paquete, edit, carpeta):
    base = os.path.join(RAIZ, carpeta, "store-listing")
    idiomas = sorted(d for d in os.listdir(base) if os.path.isdir(os.path.join(base, d)))
    for loc in idiomas:
        d = os.path.join(base, loc)
        leer = lambda n: open(os.path.join(d, n), encoding="utf-8").read().strip()
        ed.listings().update(packageName=paquete, editId=edit, language=loc, body={
            "language": loc, "title": leer("title.txt"), "shortDescription": leer("short_description.txt"),
            "fullDescription": leer("full_description.txt")}).execute()
        img = os.path.join(d, "images")
        for nombre, tipo in IMAGENES.items():
            if os.path.exists(os.path.join(img, nombre)):
                ed.images().deleteall(packageName=paquete, editId=edit, language=loc, imageType=tipo).execute()
                ed.images().upload(packageName=paquete, editId=edit, language=loc, imageType=tipo,
                                   media_body=MediaFileUpload(os.path.join(img, nombre), mimetype="image/png")).execute()
        capturas = sorted(glob.glob(os.path.join(img, "phoneScreenshots", "*.png")), key=lambda p: int(re.sub(r"\D", "", os.path.basename(p)) or 0))
        if capturas:
            ed.images().deleteall(packageName=paquete, editId=edit, language=loc, imageType="phoneScreenshots").execute()
            for c in capturas:
                ed.images().upload(packageName=paquete, editId=edit, language=loc, imageType="phoneScreenshots",
                                   media_body=MediaFileUpload(c, mimetype="image/png")).execute()
        print(f"   ficha {loc}: textos, {len(capturas)} capturas")
    return idiomas


def publicar(api, clave, version, pista="production", ficha=False, borrador=False, prueba=False, porcentaje=None, prioridad=None):
    carpeta, paquete = JUEGOS[clave]
    aabs = glob.glob(os.path.join(RAIZ, carpeta, "salida", f"*-{version}.aab"))
    if len(aabs) != 1:
        print(f"✗ {clave}: se esperaba un AAB *-{version}.aab en {carpeta}/salida (hay {len(aabs)})"); return False
    ed = api.edits()
    edit = ed.insert(packageName=paquete, body={}).execute()["id"]
    try:
        idiomas = subir_ficha(ed, paquete, edit, carpeta) if ficha else \
            [l["language"] for l in ed.listings().list(packageName=paquete, editId=edit).execute().get("listings", [])]
        print(f"→ {clave}: subiendo {os.path.basename(aabs[0])} …")
        vc = ed.bundles().upload(packageName=paquete, editId=edit,
                                 media_body=MediaFileUpload(aabs[0], mimetype="application/octet-stream", resumable=True)).execute()["versionCode"]
        # Play solo acepta notas en idiomas que la ficha tiene
        ns = [n for n in notas(os.path.join(RAIZ, carpeta, "salida", f"notas-{version}.txt")) if n["language"] in idiomas]
        release = {"name": version, "versionCodes": [str(vc)], "releaseNotes": ns,
                   "status": "draft" if borrador else ("inProgress" if porcentaje else "completed")}
        if porcentaje and not borrador:
            release["userFraction"] = porcentaje / 100
        # prioridad de actualización dentro de la app (0-5): 5 = crítica, UpdateManager la instala de inmediato
        if prioridad is not None:
            release["inAppUpdatePriority"] = prioridad
        ed.tracks().update(packageName=paquete, editId=edit, track=pista, body={"track": pista, "releases": [release]}).execute()
        if prueba:
            ed.validate(packageName=paquete, editId=edit).execute()
            ed.delete(packageName=paquete, editId=edit).execute()
            print(f"✓ {clave}: PRUEBA ok (versionCode {vc}, notas en {len(ns)} idiomas) — no se cambió nada en Play"); return True
        ed.commit(packageName=paquete, editId=edit).execute()
        print(f"✓ {clave}: {version} (versionCode {vc}) en {pista}{' como borrador' if borrador else ', enviada a revisión'}; notas en {len(ns)} idiomas")
        return True
    except HttpError as e:
        try: ed.delete(packageName=paquete, editId=edit).execute()
        except HttpError: pass
        print(f"✗ {clave}: {e.reason if hasattr(e, 'reason') else e}\n   {e.content.decode('utf-8', 'replace')[:600]}")
        return False


def solo_ficha(api, clave, prueba=False):
    carpeta, paquete = JUEGOS[clave]
    ed = api.edits(); edit = ed.insert(packageName=paquete, body={}).execute()["id"]
    try:
        subir_ficha(ed, paquete, edit, carpeta)
        if prueba:
            ed.validate(packageName=paquete, editId=edit).execute(); ed.delete(packageName=paquete, editId=edit).execute()
            print(f"✓ {clave}: ficha PRUEBA ok"); return
        ed.commit(packageName=paquete, editId=edit).execute(); print(f"✓ {clave}: ficha subida")
    except HttpError as e:
        ed.delete(packageName=paquete, editId=edit).execute(); print(f"✗ {clave}: {e.content.decode('utf-8', 'replace')[:600]}")


def estado(api):
    for clave, (_, paquete) in JUEGOS.items():
        try:
            ed = api.edits(); edit = ed.insert(packageName=paquete, body={}).execute()["id"]
            pistas = ed.tracks().list(packageName=paquete, editId=edit).execute().get("tracks", [])
            ed.delete(packageName=paquete, editId=edit).execute()
            txt = "  ".join(f"{t['track']}: " + ", ".join(f"{r.get('name', '?')}({','.join(r.get('versionCodes', []))}) {r['status']}"
                                                       for r in t.get("releases", [])) for t in pistas if t.get("releases"))
            print(f"{clave:<11} {txt or '—'}")
        except HttpError as e:
            print(f"{clave:<11} sin acceso ({e.resp.status})")


if __name__ == "__main__":
    p = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    p.add_argument("orden", choices=["estado", "publicar", "ficha", "todos"])
    p.add_argument("juego_o_version", nargs="?")
    p.add_argument("version", nargs="?")
    p.add_argument("--pista", default="production")
    p.add_argument("--ficha", action="store_true")
    p.add_argument("--borrador", action="store_true")
    p.add_argument("--prueba", action="store_true")
    p.add_argument("--porcentaje", type=float, help="lanzamiento gradual (p. ej. 20 = 20 %% de usuarios)")
    p.add_argument("--prioridad", type=int, choices=range(6), help="prioridad de actualización en la app: 5 = crítica (inmediata)")
    a = p.parse_args()
    api = servicio()
    if a.orden == "estado":
        estado(api)
    elif a.orden == "publicar":
        publicar(api, a.juego_o_version, a.version, a.pista, a.ficha, a.borrador, a.prueba, a.porcentaje, a.prioridad)
    elif a.orden == "ficha":
        solo_ficha(api, a.juego_o_version, a.prueba)
    else:
        ok = [c for c in PUBLICADOS if glob.glob(os.path.join(RAIZ, JUEGOS[c][0], "salida", f"*-{a.juego_o_version}.aab"))
              and publicar(api, c, a.juego_o_version, a.pista, a.ficha, a.borrador, a.prueba, a.porcentaje, a.prioridad)]
        print(f"\n{len(ok)} juegos listos: {', '.join(ok)}")
