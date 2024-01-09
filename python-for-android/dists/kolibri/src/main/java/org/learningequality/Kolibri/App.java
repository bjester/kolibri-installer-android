package org.learningequality.Kolibri;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.NotificationChannelCompat;
import androidx.work.Configuration;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import org.learningequality.NotificationRef;
import org.learningequality.Task;

import java.util.concurrent.Executors;

import java9.util.concurrent.CompletableFuture;

public class App extends Application implements Configuration.Provider {
    @Override
    public void onCreate() {
        super.onCreate();
        NotificationRef.initialize(this);
        createNotificationChannels();
        reconcileTasks();
    }

    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        String processName = getApplicationContext().getPackageName();
        processName += getApplicationContext().getString(R.string.task_worker_process);

        // Using the same quantity of worker threads as Kolibri's python side:
        // https://github.com/learningequality/kolibri/blob/release-v0.16.x/kolibri/utils/options.py#L683
        return new Configuration.Builder()
                .setDefaultProcessName(processName)
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .setExecutor(Executors.newFixedThreadPool(6))
            .build();
    }

    private void createNotificationChannels() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Context context = getApplicationContext();
            NotificationChannelCompat serviceChannel = new NotificationChannelCompat.Builder(
                    context.getString(R.string.notification_service_channel_id),
                    NotificationManagerCompat.IMPORTANCE_MIN
            )
                    .setName(context.getString(R.string.notification_service_channel_title))
                    .setShowBadge(false)
                    .build();
            NotificationChannelCompat taskChannel = new NotificationChannelCompat.Builder(
                    context.getString(R.string.notification_default_channel_id),
                    NotificationManagerCompat.IMPORTANCE_DEFAULT
            )
                    .setName(context.getString(R.string.notification_default_channel_title))
                    .build();

            // Register the channel with the system. You can't change the importance
            // or other notification behaviors after this.
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.createNotificationChannel(serviceChannel);
            notificationManager.createNotificationChannel(taskChannel);
        }
    }

    private void reconcileTasks() {
        // Reconcile tasks on startup, in this main thread (blocking!)
        boolean result = Task.reconcile(this, null);
        if (result) {
            Log.i("Kolibri", "Main thread task reconciliation completed");
        } else {
            Log.d("Kolibri", "Main thread task reconciliation no-op");
        }
    }
}
