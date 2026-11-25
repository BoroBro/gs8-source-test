# GS8 Source Test

Минимальный тест для проверки интеграции с ZHLogic (`com.zhonghong.logic`)
на штатной магнитоле GAC GS8.

Делает ровно одно: биндится к `LogicService`, регистрирует колбэк на
домен источников (DOMAIN_SOURCE = 2) и по кнопкам шлёт `setSourceId`.
Все события от ГУ (`onSourceChanged`, `onFoucsStateChanged`,
`onThirdSourceChanged`, `onMediaButtonPressed`) печатаются в лог на экране.

## Цель теста
1. Запустить Android Auto / HUR, включить музыку.
2. Открыть это приложение, нажать **BIND + register**.
3. Нажать **setSourceId = 15** и смотреть:
   - встаёт ли HUR на паузу (аудиофокус);
   - какие события приходят в лог.
4. Нажать кнопку руля Next/Prev и смотреть, приходит ли
   `onMediaButtonPressed` к нам и реагирует ли радио параллельно.

APK собирается в GitHub Actions, артефакт `GS8SourceTest-apk`.
