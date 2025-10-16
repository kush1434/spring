---
layout: post
title: Social Media - Create & View Posts
permalink: /social-media
---

<style>
/* Social Media Styling */
.social-container {
    max-width: 800px;
    margin: 0 auto;
    padding: 20px;
}

.auth-warning {
    background-color: #fff3cd;
    border: 1px solid #ffc107;
    border-radius: 8px;
    padding: 15px;
    margin-bottom: 20px;
}

.post-form {
    background: white;
    border-radius: 12px;
    padding: 20px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    margin-bottom: 30px;
}

.post-form textarea {
    width: 100%;
    min-height: 120px;
    padding: 12px;
    border: 1px solid #ddd;
    border-radius: 8px;
    resize: vertical;
    font-family: inherit;
}

.post-form select,
.post-form input {
    width: 100%;
    padding: 10px;
    margin-top: 10px;
    border: 1px solid #ddd;
    border-radius: 6px;
}

.post-form button {
    background-color: #007bff;
    color: white;
    border: none;
    padding: 12px 24px;
    border-radius: 6px;
    cursor: pointer;
    margin-top: 10px;
    font-size: 16px;
}

.post-form button:hover {
    background-color: #0056b3;
}

.post-feed {
    margin-top: 30px;
}

.post-card {
    background: white;
    border-radius: 12px;
    padding: 20px;
    margin-bottom: 20px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.post-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    border-bottom: 1px solid #eee;
    padding-bottom: 10px;
}

.post-author {
    font-weight: bold;
    color: #333;
}

.post-timestamp {
    color: #666;
    font-size: 0.9em;
}

.post-content {
    margin: 15px 0;
    line-height: 1.6;
    color: #333;
}

.post-grade {
    display: inline-block;
    background-color: #e3f2fd;
    color: #1976d2;
    padding: 4px 12px;
    border-radius: 12px;
    font-size: 0.9em;
    margin-bottom: 10px;
}

.post-actions {
    display: flex;
    gap: 15px;
    margin-top: 15px;
    padding-top: 15px;
    border-top: 1px solid #eee;
}

.post-actions button {
    background: none;
    border: none;
    color: #007bff;
    cursor: pointer;
    font-size: 14px;
}

.post-actions button:hover {
    text-decoration: underline;
}

.reply-section {
    margin-top: 15px;
    padding-left: 30px;
    border-left: 3px solid #e0e0e0;
}

.reply-card {
    background: #f9f9f9;
    padding: 12px;
    border-radius: 8px;
    margin-top: 10px;
}

.reply-form {
    margin-top: 10px;
}

.reply-form textarea {
    width: 100%;
    padding: 8px;
    border: 1px solid #ddd;
    border-radius: 6px;
    resize: vertical;
}

.reply-form button {
    background-color: #28a745;
    color: white;
    border: none;
    padding: 8px 16px;
    border-radius: 6px;
    cursor: pointer;
    margin-top: 8px;
}

.reply-form button:hover {
    background-color: #218838;
}

.loading {
    text-align: center;
    padding: 40px;
    color: #666;
}

.error-message {
    background-color: #f8d7da;
    color: #721c24;
    padding: 12px;
    border-radius: 8px;
    margin-bottom: 15px;
}

