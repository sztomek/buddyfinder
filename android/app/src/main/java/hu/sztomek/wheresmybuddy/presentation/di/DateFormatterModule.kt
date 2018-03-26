package hu.sztomek.wheresmybuddy.presentation.di

import dagger.Module
import dagger.Provides
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Named

@Module
class DateFormatterModule {

    @Provides
    fun provideDateFormat(): DateFormat {
        return SimpleDateFormat("MM. dd. yyyy", Locale.US)
    }

    @Named("LONG")
    @Provides
    fun provideDateFormatLong(): DateFormat {
        return SimpleDateFormat("MM. dd. YYYY. kk:mm", Locale.US)
    }

}