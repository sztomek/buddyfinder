package hu.sztomek.wheresmybuddy.domain.model

import hu.sztomek.wheresmybuddy.domain.common.Entity

data class SearchResultModel(val results: List<SearchResultItemModel>) : Entity