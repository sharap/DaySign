# Implementation Plan - Fix Widget Updates

This plan aims to ensure the widget updates on any database change (people) and every hour automatically.

## User Review Required

> [!IMPORTANT]
> The "every hour" update will be implemented using `WorkManager`. This is more reliable than the standard `updatePeriodMillis` which can be delayed by the system for battery optimization.

## Proposed Changes

### Dependencies

#### [MODIFY] [libs.versions.toml](file:///home/user/and/Daysign/gradle/libs.versions.toml)
- Add `androidx-work-runtime-ktx` library.

#### [MODIFY] [build.gradle.kts](file:///home/user/and/Daysign/app/build.gradle.kts)
- Add WorkManager dependency.

### Widget Update Logic

#### [NEW] [WidgetUpdateWorker.kt](file:///home/user/and/Daysign/app/src/main/java/calendar/maya/daysign/ui/widget/WidgetUpdateWorker.kt)
- Create a worker that calls `DaysignWidget().updateAll(context)`.

#### [MODIFY] [DaysignWidget.kt](file:///home/user/and/Daysign/app/src/main/java/calendar/maya/daysign/ui/widget/DaysignWidget.kt)
- Add a helper function to schedule/enqueue the periodic worker.
- Update `DaysignWidgetReceiver` to schedule the worker when the widget is enabled and cancel it when disabled.

### Database Change Triggers

#### [MODIFY] [MainViewModel.kt](file:///home/user/and/Daysign/app/src/main/java/calendar/maya/daysign/ui/MainViewModel.kt)
- Ensure all methods that modify the people database trigger a widget update.
- I will also add the trigger to group modifications just in case, although the widget currently only shows people birthdays.

## Verification Plan

### Automated Tests
- Build the project to ensure dependencies are correct.

### Manual Verification
1. Add a new person in the app and verify the widget updates (if the person's kin matches today).
2. Edit an existing person and verify the widget updates.
3. Delete a person and verify the widget updates.
4. Verify that the periodic worker is scheduled (using `adb shell dumpsys jobscheduler` or similar, or just waiting).
5. Trigger the worker manually via ADB to verify it updates the widget.
