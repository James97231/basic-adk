package com.beautifulapp.basis_adk

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.*
import com.google.android.gms.ads.reward.RewardItem
import com.google.android.gms.ads.reward.RewardedVideoAd
import com.google.android.gms.ads.reward.RewardedVideoAdListener
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

abstract class Pub : AppCompatActivity() {
    lateinit var mInterstitialAd: InterstitialAd
    lateinit var mInterstitialAdId: String
    lateinit var mRewardedVideoAd: RewardedVideoAd
    lateinit var mRewardedVideoAdId: String
    lateinit var mAdRequest: AdRequest
    var mode: Mode = Mode.MODE_INTERSTITIEL
    var action: (() -> Unit)? = null

    var frequency: Double = 0.5

    private lateinit var mRewardedAd: RewardedAd//new
    val adLoadCallback = object : RewardedAdLoadCallback() {
        override fun onRewardedAdFailedToLoad(p0: Int) {
            Toast.makeText(this@Pub, "onRewardedAdFailedToLoad", Toast.LENGTH_SHORT).show()
            Log.e("Pub", "onRewardedAdFailedToLoad : $p0")
        }

        override fun onRewardedAdLoaded() {
            Toast.makeText(this@Pub, "onRewardedAdLoaded", Toast.LENGTH_SHORT).show()
            Log.e("Pub", "onRewardedAdLoaded ")
        }
    }
    val adCallback = object : RewardedAdCallback() {
        override fun onUserEarnedReward(p0: com.google.android.gms.ads.rewarded.RewardItem) {
            Toast.makeText(this@Pub, "onUserEarnedReward! currency: ${p0.type} amount: ${p0.amount}", Toast.LENGTH_SHORT).show()
            Log.e("Pub", "onUserEarnedReward! currency: ${p0.type} amount: ${p0.amount}")
            //loadAd()
        }

        override fun onRewardedAdFailedToShow(p0: Int) {
            Log.e("Pub", "onRewardedAdFailedToShow: ${p0}")
            super.onRewardedAdFailedToShow(p0)
        }

        override fun onRewardedAdClosed() {
            Toast.makeText(this@Pub, "onRewardedAdClosed", Toast.LENGTH_SHORT).show()
            Log.e("Pub", "onRewardedAdClosed: ")
            mRewardedAd = RewardedAd(this@Pub, mRewardedVideoAdId).apply {
                setOnPaidEventListener {
                    Log.e("Pub", "setOnPaidEventListener: ${it.valueMicros}")
                }
            }//new
            loadAd()
        }

        override fun onRewardedAdOpened() {
            action?.invoke()
        }
    }


    var adIsLoaded = false
        get() {
            return if (mode == Mode.MODE_INTERSTITIEL) mInterstitialAd.isLoaded else mRewardedAd.isLoaded//mRewardedVideoAd.isLoaded
        }

    fun initialize(
        adView: AdView,
        adRequest: AdRequest,
        appAdId: String,
        interstitialAdId: String,
        rewardedVideoAdId: String
    ) {
        //this.activity = activity
        mAdRequest = adRequest

        //MobileAds.initialize(activity, appAdId)//modifié le 18/04/2020
        MobileAds.initialize(this) {
            it.adapterStatusMap
        }
        adView.loadAd(mAdRequest)


        mode = Mode.MODE_INTERSTITIEL
        mInterstitialAdId = interstitialAdId
        mRewardedVideoAdId = rewardedVideoAdId
        mInterstitialAd = InterstitialAd(this).apply { adUnitId = interstitialAdId }
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this).apply { userId = (Math.random() * 20000).toString(); }


        mRewardedAd = RewardedAd(this, rewardedVideoAdId)//new

