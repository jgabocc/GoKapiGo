/* Marca GoKapiGo en el anuncio jugable: logo al arrancar y final con el carrusel de juegos (ver gokapigo_playable.py). */
(function () {
  var C = window.GKG_CFG, P = window.PLAYABLE;
  if (!C || !P) return;

  // 1. Logo al arrancar: el juego empieza (y corre su reloj de 15-30 s) cuando la pantalla de GoKapiGo se va
  function intro(cb) {
    var d = document.createElement("div"); d.id = "gkgIntro";
    d.innerHTML = '<img src="' + C.logo + '" alt="GoKapiGo">';
    document.body.appendChild(d);
    setTimeout(function () {
      d.className = "out";
      setTimeout(function () { d.parentNode && d.parentNode.removeChild(d); cb(); }, 450);
    }, 1500);
  }
  var listo = P.ready;
  P.ready = function (cb) {
    var empezar = function () { intro(cb); };
    if (listo) listo(empezar); else empezar();
  };

  // 2. Final: cuando el juego muestra su #playEnd, lo tapa el final común (con sus mismos textos ya traducidos)
  var hecho = false;
  function revisar() {
    var e = document.getElementById("playEnd");
    if (hecho || !e || e.hidden || getComputedStyle(e).display === "none") return;
    hecho = true; final(e);
  }
  function texto(e, sel, def) { var n = e.querySelector(sel); return n ? n.textContent.replace(/\s+/g, " ").trim() : def; }
  function esc(s) { return s.replace(/[&<>"]/g, function (c) { return { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;" }[c]; }); }

  function final(e) {
    var cta = texto(e, ".play, .cta, button", "Play"), msg = texto(e, "h2", "");
    // carrusel: los demás juegos tres vueltas y al final el nuestro
    var lista = [];
    for (var v = 0; v < 3; v++) lista = lista.concat(C.icons);
    var html = "";
    for (var i = 0; i < lista.length; i++) html += '<img src="' + lista[i] + '" alt="">';
    html += '<img class="me" src="' + C.me + '" alt="' + esc(C.name) + '">';
    for (var k = 0; k < Math.min(4, C.icons.length); k++) html += '<img src="' + C.icons[k] + '" alt="">';   // a la derecha del nuestro, para que no quede vacío
    var f = document.createElement("section"); f.id = "gkgEnd";
    f.innerHTML = '<img class="gk" src="' + C.logo + '" alt="GoKapiGo"><div class="strip"><div class="reel">' + html +
      '</div></div><h1 class="nm">' + esc(C.name) + '</h1>' + (msg ? "<h2>" + esc(msg) + "</h2>" : "") +
      '<button class="cta" type="button">' + esc(cta) + "</button>";
    f.onclick = function () { try { P.install(); } catch (_) {} };
    document.body.appendChild(f);

    var strip = f.querySelector(".strip"), reel = f.querySelector(".reel"), yo = f.querySelector(".me");
    var s = Math.round(Math.min(Math.max(window.innerWidth, window.innerHeight) * .11, Math.min(window.innerWidth, window.innerHeight) * (window.innerWidth > window.innerHeight ? .15 : .2), 104));
    var g = Math.round(s * .22);
    f.style.setProperty("--s", s + "px"); f.style.setProperty("--g", g + "px");
    var centro = function (k) { return strip.offsetWidth / 2 - (k * (s + g) + s / 2); };
    reel.style.transform = "translate(" + centro(0) + "px,-50%)";
    void reel.offsetWidth;
    reel.style.transition = "transform 2.8s cubic-bezier(.1,.62,.12,1)";
    reel.style.transform = "translate(" + centro(lista.length) + "px,-50%)";
    var fin = function () { yo.className = "me on"; f.className = "ok"; };
    reel.addEventListener("transitionend", fin, { once: true });
    setTimeout(fin, 3100);          // por si el navegador no avisa del final de la animación
  }

  new MutationObserver(revisar).observe(document.documentElement, { subtree: true, childList: true, attributes: true, attributeFilter: ["hidden", "style", "class"] });
})();
