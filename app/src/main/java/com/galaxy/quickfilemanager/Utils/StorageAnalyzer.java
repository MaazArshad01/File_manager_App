package com.galaxy.quickfilemanager.Utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.galaxy.quickfilemanager.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Umiya Mataji on 2/14/2017.
 */

public class StorageAnalyzer extends Fragment {

    protected String[] Volumes = new String[]{
            "Free", "Images", "Music", "Video", "Docs", "Apps", "Others"
    };
    private Context context;
    private PieChart mInternalChart, mExternalChart;
    private Typeface mTfRegular, mTfLight;
    private StorageDetails storageDetails;

    //private float[] values = {50, 10, 10, 10, 10, 5, 5};
    private float[] values; //= {50, 10, 10, 10, 10, 5, 5};

    private Futils futils;
    private TextView internal_totalspace, internal_freespace;
    private TextView internal_free_space_txt, internal_img_space_txt, internal_audio_space_txt, internal_video_space_txt, internal_docs_space_txt, internal_apps_space_txt, internal_others_space_txt;

    private TextView external_totalspace, external_freespace;
    private TextView external_free_space_txt, external_img_space_txt, external_audio_space_txt, external_video_space_txt, external_docs_space_txt, external_apps_space_txt, external_others_space_txt;
    private CardView external_storage_main;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_storage_analyzer_layout, container, false);
        context = getActivity();
        setHasOptionsMenu(true);
        init(view);

        storageDetails.getStorageSpaceList("InternalStorage", new StorageDetails.LoadCompletedListener() {
            @Override
            public void onLoadstart() {

            }

            @Override
            public void onLoadCompleted(HashMap<String, Long> storageSpaces) {

                long totalSpace = storageSpaces.get("totalSpace");
                long freeSpace = storageSpaces.get("freeSpace");

                long imageSpace = storageSpaces.get("imageSpace");
                long audioSpace = storageSpaces.get("audioSpace");
                long videoSpace = storageSpaces.get("videoSpace");
                long documentSpace = storageSpaces.get("documentSpace");
                long applicationSpace = storageSpaces.get("applicationSpace");
                long otherSpace = storageSpaces.get("otherSpace");


                Log.d("Internal Spaces", "Image :- " + imageSpace +
                        "\n audio :- " + audioSpace +
                        "\n video :- " + videoSpace +
                        "\n document :- " + documentSpace +
                        "\n application :- " + applicationSpace +
                        "\n other :- " + otherSpace +
                        "\n\n Total :- " + totalSpace +
                        "\n\n free :- " + freeSpace
                );


                internal_totalspace.setText("Total Space :- " + futils.readableFileSize(totalSpace));
                internal_freespace.setText("Free Space :- " + futils.readableFileSize(freeSpace));

                internal_free_space_txt.setText(futils.readableFileSize(freeSpace));

                internal_img_space_txt.setText(futils.readableFileSize(imageSpace));
                internal_audio_space_txt.setText(futils.readableFileSize(audioSpace));
                internal_video_space_txt.setText(futils.readableFileSize(videoSpace));
                internal_docs_space_txt.setText(futils.readableFileSize(documentSpace));
                internal_apps_space_txt.setText(futils.readableFileSize(applicationSpace));
                internal_others_space_txt.setText(futils.readableFileSize(otherSpace));


                //Setting PieChart Values

                double v = ((double) freeSpace / totalSpace) * 100;
                String usedPertange = String.valueOf(Math.round(100 - v));
                mInternalChart.setCenterText(usedPertange + "%");
                mInternalChart.invalidate();

                //getSpacepercentage(totalSpace, imageSpace);
                values = new float[]{getSpacepercentage(totalSpace, freeSpace), getSpacepercentage(totalSpace, imageSpace)
                        , getSpacepercentage(totalSpace, audioSpace), getSpacepercentage(totalSpace, videoSpace)
                        , getSpacepercentage(totalSpace, documentSpace), getSpacepercentage(totalSpace, applicationSpace)
                        , getSpacepercentage(totalSpace, otherSpace)};

                setChartData(mInternalChart, values);
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (storageDetails.isSDCardMounted()) {
                    external_storage_main.setVisibility(View.VISIBLE);
                    storageDetails.getStorageSpaceList("ExternalStorage", new StorageDetails.LoadCompletedListener() {
                        @Override
                        public void onLoadstart() {

                        }

                        @Override
                        public void onLoadCompleted(HashMap<String, Long> storageSpaces) {

                            long totalSpace = storageSpaces.get("totalSpace");
                            long freeSpace = storageSpaces.get("freeSpace");

                            long imageSpace = storageSpaces.get("imageSpace");
                            long audioSpace = storageSpaces.get("audioSpace");
                            long videoSpace = storageSpaces.get("videoSpace");
                            long documentSpace = storageSpaces.get("documentSpace");
                            long applicationSpace = storageSpaces.get("applicationSpace");
                            long otherSpace = storageSpaces.get("otherSpace");


                            Log.d("External Spaces", "Image :- " + imageSpace +
                                    "\n audio :- " + audioSpace +
                                    "\n video :- " + videoSpace +
                                    "\n document :- " + documentSpace +
                                    "\n application :- " + applicationSpace +
                                    "\n other :- " + otherSpace +
                                    "\n\n Total :- " + totalSpace +
                                    "\n\n free :- " + freeSpace
                            );

                            external_totalspace.setText("Total Space :- " + futils.readableFileSize(totalSpace));
                            external_freespace.setText("Free Space :- " + futils.readableFileSize(freeSpace));

                            external_free_space_txt.setText(futils.readableFileSize(freeSpace));

                            external_img_space_txt.setText(futils.readableFileSize(imageSpace));
                            external_audio_space_txt.setText(futils.readableFileSize(audioSpace));
                            external_video_space_txt.setText(futils.readableFileSize(videoSpace));
                            external_docs_space_txt.setText(futils.readableFileSize(documentSpace));
                            external_apps_space_txt.setText(futils.readableFileSize(applicationSpace));
                            external_others_space_txt.setText(futils.readableFileSize(otherSpace));


                            //Setting PieChart Values

                            double v = ((double) freeSpace / totalSpace) * 100;
                            String usedPertange = String.valueOf(Math.round(100 - v));
                            mExternalChart.setCenterText(usedPertange + "%");
                            mExternalChart.invalidate();

                            //getSpacepercentage(totalSpace, imageSpace);
                            values = new float[]{getSpacepercentage(totalSpace, freeSpace), getSpacepercentage(totalSpace, imageSpace)
                                    , getSpacepercentage(totalSpace, audioSpace), getSpacepercentage(totalSpace, videoSpace)
                                    , getSpacepercentage(totalSpace, documentSpace), getSpacepercentage(totalSpace, applicationSpace)
                                    , getSpacepercentage(totalSpace, otherSpace)};

                            setChartData(mExternalChart, values);
                        }
                    });
                } else
                    external_storage_main.setVisibility(View.GONE);
            }
        }, 1000);


        return view;
    }


    public void setChartData(PieChart mChart, float[] values) {
        setData(mChart, values);

        mChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);

        Legend l = mChart.getLegend();
        l.setEnabled(false);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);

        // entry label styling
        mChart.setEntryLabelColor(Color.WHITE);
        mChart.setEntryLabelTypeface(mTfRegular);
        mChart.setEntryLabelTextSize(12f);

        // Hide lable Value on chart
        mChart.setDrawEntryLabels(!mChart.isDrawEntryLabelsEnabled());

        // for change the percentage value randomaly
        mChart.setUsePercentValues(false);

        // Hide Percentage Value on chart
        for (IDataSet<?> set : mChart.getData().getDataSets())
            set.setDrawValues(!set.isDrawValuesEnabled());

        mChart.invalidate();
    }

    public int getSpacepercentage(long totalSpace, long usedSpace) {
        float v = ((float) usedSpace / totalSpace) * 100;
        // Log.d("Used", "Per :- " + v);

        return (int) v < 1 ? 3 : (int) v;
    }

    private void init(View view) {

        mTfRegular = Typeface.createFromAsset(context.getAssets(), "fonts/OpenSans-Regular.ttf");
        mTfLight = Typeface.createFromAsset(context.getAssets(), "fonts/OpenSans-Light.ttf");

        storageDetails = new StorageDetails(this.getActivity());
        futils = new Futils();

        internal_totalspace = (TextView) view.findViewById(R.id.internal_totalspace);
        internal_freespace = (TextView) view.findViewById(R.id.internal_freespace);
        internal_free_space_txt = (TextView) view.findViewById(R.id.internal_free_space_txt);
        internal_img_space_txt = (TextView) view.findViewById(R.id.internal_img_space_txt);
        internal_audio_space_txt = (TextView) view.findViewById(R.id.internal_audio_space_txt);
        internal_video_space_txt = (TextView) view.findViewById(R.id.internal_video_space_txt);
        internal_docs_space_txt = (TextView) view.findViewById(R.id.internal_docs_space_txt);
        internal_apps_space_txt = (TextView) view.findViewById(R.id.internal_apps_space_txt);
        internal_others_space_txt = (TextView) view.findViewById(R.id.internal_others_space_txt);
        mInternalChart = (PieChart) view.findViewById(R.id.chart1);

        initInternalChart(mInternalChart);

        external_storage_main = (CardView) view.findViewById(R.id.external_storage_main);
        external_totalspace = (TextView) view.findViewById(R.id.external_totalspace);
        external_freespace = (TextView) view.findViewById(R.id.external_freespace);
        external_free_space_txt = (TextView) view.findViewById(R.id.external_free_space_txt);
        external_img_space_txt = (TextView) view.findViewById(R.id.external_img_space_txt);
        external_audio_space_txt = (TextView) view.findViewById(R.id.external_audio_space_txt);
        external_video_space_txt = (TextView) view.findViewById(R.id.external_video_space_txt);
        external_docs_space_txt = (TextView) view.findViewById(R.id.external_docs_space_txt);
        external_apps_space_txt = (TextView) view.findViewById(R.id.external_apps_space_txt);
        external_others_space_txt = (TextView) view.findViewById(R.id.external_others_space_txt);
        mExternalChart = (PieChart) view.findViewById(R.id.chart2);

        initInternalChart(mExternalChart);

    }

    public void initInternalChart(PieChart mChart) {

        // mChart.setUsePercentValues(true);
        mChart.getDescription().setEnabled(false);
        mChart.setExtraOffsets(5, 10, 5, 5);

        mChart.setDragDecelerationFrictionCoef(0.95f);

        mChart.setCenterTextTypeface(mTfLight);
        mChart.setCenterTextSize(16);
        // mChart.setCenterText("Storage");

        mChart.setDrawHoleEnabled(true);
        mChart.setHoleColor(Color.WHITE);

        mChart.setHoleRadius(58f);
        mChart.setTransparentCircleRadius(61f);

        mChart.setDrawCenterText(true);

        mChart.setRotationAngle(0);
        // enable rotation of the chart by touch
        mChart.setRotationEnabled(true);
        mChart.setHighlightPerTapEnabled(true);

        // mChart.setUnit(" €");
        // mChart.setDrawUnitsInChart(true);

        // add a selection listener
        mChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {

            }

            @Override
            public void onNothingSelected() {

            }
        });

        //  mChart.invalidate();
        // setData(7);

       /* mChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);

        Legend l = mChart.getLegend();
        l.setEnabled(false);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);

        // entry label styling
        mChart.setEntryLabelColor(Color.WHITE);
        mChart.setEntryLabelTypeface(mTfRegular);
        mChart.setEntryLabelTextSize(12f);

        // Hide lable Value on chart
        mChart.setDrawEntryLabels(!mChart.isDrawEntryLabelsEnabled());

        // for change the percentage value randomaly
        mChart.setUsePercentValues(false);

        // Hide Percentage Value on chart
        for (IDataSet<?> set : mChart.getData().getDataSets())
            set.setDrawValues(!set.isDrawValuesEnabled());

        mChart.invalidate();*/
    }

    private void setData(PieChart mChart, float[] values) {


        ArrayList<PieEntry> entries = new ArrayList<PieEntry>();

        // NOTE: The order of the entries when being added to the entries array determines their position around the center of
        // the chart.
        for (int i = 0; i < values.length; i++) {
            entries.add(new PieEntry(values[i], Volumes[i]));
            //entries.add(new PieEntry((float) ((Math.random() * mult) + values[i] / values.length), Volumes[i % Volumes.length]));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Election Results");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        // add a lot of colors

        ArrayList<Integer> colors = new ArrayList<Integer>();

        try {
            colors.add(ColorTemplate.rgb(getString(R.color.free_space_color)));
            colors.add(ColorTemplate.rgb(getString(R.color.image_space_color)));
            colors.add(ColorTemplate.rgb(getString(R.color.music_space_color)));
            colors.add(ColorTemplate.rgb(getString(R.color.video_space_color)));
            colors.add(ColorTemplate.rgb(getString(R.color.documents_space_color)));
            colors.add(ColorTemplate.rgb(getString(R.color.apps_space_color)));
            colors.add(ColorTemplate.rgb(getString(R.color.others_space_color)));
        } catch (Exception e) {
        }
        dataSet.setColors(colors);
        //dataSet.setSelectionShift(0f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);
        data.setValueTypeface(mTfLight);
        mChart.setData(data);

        // undo all highlights
        mChart.highlightValues(null);

        mChart.invalidate();
    }
}
