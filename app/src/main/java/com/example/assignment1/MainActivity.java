package com.example.assignment1;

import android.content.res.Configuration;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    // BMI category thresholds (kg/m^2), based on calculator.net/bmi-calculator.html
    private static final double UNDERWEIGHT_MAX = 18.5;
    private static final double HEALTHY_MAX = 25.0;
    private static final double OVERWEIGHT_MAX = 30.0;

    private EditText inputHeight;
    private EditText inputWeight;
    private TextView inputBmi;
    private TextView inputStatus;
    private MaterialButton btnCalculate;
    private MaterialButton btnClear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bindViews();

        btnCalculate.setOnClickListener(v -> calculateBmi());
        btnClear.setOnClickListener(v -> clearFields());
    }

    /**
     * AndroidManifest.xml declares android:configChanges="fontScale" for this
     * activity, so when the user changes the device's font size while the app
     * is running, Android calls this method instead of destroying and
     * recreating the whole Activity. We re-create it ourselves so every
     * TextView/EditText re-inflates with the new font scale applied.
     */
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        recreate();
    }

    private void bindViews() {
        inputHeight = findViewById(R.id.input_height);
        inputWeight = findViewById(R.id.input_weight);
        inputBmi = findViewById(R.id.input_bmi);
        inputStatus = findViewById(R.id.input_status);
        btnCalculate = findViewById(R.id.calculate2);
        btnClear = findViewById(R.id.clear);

        inputHeight.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(8, 2)});
        inputWeight.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(8, 2)});
    }

    /**
     * Reads height (cm) and weight (kg) from the input fields, computes BMI,
     * and shows the result + risk category. Can be called repeatedly.
     */
    private void calculateBmi() {
        String heightText = inputHeight.getText().toString().trim();
        String weightText = inputWeight.getText().toString().trim();

        if (TextUtils.isEmpty(heightText) || TextUtils.isEmpty(weightText)) {
            showError(getString(R.string.error_empty_input));
            return;
        }

        double heightCm;
        double weightKg;
        try {
            heightCm = Double.parseDouble(heightText);
            weightKg = Double.parseDouble(weightText);
        } catch (NumberFormatException e) {
            showError(getString(R.string.error_invalid_number));
            return;
        }

        if (heightCm <= 0 || weightKg <= 0) {
            showError(getString(R.string.error_non_positive));
            return;
        }

        double heightM = heightCm / 100.0;
        double bmi = weightKg / (heightM * heightM);

        DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
        inputBmi.setText(decimalFormat.format(bmi));

        applyCategory(bmi);
    }

    /** Sets the status text + color according to the BMI risk category. */
    private void applyCategory(double bmi) {
        String label;
        int colorRes;

        if (bmi < UNDERWEIGHT_MAX) {
            label = getString(R.string.category_underweight);
            colorRes = R.color.category_underweight;
        } else if (bmi < HEALTHY_MAX) {
            label = getString(R.string.category_healthy);
            colorRes = R.color.category_healthy;
        } else if (bmi < OVERWEIGHT_MAX) {
            label = getString(R.string.category_overweight);
            colorRes = R.color.category_overweight;
        } else {
            label = getString(R.string.category_obesity);
            colorRes = R.color.category_obesity;
        }

        inputStatus.setText(label);
        inputStatus.setTextColor(ContextCompat.getColor(this, colorRes));
    }

    private void showError(String message) {
        inputBmi.setText("");
        inputStatus.setText("");
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /** Resets all fields so the user can start over / recalculate. */
    private void clearFields() {
        inputHeight.setText("");
        inputWeight.setText("");
        inputBmi.setText("");
        inputStatus.setText("");
        inputStatus.setTextColor(ContextCompat.getColor(this, R.color.black));
    }
}

/**
 * Limits an EditText to a maximum number of total digits and digits after
 * the decimal point (e.g. 8 total digits, 2 after the decimal point).
 */
class DecimalDigitsInputFilter implements InputFilter {
    private final Pattern mPattern;

    DecimalDigitsInputFilter(int digits, int digitsAfterZero) {
        mPattern = Pattern.compile("[0-9]{0," + (digits - 1) + "}+((\\.[0-9]{0,"
                + (digitsAfterZero - 1) + "})?)|(\\.)?");
    }

    @Override
    public CharSequence filter(@NonNull CharSequence source, int start, int end,
                               @NonNull Spanned dest, int dstart, int dend) {
        Matcher matcher = mPattern.matcher(dest);
        if (!matcher.matches())
            return "";
        return null;
    }
}