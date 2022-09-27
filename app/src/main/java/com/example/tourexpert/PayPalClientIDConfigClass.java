package com.example.tourexpert;

import com.paypal.android.sdk.payments.PayPalConfiguration;

/**
 * paypal configuration class which associates this application
 * to a paypal client which will receive all the transactions
 */
public class PayPalClientIDConfigClass {

    public static final String PAYPAL_CLIENT_ID = "ARqbLX9S8Gea_lF_lreNxxbu7tn2yteg4CZIlJIK5z9KYVGUPUqcdWhpU7wku9jU9CfFuyahJV7AEX_z";

    public static int PAYPAL_REQ_CODE = 12;
    public static final PayPalConfiguration paypalConfig = new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_NO_NETWORK)
            .clientId(PAYPAL_CLIENT_ID);
}
