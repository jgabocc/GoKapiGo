// GoKapiGo — JS mínimo: año del pie y botón de copiar correo en la política.
(function () {
  var y = document.getElementById('year');
  if (y) y.textContent = new Date().getFullYear();

  document.querySelectorAll('.copy[data-copy]').forEach(function (btn) {
    btn.addEventListener('click', function () {
      var text = btn.getAttribute('data-copy');
      var done = function () {
        var old = btn.textContent;
        btn.textContent = 'Copiado';
        setTimeout(function () { btn.textContent = old; }, 1600);
      };
      var fallback = function () {
        var el = document.getElementById(btn.getAttribute('data-target'));
        if (!el) return;
        var r = document.createRange(); r.selectNodeContents(el);
        var s = window.getSelection(); s.removeAllRanges(); s.addRange(r);
      };
      try {
        navigator.clipboard.writeText(text).then(done, fallback);
      } catch (e) { fallback(); }
    });
  });
})();

// Portada: cuántos juegos hay y la cinta infinita con sus iconos (duplicada para que el bucle no se note).
(function () {
  var cards = document.querySelectorAll('#juegos .card');
  var n = document.querySelector('[data-count="juegos"]');
  if (n && cards.length) n.textContent = cards.length;
  var track = document.querySelector('.marquee .track');
  if (!track) return;
  var imgs = Array.prototype.map.call(document.querySelectorAll('#juegos .card .icon img, #apps .card .icon img'), function (i) { return i.getAttribute('src'); });
  var html = imgs.map(function (s) { return '<img src="' + s + '" alt="" width="56" height="56" loading="lazy">'; }).join('');
  track.innerHTML = html + html;
})();
