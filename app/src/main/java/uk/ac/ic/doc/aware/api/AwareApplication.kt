package uk.ac.ic.doc.aware.api

import android.app.Activity
import android.app.Application
import android.os.Bundle


class AwareApplication : Application() {
    private var currentActivity: Activity? = null

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(ActivityLifecycleListener())
    }

    private inner class ActivityLifecycleListener : ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            currentActivity = activity
        }

        override fun onActivityStarted(activity: Activity) {
            currentActivity = activity
        }

        override fun onActivityResumed(activity: Activity) {
            currentActivity = activity
        }

        override fun onActivityPaused(activity: Activity) {
            // No need to track paused activities
        }

        override fun onActivityStopped(activity: Activity) {
            // No need to track stopped activities
        }

        override fun onActivityDestroyed(activity: Activity) {
            // No need to track destroyed activities
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            // No need to track activity state changes
        }
    }

    fun getCurrentActivity(): Activity? {
        return currentActivity
    }
}