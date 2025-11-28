document.addEventListener("DOMContentLoaded", function () {
    document.querySelectorAll("form").forEach(form => {
        form.addEventListener("submit", function () {
            const btn = form.querySelector("button[type='submit']");
            const btnContent = btn.querySelector(".btnContent");
            const spinner = btn.querySelector(".loadingSpinner");

            if (btn && spinner && btnContent) {
                btnContent.classList.add("d-none");
                spinner.classList.remove("d-none");
                btn.disabled = true;
            }

            // Hiển thị overlay toàn trang
            const pageOverlay = document.getElementById("pageLoadingOverlay");
            if (pageOverlay) {
                pageOverlay.classList.remove("d-none");
            }
        });
    });
});