"""Agrega los textos del video ×10 ★ (regla del dueño 2026-11-12) al strings de un juego, en los 7 idiomas.
Uso: python textos_x10.py <carpeta del juego> [archivo=game_strings.xml]
No duplica: si un texto ya existe en un idioma, lo deja como está. El francés lleva el apóstrofo escapado."""
import os
import sys

T = {
    "x10Btn": {"en": "Watch a video: ×10 ★", "es": "Mira un video: ×10 ★", "pt": "Assista a um vídeo: ×10 ★", "fr": "Regarde une vidéo : ×10 ★",
               "de": "Video ansehen: ×10 ★", "pl": "Obejrzyj wideo: ×10 ★", "ru": "Посмотри видео: ×10 ★"},
    "x10Sub": {"en": "+{n} more ★ to spend", "es": "+{n} ★ más para gastar", "pt": "+{n} ★ a mais para gastar", "fr": "+{n} ★ en plus à dépenser",
               "de": "+{n} ★ extra zum Ausgeben", "pl": "+{n} ★ więcej do wydania", "ru": "ещё +{n} ★ на покупки"},
    "x10Done": {"en": "Stars multiplied!", "es": "¡Estrellas multiplicadas!", "pt": "Estrelas multiplicadas!", "fr": "Étoiles multipliées !",
                "de": "Sterne multipliziert!", "pl": "Gwiazdki pomnożone!", "ru": "Звёзды умножены!"},
    "x10Got": {"en": "×10! +{n} ★", "es": "¡×10! +{n} ★", "pt": "×10! +{n} ★", "fr": "×10 ! +{n} ★", "de": "×10! +{n} ★", "pl": "×10! +{n} ★", "ru": "×10! +{n} ★"},
}


def main():
    juego = sys.argv[1]
    archivo = sys.argv[2] if len(sys.argv) > 2 else "game_strings.xml"
    res = os.path.join(juego, "app", "src", "main", "res")
    for lang in ["en", "es", "pt", "fr", "de", "pl", "ru"]:
        p = os.path.join(res, "values" if lang == "en" else "values-" + lang, archivo)
        if not os.path.exists(p):
            print("falta", p); continue
        s = open(p, encoding="utf-8").read()
        nuevos = ""
        for k, v in T.items():
            if f'name="{k}"' in s: continue
            txt = v[lang].replace("&", "&amp;").replace("'", "\\'")
            nuevos += f'    <string name="{k}">{txt}</string>\n'
        if nuevos:
            s = s.replace("</resources>", nuevos + "</resources>")
            open(p, "w", encoding="utf-8", newline="\n").write(s)
    print("ok", juego)


if __name__ == "__main__":
    main()
