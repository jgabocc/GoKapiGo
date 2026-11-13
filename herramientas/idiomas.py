"""Idiomas del estudio: de 7 a 15 (dueño 2026-11-12). Prepara y revisa un juego; NO traduce (eso lo escribe el agente).
Procedimiento completo en IDIOMAS.md (misma carpeta). Piloto: maze-quest.

Uso:  python idiomas.py <orden> <carpeta del juego>
  estado    qué falta: carpetas values-*, archivos y textos por idioma nuevo; ficha de Play por idioma
  filtros   agrega los 8 códigos a localeFilters (o resConfigs) de app/build.gradle(.kts)
  selector  agrega los 8 idiomas con su nombre propio al mapa de idiomas del selector (LANG_NAMES o parecido)
  home      copia la portada común canónica (GoKapiGo/componentes/gokapigo-home/*.kt) a com/gokapigo/home/
  fuentes   dice qué fuentes del juego no tienen letras de vietnamita/turco (→ usar Textos.fuente, ver IDIOMAS.md)
  validar   revisa TODOS los values-*/*.xml contra values/: XML bien formado, mismas claves, mismos marcadores
            ({n}, %1$s…), mismos elementos en string-array/plurals, apóstrofos y comillas escapados;
            y la ficha (store-listing/<loc>: límites 30/80/4000, capturas, imagen destacada)
  todo      filtros + selector + home + estado + validar
Sale con código 1 si validar encuentra errores.
"""
import glob
import os
import re
import shutil
import sys
import xml.etree.ElementTree as ET

sys.stdout.reconfigure(encoding="utf-8", errors="replace")
AQUI = os.path.dirname(os.path.abspath(__file__))
HOME_CANON = os.path.join(AQUI, "..", "componentes", "gokapigo-home")

# idioma: (carpeta de recursos, código de localeFilters, clave del selector, nombre propio, idioma de Play)
VIEJOS = {
    "en": ("values", "en", "en", "English", ["en-US"]),
    "es": ("values-es", "es", "es", "Español", ["es-419", "es-ES"]),
    "pt": ("values-pt", "pt", "pt", "Português", ["pt-BR", "pt-PT"]),
    "fr": ("values-fr", "fr", "fr", "Français", ["fr-FR"]),
    "de": ("values-de", "de", "de", "Deutsch", ["de-DE"]),
    "pl": ("values-pl", "pl", "pl", "Polski", ["pl-PL"]),
    "ru": ("values-ru", "ru", "ru", "Русский", ["ru-RU"]),
}
NUEVOS = {
    "tr": ("values-tr", "tr", "tr", "Türkçe", ["tr-TR"]),
    "id": ("values-in", "in", "in", "Bahasa Indonesia", ["id"]),        # indonesio: "in" es el código de Android
    "vi": ("values-vi", "vi", "vi", "Tiếng Việt", ["vi"]),
    "th": ("values-th", "th", "th", "ไทย", ["th"]),
    "hi": ("values-hi", "hi", "hi", "हिन्दी", ["hi-IN"]),
    "ja": ("values-ja", "ja", "ja", "日本語", ["ja-JP"]),
    "ko": ("values-ko", "ko", "ko", "한국어", ["ko-KR"]),
    "zh": ("values-zh-rCN", "zh-rCN", "zh-CN", "简体中文", ["zh-CN"]),     # solo chino simplificado
}
TODOS = {**VIEJOS, **NUEVOS}
PLAY = [p for v in TODOS.values() for p in v[4]]          # 17 idiomas de la ficha de Play


def res_de(juego):
    r = os.path.join(juego, "app", "src", "main", "res")
    if not os.path.isdir(r):
        sys.exit(f"no encuentro {r}")
    return r


def archivos_base(res):
    """Archivos de values/ que tienen textos traducibles."""
    out = []
    for f in sorted(glob.glob(os.path.join(res, "values", "*.xml"))):
        try:
            raiz = ET.parse(f).getroot()
        except ET.ParseError:
            continue
        if any(e.tag in ("string", "string-array", "plurals") and e.get("translatable") != "false" for e in raiz):
            out.append(os.path.basename(f))
    return out


def textos(f):
    """{(tipo, nombre): [textos]} de un XML de recursos (sin los translatable=false)."""
    d = {}
    for e in ET.parse(f).getroot():
        if e.tag not in ("string", "string-array", "plurals") or e.get("translatable") == "false":
            continue
        if e.tag == "string":
            d[(e.tag, e.get("name"))] = ["".join(e.itertext())]
        elif e.tag == "string-array":
            d[(e.tag, e.get("name"))] = ["".join(i.itertext()) for i in e.findall("item")]
        else:
            d[(e.tag, e.get("name"))] = ["".join(i.itertext()) for i in e.findall("item")]
    return d