.filter-bar {
    background: white;
    padding: 15px;
    border-radius: 8px;
    margin-bottom: 20px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.filter-bar input {
    padding: 8px;
    border: 1px solid #ddd;
    border-radius: 6px;
    width: 300px;
}
</style>

<div class="social-container">
    <h1>Social Media Feed</h1>
    
    <!-- Authentication Warning -->
    <div id="authWarning" class="auth-warning" style="display: none;">
        ⚠️ You must be logged in to create posts. <a href="/login">Login here</a>
    </div>
    
    <!-- Post Creation Form -->
    <div id="postFormContainer" class="post-form" style="display: none;">
        <h2>Create a Post</h2>
        <div id="postError" class="error-message" style="display: none;"></div>
        
        <textarea id="postContent" placeholder="What's on your mind?"></textarea>
        
        <select id="gradeSelect">
            <option value="">Select Grade (Optional)</option>
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
        
        <input type="text" id="pageTitle" placeholder="Page Title (Optional)">
        
        <button onclick="submitPost()">Post</button>
    </div>
    
    <!-- Filter Bar -->
    <div class="filter-bar">
        <input type="text" id="searchFilter" placeholder="Search posts..." onkeyup="filterPosts()">
    </div>
    
    <!-- Loading Indicator -->
    <div id="loading" class="loading">
        Loading posts...
    </div>
    
    <!-- Posts Feed -->
    <div id="postFeed" class="post-feed"></div>
</div>

<script src="{{ site.baseurl }}/assets/js/social-media-api.js"></script>
<script>
// Initialize the page
document.addEventListener('DOMContentLoaded', async () => {
    // Check authentication
    if (window.SocialMediaAPI.isAuthenticated()) {
        document.getElementById('postFormContainer').style.display = 'block';
    } else {
        document.getElementById('authWarning').style.display = 'block';
    }
    
    // Load posts
    await loadPosts();
});

// Global variable to store all posts for filtering
let allPosts = [];

// Load all posts
async function loadPosts() {
    const loadingEl = document.getElementById('loading');
    const feedEl = document.getElementById('postFeed');
    
    try {
        loadingEl.style.display = 'block';
        feedEl.innerHTML = '';
        
        // Fetch posts
        allPosts = await window.SocialMediaAPI.getAllPosts();
        
        loadingEl.style.display = 'none';
        
        if (allPosts.length === 0) {
            feedEl.innerHTML = '<p style="text-align: center; color: #666;">No posts yet. Be the first to post!</p>';
            return;
        }
        
        // Render posts
        renderPosts(allPosts);
        
    } catch (error) {
        loadingEl.style.display = 'none';
        feedEl.innerHTML = `<div class="error-message">Error loading posts: ${error.message}</div>`;
        console.error('Error loading posts:', error);
    }
}

// Render posts to the feed
function renderPosts(posts) {
    const feedEl = document.getElementById('postFeed');
    feedEl.innerHTML = '';
    
    posts.forEach(post => {
        const postEl = createPostElement(post);
        feedEl.appendChild(postEl);
    });
}

// Create a post element
function createPostElement(post) {
    const div = document.createElement('div');
    div.className = 'post-card';
    div.setAttribute('data-post-id', post.id);
    
    let gradeHTML = '';
    if (post.gradeReceived) {
        gradeHTML = `<div class="post-grade">Grade: ${post.gradeReceived}</div>`;
    }
    
    let pageTitleHTML = '';
    if (post.pageTitle) {
        pageTitleHTML = `<div style="color: #666; font-size: 0.9em; margin-bottom: 8px;">📄 ${post.pageTitle}</div>`;
    }
    
    div.innerHTML = `
        <div class="post-header">
            <span class="post-author">${post.studentName}</span>
            <span class="post-timestamp">${window.SocialMediaAPI.formatTimestamp(post.timestamp)}</span>
        </div>
        ${pageTitleHTML}
        ${gradeHTML}
        <div class="post-content">${escapeHtml(post.content)}</div>
        <div class="post-actions">
            <button onclick="toggleReplyForm(${post.id})">💬 Reply (${post.replyCount})</button>
        </div>
        <div id="replyForm-${post.id}" class="reply-form" style="display: none;">
            <textarea id="replyContent-${post.id}" placeholder="Write a reply..."></textarea>
            <button onclick="submitReply(${post.id})">Post Reply</button>
            <button onclick="toggleReplyForm(${post.id})" style="background-color: #6c757d;">Cancel</button>
        </div>
        <div id="replies-${post.id}" class="reply-section"></div>
    `;
    
    // Render replies if they exist
    if (post.replies && post.replies.length > 0) {
        const repliesContainer = div.querySelector(`#replies-${post.id}`);
        post.replies.forEach(reply => {
            const replyEl = createReplyElement(reply);
            repliesContainer.appendChild(replyEl);
        });
    }
    
    return div;
}

// Create a reply element
function createReplyElement(reply) {
    const div = document.createElement('div');
    div.className = 'reply-card';
    div.innerHTML = `
        <div style="display: flex; justify-content: space-between; margin-bottom: 8px;">
            <strong>${reply.studentName}</strong>
            <span style="color: #666; font-size: 0.85em;">${window.SocialMediaAPI.formatTimestamp(reply.timestamp)}</span>
        </div>
        <div>${escapeHtml(reply.content)}</div>
    `;
    return div;
}

// Submit a new post
async function submitPost() {
    const content = document.getElementById('postContent').value;
    const grade = document.getElementById('gradeSelect').value;
    const pageTitle = document.getElementById('pageTitle').value;
    const errorEl = document.getElementById('postError');
    
    errorEl.style.display = 'none';
    
    if (!content.trim()) {
        errorEl.textContent = 'Please enter some content for your post';
        errorEl.style.display = 'block';
        return;
    }
    
    try {
        const postData = {
            content: content,
            gradeReceived: grade || null,
            pageTitle: pageTitle || null,
            pageUrl: window.location.href
        };
        
        await window.SocialMediaAPI.createPost(postData);
        
        // Clear form
        document.getElementById('postContent').value = '';
        document.getElementById('gradeSelect').value = '';
        document.getElementById('pageTitle').value = '';
        
        // Reload posts
        await loadPosts();
        
    } catch (error) {
        errorEl.textContent = `Error creating post: ${error.message}`;
        errorEl.style.display = 'block';
        console.error('Error creating post:', error);
    }
}

// Toggle reply form visibility
function toggleReplyForm(postId) {
    const form = document.getElementById(`replyForm-${postId}`);
    form.style.display = form.style.display === 'none' ? 'block' : 'none';
}

// Submit a reply
async function submitReply(postId) {
    const content = document.getElementById(`replyContent-${postId}`).value;
    
    if (!content.trim()) {
        alert('Please enter a reply');
        return;
    }
    
    try {
        await window.SocialMediaAPI.createReply(postId, content);
        
        // Clear form and hide
        document.getElementById(`replyContent-${postId}`).value = '';
        toggleReplyForm(postId);
        
        // Reload posts
        await loadPosts();
        
    } catch (error) {
        alert(`Error posting reply: ${error.message}`);
        console.error('Error posting reply:', error);
    }
}

// Filter posts by search term
function filterPosts() {
    const searchTerm = document.getElementById('searchFilter').value.toLowerCase();
    
    if (!searchTerm) {
        renderPosts(allPosts);
        return;
    }
    
    const filtered = allPosts.filter(post => {
        return post.content.toLowerCase().includes(searchTerm) ||
               post.studentName.toLowerCase().includes(searchTerm) ||
               (post.pageTitle && post.pageTitle.toLowerCase().includes(searchTerm));
    });
    
    renderPosts(filtered);
}

// Escape HTML to prevent XSS
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
</script>

