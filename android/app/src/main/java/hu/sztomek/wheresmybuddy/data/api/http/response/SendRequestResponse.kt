package hu.sztomek.wheresmybuddy.data.api.http.response

import hu.sztomek.wheresmybuddy.data.api.http.model.ConnectionRequestApiModel

data class SendRequestResponse(val result: ConnectionRequestApiModel?, val error: String?)