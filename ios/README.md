# 📶 EasyCellular

![iOS](https://img.shields.io/badge/iOS-16%2B-blue?logo=apple)
![Swift](https://img.shields.io/badge/Swift-5-orange?logo=swift)
![Languages](https://img.shields.io/badge/языки-RU%20%7C%20EN-green)
![Build](https://github.com/<ваш-логин>/EasyCellular/actions/workflows/build.yml/badge.svg)

**EasyCellular** — простое приложение для iPhone, которое показывает, в какой сети вы сейчас находитесь, и проверяет её скорость и пинг. Без лишних цифр и сложных настроек: открыли, увидели, поняли.

> Поддержка: iPhone с iOS 16 и новее. Интерфейс на русском и английском.

---

## ✨ Возможности

### 📡 Сеть
- Тип сети: **2G, 3G, 4G, 5G** и **5G+** (до 1.5 Гбит/с)
- Название оператора, страна и код MCC-MNC
- Все SIM-карты, включая eSIM и две SIM одновременно
- Статус мобильных данных и режим экономии трафика
- Активный интерфейс: Wi-Fi, сотовая сеть или Ethernet
- Локальный IP (сотовый и Wi-Fi) и внешний IP

### ⚡ Тест скорости
- Пинг с минимумом, максимумом, джиттером и потерями
- Спидометр для **загрузки** и **отдачи**
- Плавная анимация стрелки и пульсация во время теста
- Результат можно отправить через системное меню «Поделиться»

### 🕘 История
- Все результаты сохраняются на устройстве
- График загрузки за последние тесты
- Удаление отдельных записей или очистка всей истории

### ⚙️ Настройки
- Язык интерфейса: **русский** или **английский**
- Хост для пинга
- Очистка истории

---

## 🚀 Установка

Приложение пока не опубликовано в App Store. Установить его можно из собранного IPA-файла.

1. Откройте вкладку **Actions** в этом репозитории.
2. Выберите последний успешный запуск **Build IPA**.
3. Внизу страницы скачайте артефакт **EasyCellular-IPA**.
4. Установите `EasyCellular.ipa` на iPhone через одно из приложений:
   - [Sideloadly](https://sideloadly.io)
   - [AltStore](https://altstore.io)
   - [Feather](https://github.com/khcrysalis/Feather)

Понадобится бесплатный Apple ID. Приложение подписывает установщик, поэтому сам IPA подписи не содержит.

---

## 🛠 Сборка из исходников

Проект описан через [XcodeGen](https://github.com/yonaskolb/XcodeGen), поэтому файл `.xcodeproj` в репозитории не хранится.

```bash
brew install xcodegen
xcodegen generate
open EasyCellular.xcodeproj
```

Сборка в GitHub Actions запускается автоматически при каждом push в `main` и вручную через **Run workflow**.

---

## 🗂 Структура проекта

```
EasyCellular/
├── project.yml              # описание проекта для XcodeGen
├── .github/workflows/       # GitHub Action: сборка IPA
└── EasyCellular/
    ├── Strings.swift        # все тексты: RU и EN
    ├── Assets.xcassets/     # иконка приложения
    ├── App/                 # точка входа и настройки
    ├── Core/                # сеть, пинг, скорость, история
    └── UI/                  # экраны и общие элементы
```

---

## 🌍 Добавить язык

1. В `App/AppSettings.swift` добавьте новый case в `enum AppLanguage`.
2. В `Strings.swift` для каждого ключа добавьте перевод под новым кодом, например `"de": "…"`.

Если перевода нет, приложение покажет английский вариант.

---

## ⚠️ Ограничения iOS

Apple не открывает приложениям некоторые данные о сети. Поэтому EasyCellular честно показывает то, что система разрешает:

- **Уровень сигнала** в dBm и «полоски» недоступны.
- **Вышки, Cell ID и диапазоны (band)** недоступны.
- **Значок 5G+** iOS не отдаёт. Приложение показывает 5G+, если сеть 5G, а тест дал от 500 Мбит/с. Порог меняется в `SpeedConfig`.
- **Название оператора** с iOS 16 часто скрыто. В таком случае будет написано «Недоступно в iOS».
- **Пинг** измеряется как время TCP-подключения к `1.1.1.1:443`, потому что ICMP без специальных прав недоступен.

> 💡 Тест скорости расходует трафик. На канале 1 Гбит/с и выше один запуск может потратить около гигабайта. Лучше запускать его на Wi-Fi или на безлимитном тарифе.

---

## 🔒 Приватность

- Приложение не отправляет ваши данные на свои серверы.
- История хранится только на устройстве.
- Замеры идут на публичные серверы [Cloudflare](https://speed.cloudflare.com), а внешний IP определяется через [ipify](https://www.ipify.org).

---

## 🤝 Вклад в проект

Идеи, баг-репорты и pull request приветствуются. Откройте Issue, если нашли ошибку или хотите новую функцию.

---

## 📄 Лицензия

Пока не выбрана. Добавьте файл `LICENSE`, когда решите, как распространять проект.

---

<details>
<summary><b>🇬🇧 English summary</b></summary>

**EasyCellular** is a simple iPhone app (iOS 16+) that shows your current cellular network type (2G to 5G+), carrier, SIM cards, ping, and download/upload speed. The UI is available in Russian and English, and you can switch languages in Settings.

**Install:** download the `EasyCellular-IPA` artifact from the latest successful **Build IPA** run in the Actions tab, then sideload it with Sideloadly, AltStore, or Feather using a free Apple ID.

**Build:** `brew install xcodegen && xcodegen generate`, then open `EasyCellular.xcodeproj`.

**Limits:** iOS does not expose signal strength, cell towers, bands, or the 5G+ badge, so 5G+ is estimated from 5G plus a test speed of at least 500 Mbps. Carrier names are often hidden on iOS 16 and later.

</details>
