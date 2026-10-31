"""Arma el sitio del estudio (GitHub Pages) en _site/:
- landing/: portada del estudio (index.html, styles.css, script.js, img/) tal cual.
- privacy/index.html: elige el idioma del navegador (o ?lang=xx) y abre privacy/<idioma>.html (inglés si no es uno de los 7).
- privacy/<idioma>.html: la política común, desde politica/<idioma>.html, con la barra y el pie de la portada.
- delete-data/index.html: cómo pedir el borrado de datos (es + en), para Play Console.
- privacidad.html: enlace antiguo, lleva a privacy/es.html.
- catalog.json: catálogo de la portada GoKapiGo Home de los juegos (herramientas/catalogo.py; iconos en landing/iconos/).
Uso: python herramientas/generar_sitio.py   (lo corre GitHub Actions en cada push a main)
"""
import html, json, os, shutil

import catalogo

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "_site")
EMAIL = "ayuda@capibaradev.com"
LANGS = {"en": "English", "es": "Español", "pt": "Português", "fr": "Français", "de": "Deutsch", "pl": "Polski", "ru": "Русский"}
# título, "Juegos", "Privacidad", "Volver a los juegos"
UI = {
    "en": ("Privacy Policy", "Games", "Privacy", "Back to the games"),
    "es": ("Política de privacidad", "Juegos", "Privacidad", "Volver a los juegos"),
    "pt": ("Política de Privacidade", "Jogos", "Privacidade", "Voltar aos jogos"),
    "fr": ("Politique de confidentialité", "Jeux", "Confidentialité", "Retour aux jeux"),
    "de": ("Datenschutzerklärung", "Spiele", "Datenschutz", "Zurück zu den Spielen"),
    "pl": ("Polityka prywatności", "Gry", "Prywatność", "Wróć do gier"),
    "ru": ("Политика конфиденциальности", "Игры", "Конфиденциальность", "Вернуться к играм"),
}


def head(title, lang, desc, base):
    return f"""<!doctype html>
<html lang="{lang}">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover">
  <title>{html.escape(title)}</title>
  <meta name="description" content="{html.escape(desc)}">
  <meta name="theme-color" content="#0f0c09">
  <link rel="icon" href="{base}img/favicon.png">
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Russo+One&family=Rubik:wght@400;500;600&display=swap">
  <link rel="stylesheet" href="{base}styles.css">
</head>
<body>
"""


def topbar(lang, base):
    _, games, privacy, _ = UI[lang]
    return f"""<header class="topbar">
  <div class="wrap">
    <a class="brand" href="{base}index.html" aria-label="GoKapiGo">
      <img src="{base}img/favicon.png" alt="">
      <span><b>GO</b> KAPI <b>GO</b></span>
    </a>
    <nav class="nav">
      <a href="{base}index.html#juegos">{games}</a>
      <a href="{base}privacy/" aria-current="page">{privacy}</a>
    </nav>
  </div>
</header>
"""


def footer(lang, base):
    return f"""<footer class="footer">
  <div class="wrap">
    <span>© <span id="year">2026</span> GoKapiGo · Capibara Comercializadora C.A. · Mérida, Venezuela · <a href="mailto:{EMAIL}">{EMAIL}</a></span>
    <a href="{base}index.html">{UI[lang][3]}</a>
  </div>
</footer>
<script src="{base}script.js"></script>
</body>
</html>
"""


def main():
    if os.path.exists(OUT): shutil.rmtree(OUT)
    shutil.copytree(os.path.join(ROOT, "landing"), OUT)
    os.makedirs(os.path.join(OUT, "privacy"), exist_ok=True)

    # política por idioma
    for k in LANGS:
        body = open(os.path.join(ROOT, "politica", f"{k}.html"), encoding="utf-8").read()
        langs = '<nav class="langs">' + "".join(
            f'<a href="{c}.html" lang="{c}" class="{"on" if c == k else ""}">{n}</a>' for c, n in LANGS.items()) + "</nav>"
        out = (head(f"{UI[k][0]} · GoKapiGo", k, UI[k][0], "../") + topbar(k, "../")
               + f'<main class="legal"><div class="wrap"><article>{langs}{body}</article></div></main>\n' + footer(k, "../"))
        open(os.path.join(OUT, "privacy", f"{k}.html"), "w", encoding="utf-8", newline="\n").write(out)

    # privacy/: elige idioma (navegador o ?lang=xx); sin JS muestra los enlaces
    links = "".join(f'<li><a href="{k}.html" lang="{k}">{UI[k][0]} ({v})</a></li>' for k, v in LANGS.items())
    script = """<script>(function(){var L=%s,q=(location.search.match(/[?&]lang=([a-z]{2})/)||[])[1],
c=[q].concat(navigator.languages||[navigator.language||"en"]);for(var i=0;i<c.length;i++){var k=String(c[i]||"").slice(0,2).toLowerCase();
if(L.indexOf(k)>=0){location.replace(k+".html"+location.hash);return}}location.replace("en.html"+location.hash)})();</script>
""" % json.dumps(list(LANGS))
    open(os.path.join(OUT, "privacy", "index.html"), "w", encoding="utf-8", newline="\n").write(
        head("Privacy Policy · GoKapiGo", "en", "GoKapiGo privacy policy", "../") + script + topbar("en", "../")
        + f'<main class="legal"><div class="wrap"><article><h1>Privacy Policy</h1><ul>{links}</ul></article></div></main>\n' + footer("en", "../"))

    # delete-data/: cómo pedir el borrado de datos (URL de "Seguridad de los datos" en Play Console), es + en
    os.makedirs(os.path.join(OUT, "delete-data"), exist_ok=True)
    body = open(os.path.join(ROOT, "politica", "eliminar-datos.html"), encoding="utf-8").read()
    open(os.path.join(OUT, "delete-data", "index.html"), "w", encoding="utf-8", newline="\n").write(
        head("Eliminar tus datos · Delete your data · GoKapiGo", "es", "Cómo eliminar tus datos de los juegos y apps de GoKapiGo", "../")
        + topbar("es", "../") + f'<main class="legal"><div class="wrap"><article>{body}</article></div></main>\n' + footer("es", "../"))

    # enlace antiguo de la portada
    open(os.path.join(OUT, "privacidad.html"), "w", encoding="utf-8", newline="\n").write(
        '<!doctype html><meta charset="utf-8"><meta http-equiv="refresh" content="0; url=privacy/es.html">'
        '<link rel="canonical" href="privacy/es.html"><a href="privacy/es.html">Política de privacidad</a>\n')
    # catalog.json de la portada GoKapiGo Home (comprueba en Play qué juegos ya son públicos)
    print("catálogo GoKapiGo Home:")
    catalogo.generar(os.path.join(OUT, "catalog.json"))
    print("sitio en", OUT)


if __name__ == "__main__":
    main()
