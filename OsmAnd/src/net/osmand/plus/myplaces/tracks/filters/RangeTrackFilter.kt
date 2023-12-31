package net.osmand.plus.myplaces.tracks.filters

import com.google.gson.annotations.Expose
import net.osmand.plus.OsmandApplication
import net.osmand.plus.R
import kotlin.math.max
import kotlin.math.min

abstract class RangeTrackFilter(
	minValue: Float,
	maxValue: Float,
	val app: OsmandApplication,
	displayNameId: Int,
	filterType: FilterType,
	filterChangedListener: FilterChangedListener)
	: BaseTrackFilter(displayNameId, filterType, filterChangedListener) {

	@Expose
	var minValue: Float

	@Expose
	var maxValue: Float

	@Expose
	var valueFrom: Float

	@Expose
	var valueTo: Float

	init {
		this.minValue = minValue
		this.maxValue = maxValue
		this.valueFrom = minValue
		this.valueTo = maxValue
	}

	open val unitResId = R.string.shared_string_minute_lowercase

	fun setValueFrom(from: Float, updateListeners: Boolean = true) {
		valueFrom = max(minValue, from)
		valueFrom = min(valueFrom, valueTo)
		if (updateListeners) {
			filterChangedListener.onFilterChanged()
		}
	}

	fun setValueTo(to: Float, updateListeners: Boolean = true) {
		valueTo = to
		if (valueTo > maxValue) {
			maxValue = valueTo
		}
		valueTo = max(valueFrom, valueTo)
		if (updateListeners) {
			filterChangedListener.onFilterChanged()
		}
	}

	override fun isEnabled(): Boolean {
		return valueFrom > minValue || valueTo < maxValue
	}
}