package com.muzima.messaging.jobs;

import android.content.Context;
import android.support.annotation.NonNull;

import com.muzima.messaging.jobmanager.Job;
import com.muzima.messaging.jobmanager.JobParameters;
import com.muzima.messaging.jobmanager.dependencies.ContextDependent;

import androidx.work.WorkerParameters;

public abstract class ContextJob extends Job implements ContextDependent {
    protected transient Context context;

    protected ContextJob(@NonNull Context context, @NonNull WorkerParameters workerParameters) {
        super(context, workerParameters);
    }

    protected ContextJob(@NonNull Context context, @NonNull JobParameters parameters) {
        super(context, parameters);
        this.context = context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    protected Context getContext() {
        return context;
    }
}
