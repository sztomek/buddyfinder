package hu.sztomek.wheresmybuddy.presentation.di

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.view.View
import dagger.android.AndroidInjection
import dagger.android.support.AndroidSupportInjection
import hu.sztomek.wheresmybuddy.presentation.app.BuddyApp
import timber.log.Timber

fun initialize(app: BuddyApp) {
        DaggerAppComponent
                .builder()
                .application(app)
                .build()
                .inject(app)

        app.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityPaused(p0: Activity?) {
                Timber.d("${p0!!::class.java.simpleName}: onActivityPaused")
            }

            override fun onActivityResumed(p0: Activity?) {
                Timber.d("${p0!!::class.java.simpleName}: onActivityResumed")
            }

            override fun onActivityStarted(p0: Activity?) {
                Timber.d("${p0!!::class.java.simpleName}: onActivityStarted")
            }

            override fun onActivityDestroyed(p0: Activity?) {
                Timber.d("${p0!!::class.java.simpleName}: onActivityDestroyed")
            }

            override fun onActivitySaveInstanceState(p0: Activity?, p1: Bundle?) {
                Timber.d("${p0!!::class.java.simpleName}: onActivitySaveInstanceState")
            }

            override fun onActivityStopped(p0: Activity?) {
                Timber.d("${p0!!::class.java.simpleName}: onActivityStopped")
            }

            override fun onActivityCreated(p0: Activity?, p1: Bundle?) {
                Timber.d("${p0!!::class.java.simpleName}: onActivityCreated")
                if (p0 is Injectable) {
                    AndroidInjection.inject(p0)
                }

                if (p0 is FragmentActivity) {
                    p0.supportFragmentManager.registerFragmentLifecycleCallbacks(
                            object : FragmentManager.FragmentLifecycleCallbacks() {
                                override fun onFragmentAttached(fm: FragmentManager?, f: Fragment?, context: Context?) {
                                    Timber.d("${f!!::class.java.simpleName}: onAttached")
                                    if (f is Injectable) {
                                        AndroidSupportInjection.inject(f)
                                    }
                                }

                                override fun onFragmentViewCreated(fm: FragmentManager?, f: Fragment?, v: View?, savedInstanceState: Bundle?) {
                                    super.onFragmentViewCreated(fm, f, v, savedInstanceState)
                                    Timber.d("${f!!::class.java.simpleName}: onViewCreated")
                                }

                                override fun onFragmentStopped(fm: FragmentManager?, f: Fragment?) {
                                    super.onFragmentStopped(fm, f)
                                    Timber.d("${f!!::class.java.simpleName}: onStopped")
                                }

                                override fun onFragmentCreated(fm: FragmentManager?, f: Fragment?, savedInstanceState: Bundle?) {
                                    super.onFragmentCreated(fm, f, savedInstanceState)
                                    Timber.d("${f!!::class.java.simpleName}: onCreated")
                                }

                                override fun onFragmentResumed(fm: FragmentManager?, f: Fragment?) {
                                    super.onFragmentResumed(fm, f)
                                    Timber.d("${f!!::class.java.simpleName}: onResumed")
                                }

                                override fun onFragmentPreAttached(fm: FragmentManager?, f: Fragment?, context: Context?) {
                                    super.onFragmentPreAttached(fm, f, context)
                                    Timber.d("${f!!::class.java.simpleName}: onPreAttached")
                                }

                                override fun onFragmentDestroyed(fm: FragmentManager?, f: Fragment?) {
                                    super.onFragmentDestroyed(fm, f)
                                    Timber.d("${f!!::class.java.simpleName}: onDestroyed")
                                }

                                override fun onFragmentSaveInstanceState(fm: FragmentManager?, f: Fragment?, outState: Bundle?) {
                                    super.onFragmentSaveInstanceState(fm, f, outState)
                                    Timber.d("${f!!::class.java.simpleName}: onSaveInstanceState")
                                }

                                override fun onFragmentStarted(fm: FragmentManager?, f: Fragment?) {
                                    super.onFragmentStarted(fm, f)
                                    Timber.d("${f!!::class.java.simpleName}: onStarted")
                                }

                                override fun onFragmentViewDestroyed(fm: FragmentManager?, f: Fragment?) {
                                    super.onFragmentViewDestroyed(fm, f)
                                    Timber.d("${f!!::class.java.simpleName}: onViewDestroyed")
                                }

                                override fun onFragmentPreCreated(fm: FragmentManager?, f: Fragment?, savedInstanceState: Bundle?) {
                                    super.onFragmentPreCreated(fm, f, savedInstanceState)
                                    Timber.d("${f!!::class.java.simpleName}: onPreCreated")
                                }

                                override fun onFragmentActivityCreated(fm: FragmentManager?, f: Fragment?, savedInstanceState: Bundle?) {
                                    super.onFragmentActivityCreated(fm, f, savedInstanceState)
                                    Timber.d("${f!!::class.java.simpleName}: onActivityCreated")
                                }

                                override fun onFragmentPaused(fm: FragmentManager?, f: Fragment?) {
                                    super.onFragmentPaused(fm, f)
                                    Timber.d("${f!!::class.java.simpleName}: onPaused")
                                }

                                override fun onFragmentDetached(fm: FragmentManager?, f: Fragment?) {
                                    super.onFragmentDetached(fm, f)
                                    Timber.d("${f!!::class.java.simpleName}: onDetached")
                                }
                            },
                            true
                    )
                }
            }
        })
}