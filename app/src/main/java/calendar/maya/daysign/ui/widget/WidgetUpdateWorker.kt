package calendar.maya.daysign.ui.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class WidgetUpdateWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            DaysignWidget().updateAll(applicationContext)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
