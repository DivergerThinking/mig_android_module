import android.content.Context
import androidx.lifecycle.ViewModel
import com.diverger.mig_android_sdk.R
import com.diverger.mig_android_sdk.data.EventModel
import com.diverger.mig_android_sdk.data.Reservation
import com.diverger.mig_android_sdk.ui.StringResourcesProvider
import com.diverger.mig_android_sdk.ui.competitions.formatDateFromShort
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ReservationDetailViewModel(
    private val stringResourcesProvider: StringResourcesProvider,
    private val reservation: Reservation? = null,
    private val training: EventModel? = null
) : ViewModel() {

    // We expose the flipped state and rotation as before
    private val _isFlipped = MutableStateFlow(false)
    val isFlipped: StateFlow<Boolean> = _isFlipped

    private val _rotationY = MutableStateFlow(0f)
    val rotationY: StateFlow<Float> = _rotationY

    // Flip card toggle
    fun flipCard() {
        _isFlipped.value = !_isFlipped.value
        _rotationY.value = if (_isFlipped.value) 180f else 0f
    }

    fun getReservationLocation(): String {
        reservation?.let {
            return if (it.slot.space.translations.first().description.contains("Virtual", true)) {
                stringResourcesProvider.getString(R.string.esports_virtual_center)
            } else {
                stringResourcesProvider.getString(R.string.esports_madrid_center)
            }
        }
        training?.let {
            return if (it.type == "virtual") {
                stringResourcesProvider.getString(R.string.esports_virtual_center)
            } else {
                stringResourcesProvider.getString(R.string.esports_madrid_center)
            }
        }
        return stringResourcesProvider.getString(R.string.unknown_label_female)
    }

    fun getReservationConsole(): String {
        reservation?.let {
            return when {
                it.slot.space.translations.first().device.contains("PlayStation", true) -> "PlayStation"
                it.slot.space.translations.first().device.contains("Xbox", true) -> "Xbox"
                it.slot.space.translations.first().device.contains("PC", true) -> "PC"
                else -> stringResourcesProvider.getString(R.string.unknown_label_female)
            }
        }
        // No console info for training, or add if applicable
        return stringResourcesProvider.getString(R.string.unknown_label_female)
    }

    fun getFormattedDate(): String {
        reservation?.let { return formatDateFromShort(it.date) }
        training?.let { return formatDateFromShort(it.startDate) }
        return stringResourcesProvider.getString(R.string.unknown_label_female)
    }

    fun getFormattedTimes(): String {
        reservation?.let { return it.times.joinToString(", ") { time -> time.time } }
        training?.let { return it.time }
        return stringResourcesProvider.getString(R.string.unknown_label_female)
    }

    fun getQRValue(): String? {
        reservation?.let { return it.qrValue }
        training?.let { return null }
        return null
    }
}
