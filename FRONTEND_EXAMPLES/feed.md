---
layout: post
title: Social Feed - View Only
permalink: /social-feed
---

<style>
/* Social Feed Styling */
.feed-container {
    max-width: 900px;
    margin: 0 auto;
    padding: 20px;
}

.feed-header {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    padding: 30px;
    border-radius: 12px;
    margin-bottom: 30px;
    text-align: center;
}

.feed-header h1 {
    margin: 0;
    font-size: 2.5em;
}

.filter-controls {
    background: white;
    padding: 20px;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    margin-bottom: 25px;
    display: flex;
    gap: 15px;
    flex-wrap: wrap;
    align-items: center;
}

.filter-controls input,
.filter-controls select {
    padding: 10px;
    border: 1px solid #ddd;
    border-radius: 6px;
    flex: 1;
    min-width: 200px;
}

.filter-controls button {
    background-color: #667eea;
    color: white;
    border: none;
    padding: 10px 20px;
    border-radius: 6px;
    cursor: pointer;
}

.filter-controls button:hover {
    background-color: #5568d3;
}

.stats-bar {
    display: flex;
    gap: 15px;
    margin-bottom: 25px;
}

.stat-card {
    background: white;
    padding: 15px 20px;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    flex: 1;
    text-align: center;
}

.stat-number {
    font-size: 2em;
    font-weight: bold;
    color: #667eea;
}

.stat-label {
    color: #666;
    font-size: 0.9em;
    margin-top: 5px;
}

.post-grid {
    display: grid;
    gap: 20px;
}

.post-item {
    background: white;
    border-radius: 12px;
    padding: 20px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    transition: transform 0.2s, box-shadow 0.2s;
}

.post-item:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 16px rgba(0,0,0,0.15);
}

.post-meta {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;
    padding-bottom: 12px;
    border-bottom: 2px solid #f0f0f0;
}

.author-info {
    display: flex;
    align-items: center;
    gap: 10px;
}

.author-avatar {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-weight: bold;
    font-size: 1.2em;
}

.author-details {
    display: flex;
    flex-direction: column;
}

.author-name {
    font-weight: bold;
    color: #333;
}

.post-time {
    color: #999;
    font-size: 0.85em;
}

.post-badge {
    display: inline-block;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    padding: 4px 12px;
    border-radius: 12px;
    font-size: 0.85em;
    font-weight: bold;
}

.post-page-info {
    background: #f8f9fa;
    padding: 10px;
    border-radius: 6px;
    margin-bottom: 12px;
    font-size: 0.9em;
    color: #666;
}

.post-text {
    margin: 15px 0;
    line-height: 1.8;
    color: #333;
    font-size: 1.05em;
}

.post-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 15px;
    padding-top: 15px;
    border-top: 1px solid #f0f0f0;
}

.reply-count {
    color: #667eea;
    font-weight: 500;
}

.replies-section {
    margin-top: 15px;
    padding-left: 20px;
    border-left: 3px solid #667eea;
}

.reply-item {
    background: #f8f9fa;
    padding: 12px;
    border-radius: 8px;
    margin-top: 10px;
}

.reply-header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 8px;
}

.reply-author {
    font-weight: 600;
    color: #555;
}

.reply-time {
    color: #999;
    font-size: 0.85em;
}

.reply-text {
    color: #666;
    line-height: 1.6;
}

.loading-spinner {
    text-align: center;
    padding: 60px 20px;
}

.spinner {
    border: 4px solid #f3f3f3;
    border-top: 4px solid #667eea;
    border-radius: 50%;
    width: 50px;
    height: 50px;
    animation: spin 1s linear infinite;
    margin: 0 auto;
}

@keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
}

.error-box {
    background-color: #fee;
    color: #c33;
    padding: 20px;
    border-radius: 8px;
    margin: 20px 0;
    text-align: center;
}

.empty-state {
    text-align: center;
    padding: 60px 20px;
    color: #999;
}

.empty-state-icon {
    font-size: 4em;
    margin-bottom: 20px;
}

.view-toggle {
    background: white;
    padding: 10px;
    border-radius: 8px;
    margin-bottom: 20px;
    display: flex;
    gap: 10px;
}

.view-toggle button {
    flex: 1;
    padding: 10px;
    border: 1px solid #ddd;
    background: white;
    border-radius: 6px;
    cursor: pointer;
}

