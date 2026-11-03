# EasyCellular

iOS-приложение для iPhone (iOS 16+): тип сотовой сети (2G / 3G / 4G / 5G / 5G+), оператор, пинг, измеритель скорости, история результатов. Интерфейс на русском и английском.

## Структура проекта

```
EasyCellular/                          ← корень репозитория (здесь project.yml)
├── project.yml                        ← описание проекта для XcodeGen
├── README.md                          ← этот файл
├── .gitignore
├── .github/
│   └── workflows/
│       └── build.yml                  ← GitHub Action: сборка IPA
└── EasyCellular/                      ← исходный код приложения
    ├── Assets.xcassets/               ← иконка приложения (AppIcon)
    ├── Strings.swift                  ← все тексты (RU / EN)
    ├── App/
    │   ├── EasyCellularApp.swift      ← точка входа, вкладки
    │   └── AppSettings.swift          ← язык, хост для пинга
    ├── Core/
    │   ├── CellularMonitor.swift      ← оператор, SIM, активная сеть, IP
    │   ├── NetworkGeneration.swift    ← 2G / 3G / 4G / 5G / 5G+
    │   ├── PingTester.swift           ← пинг
    │   ├── SpeedTestEngine.swift      ← замер скорости и SpeedConfig
    │   └── HistoryStore.swift         ← история результатов
    └── UI/
        ├── Theme.swift                ← цвета, карточки, общие элементы
        ├── NetworkView.swift          ← вкладка «Сеть»
        ├── SpeedTestView.swift        ← вкладка «Тест»
        ├── HistoryView.swift          ← вкладка «История»
        └── SettingsView.swift         ← вкладка «Настройки»
```

Если создаёте проект вручную, повторите эту структуру: файлы из папки `EasyCellular/` кладите в указанные подпапки, а `project.yml`, `.github` и `.gitignore` — в корень.

## Сборка через GitHub Actions

1. Создайте новый репозиторий на GitHub, например `EasyCellular`.
2. Распакуйте архив и залейте содержимое в репозиторий:

```bash
cd EasyCellular
git init
git add .
git commit -m "EasyCellular"
git branch -M main
git remote add origin https://github.com/<ваш-логин>/EasyCellular.git
git push -u origin main
```

3. Откройте вкладку **Actions**. Сборка запустится после push. Можно также выбрать **Build IPA → Run workflow**.
4. Когда появится зелёная галочка, откройте запуск и скачайте артефакт **EasyCellular-IPA** внизу страницы.
5. Установите `EasyCellular.ipa` на iPhone через Sideloadly, AltStore или Feather. IPA собирается без подписи, подпись ставит установщик с вашим Apple ID.

Локально, если хотите открыть в Xcode: установите XcodeGen (`brew install xcodegen`), в корне выполните `xcodegen generate` и откройте `EasyCellular.xcodeproj`.

## Что где настраивается

- Bundle ID: `com.werty.easycellular` в `project.yml` (`PRODUCT_BUNDLE_IDENTIFIER`).
- Порог 5G+, длительность теста, адреса серверов и шкала спидометра: `SpeedConfig` в `Core/SpeedTestEngine.swift`.
- Хост для пинга: во вкладке «Настройки» приложения.
- Иконка: лежит в `EasyCellular/Assets.xcassets/AppIcon.appiconset/AppIcon.png` (1024×1024). Замените файл своим PNG без прозрачности, имя оставьте прежним.

## Как добавить язык

1. В `App/AppSettings.swift` добавьте case в `enum AppLanguage` и его `displayName`.
2. В `Strings.swift` для каждого ключа добавьте перевод под новым кодом, например `"de": "..."`.

## Ограничения iOS (честно)

- Уровень сигнала в dBm и «полоски» публично приложениям не доступны. Поэтому в приложении их нет.
- Cell ID, номер вышки, диапазон (band) и PCI недоступны.
- Значок 5G+ iOS напрямую не отдаёт. Приложение показывает 5G+, когда сеть 5G и последний тест дал от 500 Мбит/с (порог меняется в `SpeedConfig`).
- Название оператора: начиная с iOS 16 Apple часто скрывает его от сторонних приложений. В таком случае показывается «Недоступно в iOS».
- ICMP-пинг (как `ping` в терминале) требует привилегий. Поэтому пинг измеряет время TCP-подключения к 1.1.1.1:443.
- Тест скорости тратит трафик. На канале 1 Гбит/с и выше один тест может съесть около гигабайта и больше. Лучше запускать его на Wi-Fi или на безлимитном тарифе.
- Тесты используют публичные серверы Cloudflare (`speed.cloudflare.com`), внешний IP определяется через `api.ipify.org`.

## Если сборка упала

Откройте лог шага «Build (unsigned, Release)», найдите строку с `error:` и пришлите её. Проект не проверялся в Xcode, поэтому мелкие правки после первой сборки возможны.
