package hu.sztomek.wheresmybuddy.presentation.common.adapter

import android.graphics.drawable.Drawable

data class ErrorRecyclerRowModel(val customErrorMessage: String? = null, val customDrawable: Drawable? = null): RecyclerViewItem