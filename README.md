# 🌾 Rice Disease Detection - Android App

An AI-powered native Android application that detects diseases in rice leaves using lightweight TensorFlow Lite models and ensemble learning. Built to help farmers identify diseases quickly and accurately in the field.

<p align="center">
  <img src="https://img.shields.io/badge/platform-Android-brightgreen?style=for-the-badge&logo=android">
  <img src="https://img.shields.io/badge/ML-TensorFlow%20Lite-orange?style=for-the-badge&logo=tensorflow">
</p>

---

## 📱 Features

- 🔍 Detects multiple rice leaf diseases using **DenseNet169** and **InceptionV3** models
- 🧠 Ensemble model with meta-learning for improved accuracy
- 🌙 Dark mode support
- ✂️ Image cropping & adjustment before prediction
- 🌐 Multi-language support for accessibility
- 📤 Share detection results easily
- 📈 Displays confidence score and result summary

---

## 🧠 Models Used

- `DenseNet169.tflite`
- `InceptionV3.tflite`
- Meta-model (stacked neural net in TFLite format)

---

## 🛠️ Built With

- Android Studio (Java/Kotlin)
- TensorFlow Lite
- XML Layouts & Jetpack components
- Glide (for image loading)
- ML Model integration using `Interpreter`

---

## 🧪 How It Works

1. User captures or uploads a rice leaf image.
2. App preprocesses the image and runs inference using both DenseNet and InceptionV3 models.
3. The confidence scores from each model are sent to the meta-model.
4. The final prediction is displayed with animation and UI feedback.

---
## 🖼️ Screenshots

> 📌 *Add screenshots here after capturing them from the emulator/device.*
> ![WhatsApp Image 2025-04-08 at 9 42 17 AM](https://github.com/user-attachments/assets/986a95ae-717b-46f6-89f2-d131c6e6f47c)
> ![WhatsApp Image 2025-04-08 at 9 42 17 AM](https://github.com/user-attachments/assets/2f699e54-503f-4719-9cfe-0b70bafee17d)
> ![WhatsApp Image 2025-04-08 at 9 42 17 AM](https://github.com/user-attachments/assets/661dea96-2e1d-4912-939f-246536a3fc4e)




---


## 🤝 Contributing

Feel free to fork the repo, make enhancements, and create pull requests! Let's build smarter tools for agriculture together 🌱

---


## ⭐️ Show some love

If you like this project, don't forget to ⭐️ the repo and share it!

