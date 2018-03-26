package hu.sztomek.wheresmybuddy.domain.model

import hu.sztomek.wheresmybuddy.domain.common.Entity

data class IncomingRequestsListModel(val results: List<PendingConnectionRequestItemModel>): Entity