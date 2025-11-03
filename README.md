# 🌍 EarthCast  

**EarthCast** is a native Android weather application built with **Kotlin**, designed to bring together my understanding of Android development, APIs, and location-based services into one practical project.  

The goal behind EarthCast was simple — to take what I learned about Android and apply it to something useful and real. The app automatically detects your location using the **Fused Location Provider**, fetches the nearest city or town through **Open-Meteo’s reverse geocoding endpoint**, and retrieves accurate, real-time weather data for that area.  

All data — including reverse geocoding and weather details — comes directly from **Open-Meteo**, which provides fast, reliable, and free access to JSON-based weather information through dynamic URL queries. There are no external APIs or SDKs involved — just pure Kotlin code handling network requests and JSON parsing.  

Building EarthCast helped me understand how Android components work together — from permissions and location handling to asynchronous API calls, data parsing, and UI updates. It’s a simple concept executed with clean logic, and I’m genuinely proud of how it turned out.  

---

## 🔧 Key Features  

- Developed entirely in **Kotlin**  
- Uses **Fused Location Provider** for high-accuracy coordinates  
- Fetches both **location name** and **weather data** directly from **Open-Meteo** endpoints  
- Performs **reverse geocoding** via Open-Meteo’s JSON response (no other third-party services)  
- Implements **manual JSON parsing** for better control and learning  
- Displays temperature, conditions, and other weather parameters in a clean layout  
- Fully responsive and optimized for mobile devices  
- Built to strengthen understanding of **networking, permissions, and location APIs**  

---

## ⚙️ Tech Stack  

- **Language:** Kotlin  
- **Platform:** Android SDK  
- **Location Service:** FusedLocationProviderClient  
- **Weather & Reverse Geocoding:** Open-Meteo endpoints  
- **Parsing:** JSON (using native Android tools)  
- **Build System:** Gradle  

---

## 🧠 Learning & Purpose  

EarthCast was built as a learning-driven project — not from a tutorial, but by connecting ideas and experimenting.  
Through this, I learned:  
- How to handle Android permissions (especially location) correctly  
- How to make HTTP requests and parse JSON manually  
- How to use `FusedLocationProviderClient` effectively for real-time location updates  
- How to integrate APIs that don’t offer SDKs, by manually constructing query URLs  
- How to connect UI elements to live data updates for a smooth experience  

The goal was never just to make a weather app — it was to make something that actually uses real-world data and improves my understanding of Android architecture in the process.  

---

## 🧩 Future Improvements  

- Add hourly and weekly forecast data  
- Include icons or animations for weather conditions  
- Add offline caching for previously fetched data  
- Improve UI with Material You design principles  

---

## 📫 Connect with Me  

- **LinkedIn:** [linkedin.com/in/manasbeniwal](https://linkedin.com/in/manasbeniwal)  
- **GitHub:** [MaNaS0708](https://github.com/MaNaS0708)
