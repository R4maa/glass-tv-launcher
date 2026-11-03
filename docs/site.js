(function () {
  var reduce = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  document.documentElement.classList.add('js');

  // Scroll reveals: once per element.
  var reveals = document.querySelectorAll('.reveal');
  if ('IntersectionObserver' in window) {
    var ro = new IntersectionObserver(function (entries) {
      entries.forEach(function (e) {
        if (e.isIntersecting) { e.target.classList.add('in'); ro.unobserve(e.target); }
      });
    }, { rootMargin: '0px 0px -10% 0px' });
    reveals.forEach(function (el) { ro.observe(el); });
  } else {
    reveals.forEach(function (el) { el.classList.add('in'); });
  }

  // Tagline: each word lights up as it crosses a line 40% from the bottom.
  document.querySelectorAll('[data-words]').forEach(function (p) {
    var words = p.textContent.trim().split(/\s+/);
    p.textContent = '';
    words.forEach(function (w, i) {
      var s = document.createElement('span');
      s.className = 'w';
      s.textContent = w;
      p.appendChild(s);
      if (i < words.length - 1) p.appendChild(document.createTextNode(' '));
    });
    if (reduce || !('IntersectionObserver' in window)) {
      p.querySelectorAll('.w').forEach(function (s) { s.classList.add('on'); });
      return;
    }
    var wo = new IntersectionObserver(function (entries) {
      entries.forEach(function (e) {
        if (e.isIntersecting) { e.target.classList.add('on'); wo.unobserve(e.target); }
      });
    }, { rootMargin: '0px 0px -40% 0px' });
    p.querySelectorAll('.w').forEach(function (s) { wo.observe(s); });
  });

  // Mobile menu.
  var toggle = document.querySelector('.menu-toggle');
  var menu = document.getElementById('mobile-menu');
  function setMenu(open) {
    toggle.setAttribute('aria-expanded', String(open));
    toggle.setAttribute('aria-label', open ? 'Close menu' : 'Open menu');
    document.body.style.overflow = open ? 'hidden' : '';
    if (open) {
      menu.hidden = false;
      var items = menu.querySelectorAll('li');
      var step = Math.min(60, 400 / items.length);
      items.forEach(function (li, i) { li.style.transitionDelay = (i * step) + 'ms'; });
      requestAnimationFrame(function () { requestAnimationFrame(function () { menu.classList.add('open'); }); });
    } else {
      menu.classList.remove('open');
      menu.hidden = true;
    }
  }
  toggle.addEventListener('click', function () { setMenu(toggle.getAttribute('aria-expanded') !== 'true'); });
  menu.addEventListener('click', function (e) { if (e.target.closest('a')) setMenu(false); });
  document.addEventListener('keydown', function (e) { if (e.key === 'Escape' && !menu.hidden) { setMenu(false); toggle.focus(); } });

  // Current section in the nav.
  var navLinks = document.querySelectorAll('.nav-links a');
  if ('IntersectionObserver' in window) {
    var so = new IntersectionObserver(function (entries) {
      entries.forEach(function (e) {
        if (!e.isIntersecting) return;
        navLinks.forEach(function (a) {
          var on = a.getAttribute('href') === '#' + e.target.id;
          a.classList.toggle('active', on);
          if (on) a.setAttribute('aria-current', 'true'); else a.removeAttribute('aria-current');
        });
      });
    }, { rootMargin: '-45% 0px -50% 0px' });
    ['top', 'features', 'pond', 'setup', 'faq'].forEach(function (id) { var el = document.getElementById(id); if (el) so.observe(el); });
  }

  // Pond weather tabs.
  var img = document.getElementById('pond-img');
  var tabs = document.querySelectorAll('.pond-tabs [role="tab"]');
  function pick(tab) {
    tabs.forEach(function (t) { t.setAttribute('aria-selected', String(t === tab)); t.tabIndex = t === tab ? 0 : -1; });
    if (img.getAttribute('src') === tab.dataset.src) return;
    img.classList.add('fading');
    setTimeout(function () {
      img.onload = function () { img.classList.remove('fading'); };
      img.src = tab.dataset.src;
      img.alt = tab.dataset.alt;
    }, reduce ? 0 : 200);
  }
  tabs.forEach(function (t, i) {
    t.tabIndex = i === 0 ? 0 : -1;
    t.addEventListener('click', function () { pick(t); });
    t.addEventListener('keydown', function (e) {
      var d = e.key === 'ArrowRight' ? 1 : e.key === 'ArrowLeft' ? -1 : 0;
      if (!d) return;
      var next = tabs[(i + d + tabs.length) % tabs.length];
      next.focus(); pick(next);
    });
  });
  tabs.forEach(function (t) { var p = new Image(); p.src = t.dataset.src; });

  // Commands with the visitor's IP filled in.
  var codes = document.querySelectorAll('[data-cmd]');
  codes.forEach(function (c) { c.dataset.tpl = c.textContent; });
  function esc(s) { return s.replace(/[&<>]/g, function (ch) { return { '&': '&amp;', '<': '&lt;', '>': '&gt;' }[ch]; }); }
  function render(ip) {
    codes.forEach(function (c) {
      var parts = c.dataset.tpl.split('{ip}');
      c.innerHTML = parts.map(esc).join('<mark>' + esc(ip) + '</mark>');
      c.dataset.plain = c.dataset.tpl.split('{ip}').join(ip);
    });
  }
  render('192.168.1.50');

  var form = document.querySelector('.ip-form');
  var input = document.getElementById('ip');
  var err = document.getElementById('ip-error');
  function valid(v) {
    var m = v.match(/^(\d{1,3})\.(\d{1,3})\.(\d{1,3})\.(\d{1,3})$/);
    return !!m && m.slice(1).every(function (n) { return +n <= 255; });
  }
  form.addEventListener('submit', function (e) {
    e.preventDefault();
    var v = input.value.trim();
    if (!v) { showErr('Enter the IP address shown in Settings, My Fire TV, About, Network.'); return; }
    if (!valid(v)) { showErr('That is not an IP address. It looks like 192.168.1.50: four numbers from 0 to 255 with dots between.'); return; }
    err.hidden = true; input.removeAttribute('aria-invalid');
    render(v);
  });
  input.addEventListener('input', function () { if (!err.hidden && valid(input.value.trim())) { err.hidden = true; input.removeAttribute('aria-invalid'); } });
  function showErr(msg) { err.textContent = msg; err.hidden = false; input.setAttribute('aria-invalid', 'true'); input.focus(); }

  // Copy buttons.
  document.querySelectorAll('.copy').forEach(function (b) {
    var label = b.querySelector('span');
    b.addEventListener('click', function () {
      var text = b.parentNode.querySelector('[data-cmd]').dataset.plain;
      var done = function (ok) {
        b.classList.toggle('done', ok); b.classList.toggle('failed', !ok);
        label.textContent = ok ? 'Copied' : 'Copy failed, select the text';
        setTimeout(function () { b.classList.remove('done', 'failed'); label.textContent = 'Copy'; }, 2400);
      };
      if (navigator.clipboard) navigator.clipboard.writeText(text).then(function () { done(true); }, function () { done(false); });
      else done(false);
    });
  });
})();