# sin la bandera « » de printf: «+15% fire» o «20 % de daño» son porcentajes, no marcadores
MARCA = re.compile(r"\{[A-Za-z_][A-Za-z0-9_]*\}|%(?:\d+\$)?[-#+0,(]*\d*(?:\.\d+)?[sdfxXc%]")


def marcadores(s):
    return sorted(m for m in MARCA.findall(s) if m != "%%")


def problemas_escape(crudo):
    """Revisa el texto tal como está en el archivo (sin interpretar)."""
    p = []
    sin_esc = re.sub(r"\\.", "", crudo)
    if "'" in sin_esc and not (crudo.startswith('"') and crudo.endswith('"')):
        p.append("apóstrofo sin escapar (usar \\')")
    if '"' in sin_esc:
        p.append('comilla doble sin escapar (usar \\" o « »)')
    if crudo[:1] in ("@", "?"):
        p.append(f"empieza con {crudo[0]} (usar \\{crudo[0]})")
    return p


def crudos(f):
    """Textos crudos por nombre, para revisar escapes (ET ya los interpreta)."""
    s = open(f, encoding="utf-8").read()
    out = []
    s = re.sub(r"<!--.*?-->", "", s, flags=re.S)
    for m in re.finditer(r'<(string|item)(\s[^>]*)?>(.*?)</\1>', s, flags=re.S):
        n = re.search(r'name="([^"]*)"', m.group(2) or "")
        if 'translatable="false"' not in (m.group(2) or ""):
            out.append((n.group(1) if n else "item", m.group(3)))
    return out


def usados_viejos(res, a):
    """Claves de a que traduce al menos un idioma viejo (las demás no se traducen a propósito)."""
    u = set()
    for k, v in VIEJOS.items():
        f = os.path.join(res, v[0], a)
        if k != "en" and os.path.exists(f):
            try: u |= set(textos(f))
            except ET.ParseError: pass
    return u


# ---------------------------------------------------------------- órdenes

def estado(juego):
    res = res_de(juego)
    base = archivos_base(res)
    print(f"== {os.path.basename(os.path.abspath(juego))}: archivos con textos en values/: {', '.join(base)}")
    for lang, (carpeta, *_r) in NUEVOS.items():
        faltan = []
        for a in base:
            f = os.path.join(res, carpeta, a)
            if not os.path.exists(f):
                faltan.append(f"{a} (no existe)")
                continue
            try:
                k = set(textos(f)); kb = set(textos(os.path.join(res, "values", a)))
            except ET.ParseError as e:
                faltan.append(f"{a} (XML roto: {e})"); continue
            kb &= usados_viejos(res, a) or kb
            if kb - k:
                faltan.append(f"{a} (faltan {len(kb - k)}: {', '.join(sorted(n for _, n in kb - k))[:120]})")
        print(f"  {carpeta:15} {'OK' if not faltan else 'FALTA: ' + '; '.join(faltan)}")
    fichas = os.path.join(juego, "store-listing")
    if os.path.isdir(fichas):
        hay = set(os.listdir(fichas))
        print("  ficha de Play, faltan:", ", ".join(p for p in PLAY if p not in hay) or "nada")


def filtros(juego):
    for g in ("app/build.gradle.kts", "app/build.gradle"):
        f = os.path.join(juego, g)
        if os.path.exists(f):
            break
    else:
        print("✗ no hay app/build.gradle(.kts)"); return
    s = open(f, encoding="utf-8").read()
    m = re.search(r'(localeFilters\s*\+=\s*listOf\(|resConfigs?\s*\(?|resourceConfigurations\s*\+=\s*(?:listOf|setOf)\()([^)\n]*)', s)
    if not m:
        print(f"✗ {g}: no encontré localeFilters/resConfigs; agrégalo a mano (ver IDIOMAS.md)"); return
    actuales = re.findall(r'"([^"]+)"', m.group(2))
    nuevos = [v[1] for v in NUEVOS.values() if v[1] not in actuales]
    if not nuevos:
        print(f"= {g}: localeFilters ya tiene los 15"); return
    lista = m.group(2).rstrip()
    añadido = ", ".join(f'"{c}"' for c in nuevos)
    s = s[:m.start(2)] + (lista + ", " if lista.strip() else "") + añadido + s[m.end(2):]
    open(f, "w", encoding="utf-8", newline="").write(s)
    print(f"✓ {g}: localeFilters + {', '.join(nuevos)}")


