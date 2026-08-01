(function () {
  const stored = localStorage.getItem('reconx-theme') || 'light';
  document.documentElement.dataset.theme = stored;

  document.addEventListener('DOMContentLoaded', () => {
    const btn = document.getElementById('theme-toggle');
    if (!btn) return;

    // sync aria-pressed to whatever theme was applied pre-paint
    btn.setAttribute('aria-pressed', String(document.documentElement.dataset.theme === 'dark'));

    btn.addEventListener('click', () => {
      const next = document.documentElement.dataset.theme === 'light' ? 'dark' : 'light';
      document.documentElement.dataset.theme = next;
      localStorage.setItem('reconx-theme', next);
      btn.setAttribute('aria-pressed', String(next === 'dark'));
    });
  });
})();