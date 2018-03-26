package hu.sztomek.wheresmybuddy.presentation.screens.landing.discover

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import hu.sztomek.wheresmybuddy.R
import hu.sztomek.wheresmybuddy.presentation.common.Helpers
import hu.sztomek.wheresmybuddy.presentation.model.persistable.ProfileModel

class InfoWindowAdapter(private val context: Context) : GoogleMap.InfoWindowAdapter {

    var imageCache: MarkerImageCache? = null

    override fun getInfoContents(p0: Marker?): View? {
        val viewHolder: LocationViewHolder = when {
            p0?.tag is ProfileModel -> FriendsLocationViewHolder(context, imageCache)
            else -> SelfLocationViewHolder(context)
        }
        viewHolder.bind(p0)

        return viewHolder.view
    }

    override fun getInfoWindow(p0: Marker?): View? {
        return null
    }

    interface LocationViewHolder {
        val view: View
        fun bind(marker: Marker?)
    }

    class SelfLocationViewHolder(context: Context) : LocationViewHolder {

        @BindView(R.id.ivProfile)
        lateinit var ivProfile: ImageView
        @BindView(R.id.tvTitle)
        lateinit var tvTitle: TextView
        @BindView(R.id.tvSnippet)
        lateinit var tvSnippet: TextView

        override val view = LayoutInflater.from(context).inflate(R.layout.layout_info_window, null)

        init {
            ButterKnife.bind(this, view)
        }

        override fun bind(marker: Marker?) {
            ivProfile.visibility = View.GONE
            tvTitle.text = marker?.title
            tvSnippet.text = marker?.snippet
        }
    }

    class FriendsLocationViewHolder(context: Context, private val imageCache: MarkerImageCache?) : LocationViewHolder {

        @BindView(R.id.ivProfile)
        lateinit var ivProfile: ImageView
        @BindView(R.id.tvTitle)
        lateinit var tvTitle: TextView
        @BindView(R.id.tvSnippet)
        lateinit var tvSnippet: TextView

        override val view = LayoutInflater.from(context).inflate(R.layout.layout_info_window, null)

        init {
            ButterKnife.bind(this, view)
        }

        override fun bind(marker: Marker?) {
            marker?.let {
                val url = Helpers.safeCastTo<ProfileModel>(marker.tag)?.profilePicture
                url?.let {
                    imageCache?.let {
                        ivProfile.setImageBitmap(imageCache.getBitmap(url))
                    }
                }
                tvTitle.text = marker.title
                tvSnippet.text = marker.snippet
            }
        }

    }

}