import { formatTimeAgo } from './timeUtils.js';
// Profile Posts JavaScript
class ProfilePostManager {
    constructor() {
        const url = new URL(window.location.href);     // URL hiện tại
        const path = url.pathname;                     // "/profile/jane_smith" hoặc "/profile/jane_smith/posts/123"

        const match = path.match(/^\/profile\/([^/]+)/);
        this.username = match ? match[1] : null;
        this.searchTimeout = null;
        this.setupEventListeners();

    }

    setupEventListeners() {
        // Search functionality
        const searchInput = document.getElementById('post-search-input');
        if (searchInput) {
            searchInput.addEventListener('input', (e) => this.handleSearch(e));
        }
    }

    // Search functionality
    handleSearch(event) {
        const keyword = event.target.value.trim();

        // Clear previous timeout
        if (this.searchTimeout) {
            clearTimeout(this.searchTimeout);
        }

        if (keyword.length === 0) {
            this.clearSearchResults();
            return;
        }

        // Debounce search
        this.searchTimeout = setTimeout(() => {
            this.searchPosts(keyword);
        }, 500);
    }

    async searchPosts(keyword) {
        const loadingEl = document.getElementById('search-loading');
        const resultsContainer = document.getElementById('search-results-container');
        const noResultsEl = document.getElementById('no-search-results');

        // Show loading
        loadingEl.style.display = 'block';
        resultsContainer.innerHTML = '';
        noResultsEl.style.display = 'none';

        try {
            const response = await fetch(`/posts/api/search?keyword=${encodeURIComponent(keyword)}&size=10&username=${this.username}`);
            const data = await response.json();

            if (data.content && data.content.length > 0) {

                data.content.forEach(post => {

                    const postElement = this.createSearchResultElement(post);

                    resultsContainer.insertAdjacentHTML('beforeend', postElement);
                });
            } else {
                noResultsEl.style.display = 'block';
            }
        } catch (error) {
            console.error('Error searching posts:', error);
            this.showNotification('Có lỗi xảy ra khi tìm kiếm', 'error');
        } finally {
            loadingEl.style.display = 'none';
        }
    }

    createSearchResultElement(post) {

        const timeAgo = formatTimeAgo(post.createdAt);

        const shortContent = post.content.length > 100 ?
            post.content.substring(0, 100) + '...' :
            post.content;

        return `
            <div class="search-result-item border-bottom py-3" onclick="profilePostManager.goToPost(${post.id})">
                <div class="d-flex">
                    <div class="flex-shrink-0 me-3">
                        <img src="${post.userAvatarUrl || '/images/default-avatar.jpg'}" 
                             class="rounded-circle" width="40" height="40" alt="Avatar">
                    </div>
                    <div class="flex-grow-1">
                        <div class="fw-bold">${post.userFullName}</div>
                        <div class="text-muted small">${timeAgo}</div>
                        <div class="mt-2">${shortContent}</div>
                        ${post.imageUrls && post.imageUrls.length > 0 ? `
                            <div class="mt-2">
                                <small class="text-muted">
                                    <i class="fas fa-image"></i> ${post.imageUrls.length} ảnh
                                </small>
                            </div>
                        ` : ''}
                    </div>
                </div>
            </div>
        `;
    }

    clearSearchResults() {
        const resultsContainer = document.getElementById('search-results-container');
        const noResultsEl = document.getElementById('no-search-results');

        if (resultsContainer) resultsContainer.innerHTML = '';
        if (noResultsEl) noResultsEl.style.display = 'none';
    }

    goToPost(postId) {
        // Close search modal
        const modal = bootstrap.Modal.getInstance(document.getElementById('searchPostsModal'));
        if (modal) {
            modal.hide();
        }

        // Scroll to post
        const postElement = document.querySelector(`[data-post-id="${postId}"]`);
        if (postElement) {
            postElement.scrollIntoView({behavior: 'smooth', block: 'center'});

            // Highlight post briefly
            postElement.style.backgroundColor = '#e3f2fd';
            setTimeout(() => {
                postElement.style.backgroundColor = '';
            }, 2000);
        }
    }

    // Photos functionality
    async loadPhotos() {
        const photosContainer = document.getElementById('profile-photos-container');
        const loadingEl = document.getElementById('photos-loading');
        const noPhotosEl = document.getElementById('no-photos');

        if (!photosContainer || !this.username) return;

        loadingEl.style.display = 'block';

        try {
            const response = await fetch(`/posts/api/user/${this.username}/photos`);
            const photos = await response.json();

            if (photos && photos.length > 0) {
                photos.forEach(photo => {
                    const photoElement = this.createPhotoElement(photo);
                    photosContainer.insertAdjacentHTML('beforeend', photoElement);
                });
                noPhotosEl.style.display = 'none';
            } else {
                noPhotosEl.style.display = 'block';
            }
        } catch (error) {
            console.error('Error loading photos:', error);
            this.showNotification('Không thể tải ảnh', 'error');
        } finally {
            loadingEl.style.display = 'none';
        }
    }

    createPhotoElement(photo) {
        return `
            <div class="photo-item" onclick="profilePostManager.viewImage('${photo.url}')">
                <img src="${photo.url}" alt="Photo from post" loading="lazy">
            </div>
        `;
    }

    showNotification(msg, type = 'info') {
        alert(msg); // hoặc custom UI notification
    }

}
document.addEventListener('DOMContentLoaded', function () {
    window.profilePostManager  = new ProfilePostManager();

});