.view-toggle button.active {
    background: #667eea;
    color: white;
    border-color: #667eea;
}
</style>

<div class="feed-container">
    <div class="feed-header">
        <h1>📱 Social Feed</h1>
        <p>View posts from the community</p>
    </div>
    
    <!-- Statistics Bar -->
    <div class="stats-bar">
        <div class="stat-card">
            <div class="stat-number" id="totalPosts">0</div>
            <div class="stat-label">Total Posts</div>
        </div>
        <div class="stat-card">
            <div class="stat-number" id="totalReplies">0</div>
            <div class="stat-label">Total Replies</div>
        </div>
        <div class="stat-card">
            <div class="stat-number" id="activeUsers">0</div>
            <div class="stat-label">Contributors</div>
        </div>
    </div>
    
    <!-- Filter Controls -->
    <div class="filter-controls">
        <input type="text" id="searchInput" placeholder="🔍 Search posts..." onkeyup="applyFilters()">
        <select id="gradeFilter" onchange="applyFilters()">
            <option value="">All Grades</option>
            <option value="A+">A+</option>
            <option value="A">A</option>
            <option value="A-">A-</option>
            <option value="B+">B+</option>
            <option value="B">B</option>
            <option value="B-">B-</option>
            <option value="C+">C+</option>
            <option value="C">C</option>
            <option value="C-">C-</option>
            <option value="D">D</option>
            <option value="F">F</option>
        </select>
        <select id="sortOrder" onchange="applyFilters()">
            <option value="newest">Newest First</option>
            <option value="oldest">Oldest First</option>
            <option value="mostReplies">Most Replies</option>
        </select>
        <button onclick="resetFilters()">Reset Filters</button>
    </div>
    
    <!-- View Toggle -->
    <div class="view-toggle">
        <button id="showAllBtn" class="active" onclick="toggleRepliesView(true)">Show All Replies</button>
        <button id="hideAllBtn" onclick="toggleRepliesView(false)">Hide All Replies</button>
    </div>
    
    <!-- Loading State -->
    <div id="loadingState" class="loading-spinner">
        <div class="spinner"></div>
        <p>Loading posts...</p>
    </div>
    
    <!-- Error State -->
    <div id="errorState" class="error-box" style="display: none;"></div>
    
    <!-- Empty State -->
    <div id="emptyState" class="empty-state" style="display: none;">
        <div class="empty-state-icon">📭</div>
        <h3>No posts found</h3>
        <p>Check back later or adjust your filters</p>
    </div>
    
    <!-- Post Grid -->
    <div id="postGrid" class="post-grid"></div>
</div>

<script src="{{ site.baseurl }}/assets/js/social-media-api.js"></script>
<script>
// Global state
let allPosts = [];
let filteredPosts = [];
let showReplies = true;

// Initialize on page load
document.addEventListener('DOMContentLoaded', async () => {
    await loadFeed();
});

// Load all posts
async function loadFeed() {
    const loadingEl = document.getElementById('loadingState');
    const errorEl = document.getElementById('errorState');
    const gridEl = document.getElementById('postGrid');
    
    try {
        loadingEl.style.display = 'block';
        errorEl.style.display = 'none';
        gridEl.innerHTML = '';
        
        // Fetch posts
        allPosts = await window.SocialMediaAPI.getAllPosts();
        filteredPosts = [...allPosts];
        
        loadingEl.style.display = 'none';
        
        // Update statistics
        updateStatistics();
        
        // Render posts
        renderPosts();
        
    } catch (error) {
        loadingEl.style.display = 'none';
        errorEl.textContent = `Failed to load posts: ${error.message}`;
        errorEl.style.display = 'block';
        console.error('Error loading feed:', error);
    }
}

// Update statistics
function updateStatistics() {
    const totalPosts = allPosts.length;
    let totalReplies = 0;
    const uniqueAuthors = new Set();
    
    allPosts.forEach(post => {
        totalReplies += post.replyCount || 0;
        uniqueAuthors.add(post.studentName);
    });
    
    document.getElementById('totalPosts').textContent = totalPosts;
    document.getElementById('totalReplies').textContent = totalReplies;
    document.getElementById('activeUsers').textContent = uniqueAuthors.size;
}

