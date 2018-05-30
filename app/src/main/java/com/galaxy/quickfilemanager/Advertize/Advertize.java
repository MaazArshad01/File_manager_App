package com.galaxy.quickfilemanager.Advertize;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.galaxy.quickfilemanager.InnerAppPurchase.BillingProcessor;
import com.galaxy.quickfilemanager.InnerAppPurchase.TransactionDetails;
import com.galaxy.quickfilemanager.R;

public class Advertize extends AppCompatActivity {

    private BillingProcessor bp;
    public static final String PRODUCT_ID = "android.test.purchased";
    public static final String LICENSE_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqXw7RRx6vMRGBG70nAM02Gv6XuTToHN5629aqIOPuiYMWo+AZKFQjxgxTty1lLMb7DJxDVWftSqUVbSFWVQBMs11SW5X6IxpZvDym0NxJ0f8C6MvsPk/XZv6FCYQ6ahTBWzyiu+kQPAgoYf7z/Hnq2OfvCP9TsiVoGkT3mlRvgSwZYSMWa3ppFW+Ub0Im/K8I9ulCoGk/bevdd5uURP1n7khmakO7mnPtp2oYd1LUPHh+VJcsvMqp0hB+0rY205078r+yVQcQSpYPxRvCHFmCqa9qwUnZW4zVMofhsO7BxwXzW8oVB1V5jhbYR8i8tijjD4HCc9G2s32i5GjaQxs7wIDAQAB"; // PUT YOUR MERCHANT KEY HERE;
    private boolean readyToPurchase = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advertize);

        if (!bp.isIabServiceAvailable(Advertize.this)) {
            // showToast("In-app billing service is unavailable, please upgrade Android Market/Play to version >= 3.9.16");
        }

        bp = new BillingProcessor(Advertize.this, LICENSE_KEY, new BillingProcessor.IBillingHandler() {
            @Override
            public void onProductPurchased(String productId, TransactionDetails details) {
                Log.e("purchse", "onProductPurchased: " + productId);

                if (productId == PRODUCT_ID) {
                    System.out.println("=============>>>Purchase Successfully Complite");
                }
            }

            @Override
            public void onBillingError(int errorCode, Throwable error) {
                Log.e("purchse", "onBillingError: " + Integer.toString(errorCode) + " Error :- " + error.getMessage());
            }

            @Override
            public void onBillingInitialized() {
                Log.e("purchse", "onBillingInitialized");
                readyToPurchase = true;
            }

            @Override
            public void onPurchaseHistoryRestored() {
                Log.e("purchse", "onPurchaseHistoryRestored");
                for (String sku : bp.listOwnedProducts()) {
                    Log.d("purchse", "Owned Managed Product: " + sku);
                    if (sku != null) {
                    }
                }
                for (String sku : bp.listOwnedSubscriptions())
                    Log.d("purchse", "Owned Subscription: " + sku);
            }
        });
    }

    public void onClick(View view) {
        bp.purchase(Advertize.this, PRODUCT_ID);
    }

   /* @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        super.onActivityResult(requestCode, responseCode, intent);


        if (!bp.handleActivityResult(requestCode, responseCode, intent)) {
            super.onActivityResult(requestCode, responseCode, intent);
            if (responseCode == RESULT_OK) {

            }
        }
    }*/
}
