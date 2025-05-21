# 🔐 CryptoSentinel

CryptoSentinel is a modern Android app for secure AES-based text encryption and decryption with biometric protection, QR code generation, theme/language selection, and local history tracking.

## 🚀 Features

- 🔒 AES-128 CBC mode encryption & decryption
- 📦 Room database to store encryption/decryption history
- 📱 Biometric authentication (Fingerprint/PIN) on app launch
- 📷 Automatic QR code generation for encrypted/decrypted text
- 🌓 Theme selection: Light / Dark / System default
- 🌍 Language selection: English / Turkish / System default
- 📋 Copy & Share functionality
- 🧠 MVVM architecture with ViewBinding & NavigationComponent

## 📱 Screens

- **Home:** Navigate to Encryption, Decryption, History, or Settings
- **Encryption:** Enter text → encrypt → view result + QR code
- **Decryption:** Enter encrypted text → decrypt → view result + QR code
- **History:** View all stored encryption/decryption records
- **Settings:** Theme and language preferences

## 📸 Screenshots

| Home | Encrypt | Decrypt |
|------|---------|---------|
| ![](assets/home_screen.png) | ![](assets/encrypt_screen.png) | ![](assets/decrypt_screen.png) |
| History | Home Dark |
|---------|-----------|
| ![](assets/history_screen.png) | ![](assets/dark_home_screen.png) |

## 💾 Technologies Used

- Kotlin + Jetpack libraries (Navigation, Room, ViewModel, Biometric)
- ZXing for QR code generation
- Material Design 3
- MVVM pattern + ViewBinding
- Proguard optimizations

## 📦 Installation

```bash
git clone https://github.com/yourusername/CryptoSentinel.git
