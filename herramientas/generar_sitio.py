"""Arma el sitio del estudio (GitHub Pages) en _site/:
- index.html: GoKapiGo y sus juegos (enlaces a Google Play).
- privacy/index.html: elige el idioma del navegador y abre privacy/<idioma>.html (inglés si no es uno de los 7).
- privacy/<idioma>.html: la política común, desde politica/<idioma>.html.
Uso: python herramientas/generar_sitio.py   (lo corre GitHub Actions en cada push a main)
"""
import html, json, os, shutil

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "_site")
LANGS = {"en": "English", "es": "Español", "pt": "Português", "fr": "Français", "de": "Deutsch", "pl": "Polski", "ru": "Русский"}
TITLES = {"en": "Privacy Policy", "es": "Política de privacidad", "pt": "Política de Privacidade", "fr": "Politique de confidentialité",
          "de": "Datenschutzerklärung", "pl": "Polityka prywatności", "ru": "Политика конфиденциальности"}
GAMES = json.load(open(os.path.join(ROOT, "juegos.json"), encoding="utf-8"))

CSS = """
:root{--bg:#FFF8F1;--paper:#FFFFFF;--ink:#24170D;--muted:#6E5A4A;--line:#F0DFCE;--accent:#E8740C;--accent-ink:#FFFFFF;--soft:#FDEBD8}
@media (prefers-color-scheme:dark){:root{--bg:#1E1006;--paper:#2A1709;--ink:#F6EBDF;--muted:#C7B19D;--line:#4A2E17;--accent:#FF8A1F;--accent-ink:#1E1006;--soft:#3A1F0B}}
*{box-sizing:border-box}html{-webkit-text-size-adjust:100%}
body{margin:0;background:var(--bg);color:var(--ink);font:16px/1.6 system-ui,-apple-system,"Segoe UI",Roboto,sans-serif}
a{color:var(--accent)}
.wrap{max-width:820px;margin:0 auto;padding:24px 16px 64px}
header.top{display:flex;align-items:center;gap:12px;justify-content:space-between;flex-wrap:wrap;margin-bottom:16px}
.brand{display:flex;align-items:center;gap:10px;text-decoration:none;color:var(--ink);font-weight:800;font-size:20px}
.brand img{width:44px;height:44px;border-radius:12px;background:#3A1A05}
.langs{display:flex;gap:6px;flex-wrap:wrap}
.langs a{padding:4px 10px;border-radius:999px;border:1px solid var(--line);text-decoration:none;color:var(--muted);font-size:14px}
.langs a.on{background:var(--accent);border-color:var(--accent);color:var(--accent-ink)}
article{background:var(--paper);border:1px solid var(--line);border-radius:18px;padding:24px 22px}
h1{font-size:30px;line-height:1.2;margin:0 0 4px}h2{font-size:21px;margin:28px 0 8px}h3{font-size:17px;margin:18px 0 4px}
.meta{color:var(--muted);margin:0 0 16px;font-size:14px}
.summary{background:var(--soft);border-radius:14px;padding:4px 18px 8px;margin:16px 0}
.summary h2{margin-top:14px}
li{margin:4px 0}
footer{color:var(--muted);font-size:14px;text-align:center;margin-top:28px}
.hero{text-align:center;padding:12px 0 8px}.hero img{width:min(260px,70vw);height:auto;border-radius:28px;background:#3A1A05}
.hero p{color:var(--muted);margin:6px 0 0}
.games{display:grid;grid-template-columns:repeat(auto-fill,minmax(150px,1fr));gap:14px;margin:24px 0}
.game{background:var(--paper);border:1px solid var(--line);border-radius:18px;padding:14px;text-align:center;text-decoration:none;color:var(--ink);font-weight:700}
.game img{width:96px;height:96px;border-radius:22px;display:block;margin:0 auto 8px}
.links{display:flex;gap:12px;justify-content:center;flex-wrap:wrap}
.btn{display:inline-block;padding:10px 18px;border-radius:999px;background:var(--accent);color:var(--accent-ink);text-decoration:none;font-weight:700}
"""


def page(title, body, lang="en", desc=""):
    return f"""<!doctype html>
<html lang="{lang}">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>{html.escape(title)}</title>
<meta name="description" content="{html.escape(desc)}">
<link rel="icon" href="/GoKapiGo/favicon.png">
<style>{CSS}</style>
</head>
<body>
<div class="wrap">
{body}
<footer>© GoKapiGo · Capibara Comercializadora C.A. · <a href="mailto:ayuda@capibaradev.com">ayuda@capibaradev.com</a></footer>
</div>
</body>
</html>
"""


def header(active_lang=None, base="../"):
    langs = ""
    if active_lang:
        langs = '<nav class="langs">' + "".join(
            f'<a href="{k}.html" lang="{k}" class="{"on" if k == active_lang else ""}">{v}</a>' for k, v in LANGS.items()) + "</nav>"
    return f'<header class="top"><a class="brand" href="{base}"><img src="{base}img/gokapigo.webp" alt="">GoKapiGo</a>{langs}</header>'


def main():
    if os.path.exists(OUT): shutil.rmtree(OUT)
    shutil.copytree(os.path.join(ROOT, "site"), OUT)
    os.makedirs(os.path.join(OUT, "privacy"), exist_ok=True)

    # política por idioma
    for k in LANGS:
        body = open(os.path.join(ROOT, "politica", f"{k}.html"), encoding="utf-8").read()
        out = page(f"{TITLES[k]} · GoKapiGo", header(k) + f"<article>{body}</article>", k, TITLES[k])
        open(os.path.join(OUT, "privacy", f"{k}.html"), "w", encoding="utf-8", newline="\n").write(out)

    # privacy/: elige idioma (navegador o ?lang=xx); sin JS muestra los enlaces
    links = "".join(f'<li><a href="{k}.html" lang="{k}">{TITLES[k]} ({v})</a></li>' for k, v in LANGS.items())
    script = """<script>(function(){var L=%s,q=(location.search.match(/[?&]lang=([a-z]{2})/)||[])[1],
c=[q].concat(navigator.languages||[navigator.language||"en"]);for(var i=0;i<c.length;i++){var k=String(c[i]||"").slice(0,2).toLowerCase();
if(L.indexOf(k)>=0){location.replace(k+".html"+location.hash);return}}location.replace("en.html"+location.hash)})();</script>""" % json.dumps(list(LANGS))
    open(os.path.join(OUT, "privacy", "index.html"), "w", encoding="utf-8", newline="\n").write(
        page("Privacy Policy · GoKapiGo", header() + script + f"<article><h1>Privacy Policy</h1><ul>{links}</ul></article>",
             desc="GoKapiGo privacy policy"))

    # inicio del estudio
    cards = "".join(
        f'<a class="game" href="https://play.google.com/store/apps/details?id={g["package"]}"><img src="img/{g["id"]}.webp" alt="">{html.escape(g["name"])}</a>'
        for g in GAMES)
    home = f"""{header(base="./")}
<section class="hero"><img src="img/gokapigo.webp" alt="GoKapiGo"><p>Casual games made to play in one tap.</p></section>
<div class="games">{cards}</div>
<div class="links"><a class="btn" href="privacy/">Privacy Policy</a></div>"""
    open(os.path.join(OUT, "index.html"), "w", encoding="utf-8", newline="\n").write(
        page("GoKapiGo — Casual games", home, desc="GoKapiGo casual games for Android"))
    print("sitio en", OUT)


if __name__ == "__main__":
    main()
