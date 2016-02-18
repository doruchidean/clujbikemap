package com.doruchidean.clujbikemap;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.SystemClock;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.edmodo.rangebar.RangeBar;

import java.util.ArrayList;

/**
 * Created by Doru on 13/01/16.
 *
 */
public class SettingsDialogs {

    private static SettingsDialogs ourInstance;
    private int overallBikes, overallSpots, overallTotal;
    private String[] pickerDisplayedValues =
            new String[]{"30 min", "45 min", "60 min", "4 hours", "8 hours", "12 hours", "24 hours"};

    private SettingsDialogs(){
    }

    public static SettingsDialogs getInstance() {
        if(ourInstance == null){
            ourInstance = new SettingsDialogs();
        }
        return ourInstance;
    }

    public void showContactDialog(final Context context) {

        View dialogContainer = View.inflate(context, R.layout.dialog_contact, null);

        TextView btnContactDev = (TextView) dialogContainer.findViewById(R.id.btn_contact_developer);
        TextView btnContactCallCenter = (TextView) dialogContainer.findViewById(R.id.btn_contact_call_center);

        btnContactDev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:"));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "ClujBike Map - android support");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "Hello, \n\n");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"doru.chidean@gmai.com"});
                context.startActivity(Intent.createChooser(emailIntent, "Send email..."));
            }
        });
        btnContactCallCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:0371784172"));
                context.startActivity(callIntent);
            }
        });

        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.options_menu_contact))
                .setView(dialogContainer)
                .setCancelable(true)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        dialog = builder.create();
        dialog.show();
    }

    public void showMarginsDialog(final Context context, final Callbacks.SettingsDialogsCallback caller) {

        final PersistenceManager pm = PersistenceManager.getInstance(context);

        final int mColdLimit = pm.getColdLimit();
        final int mHotLimit = pm.getHotLimit();

        View dialogContainer = View.inflate(context, R.layout.dialog_margings, null);

        final TextView tvColdMargin = (TextView) dialogContainer.findViewById(R.id.tv_dialog_margins_cold);
        final TextView tvHotMargin = (TextView) dialogContainer.findViewById(R.id.tv_dialog_margins_hot);

        tvColdMargin.setText(String.format("%s %s", context.getString(R.string.dialog_margins_cold), mColdLimit));
        tvHotMargin.setText(String.format("%s %s", context.getString(R.string.dialog_margins_hot), mHotLimit));

        final byte hypotheticalRange = 30;

        RangeBar rangeBar = (RangeBar) dialogContainer.findViewById(R.id.rangebar);
        rangeBar.setTickCount(hypotheticalRange + 1);
        rangeBar.setThumbIndices(mColdLimit, hypotheticalRange - mHotLimit);
        rangeBar.setConnectingLineColor(Color.GREEN);
        rangeBar.setThumbColorNormal(Color.GREEN);
        rangeBar.setThumbColorPressed(Color.RED);
        rangeBar.setBackgroundResource(R.drawable.background_rangebar);
        rangeBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onIndexChangeListener(RangeBar rangeBar, int i, int i1) {
                pm.setColdLimit(i);
                pm.setHotLimit(hypotheticalRange - i1);

                tvColdMargin.setText(String.format("%s %s", context.getString(R.string.dialog_margins_cold), i));
                tvHotMargin.setText(String.format("%s %s", context.getString(R.string.dialog_margins_hot), hypotheticalRange - i1));
            }
        });

        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.options_menu_hot_cold_margins))
                .setView(dialogContainer)
                .setCancelable(true)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        caller.setUpMap();

                        dialog.dismiss();
                    }
                });
        dialog = builder.create();
        dialog.show();

    }

    public void updateOverallStats(Context context, ArrayList<StationsModel> stations){
        PersistenceManager pm = PersistenceManager.getInstance(context);

        for (StationsModel s : stations) {
            overallBikes += s.ocuppiedSpots;
            overallSpots += s.emptySpots;
            overallTotal += s.maximumNumberOfBikes;
            s.isFavourite = pm.isFavourite(s.stationName);
        }
    }

    public void showOverallStatsDialog(Context context) {
        View dialogContainer = View.inflate(context, R.layout.dialog_overall_stats, null);

        final TextView tvBikes = (TextView) dialogContainer.findViewById(R.id.tv_overall_stats_bikes);
        final TextView tvSpots = (TextView) dialogContainer.findViewById(R.id.tv_overall_stats_empty_spots);
        final TextView tvTotal = (TextView) dialogContainer.findViewById(R.id.tv_overall_stats_total);

        tvBikes.setText(String.format("Total Biciclete: %s", overallBikes));
        tvSpots.setText(String.format("Total Locuri Libere: %s", overallSpots));
        tvTotal.setText(String.format("Total Locuri: %s", overallTotal));

        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.options_menu_overall_stats))
                .setView(dialogContainer)
                .setCancelable(true)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        dialog = builder.create();
        dialog.show();
    }

    public void showTimerLimitDialog(Context context){

        final PersistenceManager persistenceManager = PersistenceManager.getInstance(context);
        final int[] timerLimit = {persistenceManager.getTimerMinutes()};

        View dialogContainer = View.inflate(context, R.layout.dialog_timer_limit, null);

        NumberPicker picker = (NumberPicker) dialogContainer.findViewById(R.id.picker_dialog_timer);
        picker.setMinValue(1);
        picker.setMaxValue(pickerDisplayedValues.length);
        picker.setDisplayedValues(pickerDisplayedValues);
        picker.setValue(persistenceManager.getTimerMinutes());
        picker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                timerLimit[0] = newVal;
            }
        });

        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogContainer)
                .setCancelable(true)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        persistenceManager.setTimerMinutes(timerLimit[0]);

                        dialog.dismiss();
                    }
                });
        dialog = builder.create();
        dialog.show();
    }

    public void showWidgetUpdateTimeDialog(final Context context) {

        final PersistenceManager persistenceManager = PersistenceManager.getInstance(context);
        final int[] updateTime = {persistenceManager.getWidgetUpdateInterval()};

        View dialogContainer = View.inflate(context, R.layout.dialog_widget_update_time, null);
        NumberPicker picker = (NumberPicker) dialogContainer.findViewById(R.id.picker_dialog_widget_time);
        picker.setMinValue(1);
        picker.setMaxValue(pickerDisplayedValues.length);
        picker.setValue(updateTime[0]);
        picker.setDisplayedValues(pickerDisplayedValues);
        picker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                updateTime[0] = newVal;
            }
        });

        new AlertDialog.Builder(context)
                .setView(dialogContainer)
                .setCancelable(true)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //set an alarm to update the widget regularly
                        AlarmManager alarmManager =
                                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                        Intent intent = new Intent(context, ClujBikeMapWidgetProvider.class);
                        PendingIntent pendingIntent= PendingIntent.getBroadcast(
                                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                        alarmManager.setInexactRepeating(
                                AlarmManager.ELAPSED_REALTIME,
                                SystemClock.elapsedRealtime(),
                                Factory.getInstance().getMinutesForDisplayedValue(updateTime[0])*1000,
                                pendingIntent
                        );

                        persistenceManager.setWidgetUpdateInterval(updateTime[0]);
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }
}
