package net.osmand.plus.myplaces.tracks.filters

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.osmand.plus.OsmandApplication
import net.osmand.plus.settings.backend.preferences.CommonPreference
import net.osmand.util.Algorithms

class TrackFiltersSettingsCollection(val app: OsmandApplication) : FilterChangedListener {
	private val preference: CommonPreference<String>
	private val gson: Gson
	private val currentFilters = ArrayList<BaseTrackFilter>()
	private val savedFilters: MutableMap<String, BaseTrackFilter> = HashMap()
	private var listeners = ArrayList<FiltersSettingsListener>()

	override fun onFilterChanged() {
		for (listener in listeners) {
			listener.onFilterChanged()
		}
	}

	interface FiltersSettingsListener {
		fun onFilterChanged()
	}

	init {
		gson = GsonBuilder()
			.excludeFieldsWithoutExposeAnnotation()
			.registerTypeAdapter(BaseTrackFilter::class.java, TrackFilterDeserializer(this))
			.create()
		preference = app.settings.registerStringPreference(TRACK_FILTERS_SETTINGS_PREF_ID, "")
			.makeProfile()
			.cache()
		readSettings()
		resetCurrentFilters()
	}

	fun addListener(listener: FiltersSettingsListener) {
		if (!listeners.contains(listener)) {
			val newListeners = ArrayList(listeners)
			newListeners.add(listener)
			listeners = newListeners
		}
	}

	fun removeListener(listener: FiltersSettingsListener) {
		if (listeners.contains(listener)) {
			val newListeners = ArrayList(listeners)
			newListeners.remove(listener)
			listeners = newListeners
		}
	}

	fun getCurrentFilters(): List<BaseTrackFilter> {
		return currentFilters
	}

	private fun createFilter(filterType: FilterType): BaseTrackFilter {
		return when (filterType) {
			FilterType.NAME -> TrackNameFilter(this)
			FilterType.DURATION -> DurationTrackFilter(
				0f,
				TrackFiltersConstants.DEFAULT_MAX_VALUE,
				app, this)

			FilterType.TIME_IN_MOTION -> TimeInMotionTrackFilter(
				0f,
				TrackFiltersConstants.DEFAULT_MAX_VALUE,
				app, this)

			FilterType.LENGTH -> LengthTrackFilter(
				0f,
				TrackFiltersConstants.LENGTH_MAX_VALUE,
				app, this)

			FilterType.AVERAGE_SPEED -> AverageSpeedTrackFilter(
				0f,
				TrackFiltersConstants.DEFAULT_MAX_VALUE,
				app, this)

			FilterType.MAX_SPEED -> MaxSpeedTrackFilter(
				0f,
				TrackFiltersConstants.DEFAULT_MAX_VALUE,
				app, this)

			FilterType.AVERAGE_ALTITUDE -> AverageAltitudeTrackFilter(
				0f,
				TrackFiltersConstants.DEFAULT_MAX_VALUE,
				app, this)

			FilterType.MAX_ALTITUDE -> MaxAltitudeTrackFilter(
				0f,
				TrackFiltersConstants.ALTITUDE_MAX_VALUE,
				app, this)

			FilterType.UPHILL -> UphillTrackFilter(
				0f,
				TrackFiltersConstants.DEFAULT_MAX_VALUE,
				app, this)

			FilterType.DOWNHILL -> DownhillTrackFilter(
				0f,
				TrackFiltersConstants.DEFAULT_MAX_VALUE,
				app, this)

			FilterType.DATE_CREATION -> DateCreationTrackFilter(this)
			FilterType.CITY -> CityTrackFilter(this)
			FilterType.OTHER -> OtherTrackFilter(app, this)
		}
		throw IllegalArgumentException("Unknown filterType $filterType")
	}

	fun getFilterClass(filterType: FilterType): Class<out BaseTrackFilter> {
		return when (filterType) {
			FilterType.NAME -> TrackNameFilter::class.java
			FilterType.DURATION -> DurationTrackFilter::class.java
			FilterType.TIME_IN_MOTION -> TimeInMotionTrackFilter::class.java
			FilterType.LENGTH -> LengthTrackFilter::class.java
			FilterType.AVERAGE_SPEED -> AverageSpeedTrackFilter::class.java
			FilterType.MAX_SPEED -> MaxSpeedTrackFilter::class.java
			FilterType.AVERAGE_ALTITUDE -> AverageAltitudeTrackFilter::class.java
			FilterType.MAX_ALTITUDE -> MaxAltitudeTrackFilter::class.java
			FilterType.UPHILL -> UphillTrackFilter::class.java
			FilterType.DOWNHILL -> DownhillTrackFilter::class.java
			FilterType.DATE_CREATION -> DateCreationTrackFilter::class.java
			FilterType.CITY -> CityTrackFilter::class.java
			FilterType.OTHER -> OtherTrackFilter::class.java
		}
		throw IllegalArgumentException("Unknown filterType $filterType")
	}

	fun resetCurrentFilters() {
		currentFilters.clear()
		for (filterType in FilterType.values()) {
			currentFilters.add(createFilter(filterType))
		}

	}

	private fun readSettings() {
		val settingsJson = preference.get()
		if (!Algorithms.isEmpty(settingsJson)) {
			val savedFilters = gson.fromJson<Map<String, BaseTrackFilter>>(settingsJson,
				object : TypeToken<Map<String?, BaseTrackFilter?>?>() {}.type)
			if (savedFilters != null) {
				this.savedFilters.putAll(savedFilters)
			}
		}
	}

	private fun writeSettings() {
		val json = gson.toJson(savedFilters)
		preference.set(json)
	}

	val nameFilter: TrackNameFilter?
		get() {
			return getFilterByType(FilterType.NAME) as TrackNameFilter
		}

	val dateFilter: DateCreationTrackFilter?
		get() {
			return getFilterByType(FilterType.DATE_CREATION) as DateCreationTrackFilter
		}

	val cityFilter: CityTrackFilter?
		get() {
			return getFilterByType(FilterType.CITY) as CityTrackFilter
		}

	private fun getFilterByType(type: FilterType): BaseTrackFilter? {
		for (filter in currentFilters) {
			if (filter.filterType == type) {
				return filter
			}
		}
		return null
	}

	companion object {
		private const val TRACK_FILTERS_SETTINGS_PREF_ID = "track_filters_settings_pref_id"
	}
}