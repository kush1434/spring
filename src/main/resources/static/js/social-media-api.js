/**
 * Social Media API Utilities
 * 
 * This file provides functions to interact with the backend Post API
 * Handles authentication via JWT cookies and provides CRUD operations for posts
 * 
 * Usage:
 * 1. Include this script in your HTML page
 * 2. Use the API functions to create, read, update, and delete posts
 * 3. Ensure users are logged in before calling authenticated endpoints
 */

// Configuration
const API_BASE_URL = 'http://localhost:8085'; // Change this to your backend URL
const API_ENDPOINTS = {
    getAllPosts: '/api/post/all',
    getMyPosts: '/api/post',
    getPostsByPage: '/api/post/page',
    createPost: '/api/post',
    createReply: '/api/post/reply',
    updatePost: '/api/post',
    deletePost: '/api/post',
    authenticate: '/authenticate'
};

/**
 * Get JWT token from cookies
 * @returns {string|null} JWT token or null if not found
 */
function getJwtToken() {
    const cookies = document.cookie.split(';');
    for (let cookie of cookies) {
        const [name, value] = cookie.trim().split('=');
        if (name === 'jwt_java_spring') {
            return value;
        }
    }
    return null;
}

/**
 * Check if user is authenticated
 * @returns {boolean} true if user has valid JWT token
 */
function isAuthenticated() {
    return getJwtToken() !== null;
}

/**
 * Make an authenticated API request
 * @param {string} endpoint - API endpoint path
 * @param {string} method - HTTP method (GET, POST, PUT, DELETE)
 * @param {object} body - Request body (optional)
 * @returns {Promise<object>} API response
 */
async function apiRequest(endpoint, method = 'GET', body = null) {
    const url = `${API_BASE_URL}${endpoint}`;
    
    const options = {
        method: method,
        headers: {
            'Content-Type': 'application/json',
            'X-Origin': 'client' // Required for JWT filter
        },
        credentials: 'include', // Include cookies
        cache: 'no-cache'
    };
    
    if (body && method !== 'GET') {
        options.body = JSON.stringify(body);
    }
    
    try {
        const response = await fetch(url, options);
        
        if (!response.ok) {
            if (response.status === 401) {
                throw new Error('Unauthorized - Please login first');
            }
            throw new Error(`API Error: ${response.status} ${response.statusText}`);
        }
        
        return await response.json();
    } catch (error) {
        console.error('API Request failed:', error);
        throw error;
    }
}

/**
 * Get all posts from all users (social feed)
 * Requires authentication
 * @returns {Promise<Array>} Array of post objects
 */
async function getAllPosts() {
    if (!isAuthenticated()) {
        throw new Error('You must be logged in to view posts');
    }
    return await apiRequest(API_ENDPOINTS.getAllPosts, 'GET');
}

/**
 * Get posts created by the current user
 * Requires authentication
 * @returns {Promise<Array>} Array of post objects
 */
async function getMyPosts() {
    if (!isAuthenticated()) {
        throw new Error('You must be logged in to view your posts');
    }
    return await apiRequest(API_ENDPOINTS.getMyPosts, 'GET');
}

/**
 * Get posts for a specific page URL
 * Does not require authentication
 * @param {string} pageUrl - The URL of the page to get posts for
 * @returns {Promise<Array>} Array of post objects
 */
async function getPostsByPage(pageUrl) {
    const endpoint = `${API_ENDPOINTS.getPostsByPage}?url=${encodeURIComponent(pageUrl)}`;
    return await apiRequest(endpoint, 'GET');
}

/**
 * Create a new post
 * Requires authentication
 * @param {object} postData - Post data
 * @param {string} postData.content - Post content (required)
 * @param {string} postData.gradeReceived - Grade received (optional)
 * @param {string} postData.pageUrl - Page URL (optional)
 * @param {string} postData.pageTitle - Page title (optional)
 * @returns {Promise<object>} Created post object
 */
async function createPost(postData) {
    if (!isAuthenticated()) {
        throw new Error('You must be logged in to create a post');
    }
    
    if (!postData.content || postData.content.trim() === '') {
        throw new Error('Post content is required');
    }
    
    return await apiRequest(API_ENDPOINTS.createPost, 'POST', postData);
}

/**
 * Create a reply to a post
 * Requires authentication
 * @param {number} parentId - ID of the parent post
 * @param {string} content - Reply content
 * @returns {Promise<object>} Created reply object
 */
async function createReply(parentId, content) {
    if (!isAuthenticated()) {
        throw new Error('You must be logged in to reply to a post');
    }
    
    if (!content || content.trim() === '') {
        throw new Error('Reply content is required');
    }
    
    return await apiRequest(API_ENDPOINTS.createReply, 'POST', {
        parentId: parentId,
        content: content
    });
}

/**
 * Update an existing post
 * Requires authentication and ownership
 * @param {number} postId - ID of the post to update
 * @param {object} updates - Fields to update
 * @returns {Promise<object>} Updated post object
 */
async function updatePost(postId, updates) {
    if (!isAuthenticated()) {
        throw new Error('You must be logged in to update a post');
    }
    
    const endpoint = `${API_ENDPOINTS.updatePost}/${postId}`;
    return await apiRequest(endpoint, 'PUT', updates);
}

/**
 * Delete a post
 * Requires authentication and ownership
 * @param {number} postId - ID of the post to delete
 * @returns {Promise<object>} Deletion confirmation
 */
async function deletePost(postId) {
    if (!isAuthenticated()) {
        throw new Error('You must be logged in to delete a post');
    }
    
    const endpoint = `${API_ENDPOINTS.deletePost}/${postId}`;
    return await apiRequest(endpoint, 'DELETE');
}

/**
 * Login user and store JWT token
 * @param {string} uid - Username or email
 * @param {string} password - Password
 * @returns {Promise<object>} Authentication response
 */
async function login(uid, password) {
    const url = `${API_BASE_URL}${API_ENDPOINTS.authenticate}`;
    
    const response = await fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        credentials: 'include', // Important: This allows cookies to be set
        body: JSON.stringify({ uid, password })
    });
    
    if (!response.ok) {
        throw new Error('Login failed: Invalid credentials');
    }
    
    return await response.text();
}

/**
 * Format timestamp for display
 * @param {string} timestamp - ISO timestamp string
 * @returns {string} Formatted date string
 */
function formatTimestamp(timestamp) {
    const date = new Date(timestamp);
    const now = new Date();
    const diff = now - date;
    
    // Less than a minute
    if (diff < 60000) {
        return 'Just now';
    }
    
    // Less than an hour
    if (diff < 3600000) {
        const minutes = Math.floor(diff / 60000);
        return `${minutes} minute${minutes > 1 ? 's' : ''} ago`;
    }
    
    // Less than a day
    if (diff < 86400000) {
        const hours = Math.floor(diff / 3600000);
        return `${hours} hour${hours > 1 ? 's' : ''} ago`;
    }
    
    // Less than a week
    if (diff < 604800000) {
        const days = Math.floor(diff / 86400000);
        return `${days} day${days > 1 ? 's' : ''} ago`;
    }
    
    // Format as date
    return date.toLocaleDateString('en-US', {
        month: 'short',
        day: 'numeric',
        year: date.getFullYear() !== now.getFullYear() ? 'numeric' : undefined
    });
}

/**
 * Export all functions for use in other scripts
 */
window.SocialMediaAPI = {
    isAuthenticated,
    getAllPosts,
    getMyPosts,
    getPostsByPage,
    createPost,
    createReply,
    updatePost,
    deletePost,
    login,
    formatTimestamp,
    API_BASE_URL
};