        configureAd()
        loadAd()

    }

    @SuppressLint("MissingPermission")
    fun initialize2(
        adView: AdView,
        adRequest: AdRequest,
        interstitialAdId: String,
        rewardedVideoAdId: String
    ) {
        mAdRequest = adRequest

        MobileAds.initialize(this) { it.adapterStatusMap }
        adView.loadAd(mAdRequest)

        mode = Mode.MODE_INTERSTITIEL
        mInterstitialAdId = interstitialAdId
        mRewardedVideoAdId = rewardedVideoAdId
        mInterstitialAd = InterstitialAd(this).apply {
            adUnitId = interstitialAdId
            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    Toast.makeText(this@Pub, "onAdLoaded", Toast.LENGTH_SHORT).show()
                }

                override fun onAdFailedToLoad(errorCode: Int) {
                    Toast.makeText(this@Pub, "onAdFailedToLoad : $errorCode", Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onAdOpened() {
                    action?.invoke()
                    //Toast.makeText(this@MainActivity, "onAdOpened", Toast.LENGTH_SHORT).show()
                }

                override fun onAdClicked() {
                    //Toast.makeText(this@MainActivity, "onAdClicked", Toast.LENGTH_SHORT).show()
                    // Code to be executed when the user clicks on an ad.
                }

                override fun onAdLeftApplication() {
                    loadAd()
                    //Toast.makeText(this@MainActivity, "onAdLeftApplication", Toast.LENGTH_SHORT).show()
                }

                override fun onAdClosed() {
                    loadAd()
                    //Toast.makeText(this@MainActivity, "onAdClosed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        mRewardedAd = RewardedAd(this, mRewardedVideoAdId).apply {
            setOnPaidEventListener {
                Log.e("Pub", "setOnPaidEventListener: ${it.valueMicros}")
            }
        }//new

        //configureAd()
        loadAd()

    }


    fun changeMode() {
        mode = if (mode == Mode.MODE_INTERSTITIEL) Mode.MODE_REWARD else Mode.MODE_INTERSTITIEL
        //configureAd()
        loadAd()
    }

    fun showAd(action: (() -> Unit)? = null) {
        this.action = action
        when (mode) {
            Mode.MODE_INTERSTITIEL -> {
                if (mInterstitialAd.isLoaded) {
                    mInterstitialAd.show()
                }
            }
            else -> {
                /*if (mRewardedVideoAd.isLoaded) {
                    mRewardedVideoAd.show()
                }*/

                if (mRewardedAd.isLoaded) {
                    mRewardedAd.show(this, adCallback)
                }


            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun loadAd() {
        when (mode) {
            Mode.MODE_INTERSTITIEL -> {
                if (!mInterstitialAd.isLoaded) {
                    mInterstitialAd.loadAd(mAdRequest)
                }
            }
            else -> {
                /*if (!mRewardedVideoAd.isLoaded) {
                    mRewardedVideoAd.loadAd(mRewardedVideoAdId, mAdRequest)
                }*/

                if (!mRewardedAd.isLoaded) {
                    Log.e("Pub", "mRewardedAd.loadAd")
                    mRewardedAd.loadAd(mAdRequest, adLoadCallback)
                }


            }
        }
    }

    private fun configureAd() {

        when (mode) {
            Mode.MODE_INTERSTITIEL -> {
                mInterstitialAd.adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        //Toast.makeText(this@MainActivity, "onAdLoaded", Toast.LENGTH_SHORT).show()
                    }

                    override fun onAdFailedToLoad(errorCode: Int) {
                        Toast.makeText(this@Pub, "onAdFailedToLoad: $errorCode", Toast.LENGTH_SHORT)
                            .show()
                    }

                    override fun onAdOpened() {
                        action?.invoke()
                        //Toast.makeText(this@MainActivity, "onAdOpened", Toast.LENGTH_SHORT).show()
                    }

                    override fun onAdClicked() {
                        //Toast.makeText(this@MainActivity, "onAdClicked", Toast.LENGTH_SHORT).show()
                        // Code to be executed when the user clicks on an ad.
                    }

                    override fun onAdLeftApplication() {
                        loadAd()
                        //Toast.makeText(this@MainActivity, "onAdLeftApplication", Toast.LENGTH_SHORT).show()
                    }

                    override fun onAdClosed() {
                        loadAd()
                        //Toast.makeText(this@MainActivity, "onAdClosed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            else -> {
                mRewardedVideoAd.rewardedVideoAdListener = object : RewardedVideoAdListener {
                    override fun onRewardedVideoAdLeftApplication() {
                        //Toast.makeText(this@MainActivity, "onRewardedVideoAdLeftApplication", Toast.LENGTH_SHORT).show()
                    }

                    override fun onRewardedVideoAdClosed() {
                        loadAd()
                        //Toast.makeText(this@MainActivity, "onRewardedVideoAdClosed", Toast.LENGTH_SHORT).show()
                    }

                    override fun onRewardedVideoAdFailedToLoad(errorCode: Int) {
                        //Toast.makeText(this@MainActivity, "onRewardedVideoAdFailedToLoad", Toast.LENGTH_SHORT).show()
                    }

                    override fun onRewardedVideoAdLoaded() {
                        //Toast.makeText(this@MainActivity, "onRewardedVideoAdLoaded: ${mRewardedVideoAd.isLoaded}", Toast.LENGTH_SHORT).show()
                    }

                    override fun onRewardedVideoAdOpened() {
                        action?.invoke()
                        //Toast.makeText(this@MainActivity, "onRewardedVideoAdOpened", Toast.LENGTH_SHORT).show()
                    }

                    override fun onRewarded(reward: RewardItem) {
                        //mRewardedVideoAd.destroy(this)
                        Toast.makeText(this@Pub, "onRewarded! currency: ${reward.type} amount: ${reward.amount}", Toast.LENGTH_SHORT).show()
                        //addCoins(reward.amount)
                    }

                    override fun onRewardedVideoStarted() {
                        //Toast.makeText(this@MainActivity, "onRewardedVideoStarted", Toast.LENGTH_SHORT).show()
                    }

                    override fun onRewardedVideoCompleted() {
                        mRewardedVideoAd.destroy(this@Pub)
                        //mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this@MainActivity).apply { userId = (Math.random() * 20000).toString(); }
                        configureAd()
                        loadAd()
                        Toast.makeText(this@Pub, "onRewardedVideoCompleted", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    enum class Mode {
        MODE_INTERSTITIEL,
        MODE_REWARD
    }
}