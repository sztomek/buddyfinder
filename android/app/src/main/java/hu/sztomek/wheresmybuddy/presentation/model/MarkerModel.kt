package hu.sztomek.wheresmybuddy.presentation.model

import hu.sztomek.wheresmybuddy.presentation.model.persistable.ProfileModel

data class MarkerModel(val location: LocationModel, val profile: ProfileModel?, val info: String?)