# Social Media Integration Guide

## Overview
This guide explains how to integrate the social media posting system with your frontend pages repository. The system allows users to:
- Login to authenticate
- Create posts with grades and page references
- View all posts in a social feed
- Reply to posts
- Filter and search posts

## Architecture

### Backend (Spring Boot)
- **Location**: `pagesBackend/src/main/java/com/open/spring/mvc/post/`
- **Components**:
  - `Post.java` - Entity model for posts
  - `PostJpaRepository.java` - Database repository
  - `PostApiController.java` - REST API endpoints
- **Authentication**: JWT tokens stored in cookies

### Frontend (GitHub Pages)
- **Location**: Your `pages` repository
- **Components**:
  - `post.md` - Create posts & view feed (2-in-1 page)
  - `feed.md` - View-only social feed with filters
  - `social-media-api.js` - JavaScript API utilities

---

## Setup Instructions

### Step 1: Backend Setup (Already Complete ✓)

The backend files have been created and configured:

1. **Post Entity** - `/src/main/java/com/open/spring/mvc/post/Post.java`
2. **Repository** - `/src/main/java/com/open/spring/mvc/post/PostJpaRepository.java`
3. **Controller** - `/src/main/java/com/open/spring/mvc/post/PostApiController.java`
4. **Security Config** - Updated `/src/main/java/com/open/spring/security/SecurityConfig.java`

### Step 2: Copy JavaScript API to Pages Repository

Copy the JavaScript API file to your frontend pages repository:

**From**: `/Users/akshay/pagesBackend/src/main/resources/static/js/social-media-api.js`
**To**: `<pages-repo>/assets/js/social-media-api.js`

**Important**: Update the `API_BASE_URL` in `social-media-api.js` to match your backend:

```javascript
const API_BASE_URL = 'http://localhost:8085'; // Development
// or
const API_BASE_URL = 'https://your-backend-domain.com'; // Production
```

### Step 3: Copy Frontend Pages

Copy the frontend page templates to your pages repository:

**From**: `/Users/akshay/pagesBackend/FRONTEND_EXAMPLES/`
**To**: `<pages-repo>/`

Files to copy:
- `post.md` → Create this at `<pages-repo>/post.md`
- `feed.md` → Create this at `<pages-repo>/feed.md`

### Step 4: Update Frontend Pages

In both `post.md` and `feed.md`, update the script source path:

```html
<!-- Update this line: -->
<script src="{{ site.baseurl }}/assets/js/social-media-api.js"></script>
```

Make sure the path matches where you placed the JavaScript file.

### Step 5: Configure CORS (If Needed)

If your frontend and backend are on different domains, ensure CORS is properly configured in your backend.

The `PostApiController.java` already has:
```java
@CrossOrigin(origins = "*", allowedHeaders = "*", allowCredentials = "true")
```

For production, you should specify specific origins instead of `"*"`.

---

## API Endpoints

### Authentication Required Endpoints

#### 1. Get All Posts (Social Feed)
```
GET /api/post/all
Headers: Cookie: jwt_java_spring=<token>
Response: Array of post objects
```

#### 2. Get My Posts
```
GET /api/post
Headers: Cookie: jwt_java_spring=<token>
Response: Array of current user's posts
```

#### 3. Create Post
```
POST /api/post
Headers: Cookie: jwt_java_spring=<token>
Body: {
  "content": "string (required)",
  "gradeReceived": "string (optional)",
  "pageUrl": "string (optional)",
  "pageTitle": "string (optional)"
}
Response: Created post object
```

#### 4. Create Reply
```
POST /api/post/reply
Headers: Cookie: jwt_java_spring=<token>
Body: {
  "parentId": number,
  "content": "string"
}
Response: Created reply object
```

#### 5. Update Post
```
PUT /api/post/{id}
Headers: Cookie: jwt_java_spring=<token>
Body: {
  "content": "string (optional)",
  "gradeReceived": "string (optional)"
}
Response: Updated post object
```

