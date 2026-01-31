(function () {
  const currentOrigin = window.location.origin;

  function shouldSkip(href) {
    if (!href) return true;

    const trimmed = href.trim();

    return (
      trimmed === "" ||
      trimmed.startsWith("#") ||
      trimmed.startsWith("mailto:") ||
      trimmed.startsWith("tel:") ||
      trimmed.startsWith("javascript:")
    );
  }

  function applyToLink(link) {
    if (!(link instanceof HTMLAnchorElement)) return;

    const href = link.getAttribute("href");
    if (shouldSkip(href)) return;

    let url;
    try {
      url = new URL(href, window.location.href); // handles relative + protocol-relative
    } catch {
      return;
    }

    if (url.protocol !== "http:" && url.protocol !== "https:") return;

    const isExternal = url.origin !== currentOrigin;

    if (isExternal) {
      link.target = "_blank";
      link.rel = "noopener noreferrer";
    } else {
      // optional: remove if it becomes internal later
      if (link.target === "_blank") link.removeAttribute("target");
      if (link.rel === "noopener noreferrer") link.removeAttribute("rel");
    }
  }

  function applyToNode(node) {
    if (node instanceof HTMLAnchorElement) {
      applyToLink(node);
      return;
    }

    if (node instanceof Element) {
      node.querySelectorAll("a[href]").forEach(applyToLink);
    }
  }

  // Initial pass
  document.querySelectorAll("a[href]").forEach(applyToLink);

  // Dynamic updates
  const observer = new MutationObserver((mutations) => {
    for (const m of mutations) {
      if (m.type === "childList") {
        m.addedNodes.forEach(applyToNode);
      } else if (m.type === "attributes") {
        applyToLink(m.target);
      }
    }
  });

  observer.observe(document.documentElement, {
    subtree: true,
    childList: true,
    attributes: true,
    attributeFilter: ["href"],
  });
})();
