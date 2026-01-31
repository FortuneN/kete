(function () {
  const textSelector = 'body > div.md-container > main > div > div.md-sidebar.md-sidebar--primary > div > div > nav > ul > li.md-nav__item.md-nav__item--active > a > span';
  const sidebarSelector = 'body > div.md-container > main > div > div.md-sidebar.md-sidebar--primary';

  function apply() {
    const span = document.querySelector(textSelector);
    const sidebar = document.querySelector(sidebarSelector);

    if (!span || !sidebar) {
      return;
    }

    const text = (span.textContent || "").trim();

    if (text === "Home") {
      sidebar.style.display = "none";
    }
  }

  // initial
  apply();

  // dynamic (MkDocs / SPA re-renders)
  const observer = new MutationObserver(() => apply());

  observer.observe(document.documentElement, {
    subtree: true,
    childList: true,
    characterData: true,
  });

  window.addEventListener("popstate", apply);
  window.addEventListener("hashchange", apply);
})();

