package hu.sztomek.wheresmybuddy.presentation.router

import android.content.Intent
import android.support.v4.app.Fragment

interface IRouter {

    fun close()
    fun back()
    fun restart()

    fun toLanding()
    fun toLogin()
    fun onActivityResult(reqCode: Int, result: Int, data: Intent?)
    fun toProfileEdit(userId: String)
    fun toProfile(userId: String)
    fun toMap(locationId: String)

    fun replaceContent(id: Int, fragment: Fragment)
    fun getContent(): Fragment

    fun startBroadcast()
    fun stopBroadcast()

}