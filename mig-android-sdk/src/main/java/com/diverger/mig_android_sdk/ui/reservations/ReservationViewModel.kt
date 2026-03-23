package com.diverger.mig_android_sdk.ui.reservations

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diverger.mig_android_sdk.R
import com.diverger.mig_android_sdk.data.EventModel
import com.diverger.mig_android_sdk.data.Reservation
import com.diverger.mig_android_sdk.data.ReservationApi
import com.diverger.mig_android_sdk.data.TrainingApi
import com.diverger.mig_android_sdk.data.UserManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReservationViewModel() : ViewModel() {

    // ── Individual reservations ──────────────────────────────────
    private val _reservations = MutableStateFlow<List<Reservation>>(emptyList())
    val reservations: StateFlow<List<Reservation>> = _reservations.asStateFlow()

    // ── Team reservations (trainings) ────────────────────────────
    private val _teamReservations = MutableStateFlow<List<EventModel>>(emptyList())
    val teamReservations: StateFlow<List<EventModel>> = _teamReservations.asStateFlow()

    // ── UI state ─────────────────────────────────────────────────
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage

    fun clearToastMessage() {
        _toastMessage.value = null
    }

    // ── Rol del usuario en el equipo (equivalente a getUserRol en iOS) ──

    fun getUserRole(): String {
        val userId = UserManager.getUser()?.id
        val teamPlayers = UserManager.getSelectedTeam()?.users ?: emptyList()
        for (player in teamPlayers) {
            if (userId == player.userId?.id) {
                return player.role?.name ?: "No Role"
            }
        }
        return ""
    }

    fun isManagerOrTrainer(): Boolean {
        val role = getUserRole().lowercase()
        return role == "manager" || role == "trainer"
    }

    // ── Refresh genérico (decide según modo) ─────────────────────

    fun refreshData(personalReservations: Boolean, userId: String) {
        if (personalReservations) {
            fetchReservations(userId)
        } else {
            fetchTeamReservations()
        }
    }

    // ── Fetch reservas individuales ──────────────────────────────

    fun fetchReservations(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = ReservationApi.getReservations(userId)
            result.onSuccess { reservations ->
                _reservations.value = reservations
                _isLoading.value = false
            }.onFailure {
                _isLoading.value = false
                _reservations.value = emptyList()
            }
        }
    }

    // ── Fetch reservas de equipo (trainings) ─────────────────────

    fun fetchTeamReservations() {
        val team = UserManager.getSelectedTeam() ?: return
        val teamId = team.id

        viewModelScope.launch {
            _isLoading.value = true
            val result = TrainingApi.getTrainings(teamId)
            result.onSuccess { trainings ->
                // Asignar teamName y filtrar solo futuras (red de seguridad)
                val today = Date()
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                val futureTrainings = trainings.filter { training ->
                    try {
                        val trainingDate = sdf.parse(training.startDate)
                        trainingDate != null && !trainingDate.before(sdf.parse(sdf.format(today)))
                    } catch (e: Exception) {
                        false
                    }
                }

                futureTrainings.forEach { it.teamName = team.name }
                _teamReservations.value = futureTrainings
                _isLoading.value = false
            }.onFailure {
                _teamReservations.value = emptyList()
                _isLoading.value = false
            }
        }
    }

    // ── Delete reserva individual ────────────────────────────────

    fun deleteReservation(id: Int, context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = runCatching { ReservationApi.deleteReservation(id) }

            result.onSuccess {
                _isLoading.value = false
                _toastMessage.value =
                    context.getString(R.string.reservation_cancelled_sucessfully)
                UserManager.getUser()?.id.let {
                    fetchReservations(UserManager.getUser()!!.id)
                }
            }.onFailure {
                _isLoading.value = false
                _toastMessage.value = context.getString(R.string.error_cancelling_reservation)
            }
        }
    }

    // ── Delete training (equipo) — replica lógica de iOS ─────────

    fun deleteTraining(training: EventModel, context: Context) {
        val trainingId = training.id ?: return

        viewModelScope.launch {
            _isLoading.value = true

            // Si es virtual no hay reserves físicas que borrar
            val isVirtual = training.type.lowercase() == "virtual"
            val reserveIds = if (isVirtual) {
                emptyList()
            } else {
                training.reserves?.mapNotNull { it.id } ?: emptyList()
            }

            val result = runCatching {
                TrainingApi.deleteTraining(trainingId, reserveIds)
            }

            result.onSuccess {
                _toastMessage.value =
                    context.getString(R.string.reservation_cancelled_sucessfully)
                fetchTeamReservations()
            }.onFailure {
                _toastMessage.value =
                    context.getString(R.string.error_cancelling_reservation)
            }
            _isLoading.value = false
        }
    }

    // ── DNI ──────────────────────────────────────────────────────

    fun updateUserDNI(userId: String, dni: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true

            val result = runCatching {
                UserManager.updateUser(userId, dni = dni)
            }

            result.onSuccess {
                onResult(true)
            }.onFailure {
                onResult(false)
            }

            _isLoading.value = false
        }
    }

    // ══════════════════════════════════════════════════════════════
    // Booking permission checks — Individual
    // ══════════════════════════════════════════════════════════════

    fun userCanBook(): Boolean {
        val reservationCount = reservations.value.count()
        return reservationCount < userReservationLimit()
    }

    fun userHasDNI(): Boolean {
        val user = UserManager.getUser()
        return user?.dni?.isNotBlank() ?: false
    }

//    fun userIsValidated(): Boolean {
//        val user = UserManager.getUser()
//        Log.d("ReservationVM", ">>> status: '${user?.status}', hasDNI: ${userHasDNI()}")
//        val validStatuses = listOf("published", "active")
//
//        return (userHasDNI() && user?.status?.lowercase() == "published")
//    }

    fun userIsValidated(): Boolean {
        val user = UserManager.getUser()
        val validStatuses = listOf("published", "active")
        return userHasDNI() && user?.status?.lowercase() in validStatuses
    }

    private fun userReservationLimit(): Int {
        val user = UserManager.getUser()
        return if (userIsValidated()) {
            (user?.reservesAllowed ?: 1)
        } else {
            1
        }
    }

    // ══════════════════════════════════════════════════════════════
    // Booking permission checks — Team
    // ══════════════════════════════════════════════════════════════

    /**
     * Whether the current team can create a new reservation.
     * Compares active team reservations against the team limit.
     */
    fun teamCanBook(): Boolean {
        val currentCount = teamReservations.value.count()
        return currentCount < teamReservationLimit()
    }

    /**
     * Maximum number of active reservations allowed for the team.
     *
     * Currently hardcoded to [DEFAULT_TEAM_RESERVATION_LIMIT].
     * TODO: Replace with a backend field from Team model when available,
     *       e.g. `UserManager.getSelectedTeam()?.reservesAllowed ?: DEFAULT_TEAM_RESERVATION_LIMIT`
     */
    private fun teamReservationLimit(): Int {
        // Future: read from backend team field
        // val team = UserManager.getSelectedTeam()
        // return team?.reservesAllowed ?: DEFAULT_TEAM_RESERVATION_LIMIT
        return DEFAULT_TEAM_RESERVATION_LIMIT
    }

    companion object {
        /** Default max reservations per team until backend provides the value */
        const val DEFAULT_TEAM_RESERVATION_LIMIT = 3
    }
}