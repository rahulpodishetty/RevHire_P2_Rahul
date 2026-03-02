/**
 * RevHire - Main Application JS
 */
document.addEventListener('DOMContentLoaded', function () {
    console.log('RevHire Application Initialized');

    // Smooth transitions for cards
    const cards = document.querySelectorAll('.card');
    cards.forEach((card, index) => {
        card.style.opacity = '0';
        card.style.transform = 'translateY(20px)';
        card.style.transition = 'all 0.5s cubic-bezier(0.4, 0, 0.2, 1)';

        setTimeout(() => {
            card.style.opacity = '1';
            card.style.transform = 'translateY(0)';
        }, index * 100);
    });

    // Logout Handler
    const logoutLinks = document.querySelectorAll('a[href="/auth/logout"], a[href$="/auth/logout"]');
    logoutLinks.forEach(link => {
        link.addEventListener('click', function (e) {
            e.preventDefault();
            // Clear storage
            localStorage.removeItem('jwtToken');
            localStorage.removeItem('userRole');
            localStorage.removeItem('userName');

            // Clear cookie
            document.cookie = 'jwtToken=; path=/; max-age=0; SameSite=Strict';

            // Redirect to login with logout flag
            window.location.href = '/login?logout=true';
        });
    });
});
