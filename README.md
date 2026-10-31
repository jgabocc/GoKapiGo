# GoKapiGo

Repo general del estudio GoKapiGo (Capibara Comercializadora C.A.).

- **Sitio:** https://jgabocc.github.io/GoKapiGo/
- **Política de privacidad común** (todos los juegos, 7 idiomas, válida en EE. UU., Europa y América Latina): https://jgabocc.github.io/GoKapiGo/privacy/
  - Elige sola el idioma del teléfono; también `privacy/es.html`, `en`, `pt`, `fr`, `de`, `pl`, `ru`.
  - Es la URL de `PRIVACY_URL` en cada juego y la que va en Play Console (Contenido de la app › Política de privacidad).
  - Contacto de privacidad: ayuda@capibaradev.com
- **Reglas del estudio:** [GOKAPIGO.md](GOKAPIGO.md) (cada juego guarda una copia).

## Cómo se publica

GitHub Actions (`.github/workflows/pages.yml`) corre `herramientas/generar_sitio.py` en cada push a `main` y publica `_site/` en GitHub Pages.

| Ruta | Qué es |
|---|---|
| `politica/<idioma>.html` | Texto de la política en cada idioma (solo el contenido). |
| `juegos.json` | Juegos y apps del estudio (paquete, repo, color, tipo). Fuente de `catalog.json` (portada GoKapiGo Home de los juegos): el orden es el del carrusel, `listed: false` lo oculta. |
| `componentes/gokapigo-home/` | Portada GoKapiGo Home nativa (Kotlin), copia de referencia igual en todos los juegos. |
| `landing/iconos/` | Icono de Play de cada juego (192 px), para el carrusel. |
| `landing/` | Portada del estudio (HTML, CSS, JS e imágenes). Se publica tal cual; sus estilos también visten la política. |
| `herramientas/generar_sitio.py` | Arma el sitio en `_site/` (portada + política en 7 idiomas). Para verlo en local: `python herramientas/generar_sitio.py`. |

Para cambiar la portada: edita `landing/` (si cambia el paquete de un juego, cambia su enlace a Google Play). Para cambiar la política: edita los 7 archivos de `politica/`, cambia la fecha de vigencia y haz push. Si el cambio es importante, avísalo dentro de los juegos.

## Juegos

| Juego | Paquete | Repo |
|---|---|---|
| Aqua Sorting | com.gokapigo.aquasorting | jgabocc/AquaSort |
| Sudoku Master 3000 | com.gokapigo.sudokumaster3000 | jgabocc/SUDOKUMASTER3000 |
| Shape Spin | com.gokapigo.shapespin | jgabocc/ShapeSpin |
| Parchis Party | com.gokapigo.parchisparty | jgabocc/Parchis-Party |
| Color Block Slide | com.gokapigo.colorblockslide | jgabocc/ColorBlockSlide |
| Nuts & Bolts Sort | com.gokapigo.nutsboltssort | jgabocc/Nuts-Bolts-Sort |
| Sand Block Puzzle | com.gokapigo.sandblockpuzzle | jgabocc/SandBlockPuzzle |
| Arrow Escape | com.gokapigo.arrowescape | jgabocc/ArrowScape |
| Pipe Connect | com.gokapigo.pipeconnect | jgabocc/PipeConnect |
| One Stroke | com.gokapigo.onestroke | jgabocc/OneStroke |
| Maze Quest | com.gokapigo.mazequest | jgabocc/MazeQuest |
| Shape Tiles | com.gokapigo.shapetiles | jgabocc/ShapeTiles |
| Solitaire Classic | com.gokapigo.solitaireclassic | jgabocc/SolitaireClassic |

## app-ads.txt

AdMob pide `app-ads.txt` en la **raíz** del dominio del desarrollador que figura en Play. Un sitio de proyecto (`jgabocc.github.io/GoKapiGo/`) no es raíz: para eso hace falta un dominio propio (por ejemplo `capibaradev.com`) o el repo `jgabocc.github.io`.
