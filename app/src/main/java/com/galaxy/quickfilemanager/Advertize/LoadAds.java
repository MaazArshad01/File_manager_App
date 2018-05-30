package com.galaxy.quickfilemanager.Advertize;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.FrameLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.NativeExpressAdView;
import com.galaxy.quickfilemanager.R;

import java.util.Random;

/**
 * Created by Umiya Mataji on 2/17/2017.
 */

public class LoadAds {

    private Context context;

    public LoadAds(Context context) {
        this.context = context;
    }

    public void AdLoard() {
        final InterstitialAd interstitialAds = new InterstitialAd(context);
        interstitialAds.setAdUnitId(context.getResources().getString(R.string.Eye_Protector_intertial));
        interstitialAds.loadAd(new AdRequest.Builder().build());
        interstitialAds.setAdListener(new ToastAdListener(
                context) {
            @Override
            public void onAdLoaded() {
                // TODO Auto-generated method stub
                super.onAdLoaded();
                if (interstitialAds.isLoaded()) {
                    interstitialAds.show();
                }
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // TODO Auto-generated method stub
                super.onAdFailedToLoad(errorCode);
            }
        });
    }

    public void LoadFullScreenAdd() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Random random = new Random();
                int num = random.nextInt(2);
                Log.d("Advert random", "Ads num :- " + num);
                if (num == 1) {
                    AdLoard();
                }
            }
        }, 3000);
    }

    public void LoardNativeAd(FrameLayout frameLayout) {

        try {
            NativeExpressAdView nativeExpressAdView = new NativeExpressAdView(context);
            nativeExpressAdView.setAdUnitId(context.getResources().getString(R.string.Eye_Protector_native));
            nativeExpressAdView.setAdSize(new AdSize(AdSize.FULL_WIDTH, 82));

            frameLayout.addView(nativeExpressAdView);
            nativeExpressAdView.loadAd(new AdRequest.Builder().build());
        } catch (Exception e) {
        }
    }
}
