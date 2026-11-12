# Notas de la versión 1.1 de cada juego en el formato de Play Console (<es-419>…</es-419>): <juego>/salida/notas-1.1.txt
# Uso: python GoKapiGo/herramientas/notas_version.py
# Desde 2026-11-12 la ficha tiene hasta 17 idiomas: las notas nuevas (notas-<ver>.txt) llevan también
# <th> <ko-KR> <ja-JP> <zh-CN> <hi-IN> <id> <tr-TR> <vi> (lista en play.py, IDIOMAS_PLAY). play.py sube solo las
# de los idiomas que la ficha tiene y avisa de los que faltan. Este script es el de la 1.1 (9 idiomas) y queda así.
import os

BASE = {
    "es-419": "Nueva portada GoKapiGo: descubre todos nuestros juegos y apps desde el inicio. El icono del juego ahora es el mismo de Google Play.",
    "es-ES": "Nueva portada GoKapiGo: descubre todos nuestros juegos y apps desde el inicio. El icono del juego ahora es el mismo de Google Play.",
    "en-US": "New GoKapiGo home screen: discover all our games and apps right from the start. The game icon now matches Google Play.",
    "pt-BR": "Nova tela inicial GoKapiGo: descubra todos os nossos jogos e apps logo no início. O ícone do jogo agora é o mesmo do Google Play.",
    "pt-PT": "Novo ecrã inicial GoKapiGo: descobre todos os nossos jogos e apps logo no início. O ícone do jogo agora é o mesmo do Google Play.",
    "fr-FR": "Nouvel écran d'accueil GoKapiGo : découvre tous nos jeux et applis dès le départ. L'icône du jeu est désormais la même que sur Google Play.",
    "de-DE": "Neuer GoKapiGo-Startbildschirm: Entdecke alle unsere Spiele und Apps direkt beim Start. Das Spielsymbol entspricht jetzt dem bei Google Play.",
    "pl-PL": "Nowy ekran startowy GoKapiGo: odkryj wszystkie nasze gry i aplikacje od razu na starcie. Ikona gry jest teraz taka sama jak w Google Play.",
    "ru-RU": "Новый стартовый экран GoKapiGo: все наши игры и приложения сразу при запуске. Значок игры теперь такой же, как в Google Play.",
}
EXTRA = {
    "SandBlockPuzzle": {
        "es-419": "Tablero de arena fina con gravedad real, limpieza sin residuos y aviso cuando estás a punto de completar una línea. Arena más liviana y de color parejo.",
        "es-ES": "Tablero de arena fina con gravedad real, limpieza sin residuos y aviso cuando estás a punto de completar una línea. Arena más ligera y de color uniforme.",
        "en-US": "Fine sand board with real gravity, clean line clears and a hint when a line is about to complete. Lighter sand with an even color.",
        "pt-BR": "Tabuleiro de areia fina com gravidade real, limpeza sem resíduos e aviso quando uma linha está quase completa. Areia mais leve e de cor uniforme.",
        "pt-PT": "Tabuleiro de areia fina com gravidade real, limpeza sem resíduos e aviso quando uma linha está quase completa. Areia mais leve e de cor uniforme.",
        "fr-FR": "Plateau de sable fin avec une vraie gravité, lignes nettoyées sans résidus et alerte quand une ligne est presque complète. Sable plus léger et de couleur uniforme.",
        "de-DE": "Feiner Sand mit echter Schwerkraft, saubere Linien ohne Reste und ein Hinweis, wenn eine Linie fast voll ist. Leichterer Sand in gleichmäßiger Farbe.",
        "pl-PL": "Plansza z drobnym piaskiem i prawdziwą grawitacją, czyste linie bez resztek i podpowiedź, gdy linia jest prawie pełna. Lżejszy piasek w jednolitym kolorze.",
        "ru-RU": "Поле из мелкого песка с настоящей гравитацией, чистые линии без остатков и подсказка, когда линия почти собрана. Песок легче и ровнее по цвету.",
    },
    "MazeQuest": {
        "es-419": "Tu personaje ahora se aplasta con rebote al chocar contra los muros.",
        "es-ES": "Tu personaje ahora se aplasta con rebote al chocar contra los muros.",
        "en-US": "Your character now squishes and bounces when hitting walls.",
        "pt-BR": "Seu personagem agora se achata e quica ao bater nas paredes.",
        "pt-PT": "O teu personagem agora achata-se e ressalta ao bater nas paredes.",
        "fr-FR": "Ton personnage s'écrase et rebondit désormais contre les murs.",
        "de-DE": "Deine Figur wird jetzt gequetscht und federt zurück, wenn sie gegen Wände stößt.",
        "pl-PL": "Twoja postać teraz spłaszcza się i odbija przy uderzeniu w ścianę.",
        "ru-RU": "Теперь персонаж сплющивается и отскакивает при ударе о стену.",
    },
    "OneStroke": {
        "es-419": "Nuevo icono: un corazón de un solo trazo.", "es-ES": "Nuevo icono: un corazón de un solo trazo.",
        "en-US": "New icon: a one-stroke heart.", "pt-BR": "Novo ícone: um coração de um só traço.", "pt-PT": "Novo ícone: um coração de um só traço.",
        "fr-FR": "Nouvelle icône : un cœur d'un seul trait.", "de-DE": "Neues Symbol: ein Herz in einem Zug.",
        "pl-PL": "Nowa ikona: serce jednym pociągnięciem.", "ru-RU": "Новый значок: сердце одним штрихом.",
    },
}
JUEGOS = ["SandBlockPuzzle", "AquaSorting", "NutsBoltsSort", "ColorBlockSlide", "SudokuMaster3000", "ShapeSpin", "ParchisParty",
          "PipeConnect", "OneStroke", "MazeQuest", "ShapeTiles", "SolitaireClassic"]

RAIZ = os.path.join(os.path.dirname(__file__), "..", "..")
CARPETA = {"SandBlockPuzzle": "sand-block-puzzle", "AquaSorting": "AquaSorting", "NutsBoltsSort": "nuts-bolts-sort", "ColorBlockSlide": "color-block-slide",
           "SudokuMaster3000": "SUDOKUMaster3000", "ShapeSpin": "ShapeSpin", "ParchisParty": "parchis-party", "PipeConnect": "pipe-connect",
           "OneStroke": "one-stroke", "MazeQuest": "maze-quest", "ShapeTiles": "shape-tiles", "SolitaireClassic": "solitaire-classic"}
for j in JUEGOS:
    partes = []
    for loc, texto in BASE.items():
        t = (EXTRA[j][loc] + "\n" + texto) if j in EXTRA else texto
        assert len(t) <= 500, (j, loc, len(t))
        partes.append(f"<{loc}>\n{t}\n</{loc}>")
    open(os.path.join(RAIZ, CARPETA[j], "salida", "notas-1.1.txt"), "w", encoding="utf-8", newline="\n").write("\n".join(partes) + "\n")
print("listo: salida/notas-1.1.txt en", len(JUEGOS), "juegos")
