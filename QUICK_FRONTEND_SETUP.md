# Quick Frontend Setup for ApplicatorsCSA/pages

## 🎯 Goal
Connect your GitHub Pages frontend (ApplicatorsCSA/pages) to this social media backend.

## 📋 Step-by-Step Instructions

### Step 1: Copy Files to Your Pages Repository

Clone your pages repository (if you haven't already):
```bash
git clone https://github.com/ApplicatorsCSA/pages.git
cd pages
```

### Step 2: Copy the JavaScript API

From your backend:
```bash
# Copy the JavaScript API file
cp /Users/akshay/pagesBackend/src/main/resources/static/js/social-media-api.js \
   <path-to-pages>/assets/js/social-media-api.js
```

### Step 3: Copy the Frontend Pages

```bash
# Copy the post creation page
cp /Users/akshay/pagesBackend/FRONTEND_EXAMPLES/post.md \
   <path-to-pages>/post.md

# Copy the feed view page
cp /Users/akshay/pagesBackend/FRONTEND_EXAMPLES/feed.md \
   <path-to-pages>/feed.md
```

### Step 4: Update the API URL

Edit `<path-to-pages>/assets/js/social-media-api.js`:

Find this line:
```javascript
const API_BASE_URL = 'http://localhost:8085'; // Change this to your backend URL
```

**For local testing**: Keep it as `http://localhost:8085`  
**For production**: Change to your deployed backend URL

### Step 5: Update Script Paths in Pages

In both `post.md` and `feed.md`, update the script path to match your Jekyll setup:

```html
<!-- Update this line if needed: -->
<script src="{{ site.baseurl }}/assets/js/social-media-api.js"></script>
```

Common Jekyll paths:
- `{{ site.baseurl }}/assets/js/social-media-api.js`
- `/assets/js/social-media-api.js`

### Step 6: Test Locally

1. Start your backend (from pagesBackend):
   ```bash
   ./mvnw spring-boot:run
   ```

2. Start your Jekyll frontend (from pages):
   ```bash
   bundle exec jekyll serve
   ```

3. Visit these URLs:
   - Login: `http://localhost:4000/login` (or your existing login page)
   - Create Posts: `http://localhost:4000/post` (new page!)
   - View Feed: `http://localhost:4000/feed` (new page!)

### Step 7: Create Your First Post

1. **Login** first at your existing login page
2. Go to `http://localhost:4000/post`
3. You should see:
   - A form to create posts
   - Ability to select a grade
   - Option to add page title
4. Create a post and watch it appear!

---

## 📁 File Structure After Setup

```
pages/ (Your ApplicatorsCSA/pages repo)
├── assets/
│   └── js/
│       └── social-media-api.js     ← NEW! Copy this
├── post.md                         ← NEW! Copy this  
├── feed.md                         ← NEW! Copy this
└── (your existing files)
```

---

## 🔧 Quick Commands Cheatsheet

```bash
# From pagesBackend directory
./mvnw spring-boot:run              # Start backend on port 8085

# From pages directory
bundle exec jekyll serve            # Start frontend on port 4000

# Login first at:
http://localhost:4000/login

# Then test social media:
http://localhost:4000/post          # Create & view posts
http://localhost:4000/feed          # View-only feed with filters
```

---

## ✅ Testing Checklist

- [ ] Backend running on http://localhost:8085
- [ ] Frontend running on http://localhost:4000
- [ ] Can login at `/login`
- [ ] Can create a post at `/post`
- [ ] Post appears in the feed
- [ ] Can reply to posts
- [ ] Can filter/search posts in `/feed`

---

## 🐛 Troubleshooting

### "Unauthorized" Error
- Make sure you're logged in first via `/login`
- Check browser cookies for `jwt_java_spring`

### CORS Errors
- Backend and frontend must both be running
- Check `API_BASE_URL` is correct in `social-media-api.js`

### Posts Not Showing
- Check backend logs for errors
- Verify you've created at least one post
- Open browser console (F12) to see JavaScript errors

### Can't See the Pages
- Make sure `post.md` and `feed.md` are in the root of your pages repo
- Check the permalink in frontmatter matches URL you're visiting

---

## 📝 Next Steps

After local testing works:

1. **Commit and push to pages repo**:
   ```bash
   cd pages
   git add assets/js/social-media-api.js post.md feed.md
   git commit -m "Add social media integration"
   git push
   ```

2. **Update API_BASE_URL** in `social-media-api.js` to your production backend

3. **Deploy backend** to your server

4. **Test on GitHub Pages**

---

## 🎉 What You Get

- **`/post`** - Full-featured page to create posts and view feed
  - Login required
  - Create posts with grades
  - Reply to posts
  - Real-time updates

- **`/feed`** - Beautiful read-only social feed
  - Login required
  - Advanced filters (search, grade, sort)
  - Statistics display
  - Auto-refresh every 30 seconds

---

**Need Help?** Check `SOCIAL_MEDIA_INTEGRATION_GUIDE.md` for detailed documentation.

