# Sara Assistant (Android Starter)

यह एक Android starter project है जिसमें आपने जो foundational stack बताया, उसका बेसिक ढांचा बनाया गया है:

1. **Voice Input Module** - Android SpeechRecognizer intent के जरिए आवाज़ इनपुट (`VoiceInput.kt`, `MainActivity.kt`)
2. **Wake-Word System** - `WakeWordService` का foreground service skeleton (real wake-word engine अभी प्लग करना बाकी)
3. **Local Control Layer** - offline commands जैसे time, date, battery, settings (`LocalCommandHandler.kt`)
4. **Online Browsing Module** - DuckDuckGo HTML search via OkHttp (`OnlineSearch.kt`)
5. **Permission Management, Logging, Audit Trail** - runtime mic permission, confirmation flow, `audit.log` (`SecurityPolicy.kt`, `AuditLogger.kt`)
6. **Basic Security Rules** - trusted intent allowlist, blocked keywords, risky commands पर confirmation

> यह production-ready पूरा assistant नहीं है; यह एक साफ़ starter base है जिसे आप step-by-step आगे बढ़ा सकते हैं।

## Project Structure

```text
sara-android/
  app/
    src/main/
      AndroidManifest.xml
      java/com/sara/assistant/
        voice/VoiceInput.kt
        wake/WakeWordService.kt
        local/LocalCommandHandler.kt
        online/OnlineSearch.kt
        security/SecurityPolicy.kt
        logging/AuditLogger.kt
        ui/MainActivity.kt
      res/layout/activity_main.xml
```

## Setup (Android Studio)

### 1) जरूरी चीज़ें
- **Android Studio** (latest stable)
- **JDK 17** (Android Studio bundled JBR/JDK 17 ठीक रहेगा)
- **Android SDK**: API 35 install हो
- Internet connection (Gradle dependencies डाउनलोड के लिए)

### 2) Project खोलना
1. Android Studio खोलें.
2. **Open** पर क्लिक करें.
3. `/root/sara-android` (या जहाँ आपने project कॉपी किया है) select करें.
4. Gradle sync होने दें. पहली बार dependencies डाउनलोड होंगी.

### 3) अगर Gradle Wrapper न हो
इस workspace में Gradle wrapper files auto-generate नहीं किए गए हैं. सबसे आसान तरीका:
- Android Studio में project खोलते ही अगर wrapper मांगे, **"Generate Gradle Wrapper" / "Use default gradle wrapper"** चुनें.
- या terminal में (अगर system gradle installed है):
  ```bash
  gradle wrapper
  ```

### 4) Run करना
1. Android फोन को USB debugging से connect करें, या Emulator बनाएं.
2. Android Studio में **Run > Run 'app'**.
3. App खुलने पर **Listen** दबाएं और mic permission allow करें.

## Permissions
`AndroidManifest.xml` में:
- `RECORD_AUDIO` - आवाज़ सुनने के लिए
- `INTERNET`, `ACCESS_NETWORK_STATE` - online search के लिए
- `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MICROPHONE`, `POST_NOTIFICATIONS` - wake service के लिए

## यह अभी क्या करता है

- **Listen button**: speech recognizer खोलता है, Hindi (`hi-IN`) में input लेता है.
- **Intent detection**: simple keyword-based intent पहचान (`TIME`, `DATE`, `BATTERY_STATUS`, `WIFI_STATUS`, `SEARCH_WEB`, `LOGS`, etc.).
- **Security**: unknown intent block; blocked keywords block; `SEARCH_WEB` पर confirmation.
- **Local commands**: time/date/battery/settings.
- **Online search**: DuckDuckGo HTML endpoint से basic top result लाता है.
- **Audit log**: app internal storage में `audit.log` लिखता है. UI में `logs` बोलकर/command देकर दिखा सकते हैं.

## Wake Word को реально काम करने के लिए (Step 2 पूरा करना)

`WakeWordService.kt` अभी skeleton है. असली wake word के लिए:

1. **Engine चुनें**: [Picovoice Porcupine](https://picovoice.ai/platform/porcupine/) Android SDK आम विकल्प है.
2. Porcupine dependency add करें और access key/configure करें.
3. `WakeWordService.onCreate()` में PorcupineManager init करें.
4. Wake word detect होने पर:
   - notification update करें
   - `MainActivity` को broadcast/intent भेजें
   - या voice capture flow trigger करें
5. `onDestroy()` में engine/resources release करें.
6. Android 13+ पर notification permission और foreground mic service behavior टेस्ट करें.

## Security/Limits अभी क्या हैं

- Keyword intent detection basic है; production में NLU/LLM intent parser लगाएं.
- Online module simple HTML parsing करता है; proper API या safe browser integration बेहतर है.
- Trusted commands allowlist छोटी है; नए commands जोड़ते समय security review करें.
- Sensitive actions पर explicit user confirmation रखें.
- Logs में personal data लिखने से पहले redaction policy लगाएं.

## अगले सुधार (Recommended)

- Room/SQLite में structured audit logs
- Proper command router + NLU
- Wake word engine integration
- TTS response (TextToSpeech)
- App allowlist-based app launching
- Encrypted local storage for sensitive config
- Better error handling and background/foreground lifecycle handling
