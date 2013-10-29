
package com.emediate.android;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.emediate.android.adapter.AdsSimpleListAdapter;
import com.emediate.android.emediatesdktester.R;
import com.emediate.view.EmediateView;

public class AdsSampleActivity extends Activity {

    /** EmediateView */
    private EmediateView mEmediateView;

    /** ListView */
    private ListView mListView;
    private AdsSimpleListAdapter mAdsSimpleListAdapter;

    public static final String ADS_PLACE_TYPE = "ads_place_type", ADS_TYPE = "ads_type", ADS_URL = "ads_url",
	    ADS_POSITION = "ads_position";
    public static final String ADS_PLACE_TYPE_SIMPLE = "ADS_TYPE_SIMPLE",
	    ADS_PLACE_TYPE_LISTVIEW = "ADS_TYPE_LISTVIEW";

    private static final String[] mSampleText = { "sampleText", "sampleText", "sampleText", "sampleText", "sampleText",
	"sampleText", "sampleText", "sampleText", "sampleText", "sampleText" };

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_ads_sample);

	mEmediateView = (EmediateView) findViewById(R.id.emediate_ads_view);
	mListView = (ListView) findViewById(R.id.emediate_listview);
    }

    @Override
    protected void onResume() {
	super.onResume();
	getAdsType(); // To allow ad-views to be restore
    }

    /**
     * Get Ads Type
     */
    private void getAdsType() {
	Bundle extras = getIntent().getExtras();
	String adsPlaceType = extras.getString(ADS_PLACE_TYPE);
	String adsUrl = extras.getString(ADS_URL);
	String adsType = extras.getString(ADS_TYPE);

	Toast.makeText(this, "Ads url:\n" + adsUrl + "\nAds Type:" + adsType, Toast.LENGTH_SHORT).show();

	if (adsPlaceType.equals(ADS_PLACE_TYPE_SIMPLE)) {
	    mEmediateView.setVisibility(View.VISIBLE);
	    mListView.setVisibility(View.GONE);

	    // Fetch Campaign Ads
	    mEmediateView.setAdsRefreshRate(60);
	    mEmediateView.fetchCampaignByFinalUrl(adsUrl);
	}
	else if (adsPlaceType.equals(ADS_PLACE_TYPE_LISTVIEW)) {
	    mEmediateView.setVisibility(View.GONE);
	    mListView.setVisibility(View.VISIBLE);

	    String adsPosition = extras.getString(ADS_POSITION);
	    insertAdsIntoListView(Integer.parseInt(adsPosition), adsType, adsUrl);

	}

    }

    @Override
    protected void onPause() {
	super.onPause();
	if (!isFinishing()) {
	    /*
	     * Pause refreshing of ads, will continue in onResume.
	     */
	    mEmediateView.stopService();
	}
    }

    /**
     * Insert the AdsInto ListView
     * 
     * @param position
     * @param adsType
     * @param adsUrl
     */
    private void insertAdsIntoListView(int position, String adsType, String adsUrl) {
	if (position == -1 || position > mSampleText.length + 1)
	    position = mSampleText.length;

	int itemHeight = (int) this.getResources().getDimension(R.dimen.ads_sample_list_height);
	int adsViewTop = itemHeight * position + mListView.getTop() + mListView.getDividerHeight() * position;
	mAdsSimpleListAdapter = new AdsSimpleListAdapter(this, adsUrl, adsType, mSampleText, position, adsViewTop);
	mListView.setAdapter(mAdsSimpleListAdapter);
    }

}
