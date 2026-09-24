// Bootstrap-Farbmodus folgt dem Betriebssystem (hell/dunkel), auch wenn es sich zur Laufzeit ändert.
(function () {
    const query = window.matchMedia('(prefers-color-scheme: dark)');
    const apply = () => document.documentElement.setAttribute('data-bs-theme', query.matches ? 'dark' : 'light');
    apply();
    query.addEventListener('change', apply);
})();