// Render posts
function renderPosts() {
    const gridEl = document.getElementById('postGrid');
    const emptyEl = document.getElementById('emptyState');
    
    gridEl.innerHTML = '';
    
    if (filteredPosts.length === 0) {
        emptyEl.style.display = 'block';
        return;
    }
    
    emptyEl.style.display = 'none';
    
    filteredPosts.forEach(post => {
        const postEl = createPostElement(post);
        gridEl.appendChild(postEl);
    });
}

// Create post element
function createPostElement(post) {
    const div = document.createElement('div');
    div.className = 'post-item';
    
    // Get first letter of author name for avatar
    const authorInitial = post.studentName.charAt(0).toUpperCase();
    
    // Grade badge
    let gradeBadge = '';
    if (post.gradeReceived) {
        gradeBadge = `<span class="post-badge">📊 ${post.gradeReceived}</span>`;
    }
    
    // Page info
    let pageInfo = '';
    if (post.pageTitle) {
        pageInfo = `<div class="post-page-info">📄 ${escapeHtml(post.pageTitle)}</div>`;
    }
    
    // Replies section
    let repliesHTML = '';
    if (showReplies && post.replies && post.replies.length > 0) {
        repliesHTML = `
            <div class="replies-section">
                <strong>💬 ${post.replies.length} ${post.replies.length === 1 ? 'Reply' : 'Replies'}</strong>
                ${post.replies.map(reply => `
                    <div class="reply-item">
                        <div class="reply-header">
                            <span class="reply-author">${escapeHtml(reply.studentName)}</span>
                            <span class="reply-time">${window.SocialMediaAPI.formatTimestamp(reply.timestamp)}</span>
                        </div>
                        <div class="reply-text">${escapeHtml(reply.content)}</div>
                    </div>
                `).join('')}
            </div>
        `;
    }
    
    div.innerHTML = `
        <div class="post-meta">
            <div class="author-info">
                <div class="author-avatar">${authorInitial}</div>
                <div class="author-details">
                    <div class="author-name">${escapeHtml(post.studentName)}</div>
                    <div class="post-time">${window.SocialMediaAPI.formatTimestamp(post.timestamp)}</div>
                </div>
            </div>
            ${gradeBadge}
        </div>
        ${pageInfo}
        <div class="post-text">${escapeHtml(post.content)}</div>
        <div class="post-footer">
            <span class="reply-count">💬 ${post.replyCount || 0} ${(post.replyCount || 0) === 1 ? 'reply' : 'replies'}</span>
        </div>
        ${repliesHTML}
    `;
    
    return div;
}

// Apply filters
function applyFilters() {
    const searchTerm = document.getElementById('searchInput').value.toLowerCase();
    const gradeFilter = document.getElementById('gradeFilter').value;
    const sortOrder = document.getElementById('sortOrder').value;
    
    // Filter posts
    filteredPosts = allPosts.filter(post => {
        // Search filter
        const matchesSearch = !searchTerm || 
            post.content.toLowerCase().includes(searchTerm) ||
            post.studentName.toLowerCase().includes(searchTerm) ||
            (post.pageTitle && post.pageTitle.toLowerCase().includes(searchTerm));
        
        // Grade filter
        const matchesGrade = !gradeFilter || post.gradeReceived === gradeFilter;
        
        return matchesSearch && matchesGrade;
    });
    
    // Sort posts
    if (sortOrder === 'newest') {
        filteredPosts.sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));
    } else if (sortOrder === 'oldest') {
        filteredPosts.sort((a, b) => new Date(a.timestamp) - new Date(b.timestamp));
    } else if (sortOrder === 'mostReplies') {
        filteredPosts.sort((a, b) => (b.replyCount || 0) - (a.replyCount || 0));
    }
    
    renderPosts();
}

// Reset filters
function resetFilters() {
    document.getElementById('searchInput').value = '';
    document.getElementById('gradeFilter').value = '';
    document.getElementById('sortOrder').value = 'newest';
    applyFilters();
}

// Toggle replies view
function toggleRepliesView(show) {
    showReplies = show;
    
    // Update button states
    document.getElementById('showAllBtn').classList.toggle('active', show);
    document.getElementById('hideAllBtn').classList.toggle('active', !show);
    
    renderPosts();
}

// Escape HTML to prevent XSS
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// Auto-refresh feed every 30 seconds
setInterval(async () => {
    await loadFeed();
}, 30000);
</script>