def selector(juego):
    kts = glob.glob(os.path.join(juego, "app", "src", "main", "java", "**", "*.kt"), recursive=True)
    hecho = False
    for f in kts:
        if os.sep + "gokapigo" + os.sep + "home" + os.sep in f:
            continue
        s = open(f, encoding="utf-8").read()
        m = re.search(r'"ru"\s*(to|:|,)\s*"Русский"\s*\)?', s)
        if not m:
            continue
        sep = m.group(1)
        if '"日本語"' in s:
            print(f"= {os.path.relpath(f, juego)}: el selector ya tiene los idiomas nuevos"); hecho = True; continue
        if sep == "to":
            extra = "".join(f', "{v[2]}" to "{v[3]}"' for v in NUEVOS.values())
        elif sep == ",":       # Pair("ru", "Русский") o listOf("ru", "Русский")
            cierre = s[m.end() - 1] == ")"
            if not cierre:
                print(f"? {os.path.relpath(f, juego)}: patrón raro en el selector, agrégalo a mano"); continue
            ini = s.rfind("(", 0, m.start())
            pref = s[s.rfind(" ", 0, ini) + 1:ini + 1] if ini > 0 else "Pair("
            extra = "".join(f', {pref}"{v[2]}", "{v[3]}")' for v in NUEVOS.values())
        else:
            print(f"? {os.path.relpath(f, juego)}: selector con «:», agrégalo a mano"); continue
        fin = m.end() - 1 if sep == "to" and s[m.end() - 1] == ")" else m.end()   # «… "ru" to "Русский")»: antes del )
        s = s[:fin] + extra + s[fin:]
        open(f, "w", encoding="utf-8", newline="").write(s)
        print(f"✓ {os.path.relpath(f, juego)}: selector + 8 idiomas (revisa que la lista tenga scroll)")
        hecho = True
    if not hecho:
        print("· no encontré un selector de idiomas con «\"ru\" to \"Русский\"» (si el juego no tiene selector, nada que hacer)")


def home(juego):
    destino = glob.glob(os.path.join(juego, "app", "src", "main", "java", "com", "gokapigo", "home"))
    if not destino:
        print("· el juego no tiene com/gokapigo/home (portada común): nada que copiar"); return
    d = destino[0]
    for f in sorted(glob.glob(os.path.join(HOME_CANON, "*.kt"))):
        n = os.path.basename(f)
        t = os.path.join(d, n)
        if not os.path.exists(t):
            print(f"· {n}: el juego no lo tiene (se deja así)"); continue
        if open(f, encoding="utf-8").read() == open(t, encoding="utf-8").read():
            print(f"= {n}: ya es la versión canónica"); continue
        shutil.copyfile(f, t)
        print(f"✓ {n}: copiado de componentes/gokapigo-home")


def fuentes(juego):
    try:
        from fontTools.ttLib import TTFont
    except ImportError:
        print("· falta fontTools (pip install fonttools)"); return
    vi = "ăâđêôơưạảấầẩẫậắằẳẵặẹẻẽếềểễệỉịọỏốồổỗộớờởỡợụủứừửữựỳỵỷỹ"
    tr = "ğşıİĞŞ"
    malas, sin_tr = [], []
    for f in sorted(glob.glob(os.path.join(res_de(juego), "font", "*.[to]tf"))):
        cm = TTFont(f).getBestCmap()
        fv = sum(1 for c in vi if ord(c) not in cm)
        ft = "".join(c for c in tr if ord(c) not in cm)
        print(f"  {os.path.basename(f):24} vietnamita: {'completa' if not fv else f'faltan {fv} letras'}"
              f" · turco: {'completo' if not ft else 'faltan ' + ft}")
        if fv:
            malas.append(f)
        if ft:
            sin_tr.append(os.path.basename(f))
    usa = any("Textos.fuente(" in open(k, encoding="utf-8").read()
              for k in glob.glob(os.path.join(juego, "app", "src", "main", "java", "**", "*.kt"), recursive=True)
              if os.sep + "home" + os.sep not in k)
    if sin_tr:
        print(f"  → {', '.join(sin_tr)} tampoco tiene turco: con esa fuente usar Textos.fuente(f, idioma, setOf(\"vi\", \"tr\"))")
    if malas and not usa:
        print("  → hay fuentes sin vietnamita y el juego no usa Textos.fuente(): aplicarlo en sus estilos (IDIOMAS.md, paso 5)")
    elif malas:
        print("  → OK: el juego ya pasa sus fuentes por Textos.fuente()")


