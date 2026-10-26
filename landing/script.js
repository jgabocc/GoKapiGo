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
