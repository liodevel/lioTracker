package com.liodevel.lioapp_1.Activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.liodevel.lioapp_1.R;
import com.liodevel.lioapp_1.Utils.Utils;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    EditText newPasswordEdit;
    private View mProgressView;
    String newPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        newPasswordEdit = (EditText) findViewById(R.id.change_password_new);
        mProgressView = findViewById(R.id.change_password_progress);

    }

    public void changePassword(View v){
        // comprobar campos
        newPasswordEdit.setError(null);

        // Store values at the time of the login attempt.
        newPassword = newPasswordEdit.getText().toString();

        Utils.logInfo("NEW" + newPassword);

        boolean cancel = false;
        View focusView = null;


        if (!TextUtils.isEmpty(newPassword) && !isPasswordValid(newPassword)) {
            newPasswordEdit.setError(getString(R.string.error_invalid_password));
            focusView = newPasswordEdit;
            cancel = true;
        }


        if (cancel) {
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            Utils.logInfo("ChangePassword");
            ParseUser parseUser = ParseUser.getCurrentUser();
            try {
                //if (oldPassword.equals(parseUser.get("password"))){
                    parseUser.setPassword(newPassword);
                    parseUser.save();
                    Utils.showMessage(getApplicationContext(), getResources().getString(R.string.password_changed));
                    finish();
                //} else {
                  //  Utils.showMessage(getApplicationContext(), getResources().getString(R.string.error_incorrect_password));
                //}
            } catch (Exception e){
                Utils.logInfo(e.toString());
                Utils.showMessage(getApplicationContext(), getResources().getString(R.string.error_incorrect_password));
            }

        }


        // comprobar contraseña parseuser

        // acualizarla
    }


    private boolean isPasswordValid(String password) {
        return password.length() > 5;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }
}