#### 6. Delete Post
```
DELETE /api/post/{id}
Headers: Cookie: jwt_java_spring=<token>
Response: Success message
```

### Public Endpoints

#### Get Posts by Page URL
```
GET /api/post/page?url={pageUrl}
Response: Array of posts for specific page
```

---

## Frontend JavaScript API Usage

### Check Authentication
```javascript
if (window.SocialMediaAPI.isAuthenticated()) {
    console.log('User is logged in');
}
```

### Get All Posts
```javascript
try {
    const posts = await window.SocialMediaAPI.getAllPosts();
    console.log('Posts:', posts);
} catch (error) {
    console.error('Error:', error.message);
}
```

### Create a Post
```javascript
const postData = {
    content: "This is my post content",
    gradeReceived: "A+",
    pageTitle: "My Lesson Page",
    pageUrl: window.location.href
};

try {
    const newPost = await window.SocialMediaAPI.createPost(postData);
    console.log('Post created:', newPost);
} catch (error) {
    console.error('Error:', error.message);
}
```

### Create a Reply
```javascript
try {
    const reply = await window.SocialMediaAPI.createReply(postId, "My reply content");
    console.log('Reply created:', reply);
} catch (error) {
    console.error('Error:', error.message);
}
```

### Login
```javascript
try {
    const response = await window.SocialMediaAPI.login('username', 'password');
    console.log('Login successful:', response);
} catch (error) {
    console.error('Login failed:', error.message);
}
```

---

## Authentication Flow

### 1. User Visits Social Media Page
- Page loads and checks for JWT cookie
- If authenticated: Shows post creation form
- If not authenticated: Shows login warning

### 2. User Logs In
- User goes to `/login` page
- Submits credentials
- Backend validates and sets JWT cookie
- Cookie is automatically included in subsequent API requests

### 3. User Creates Post
- Fills out post form
- JavaScript makes authenticated API call
- Backend validates JWT from cookie
- Post is saved with user association

### 4. User Views Feed
- JavaScript makes authenticated API call to `/api/post/all`
- Backend returns all posts with replies
- Frontend renders posts with filtering options

---

## Database Schema

The `posts` table will be automatically created by JPA:

```sql
CREATE TABLE posts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    person_id BIGINT NOT NULL,
    student_name VARCHAR(100) NOT NULL,
    grade_received VARCHAR(50),
    content TEXT NOT NULL,
    page_url VARCHAR(500),
    page_title VARCHAR(200),
    timestamp DATETIME NOT NULL,
    parent_id BIGINT,
    FOREIGN KEY (person_id) REFERENCES person(id),
    FOREIGN KEY (parent_id) REFERENCES posts(id)
);
```

---

## Testing Guide

### 1. Test Backend API

Start your backend server:
```bash
cd /Users/akshay/pagesBackend
./mvnw spring-boot:run
```

Test endpoints with curl:

```bash
# Login first
curl -X POST http://localhost:8085/authenticate \
  -H "Content-Type: application/json" \
  -d '{"uid":"testuser","password":"testpass"}' \
  -c cookies.txt

# Get all posts
curl -X GET http://localhost:8085/api/post/all \
  -H "X-Origin: client" \
  -b cookies.txt

# Create a post
curl -X POST http://localhost:8085/api/post \
  -H "Content-Type: application/json" \
  -H "X-Origin: client" \
  -b cookies.txt \
  -d '{"content":"Test post","gradeReceived":"A"}'
```

### 2. Test Frontend Integration

1. Start your Jekyll server for the pages repository:
   ```bash
   cd <pages-repo>
   bundle exec jekyll serve
   ```

2. Visit `http://localhost:4000/social-media`

3. Test the following:
   - Login functionality
   - Post creation
   - Viewing feed
   - Replying to posts
   - Filtering posts

---

## Troubleshooting

