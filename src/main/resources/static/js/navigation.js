(() => {
    const body = document.body;
    const toggle = document.querySelector("[data-menu-toggle]");
    const close = document.querySelector("[data-menu-close]");

    if (!toggle || !close) {
        return;
    }

    const setMenuOpen = (isOpen) => {
        body.classList.toggle("menu-open", isOpen);
        toggle.setAttribute("aria-expanded", String(isOpen));
        toggle.setAttribute("aria-label", isOpen ? "Đóng menu" : "Mở menu");
    };

    toggle.addEventListener("click", () => {
        setMenuOpen(toggle.getAttribute("aria-expanded") !== "true");
    });
    close.addEventListener("click", () => setMenuOpen(false));
    document.addEventListener("keydown", (event) => {
        if (event.key === "Escape") {
            setMenuOpen(false);
        }
    });
    document.querySelectorAll(".nav-link").forEach((link) => {
        link.addEventListener("click", () => setMenuOpen(false));
    });
})();
