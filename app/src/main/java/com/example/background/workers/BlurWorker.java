package com.example.background.workers;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.background.Constants;

public class BlurWorker extends Worker {

    private static final String TAG = BlurWorker.class.getSimpleName();

    public BlurWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Context applicationContext = getApplicationContext();

            String resourceUri = getInputData().getString(Constants.KEY_IMAGE_URI);

// REPLACE THIS CODE:
            // Bitmap picture = BitmapFactory.decodeResource(
            //        applicationContext.getResources(),
            //        R.drawable.test);
            // WITH

            if (TextUtils.isEmpty(resourceUri)) {
                Log.e(TAG, "Invalid input uri");
                throw new IllegalArgumentException("Invalid input uri");
            }
            ContentResolver resolver = applicationContext.getContentResolver();

            //Create a bitmap
            Bitmap picture = BitmapFactory.decodeStream(
                    resolver.openInputStream(Uri.parse(resourceUri)));

            // Blur the bitmap
            Bitmap blurredPicture = WorkerUtils.blurBitmap(picture, applicationContext);

            // Write bitmap to a temp file
            Uri outputUri = WorkerUtils.writeBitmapToFile(applicationContext, blurredPicture);

            WorkerUtils.makeStatusNotification("Output is " + outputUri.toString()/*uri != null ? uri.toString() : "File not found"*/, applicationContext);

            Data outputData = new Data.Builder()
                    .putString(Constants.KEY_IMAGE_URI, outputUri.toString()).build();

            // If there were no errors, return SUCCESS
            return Result.success(outputData);
        } catch (Throwable throwable) {
            // Technically WorkManager will return Result.failure()
            // but it's best to be explicit about it.
            // Thus if there were errors, we're return FAILURE
            Log.e(TAG, "Error applying blur", throwable);
            return Result.failure();
        }
    }
}
