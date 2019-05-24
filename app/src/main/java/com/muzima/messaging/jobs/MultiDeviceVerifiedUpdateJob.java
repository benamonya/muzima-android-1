package com.muzima.messaging.jobs;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.muzima.messaging.TextSecurePreferences;
import com.muzima.messaging.crypto.UnidentifiedAccessUtil;
import com.muzima.messaging.jobmanager.JobParameters;
import com.muzima.messaging.jobmanager.SafeData;
import com.muzima.messaging.jobmanager.dependencies.InjectableType;
import com.muzima.messaging.sqlite.database.IdentityDatabase.VerifiedStatus;
import com.muzima.messaging.sqlite.database.SignalAddress;
import com.muzima.model.SignalRecipient;
import com.muzima.utils.Base64;

import org.whispersystems.libsignal.IdentityKey;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.signalservice.api.SignalServiceMessageSender;
import org.whispersystems.signalservice.api.crypto.UntrustedIdentityException;
import org.whispersystems.signalservice.api.messages.multidevice.SignalServiceSyncMessage;
import org.whispersystems.signalservice.api.messages.multidevice.VerifiedMessage;
import org.whispersystems.signalservice.api.push.exceptions.PushNetworkException;

import java.io.IOException;

import javax.inject.Inject;

import androidx.work.Data;
import androidx.work.WorkerParameters;

public class MultiDeviceVerifiedUpdateJob extends ContextJob implements InjectableType {

    private static final long serialVersionUID = 1L;

    private static final String TAG = MultiDeviceVerifiedUpdateJob.class.getSimpleName();

    private static final String KEY_DESTINATION     = "destination";
    private static final String KEY_IDENTITY_KEY    = "identity_key";
    private static final String KEY_VERIFIED_STATUS = "verified_status";
    private static final String KEY_TIMESTAMP       = "timestamp";

    @Inject
    transient SignalServiceMessageSender messageSender;

    private String destination;
    private byte[] identityKey;
    private VerifiedStatus verifiedStatus;
    private long timestamp;

    public MultiDeviceVerifiedUpdateJob(@NonNull Context context, @NonNull WorkerParameters workerParameters) {
        super(context, workerParameters);
    }

    public MultiDeviceVerifiedUpdateJob(Context context, SignalAddress destination, IdentityKey identityKey, VerifiedStatus verifiedStatus) {
        super(context, JobParameters.newBuilder()
                .withNetworkRequirement()
                .withGroupId("__MULTI_DEVICE_VERIFIED_UPDATE__")
                .create());

        this.destination    = destination.serialize();
        this.identityKey    = identityKey.serialize();
        this.verifiedStatus = verifiedStatus;
        this.timestamp      = System.currentTimeMillis();
    }

    @Override
    protected void initialize(@NonNull SafeData data) {
        destination    = data.getString(KEY_DESTINATION);
        verifiedStatus = VerifiedStatus.forState(data.getInt(KEY_VERIFIED_STATUS));
        timestamp      = data.getLong(KEY_TIMESTAMP);

        try {
            identityKey = Base64.decode(data.getString(KEY_IDENTITY_KEY));
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    protected @NonNull
    Data serialize(@NonNull Data.Builder dataBuilder) {
        return dataBuilder.putString(KEY_DESTINATION, destination)
                .putString(KEY_IDENTITY_KEY, Base64.encodeBytes(identityKey))
                .putInt(KEY_VERIFIED_STATUS, verifiedStatus.toInt())
                .putLong(KEY_TIMESTAMP, timestamp)
                .build();
    }

    @Override
    public void onRun() throws IOException, UntrustedIdentityException {
        try {
            if (!TextSecurePreferences.isMultiDevice(context)) {
                Log.i(TAG, "Not multi device...");
                return;
            }

            if (destination == null) {
                Log.w(TAG, "No destination...");
                return;
            }

            SignalAddress                       canonicalDestination = SignalAddress.fromSerialized(destination);
            VerifiedMessage.VerifiedState verifiedState        = getVerifiedState(verifiedStatus);
            VerifiedMessage               verifiedMessage      = new VerifiedMessage(canonicalDestination.toPhoneString(), new IdentityKey(identityKey, 0), verifiedState, timestamp);

            messageSender.sendMessage(SignalServiceSyncMessage.forVerified(verifiedMessage),
                    UnidentifiedAccessUtil.getAccessFor(context, SignalRecipient.from(context, SignalAddress.fromSerialized(destination), false)));
        } catch (InvalidKeyException e) {
            throw new IOException(e);
        }
    }

    private VerifiedMessage.VerifiedState getVerifiedState(VerifiedStatus status) {
        VerifiedMessage.VerifiedState verifiedState;

        switch (status) {
            case DEFAULT:    verifiedState = VerifiedMessage.VerifiedState.DEFAULT;    break;
            case VERIFIED:   verifiedState = VerifiedMessage.VerifiedState.VERIFIED;   break;
            case UNVERIFIED: verifiedState = VerifiedMessage.VerifiedState.UNVERIFIED; break;
            default: throw new AssertionError("Unknown status: " + verifiedStatus);
        }

        return verifiedState;
    }

    @Override
    public boolean onShouldRetry(Exception exception) {
        return exception instanceof PushNetworkException;
    }

    @Override
    public void onCanceled() {

    }
}
