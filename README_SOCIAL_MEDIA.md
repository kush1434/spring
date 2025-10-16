# Social Media Integration - Quick Start

## ✅ What's Been Done

I've set up a complete social media posting system that connects your frontend pages repository to this Spring Boot backend. Here's what was created:

### Backend Files (✅ Complete)

1. **Post Entity** - `src/main/java/com/open/spring/mvc/post/Post.java`
   - Defines the post data structure
   - Includes support for threaded replies
   - Tracks author, content, grade, timestamp, etc.

2. **Repository** - `src/main/java/com/open/spring/mvc/post/PostJpaRepository.java`
   - Database access methods
   - Queries for filtering posts by user, page, etc.

3. **API Controller** - `src/main/java/com/open/spring/mvc/post/PostApiController.java`
   - REST API endpoints for CRUD operations
   - JWT authentication integration
   - Endpoints: GET, POST, PUT, DELETE for posts and replies

4. **Security Configuration** - Updated `src/main/java/com/open/spring/security/SecurityConfig.java`
   - Added `/api/post/**` endpoints with authentication
   - Public viewing for specific pages

### Frontend Files (📦 Ready to Deploy)

1. **JavaScript API** - `src/main/resources/static/js/social-media-api.js`
   - Complete API wrapper for all backend endpoints
   - Authentication handling with JWT cookies
   - Helper functions for formatting, etc.
   - **→ Copy this to your pages repo**: `<pages-repo>/assets/js/social-media-api.js`

2. **Post Page** - `FRONTEND_EXAMPLES/post.md`
   - Create posts & view feed (2-in-1 page)
   - Authentication check
   - Post form with grade selection
   - Reply functionality
   - **→ Copy this to your pages repo**: `<pages-repo>/post.md`

3. **Feed Page** - `FRONTEND_EXAMPLES/feed.md`
   - View-only social feed
   - Advanced filtering (search, grade, sort)
   - Statistics display
   - Auto-refresh every 30 seconds
   - **→ Copy this to your pages repo**: `<pages-repo>/feed.md`

### Documentation

- **`SOCIAL_MEDIA_INTEGRATION_GUIDE.md`** - Complete integration guide with:
  - Setup instructions
  - API documentation
  - Authentication flow
  - Testing guide
  - Troubleshooting tips
  - Production deployment guide

---

## 🚀 Quick Start

### Step 1: Test the Backend

```bash
cd /Users/akshay/pagesBackend
./mvnw spring-boot:run
```

The backend will start on `http://localhost:8085`

### Step 2: Copy Frontend Files to Pages Repo

Copy these files to your GitHub Pages repository (ApplicatorsCSA/pages):

```bash
# Copy JavaScript API
cp src/main/resources/static/js/social-media-api.js <path-to-pages-repo>/assets/js/

# Copy page templates
cp FRONTEND_EXAMPLES/post.md <path-to-pages-repo>/
cp FRONTEND_EXAMPLES/feed.md <path-to-pages-repo>/
```

### Step 3: Update API URL

Edit `<pages-repo>/assets/js/social-media-api.js` and update:

```javascript
const API_BASE_URL = 'http://localhost:8085'; // For local testing
// or
const API_BASE_URL = 'https://your-backend-url.com'; // For production
```

### Step 4: Test It!

1. Make sure backend is running
2. Start your Jekyll server:
   ```bash
   cd <pages-repo>
   bundle exec jekyll serve
   ```
3. Visit:
   - `http://localhost:4000/social-media` - Create posts
   - `http://localhost:4000/social-feed` - View feed

---

## 📋 How It Works

### Authentication Flow

1. **User logs in** via `/login` page
2. **Backend sets JWT cookie** (`jwt_java_spring`)
3. **Frontend JavaScript** automatically includes cookie in API requests
4. **Backend validates** JWT and associates posts with user

### Creating a Post

1. User must be **logged in first**
2. Fill out post form with:
   - Content (required)
   - Grade (optional)
   - Page title (optional)
3. JavaScript calls `/api/post` with authentication
4. Post is saved with user association
5. Feed updates automatically

### Viewing Posts

- `/social-media` - Create posts AND view feed (requires login)
- `/social-feed` - View-only feed with filters (requires login to view)
- Any page can embed posts using the JavaScript API

---

## 🔑 Key Features

- ✅ **User Authentication** - JWT token-based
- ✅ **Post Creation** - With grade and page reference
- ✅ **Threaded Replies** - Nested comments on posts
- ✅ **Filtering** - Search, grade filter, sort options
- ✅ **Real-time Updates** - Auto-refresh feed
- ✅ **User Ownership** - Only edit/delete own posts
- ✅ **XSS Protection** - HTML escaping
- ✅ **Responsive Design** - Mobile-friendly

---

## 📁 File Locations

### Backend (pagesBackend)
```
src/main/java/com/open/spring/mvc/post/
├── Post.java                    ✅ Created
├── PostJpaRepository.java       ✅ Created
└── PostApiController.java       ✅ Created

src/main/java/com/open/spring/security/
└── SecurityConfig.java          ✅ Updated

src/main/resources/static/js/
└── social-media-api.js          ✅ Created
```

### Frontend (pages repo) - TO DO
```
assets/js/
└── social-media-api.js          📦 Copy from backend

post.md                          📦 Copy from FRONTEND_EXAMPLES/
feed.md                          📦 Copy from FRONTEND_EXAMPLES/
```

---

## 🧪 Testing

### Test with curl:

```bash
# Login
curl -X POST http://localhost:8085/authenticate \
  -H "Content-Type: application/json" \
  -d '{"uid":"your-username","password":"your-password"}' \
  -c cookies.txt

# Get all posts
curl -X GET http://localhost:8085/api/post/all \
  -H "X-Origin: client" \
  -b cookies.txt

# Create post
curl -X POST http://localhost:8085/api/post \
  -H "Content-Type: application/json" \
  -H "X-Origin: client" \
  -b cookies.txt \
  -d '{"content":"Test post!","gradeReceived":"A+"}'
```

---

## 📚 Documentation

For complete documentation, see:
- **`SOCIAL_MEDIA_INTEGRATION_GUIDE.md`** - Full integration guide

---

## 🎯 Next Steps

1. ✅ Backend is ready (all files created)
2. ⏳ Copy frontend files to your pages repository
3. ⏳ Update `API_BASE_URL` in JavaScript
4. ⏳ Test locally with both servers running
5. ⏳ Deploy to production

---

## 🆘 Need Help?

### Common Issues:

**"Unauthorized" error**
- Make sure you're logged in via `/login` page
- Check JWT cookie exists in browser DevTools

**Posts not appearing**
- Ensure backend is running
- Check browser console for errors
- Verify API_BASE_URL is correct

**CORS errors**
- Backend and frontend must both be running
- Check CORS configuration in SecurityConfig.java

See full troubleshooting guide in `SOCIAL_MEDIA_INTEGRATION_GUIDE.md`

---

**Created**: October 16, 2025
**Status**: Backend ✅ Complete | Frontend 📦 Ready to Deploy

