# План реализации раздела "Люди и Группы"

Необходимо доработать функциональность управления людьми и группами, добавив возможность поиска, детального просмотра профилей, расчета взаимодействий (связей) между людьми и пересечений знаков в группах, основываясь на референсе из Cordova-проекта.

## User Review Required

> [!IMPORTANT]
> Для навигации между экранами (список -> профиль) будет использован стандартный `androidx.navigation`, что потребует небольшой переработки `MainActivity`.
> Расчет связей (Kin Connections) будет выполняться в памяти при инициализации ViewModel, так как это относительно легкая операция (260x260).

## Proposed Changes

### Data Layer

#### [MODIFY] [PeopleDao.kt](file:///home/user/and/Daysign/app/src/main/java/calendar/maya/daysign/data/PeopleDao.kt)
* Добавление методов `updatePerson` и `updateGroup`.
* (Опционально) Добавление `getPersonById` и `getGroupById`.

---

### Logic Layer

#### [MODIFY] [MayaCalendar.kt](file:///home/user/and/Daysign/app/src/main/java/calendar/maya/daysign/logic/MayaCalendar.kt)
* Реализация метода `calculateKinConnections(kin1: Int, kin2: Int): List<Int>`, который возвращает типы связей между двумя кинами (поддержка, контроль и т.д.).
* Перенос логики определения типов связей из `maya.js`.

---

### ViewModel

#### [MODIFY] [MainViewModel.kt](file:///home/user/and/Daysign/app/src/main/java/calendar/maya/daysign/ui/MainViewModel.kt)
* Добавление логики поиска людей.
* Реализация методов обновления данных.
* Добавление StateFlow для выбранного человека/группы (если не использовать аргументы навигации).

---

### UI Components

#### [NEW] [SignChips.kt](file:///home/user/and/Daysign/app/src/main/java/calendar/maya/daysign/ui/components/SignChips.kt)
* Компонент для отображения знака дня и трецены с названиями (аналог `SignChips.jsx`).

#### [NEW] [InfluenceList.kt](file:///home/user/and/Daysign/app/src/main/java/calendar/maya/daysign/ui/components/InfluenceList.kt)
* Список людей с указанием типа связи с текущим человеком (аналог `influencePeople.jsx`).

---

### Screens

#### [MODIFY] [PeopleScreen.kt](file:///home/user/and/Daysign/app/src/main/java/calendar/maya/daysign/ui/screens/PeopleScreen.kt)
* Добавление `Searchbar`.
* Реализация `AddGroupDialog` с выбором участников.
* Улучшение `AddPersonDialog` (выбор пола, времени восхода, предпросмотр знака).
* Переход на детальные экраны.

#### [NEW] [PersonProfileScreen.kt](file:///home/user/and/Daysign/app/src/main/java/calendar/maya/daysign/ui/screens/PersonProfileScreen.kt)
* Детальная информация о человеке: дата, кин, описание.
* Список влияний (кто из списка людей связан с этим человеком).
* Кнопка редактирования.

#### [NEW] [GroupDetailScreen.kt](file:///home/user/and/Daysign/app/src/main/java/calendar/maya/daysign/ui/screens/GroupDetailScreen.kt)
* Список участников группы.
* Раздел "Результат взаимодействия" (подсчет пересечений знаков всех участников).
* Кнопка редактирования состава группы.

## Verification Plan

### Automated Tests
* Юнит-тесты для `MayaCalendar.calculateKinConnections` для проверки корректности маппинга типов связей.

### Manual Verification
* Проверка создания человека с разными параметрами (пол, восход).
* Проверка создания группы и отображения участников.
* Проверка поиска в списке людей.
* Сравнение результатов расчета связей с Cordova-версией.
