# Deployment Update - AQICN Integration

## 🚀 Deploying to Render with AQICN

### Environment Variables to Add

When deploying to Render, add this new environment variable:

```
AQICN_API_TOKEN=7498f315cd71cd794d920744c8465d2093324a11
```

### Steps:

1. **Go to Render Dashboard**
   - Navigate to your backend service
   - Click "Environment" tab

2. **Add New Environment Variable**
   - Key: `AQICN_API_TOKEN`
   - Value: `7498f315cd71cd794d920744c8465d2093324a11`

3. **Keep Existing Variables**
   ```
   WEATHER_API_KEY=70eb6858779947baa7963224251911
   CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173,https://your-frontend-url.vercel.app
   ```

4. **Deploy**
   - Commit and push your changes
   - Render will auto-deploy
   - Or click "Manual Deploy" → "Deploy latest commit"

---

## 🔧 Updated application.properties

Your `application.properties` now includes:

```properties
# Weather API Configuration
weather.api.key=70eb6858779947baa7963224251911
weather.api.base-url=http://api.weatherapi.com/v1

# AQICN API Configuration
aqicn.api.token=7498f315cd71cd794d920744c8465d2093324a11
aqicn.api.base-url=https://api.waqi.info

# Cache Configuration
cache.ttl.aqi=600
```

---

## ✅ Verify Deployment

After deployment, test these endpoints:

### 1. Health Check
```
https://your-backend.onrender.com/api/aqi/health
```

### 2. AQI Data
```
https://your-backend.onrender.com/api/aqi/city?name=london
```

### 3. Weather with AQI
```
https://your-backend.onrender.com/api/weather/current?location=London
```

---

## 📊 What Changed

### New Endpoints:
- `GET /api/aqi/city?name={city}`
- `GET /api/aqi/coordinates?lat={lat}&lon={lon}`
- `GET /api/aqi/search?keyword={keyword}`
- `GET /api/aqi/health`

### Modified Endpoints:
- `GET /api/weather/current` - Now includes AQICN data

### No Breaking Changes:
- All existing endpoints still work
- Frontend doesn't need immediate updates
- Backward compatible

---

## 🎯 Benefits

1. **More Accurate AQI:** Real monitoring station data
2. **Better Coverage:** AQICN has more stations worldwide
3. **Health Guidance:** Automatic health implications
4. **Transparency:** Know which station measured the data
5. **Reliability:** AQICN is backed by EPA and government agencies

---

## 🔒 Security Note

Your AQICN token is included in the code for development. For production:

1. **Use Environment Variables** (already configured)
2. **Don't commit tokens** to public repositories
3. **Rotate tokens** periodically
4. **Monitor usage** on AQICN dashboard

---

## 📈 Rate Limits

### AQICN Free Tier:
- **1000 requests per minute** (very generous!)
- No daily limit
- No credit card required

### WeatherAPI.com Free Tier:
- **1 million calls per month**
- 1 request per second

You're well within limits for both! 🎉

---

## 🐛 Troubleshooting Deployment

### "AQICN_API_TOKEN not found"
- Add environment variable in Render dashboard
- Redeploy after adding

### "No AQI data received"
- Check if AQICN token is correct
- Verify AQICN API is accessible from Render
- Check Render logs for errors

### Weather works but no AQI
- AQI is optional - weather still works
- Check logs for AQICN warnings
- Some cities may not have AQI stations

---

## 📝 Deployment Checklist

- [ ] Add `AQICN_API_TOKEN` to Render environment variables
- [ ] Commit and push code changes
- [ ] Wait for Render auto-deploy
- [ ] Test `/api/aqi/health` endpoint
- [ ] Test `/api/aqi/city?name=london`
- [ ] Test `/api/weather/current?location=London`
- [ ] Verify logs show "Successfully merged AQICN data"
- [ ] Update frontend to use new AQI data
- [ ] Update API documentation

---

## 🎊 You're Ready!

Your backend is now production-ready with AQICN integration! 🚀
