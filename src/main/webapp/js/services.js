/**
 * Interactive Style Gallery Switcher (v2 - Editorial Layout)
 * Swaps image, updates active thumbnail, and animates the style badge.
 */
function switchStyle(serviceId, newImgSrc, thumbnailElement, styleName) {
    const mainImg = document.getElementById(`img-${serviceId}`);
    const badge = document.getElementById(`badge-${serviceId}`);
    if (!mainImg) return;

    // Prevent redundant switching
    if (mainImg.src.includes(newImgSrc)) return;

    // Transition effect for image
    mainImg.style.opacity = '0.3';
    mainImg.style.transform = 'scale(0.98)';

    // Update Badge
    if (badge) {
        badge.classList.remove('show');
        setTimeout(() => {
            badge.textContent = styleName;
            badge.classList.add('show');
        }, 150);
    }

    setTimeout(() => {
        mainImg.src = newImgSrc;
        mainImg.style.opacity = '1';
        mainImg.style.transform = 'scale(1)';
    }, 250);

    // Update active thumbnail
    const selector = thumbnailElement.parentElement;
    const thumbs = selector.querySelectorAll('.style-thumb');
    thumbs.forEach(t => t.classList.remove('active'));
    thumbnailElement.classList.add('active');
}

// Initialize badges on load
document.addEventListener('DOMContentLoaded', () => {
    const badges = document.querySelectorAll('.style-badge');
    badges.forEach((badge, index) => {
        setTimeout(() => {
            badge.classList.add('show');
        }, 200 * (index + 1));
    });
});
