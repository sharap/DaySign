# Выпуск новой версии

## Однократная подготовка: ключ подписи

Ключ создаётся один раз и хранится вечно: APK, подписанные разными ключами,
Android считает разными приложениями и не даёт обновить одно поверх другого.

```bash
keytool -genkeypair -v \
  -keystore ~/keys/daysign-release.jks \
  -alias daysign \
  -keyalg RSA -keysize 4096 -validity 10950 \
  -dname "CN=<имя>, O=<организация>, C=RU"
```

Затем в корне проекта создать `keystore.properties` (он в `.gitignore`,
в репозиторий не попадает):

```properties
storeFile=/home/<пользователь>/keys/daysign-release.jks
storePassword=<пароль хранилища>
keyAlias=daysign
keyPassword=<пароль ключа>
```

**Сделайте резервную копию `.jks` и паролей** в место, не связанное с этой
машиной. Потеря ключа необратима: обновлять уже установленное приложение будет
нечем.

## Выпуск

1. Поднять версию в `app/build.gradle.kts` — `versionCode` и `versionName`,
   и строку `about_version` в `res/values/strings.xml` и `res/values-en/strings.xml`.
2. Описать изменения в `CHANGELOG.md`.
3. Проверить и собрать:

```bash
./gradlew test                    # юнит-тесты
./gradlew assembleRelease         # APK с R8
```

4. Убедиться, что APK подписан рабочим ключом, а не отладочным:

```bash
$ANDROID_HOME/build-tools/36.0.0/apksigner verify --print-certs \
  app/build/outputs/apk/release/app-release.apk
```

В выводе должен быть ваш сертификат. Если там `CN=Android Debug` —
`keystore.properties` не найден, и такой файл публиковать нельзя.

5. Переименовать артефакт и поставить тег:

```bash
cp app/build/outputs/apk/release/app-release.apk DaySign-1.0.5.apk
git tag -a v1.0.5 -m "DaySign 1.0.5"
git push origin v1.0.5
```

6. Создать выпуск на GitHub, приложив `DaySign-1.0.5.apk` и раздел из
   `CHANGELOG.md`.

## Проверка перед публикацией

- [ ] `./gradlew test` проходит
- [ ] APK подписан рабочим ключом (не `CN=Android Debug`)
- [ ] `versionCode` увеличен относительно предыдущего выпуска
- [ ] `versionName` и `about_version` совпадают
- [ ] `CHANGELOG.md` описывает выпуск
- [ ] `keystore.properties` и `.jks` не попали в репозиторий
