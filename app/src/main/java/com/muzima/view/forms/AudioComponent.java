/* * Copyright (c) The Trustees of Indiana University, Moi University * and Vanderbilt University Medical Center. All Rights Reserved. * * This version of the code is licensed under the MPL 2.0 Open Source license * with additional health care disclaimer. * If the user is an entity intending to commercialize any application that uses *  this code in a for-profit venture,please contact the copyright holder. */package com.muzima.view.forms;import android.app.Activity;import android.content.Intent;import android.webkit.JavascriptInterface;import com.muzima.utils.EnDeCrypt;import com.muzima.utils.StringUtils;import com.muzima.utils.audio.AudioIntent;import com.muzima.utils.audio.AudioResult;import java.io.File;import static com.muzima.utils.audio.AudioIntent.KEY_AUDIO_CAPTION;import static com.muzima.utils.audio.AudioIntent.KEY_AUDIO_PATH;import static com.muzima.utils.audio.AudioIntent.KEY_SECTION_NAME;class AudioComponent {    private static final int REQUEST_CODE = 0x0000c0dd;    private static final String FORM_UUID = "formUuid";    private final Activity activity;    private String audioPathField;    private String audioCaptionField;    public AudioComponent(Activity activity) {        this.activity = activity;    }    @JavascriptInterface    public void startAudioIntent(String sectionName, String audioPathField, String audioPath,                                 String audioCaptionField, String audioCaption, String formUuid) {        this.audioPathField = audioPathField;        this.audioCaptionField = audioCaptionField;        Intent audioIntent = new Intent(activity.getApplication(), AudioIntent.class);        audioIntent.putExtra(FORM_UUID, formUuid);        audioIntent.putExtra(KEY_AUDIO_PATH, audioPath);        audioIntent.putExtra(KEY_AUDIO_CAPTION, audioCaption);        audioIntent.putExtra(KEY_SECTION_NAME, sectionName);        // at this point the audio is encrypted so we decrypt it        if (!StringUtils.isEmpty(audioPath))            EnDeCrypt.decrypt(new File(audioPath), "this-is-supposed-to-be-a-secure-key");        activity.startActivityForResult(audioIntent, REQUEST_CODE);    }    public String getAudioPathField() {        return audioPathField;    }    public String getAudioCaptionField() {        return audioCaptionField;    }    public static AudioResult parseActivityResult(int requestCode, int resultCode, Intent intent) {        if (requestCode == REQUEST_CODE) {            if (resultCode == Activity.RESULT_OK) {                String sectionName = intent.getStringExtra(KEY_SECTION_NAME);                String audioUri = intent.getStringExtra(KEY_AUDIO_PATH);                String audioCaption = intent.getStringExtra(KEY_AUDIO_CAPTION);                // now that we have audio we encrypt it and keep the path                if (!StringUtils.isEmpty(audioUri))                    EnDeCrypt.encrypt(new File(audioUri), "this-is-supposed-to-be-a-secure-key");                return new AudioResult(sectionName, audioUri, audioCaption);            }        }        return null;    }}