def validar(juego):
    res = res_de(juego)
    errores, avisos = [], []
    base = archivos_base(res)
    ref = {a: textos(os.path.join(res, "values", a)) for a in base}
    # textos que ningún idioma viejo traduce (p. ej. app_name: el nombre es igual en todos los países) = a propósito
    viejos = [os.path.join(res, v[0]) for k, v in VIEJOS.items() if k != "en" and os.path.isdir(os.path.join(res, v[0]))]
    for a in base:
        usados = set()
        for c in viejos:
            if os.path.exists(os.path.join(c, a)):
                try: usados |= set(textos(os.path.join(c, a)))
                except ET.ParseError: pass
        if viejos:
            ref[a] = {k: v for k, v in ref[a].items() if k in usados}
    carpetas = sorted(d for d in os.listdir(res) if d.startswith("values-") and os.path.isdir(os.path.join(res, d)))
    for c in carpetas:
        for a in base:
            f = os.path.join(res, c, a)
            if not os.path.exists(f):
                continue
            rel = f"{c}/{a}"
            try:
                d = textos(f)
            except ET.ParseError as e:
                errores.append(f"{rel}: XML mal formado: {e}"); continue
            for k in ref[a]:
                if k not in d:
                    (errores if c in [v[0] for v in TODOS.values()] else avisos).append(f"{rel}: falta {k[0]} «{k[1]}»")
                    continue
                r, t = ref[a][k], d[k]
                if k[0] == "string-array" and len(r) != len(t):
                    errores.append(f"{rel}: «{k[1]}» tiene {len(t)} elementos, values/ tiene {len(r)}")
                if k[0] == "string":
                    if marcadores(r[0]) != marcadores(t[0]):
                        errores.append(f"{rel}: «{k[1]}» marcadores {marcadores(t[0])} ≠ values/ {marcadores(r[0])}")
                    if not t[0].strip() and r[0].strip():
                        errores.append(f"{rel}: «{k[1]}» está vacío")
                if k[0] == "plurals":
                    if {tuple(marcadores(x)) for x in t} - {tuple(marcadores(x)) for x in r} - {()}:
                        avisos.append(f"{rel}: plurals «{k[1]}» con marcadores distintos (revisar)")
            for k in d:
                if k not in ref[a]:
                    avisos.append(f"{rel}: sobra {k[0]} «{k[1]}» (no está en values/)")
            for nombre, crudo in crudos(f):
                for p in problemas_escape(crudo.strip()):
                    errores.append(f"{rel}: «{nombre}»: {p}")
    for lang, (c, *_r) in NUEVOS.items():
        if not os.path.isdir(os.path.join(res, c)):
            errores.append(f"falta la carpeta {c} ({lang})")
    # ficha de Play
    fichas = os.path.join(juego, "store-listing")
    if os.path.isdir(fichas):
        for loc in sorted(os.listdir(fichas)):
            d = os.path.join(fichas, loc)
            if not os.path.isdir(d):
                continue
            if loc not in PLAY:
                avisos.append(f"store-listing/{loc}: no es uno de los 17 idiomas del estudio (¿código de Play correcto?)")
            for n, lim in (("title.txt", 30), ("short_description.txt", 80), ("full_description.txt", 4000)):
                p = os.path.join(d, n)
                if not os.path.exists(p):
                    errores.append(f"store-listing/{loc}: falta {n}"); continue
                t = open(p, encoding="utf-8").read().strip()
                if not t or len(t) > lim:
                    errores.append(f"store-listing/{loc}/{n}: {len(t)} caracteres (límite {lim})")
            caps = glob.glob(os.path.join(d, "images", "phoneScreenshots", "*.png"))
            if len(caps) < 2:
                errores.append(f"store-listing/{loc}: {len(caps)} capturas (Play pide 2 o más)")
            if not os.path.exists(os.path.join(d, "images", "featureGraphic.png")):
                avisos.append(f"store-listing/{loc}: sin images/featureGraphic.png")
    for a in avisos:
        print("  aviso:", a)
    for e in errores:
        print("  ✗", e)
    print(f"{'✓ validar: sin errores' if not errores else f'✗ validar: {len(errores)} errores'} ({len(avisos)} avisos)")
    return not errores


def main():
    if len(sys.argv) < 3:
        sys.exit(__doc__)
    orden, juego = sys.argv[1], sys.argv[2]
    if orden == "todo":
        filtros(juego); selector(juego); home(juego); fuentes(juego); estado(juego)
        sys.exit(0 if validar(juego) else 1)
    f = {"estado": estado, "filtros": filtros, "selector": selector, "home": home, "fuentes": fuentes, "validar": validar}.get(orden)
    if not f:
        sys.exit(__doc__)
    r = f(juego)
    if orden == "validar" and not r:
        sys.exit(1)


if __name__ == "__main__":
    main()
