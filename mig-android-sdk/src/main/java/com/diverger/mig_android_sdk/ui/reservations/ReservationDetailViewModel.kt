import androidx.lifecycle.ViewModel
import com.diverger.mig_android_sdk.data.Reservation
import com.diverger.mig_android_sdk.ui.competitions.formatDateFromShort
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ReservationDetailViewModel(reservation: Reservation) : ViewModel() {

    // Estado de la reserva
    private val _reservation = MutableStateFlow(reservation)
    val reservation: StateFlow<Reservation> = _reservation

    // Estado del giro de la tarjeta
    private val _isFlipped = MutableStateFlow(false)
    val isFlipped: StateFlow<Boolean> = _isFlipped

    // Control de la rotación en el eje Y
    private val _rotationY = MutableStateFlow(0f)
    val rotationY: StateFlow<Float> = _rotationY

    // 📌 **Girar la tarjeta**
    fun flipCard() {
        _isFlipped.value = !_isFlipped.value
        _rotationY.value = if (_isFlipped.value) 180f else 0f
    }

    // 📌 **Obtener la ubicación de la reserva**
    fun getReservationLocation(): String {
        return if (_reservation.value.slot.space.translations.first().description.contains("Virtual", true)) {
            "Virtual"
        } else {
            "Centro Esports Madrid"
        }
    }

    // 📌 **Obtener la plataforma de la reserva**
    fun getReservationConsole(): String {
        return when {
            _reservation.value.slot.space.translations.first().device.contains("PlayStation", true) -> "PlayStation"
            _reservation.value.slot.space.translations.first().device.contains("Xbox", true) -> "Xbox"
            _reservation.value.slot.space.translations.first().device.contains("PC", true) -> "PC"
            else -> "Desconocida"
        }
    }

    // 📌 **Formatear la fecha de la reserva**
    fun getFormattedDate(): String {
        return formatDateFromShort(_reservation.value.date)
    }

    // 📌 **Formatear las horas de la reserva**
    fun getFormattedTimes(): String {
        return _reservation.value.times.joinToString(", ") { it.time }
    }
}
