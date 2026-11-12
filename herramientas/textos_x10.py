"""Agrega los textos del video ×10 ★ (regla del dueño 2026-11-12) al strings de un juego, en los 15 idiomas
(los 8 nuevos solo si su carpeta values-* ya existe; ver idiomas.py / IDIOMAS.md).
Uso: python textos_x10.py <carpeta del juego> [archivo=game_strings.xml]
No duplica: si un texto ya existe en un idioma, lo deja como está. El francés lleva el apóstrofo escapado."""
import os
import sys

T = {
    "x10Btn": {"en": "Watch a video: ×10 ★", "es": "Mira un video: ×10 ★", "pt": "Assista a um vídeo: ×10 ★", "fr": "Regarde une vidéo : ×10 ★",
               "de": "Video ansehen: ×10 ★", "pl": "Obejrzyj wideo: ×10 ★", "ru": "Посмотри видео: ×10 ★",
               "th": "ดูวิดีโอ: ×10 ★", "ko": "동영상 보고 ×10 ★", "ja": "動画を見て×10 ★", "zh": "看视频：×10 ★",
               "hi": "वीडियो देखें: ×10 ★", "id": "Tonton video: ×10 ★", "tr": "Video izle: ×10 ★", "vi": "Xem video: ×10 ★"},
    "x10Sub": {"en": "+{n} more ★ to spend", "es": "+{n} ★ más para gastar", "pt": "+{n} ★ a mais para gastar", "fr": "+{n} ★ en plus à dépenser",
               "de": "+{n} ★ extra zum Ausgeben", "pl": "+{n} ★ więcej do wydania", "ru": "ещё +{n} ★ на покупки",
               "th": "ได้ ★ เพิ่มอีก +{n} ไว้ใช้", "ko": "쓸 수 있는 ★ +{n}개 추가", "ja": "使える★が+{n}個増える", "zh": "额外 +{n} ★ 可以花",
               "hi": "खर्च करने के लिए +{n} ★ और", "id": "+{n} ★ lagi untuk dibelanjakan", "tr": "Harcamak için +{n} ★ daha", "vi": "Thêm +{n} ★ để tiêu"},
    "x10Done": {"en": "Stars multiplied!", "es": "¡Estrellas multiplicadas!", "pt": "Estrelas multiplicadas!", "fr": "Étoiles multipliées !",
                "de": "Sterne multipliziert!", "pl": "Gwiazdki pomnożone!", "ru": "Звёзды умножены!",
                "th": "ดาวเพิ่มเป็น 10 เท่าแล้ว!", "ko": "별이 10배가 됐어요!", "ja": "スターが10倍に！", "zh": "星星翻了10倍！",
                "hi": "सितारे दस गुना हो गए!", "id": "Bintang jadi 10 kali lipat!", "tr": "Yıldızlar 10 katına çıktı!", "vi": "Sao đã nhân 10!"},
    "x10Got": {"en": "×10! +{n} ★", "es": "¡×10! +{n} ★", "pt": "×10! +{n} ★", "fr": "×10 ! +{n} ★", "de": "×10! +{n} ★", "pl": "×10! +{n} ★", "ru": "×10! +{n} ★",
               "th": "×10! +{n} ★", "ko": "×10! +{n} ★", "ja": "×10！ +{n} ★", "zh": "×10！+{n} ★",
               "hi": "×10! +{n} ★", "id": "×10! +{n} ★", "tr": "×10! +{n} ★", "vi": "×10! +{n} ★"},
}


def main():
    juego = sys.argv[1]
    archivo = sys.argv[2] if len(sys.argv) > 2 else "game_strings.xml"
    res = os.path.join(juego, "app", "src", "main", "res")
    carpeta = {"en": "values", "zh": "values-zh-rCN", "id": "values-in"}
    for lang in ["en", "es", "pt", "fr", "de", "pl", "ru", "th", "ko", "ja", "zh", "hi", "id", "tr", "vi"]:
        p = os.path.join(res, carpeta.get(lang, "values-" + lang), archivo)
        if not os.path.exists(p):
            if lang in ("en", "es", "pt", "fr", "de", "pl", "ru"): print("falta", p)
            continue
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