### Issue: "Unauthorized" Error
**Cause**: User not logged in or JWT cookie expired
**Solution**: 
- Ensure user is logged in via `/login` page
- Check that JWT cookie is present in browser DevTools
- Verify cookie domain/path settings match your setup

### Issue: CORS Error
**Cause**: Frontend and backend on different domains
**Solution**:
- Update `@CrossOrigin` annotation in `PostApiController.java`
- Ensure `credentials: 'include'` is set in fetch requests
- Check browser console for specific CORS errors

### Issue: "X-Origin header required"
**Cause**: Backend JWT filter requires `X-Origin: client` header
**Solution**:
- Ensure all API requests include `X-Origin: client` header
- Check `social-media-api.js` includes this header

### Issue: Posts not appearing
**Cause**: Database not initialized or no posts created
**Solution**:
- Create a test post via API or frontend
- Check backend logs for database errors
- Verify database connection in `application.properties`

### Issue: API_BASE_URL incorrect
**Cause**: JavaScript pointing to wrong backend URL
**Solution**:
- Update `API_BASE_URL` in `social-media-api.js`
- Use `http://localhost:8085` for local development
- Use your deployed backend URL for production

---

## Security Considerations

### 1. Authentication
- All post creation/editing requires authentication
- JWT tokens expire after configured time
- Users can only edit/delete their own posts

### 2. XSS Prevention
- All user content is escaped before rendering
- Use `escapeHtml()` function in frontend code

### 3. CORS
- Configure specific origins in production (not `*`)
- Use `allowCredentials: true` for cookie-based auth

### 4. Input Validation
- Backend validates required fields
- Content length limits enforced
- SQL injection prevented by JPA/Hibernate

---

## Customization

### Change Grade Options
Edit the grade select options in `post.md`:
```html
<select id="gradeSelect">
    <option value="">Select Grade (Optional)</option>
    <option value="A+">A+</option>
    <!-- Add/modify options -->
</select>
```

### Change Styling
Both `post.md` and `feed.md` have embedded CSS in `<style>` tags. Modify colors, layouts, etc. as needed.

### Add More Filters
In `feed.md`, add additional filter dropdowns and update the `applyFilters()` function.

---

## Production Deployment

### Backend
1. Build the JAR file:
   ```bash
   ./mvnw clean package
   ```

2. Deploy to your server (e.g., AWS, Heroku, DigitalOcean)

3. Update `application.properties` with production database

4. Configure CORS for your frontend domain

### Frontend
1. Update `API_BASE_URL` in `social-media-api.js` to production backend URL

2. Push changes to your GitHub Pages repository

3. Pages will automatically rebuild and deploy

---

## Next Steps

1. ✅ Backend files created and configured
2. ⏳ Copy frontend files to pages repository
3. ⏳ Update `API_BASE_URL` in JavaScript
4. ⏳ Test locally
5. ⏳ Deploy to production

## Support

For issues or questions:
- Check backend logs in terminal
- Check browser console for JavaScript errors
- Verify authentication cookies in browser DevTools
- Test API endpoints with curl/Postman

---

## File Structure Summary

```
pagesBackend/
├── src/main/java/com/open/spring/
│   ├── mvc/post/
│   │   ├── Post.java                    ✓ Created
│   │   ├── PostJpaRepository.java       ✓ Created
│   │   └── PostApiController.java       ✓ Created
│   └── security/
│       └── SecurityConfig.java          ✓ Updated
├── src/main/resources/static/js/
│   └── social-media-api.js              ✓ Created
└── FRONTEND_EXAMPLES/
    ├── post.md                          ✓ Created
    └── feed.md                          ✓ Created

pages/ (Your frontend repository)
├── assets/js/
│   └── social-media-api.js              ⏳ Copy here
├── post.md                              ⏳ Copy here
└── feed.md                              ⏳ Copy here
```

---

**Last Updated**: October 16, 2025
**Version**: 1.0

