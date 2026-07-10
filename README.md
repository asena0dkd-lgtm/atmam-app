# اتمم - Atmam

تطبيق مهام + ملاحظات + مدونة. يعمل 100% بدون إنترنت وبخصوصية كاملة.

## الميزات

### المهام
- إنشاء مهام مع أولوية (عاجل/مهم/عادي) ومستوى طاقة
- تكرار المهام (يومي/أسبوعي/شهري/سنوي)
- عداد التأجيل مع نظام ألوان تحذيري
- إشعارات وتذكيرات
- سلة محذوفات مؤقتة (30 يوم)
- إحصائيات وسلاسل إنجاز

### الملاحظات
- ملاحظات نصية غنية
- تصنيف بالمجلدات والوسوم
- ربط تلقائي بين الملاحظات
- قفل الملاحظات برقم سري
- تحويل الملاحظات لمهام

## التقنيات
- Kotlin
- SQLite (Room-free)
- Material Design 3
- 100% Offline
- بدون AI

## بناء APK باستخدام GitHub Actions

لإضافة workflow file، أنشئ ملف `.github/workflows/build-apk.yml` في المستودع بنفسك:

```yaml
name: Build APK

on:
  push:
    branches: [ main, master ]
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v4
    - uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
        cache: gradle
    - run: chmod +x gradlew
    - run: ./gradlew assembleDebug
    - uses: actions/upload-artifact@v4
      with:
        name: atmam-debug-apk
        path: app/build/outputs/apk/debug/app-debug.apk
```

## الترخيص
MIT
