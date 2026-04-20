// Intercept all fetch() calls to automatically include the XSRF-TOKEN cookie
// as the X-XSRF-TOKEN header, satisfying Spring Security's CookieCsrfTokenRepository.
(function () {
    function getCookie(name) {
        const match = document.cookie.match(new RegExp('(?:^|; )' + name + '=([^;]*)'));
        return match ? decodeURIComponent(match[1]) : null;
    }

    const originalFetch = window.fetch;
    window.fetch = function (url, options) {
        options = options || {};
        const method = (options.method || 'GET').toUpperCase();
        if (!['GET', 'HEAD', 'OPTIONS', 'TRACE'].includes(method)) {
            const xsrf = getCookie('XSRF-TOKEN');
            if (xsrf) {
                options.headers = Object.assign({}, options.headers, {'X-XSRF-TOKEN': xsrf});
            }
        }
        return originalFetch.call(this, url, options);
    };
})();
