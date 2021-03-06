//Created by plusminus on 19:05:55 - 12.02.2008
package org.androad.ui.map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.text.ClipboardManager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.androad.R;
import org.androad.adt.AndNavLocation;
import org.androad.adt.UnitSystem;
import org.androad.adt.DBPOI;
import org.androad.adt.Favorite;
import org.androad.adt.TrafficFeed;
import org.androad.app.APIIntentReceiver;
import org.androad.db.DBManager;
import org.androad.db.DataBaseException;
import org.androad.db.MapAnnotationsDBManager;
import org.androad.exc.Exceptor;
import org.androad.osm.util.CoordinatesExtractor;
import org.androad.osm.views.overlay.OSMMapViewCrosshairOverlay;
import org.androad.osm.views.overlay.OSMMapViewSimpleLineOverlay;
import org.androad.osm.views.tiles.util.OSMMapTilePreloader;
import org.androad.osm.views.util.Util;
import org.androad.osm.util.constants.OSMConstants;
import org.androad.nav.stats.StatisticsManager;
import org.androad.preferences.PreferenceConstants;
import org.androad.preferences.Preferences;
import org.androad.sys.ftpc.api.FTPCRequester;
import org.androad.sys.ors.adt.ds.POIGroup;
import org.androad.sys.ors.aas.AASRequester;
import org.androad.sys.ors.aas.AASResponse;
import org.androad.sys.ors.adt.GeocodedAddress;
import org.androad.sys.ors.adt.aoi.AreaOfInterest;
import org.androad.sys.ors.adt.aoi.Polygon;
import org.androad.sys.ors.adt.ds.POIType;
import org.androad.sys.ors.adt.lus.Country;
import org.androad.sys.ors.adt.lus.ReverseGeocodePreferenceType;
import org.androad.sys.ors.adt.ts.ISpatialDataOrganizer;
import org.androad.sys.ors.adt.ts.TrafficItem;
import org.androad.sys.ors.adt.ts.TrafficOverlayManager;
import org.androad.sys.ors.exceptions.ORSException;
import org.androad.sys.ors.ff.FoxyTagRequester;
import org.androad.sys.ors.lus.LUSRequester;
import org.androad.sys.ors.tuks.TUKSRequester;
import org.androad.sys.ors.util.RouteHandleIDExtractor;
import org.androad.sys.ors.views.overlay.AreaOfInterestOverlay;
import org.androad.sys.ors.views.overlay.OsmBugPoint;
import org.androad.sys.ors.views.overlay.FavoritePoint;
import org.androad.sys.ors.views.overlay.FoxyTagPoint;
import org.androad.sys.ors.views.overlay.BitmapItem;
import org.androad.sys.ors.views.overlay.BitmapOverlay;
import org.androad.sys.ors.views.overlay.CircleItem;
import org.androad.sys.ors.views.overlay.CircleOverlay;
import org.androad.sys.ors.views.overlay.TrafficOverlay;
import org.androad.sys.ors.views.overlay.TrafficOverlayItem;
import org.androad.sys.osb.adt.OpenStreetBug;
import org.androad.sys.osb.api.OSBRequester;
import org.androad.sys.postcode.uk_bs_7666.PostcodeUK_BS7776Matcher;
import org.androad.sys.vehicleregistrationplates.VRPRegistry;
import org.androad.sys.vehicleregistrationplates.tables.IVRPElement;
import org.androad.ui.camera.CameraFavorite;
import org.androad.ui.common.CommonCallback;
import org.androad.ui.common.CommonCallbackAdapter;
import org.androad.ui.common.CommonDialogFactory;
import org.androad.ui.common.CommonDialogFactory.OSBMapLongAddSelectorResult;
import org.androad.ui.common.InlineAutoCompleterConstant;
import org.androad.ui.common.views.CompassImageView;
import org.androad.ui.common.views.CompassRotateView;
import org.androad.ui.osm.api.nodes.POICategorySelector;
import org.androad.ui.sd.SDMainChoose;
import org.androad.ui.weather.WeatherForecast;
import org.androad.util.FileSizeFormatter;
import org.androad.util.TimeUtils;
import org.androad.util.UserTask;
import org.androad.util.constants.Constants;

import org.openstreetmap.api.exceptions.OSMAPIException;
import org.openstreetmap.api.node.NodeCreationRequester;

import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.MapController.AnimationType;
import org.osmdroid.views.overlay.DirectedLocationOverlay;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.ItemizedOverlayControlView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.OverlayManager;
import org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener;

public class WhereAmIMap extends OpenStreetMapAndNavBaseActivity implements PreferenceConstants, Constants, OnItemGestureListener<OverlayItem>{
	// ===========================================================
	// Final Fields
	// ===========================================================

	/** Time in milliseconds the Autocentering is disabled, after the user panned the map. */
	private static final int AUTOCENTER_BLOCKTIME = 5000;

	private static final int REQUESTCODE_WEATHER = 0;
	private static final int REQUESTCODE_STRUCTURED_SEARCH_SD_MAINCHOOSE = REQUESTCODE_WEATHER + 1;
	private static final int REQUESTCODE_DDMAP = REQUESTCODE_STRUCTURED_SEARCH_SD_MAINCHOOSE + 1;
	public static final int REQUESTCODE_PICTURE = REQUESTCODE_DDMAP + 1;
	private static final int REQUESTCODE_POICATEGORYSELECTOR = REQUESTCODE_PICTURE + 1;

	private final int LAT_INDEX = 0;
	private final int LON_INDEX = 1;

	private static final int MENU_QUIT_ID = Menu.FIRST;
	private static final int MENU_SATELLITE_ID = MENU_QUIT_ID + 1;
	private static final int MENU_SUBMENU_TRAFFIC_ID = MENU_SATELLITE_ID + 1;
	private static final int MENU_WEATHER_ID = MENU_SUBMENU_TRAFFIC_ID + 1;
	private static final int MENU_LAYER_ID = MENU_WEATHER_ID + 1;
    private static final int MENU_SUBMENU_POI = MENU_LAYER_ID + 1;
    private static final int MENU_SUBMENU_FOXYTAG = MENU_SUBMENU_POI + 1;
    private static final int MENU_SUBMENU_FAVORITE = MENU_SUBMENU_FOXYTAG + 1;
    private static final int MENU_SUBMENU_OSMBUG = MENU_SUBMENU_FAVORITE + 1;
    private static final int MENU_SUBMENU_MAPANNOTATIONS = MENU_SUBMENU_OSMBUG + 1;
	private static final int MENU_PRELOAD_ID = MENU_SUBMENU_MAPANNOTATIONS + 1;
	private static final int MENU_ACCESSIBILITYANALYSIS_ID = MENU_PRELOAD_ID + 1;
	private static final int MENU_LOAD_TRACE_ID = MENU_ACCESSIBILITYANALYSIS_ID + 1;
	private static final int MENU_SHOWLATLON_ID = MENU_LOAD_TRACE_ID + 1;
	private static final int MENU_INPUTLATLON_ID = MENU_SHOWLATLON_ID + 1;
	private static final int MENU_VEHICLEREGISTRATIONPLATE_LOOKUP_ID = MENU_INPUTLATLON_ID + 1;
	private static final int MENU_SUBMENU_TRAFFIC_CUSTOM = MENU_VEHICLEREGISTRATIONPLATE_LOOKUP_ID + 1;
	private static final int MENU_SUBMENU_TRAFFIC_CLEAR = MENU_SUBMENU_TRAFFIC_CUSTOM + 1;
	private static final int MENU_GPSSTATUS_ID = MENU_SUBMENU_TRAFFIC_CLEAR + 1;

	private static final int MENU_SUBMENU_LAYERS_OFFSET = 1000;

	private static final int DIALOG_SELECT_CUSTOM_TRAFFIC_FEED = 0;
	private static final int DIALOG_ADD_CUSTOM_TRAFFIC_FEED = DIALOG_SELECT_CUSTOM_TRAFFIC_FEED + 1;
	private static final int DIALOG_INPUT_LAT_LON = DIALOG_ADD_CUSTOM_TRAFFIC_FEED + 1;
	private static final int DIALOG_SELECT_FREEFORM_OR_STRUCTURED_SEARCH = DIALOG_INPUT_LAT_LON + 1;
	private static final int DIALOG_INPUT_FAVORITE_NAME = DIALOG_SELECT_FREEFORM_OR_STRUCTURED_SEARCH + 1;
	private static final int DIALOG_SELECT_VEHICLEREGISTRATIONPLATE_LOOKUP_COUNTRIES = DIALOG_INPUT_FAVORITE_NAME + 1;
	private static final int DIALOG_INPUT_VEHICLEREGISTRATIONPLATE_LOOKUP = DIALOG_SELECT_VEHICLEREGISTRATIONPLATE_LOOKUP_COUNTRIES + 1;
	private static final int DIALOG_SELECT_POI_OR_OSB_OR_FTPC = DIALOG_INPUT_VEHICLEREGISTRATIONPLATE_LOOKUP + 1;
    private static final int DIALOG_INPUT_OSB_BUG = DIALOG_SELECT_POI_OR_OSB_OR_FTPC + 1;
	private static final int DIALOG_INPUT_OSM_POI = DIALOG_INPUT_OSB_BUG + 1;

	private static final int CENTERMODE_NONE = 0;
	private static final int CENTERMODE_ONCE = CENTERMODE_NONE + 1;
	private static final int CENTERMODE_AUTO = CENTERMODE_ONCE + 1;

	// ===========================================================
	// Fields
	// ===========================================================

	/** Holds the timestamp until the AutoCentering is blocked, because the user has panned the map. */
	private long mAutoCenterBlockedUntil = 0;

	private SensorManager mSensorManager;

	private ImageButton mIbtnCenter;
	private ImageButton mIbtnWhereAmI;
	private ImageButton mIbtnSearch;
	private ImageButton mIbtnChooseRenderer;
	private CompassRotateView mCompassRotateView;
	private CompassImageView mIvCompass;
    private TextView mIbtnSpeed;
	private EditText mEtSearch;
	private ImageButton mIbtnNavPointsDoStart;
	private ImageButton mIbtnNavPointsDoCancel;
	private ImageButton mIbtnNavPointsSetStart;
	private ImageButton mIbtnNavPointsSetDestination;
	private GeoPoint mGPLastMapClick;

	private ItemizedOverlayControlView mMapItemControlView;
	private ScaleBarOverlay mScaleIndicatorView;

	private DirectedLocationOverlay mMyLocationOverlay;
	private OSMMapViewCrosshairOverlay mCrosshairOverlay;

	private TrafficFeed mCurrentTrafficFeed;

	private int mDoCenter = WhereAmIMap.CENTERMODE_AUTO;

	private ArrayList<OverlayItem> mSearchPinList;
	/** Currently selected index in mSearchPinList. */
	private int mSearchPinListIndex;

	private ItemizedOverlayWithFocus<OverlayItem> mItemOverlay;
	private AreaOfInterestOverlay mAASOverlay;
    private CircleOverlay mPOIOverlay;
	private CircleOverlay mFFOverlay;
	private BitmapOverlay mFavoriteOverlay;
    private CircleOverlay mOsmBugOverlay;
    private CircleOverlay mMapAnnotationsOverlay;
	private TrafficOverlay mTrafficOverlay;
	private BitmapItem mStartFlagItem;
	private BitmapItem mDestinationFlagItem;
	private BitmapOverlay mFlagsOverlay;
	private OSMMapViewSimpleLineOverlay mNavPointsConnectionLineOverlay;

	/** Keeps the screen alive when it would lock otherwise. */
	private PowerManager.WakeLock mWakeLock;

	private Animation mFadeOutDelayedAnimation;
	private Animation mFadeToLeftAnimation;
	private Animation mFadeToRightAnimation;
	private Animation mFadeOutQuickAnimation;

	private AreaOfInterestOverlay mAreaOfAvoidingsOverlay;

	private final ArrayList<AreaOfInterest> mAvoidAreas = new ArrayList<AreaOfInterest>();
    private final UnitSystem us = Preferences.getUnitSystem(this);

	private boolean mNavPointsCrosshairMode;

    private MapAnnotationsDBManager mapAnnotationDB;

	/**
	 * Indicates whether driving-statistics are generated.
	 * Loaded from Preferences in onResume().
	 */
	private boolean mStatisticsEnabled = false;
	private StatisticsManager mStatisticsManager;

    /* Show or not layers */
    private boolean showOverlayPoi = false;
    private boolean showOverlayFoxyTag = false;
    private boolean showOverlayFavorite = false;
    private boolean showOverlayOsmBug = false;
    private boolean showOverlayMapAnnotations = false;

    // POI Type of new added poi
	private POIType mAddOSMPOIType;

	// ===========================================================
	// Constructors
	// ===========================================================

	@Override
	protected void onSetupContentView() {
		this.setContentView(R.layout.whereami_map);
		this.mOSMapView = (MapView)findViewById(R.id.map_whereami);
		this.mOSMapView.setTileSource(Preferences.getMapViewProviderInfoWhereAmI(this));
        this.mOSMapView.setMapListener(new AndRoadMapListener(this));

        final OverlayManager overlaymanager = this.mOSMapView.getOverlayManager();

		/* Add a new instance of our fancy Overlay-Class to the MapView. */

		this.mMyLocationOverlay = new DirectedLocationOverlay(this);
		this.mMyLocationOverlay.setLocation(getLastKnownLocation(true));

		this.mTrafficOverlay = new TrafficOverlay(this, new ArrayList<TrafficOverlayItem>(), new OnItemGestureListener<TrafficOverlayItem>(){
			@Override
			public boolean onItemSingleTapUp(final int index, final TrafficOverlayItem item) {
				if(index >= WhereAmIMap.this.mTrafficOverlay.getOverlayItems().size()) {
					throw new IllegalArgumentException();
				}

				final TrafficOverlayItem focusedItem = WhereAmIMap.this.mTrafficOverlay.getFocusedItem();
				if(!item.equals(focusedItem)){
					WhereAmIMap.this.mTrafficOverlay.setFocusedItem(item);
				}else{
					WhereAmIMap.this.mTrafficOverlay.unSetFocusedItem();
				}

				WhereAmIMap.this.mOSMapView.getController().animateTo(item.getPoint(), AnimationType.MIDDLEPEAKSPEED);

				return true;
			}

            @Override
            public boolean onItemLongPress(final int index, final TrafficOverlayItem item) {
                return true;
            }
		});
		this.mTrafficOverlay.setDrawnItemsLimit(50);
		this.mTrafficOverlay.setFocusItemsOnTap(false);

		this.mAASOverlay = new AreaOfInterestOverlay(this);
		this.mAASOverlay.setDrawnAreasLimit(10);
        this.mPOIOverlay = new CircleOverlay(this);
		this.mFFOverlay = new CircleOverlay(this);
		this.mFavoriteOverlay = new BitmapOverlay(this);
        this.mOsmBugOverlay = new CircleOverlay(this);
        this.mMapAnnotationsOverlay = new CircleOverlay(this);
		this.mAreaOfAvoidingsOverlay = new AreaOfInterestOverlay(this, this.mAvoidAreas);
        this.mFlagsOverlay = new BitmapOverlay(this);

		/* SetNavPoints-Overlay. */
		this.mCrosshairOverlay = new OSMMapViewCrosshairOverlay(this, Color.BLACK, 2, 17);
		this.mCrosshairOverlay.setEnabled(false);
		this.mStartFlagItem = new BitmapItem(null, this, R.drawable.flag_start, null, new Point(18,47));
		this.mDestinationFlagItem = new BitmapItem(null, this, R.drawable.flag_destination, null, new Point(18,47));
        this.mFlagsOverlay.getBitmapItems().add(this.mStartFlagItem);
        this.mFlagsOverlay.getBitmapItems().add(this.mDestinationFlagItem);
		this.mNavPointsConnectionLineOverlay = new OSMMapViewSimpleLineOverlay(this);
		this.mNavPointsConnectionLineOverlay.setPaintNormal();
		this.mNavPointsConnectionLineOverlay.setEnabled(false);

		overlaymanager.add(this.mAASOverlay);
        overlaymanager.add(this.mPOIOverlay);
		overlaymanager.add(this.mFFOverlay);
		overlaymanager.add(this.mFavoriteOverlay);
        overlaymanager.add(this.mOsmBugOverlay);
        overlaymanager.add(this.mMapAnnotationsOverlay);
		overlaymanager.add(this.mAreaOfAvoidingsOverlay);
		overlaymanager.add(this.mTrafficOverlay);
		overlaymanager.add(this.mNavPointsConnectionLineOverlay);
		overlaymanager.add(this.mFlagsOverlay);
		overlaymanager.add(this.mMyLocationOverlay);
		overlaymanager.add(this.mCrosshairOverlay);

        mapAnnotationDB = new MapAnnotationsDBManager(this);
	}

	private void refreshPinOverlay(final GeoPoint pGeoPoint) {
		final ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
		items.add(new OverlayItem("", "", pGeoPoint));
		refreshPinOverlay(items);
		WhereAmIMap.this.updateUIForAutoCenterChange(WhereAmIMap.CENTERMODE_NONE);
		WhereAmIMap.this.mOSMapView.getController().animateTo(pGeoPoint, AnimationType.MIDDLEPEAKSPEED);
	}

	private void refreshPinOverlay(final ArrayList<OverlayItem> items){
		this.mSearchPinListIndex = 0;

		clearPinOverlay();

		this.mMapItemControlView.setVisibility(View.VISIBLE);

		final boolean nextPreviousEnabled = items.size() > 1;
		this.mMapItemControlView.setNextEnabled(nextPreviousEnabled);
		this.mMapItemControlView.setPreviousEnabled(nextPreviousEnabled);

		this.mSearchPinList = items;

		this.mOSMapView.getOverlayManager().add(this.mItemOverlay = new ItemizedOverlayWithFocus<OverlayItem>(this, this.mSearchPinList, this));
		this.mItemOverlay.setFocusItemsOnTap(false);
	}


	private void clearPinOverlay() {
		this.mMapItemControlView.setVisibility(View.GONE);

		if(this.mSearchPinList != null) {
			this.mSearchPinList.clear();
		}

		final OverlayManager overlaymanager = this.mOSMapView.getOverlayManager();
		if(this.mItemOverlay != null) {
			overlaymanager.remove(this.mItemOverlay);
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle icicle) {
		super.onCreate(icicle);

		/* Load all the Views. */
		this.mIbtnCenter = (ImageButton)this.findViewById(R.id.ibtn_whereami_center);
		this.mIbtnWhereAmI = (ImageButton)this.findViewById(R.id.ibtn_whereami_whereami);
		this.mIbtnSearch = (ImageButton)this.findViewById(R.id.ibtn_whereami_search);
		this.mIbtnChooseRenderer = (ImageButton)this.findViewById(R.id.ibtn_whereami_choose_renderer);
		this.mCompassRotateView = (CompassRotateView)this.findViewById(R.id.rotator_wheramimap);
        this.mCompassRotateView.toggleActive();
		this.mIvCompass = (CompassImageView)this.findViewById(R.id.iv_whereami_compass);
		this.mIbtnSpeed = (TextView)this.findViewById(R.id.ibtn_whereami_speed);
        this.mIbtnSpeed.setText("NaN");
		this.mEtSearch = (EditText)this.findViewById(R.id.et_whereami_search);
		this.mIbtnNavPointsSetStart = (ImageButton)this.findViewById(R.id.ibtn_whereami_setstartpoint);
		this.mIbtnNavPointsSetDestination = (ImageButton)this.findViewById(R.id.ibtn_whereami_setendpoint);
		this.mIbtnNavPointsDoStart = (ImageButton)this.findViewById(R.id.ibtn_whereami_setnavpoints_start);
		this.mIbtnNavPointsDoCancel = (ImageButton)this.findViewById(R.id.ibtn_whereami_setnavpoints_cancel);
		this.mMapItemControlView = (ItemizedOverlayControlView)this.findViewById(R.id.itemizedoverlaycontrol_whereami);

		this.mScaleIndicatorView = new ScaleBarOverlay(this);
        if (us == UnitSystem.IMPERIAL) {
            this.mScaleIndicatorView.setImperial();
        } else {
            this.mScaleIndicatorView.setMetric();
        }

		this.mStatisticsEnabled = Preferences.getStatisticsEnabled(this);
		if(this.mStatisticsEnabled) {
			this.mStatisticsManager = new StatisticsManager(this, us, Preferences.getStatisticsSessionStart(this));
		} else{
			this.mStatisticsManager = null;
		}

        this.mScaleIndicatorView.setScaleBarOffset(getResources().getDisplayMetrics().widthPixels/2 - getResources().getDisplayMetrics().xdpi/2, 10);
        final OverlayManager overlaymanager = this.mOSMapView.getOverlayManager();
        overlaymanager.add(this.mScaleIndicatorView);

		/* Load the animation from XML (XML file is res/anim/***.xml). */
		this.mFadeOutDelayedAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_out_delayed);
		this.mFadeOutDelayedAnimation.setFillAfter(true);

		this.mFadeOutQuickAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_out);
		this.mFadeOutQuickAnimation.setFillAfter(true);

		this.mFadeToLeftAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_to_left);
		this.mFadeToLeftAnimation.setFillAfter(true);

		this.mFadeToRightAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_to_right);
		this.mFadeToRightAnimation.setFillAfter(true);

		/* Run the Hide-Icons animation on the start, because no touch is applied yet. */
		this.startDelayedHideControlsAnimation();


		this.mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		/* This code together with the one in onResume() will make the screen be always on during navigation. */
		final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "MyWakeLock");
		this.mWakeLock.acquire();


		this.applyQuickButtonListeners();
		this.applyZoomButtonListeners();
		this.applyMapViewLongPressListener();
		this.applyAutoCompleteListeners();


		final boolean doDefault = !handlePossibleAction();

		if(doDefault){
			final GeoPoint location = getLastKnownLocation(true);
			if(location == null || Math.abs(location.getLatitudeE6()) <= 100 || Math.abs(location.getLongitudeE6()) <= 100) {
				this.mOSMapView.getController().setZoom(3);
			} else{
				this.mOSMapView.getController().setZoom(13);
				this.mOSMapView.getController().setCenter(location);
			}

			/* Show the user why the map is auto-centering on the user. */
			Toast.makeText(WhereAmIMap.this, R.string.toast_autofollow_enabled, Toast.LENGTH_SHORT).show();
			this.mDoCenter = WhereAmIMap.CENTERMODE_AUTO;
		}
	}

	/**
	 * 
	 * @return <code>true</code> whether an action was correctly recognized and handled.
	 */
	private boolean handlePossibleAction() {
		final Intent iStartedWith = this.getIntent();
		final String action = iStartedWith.getAction();
		if(action != null){
			if(action.equals(ANDROAD_VIEW_ACTION)){
				final Bundle extras = iStartedWith.getExtras();
				/* Extract geopoint-Strings from the Bundle. */
				final ArrayList<String> geoPointStrings = extras.getStringArrayList(APIIntentReceiver.WHEREAMI_EXTRAS_LOCATIONS_ID);

				if(geoPointStrings.size() > 0){
					/* And convert them to actual GeoPoints */
					final ArrayList<GeoPoint> geoPoints = new ArrayList<GeoPoint>(geoPointStrings.size());
					for (final String locationString : geoPointStrings) {
						geoPoints.add(GeoPoint.fromIntString(locationString));
					}


					/* Extract descriptions and titles from the Bundle. */
					final ArrayList<String> descriptions = extras.getStringArrayList(APIIntentReceiver.WHEREAMI_EXTRAS_LOCATIONS_DESCRIPTIONS_ID);
					final ArrayList<String> titles = extras.getStringArrayList(APIIntentReceiver.WHEREAMI_EXTRAS_LOCATIONS_TITLES_ID);


					/* Create overlay-items from the data extracted. */
					final ArrayList<OverlayItem> items = new ArrayList<OverlayItem>(geoPointStrings.size());
					for(int i = 0; i < geoPointStrings.size(); i++) {
						items.add(new OverlayItem(titles.get(i), descriptions.get(i), geoPoints.get(i)));
					}

					/* Calculate the BoundingBox around the items. */
					final BoundingBoxE6 itemBoundingBoxE6 = BoundingBoxE6.fromGeoPoints(geoPoints);

					refreshPinOverlay(items);

					WhereAmIMap.this.updateUIForAutoCenterChange(WhereAmIMap.CENTERMODE_NONE);

					new Handler().postDelayed(new Runnable(){
						@Override
						public void run() {
							if(items.size() == 1) {
								WhereAmIMap.this.mOSMapView.getController().setZoom(13);
							} else {
								WhereAmIMap.this.mOSMapView.getController().zoomToSpan(itemBoundingBoxE6);
							}

							WhereAmIMap.this.mOSMapView.getController().animateTo(itemBoundingBoxE6.getCenter(), AnimationType.MIDDLEPEAKSPEED);
						}
					}, 500);

					return true;
				}
			}else if(action.equals(android.content.Intent.ACTION_VIEW)){
				final Uri data = iStartedWith.getData();
				if(data != null && data.getScheme().equals("geo")){

					/* Extract lat/lon-String. */
					final String coordsString = iStartedWith.getData().getSchemeSpecificPart();
					if(coordsString.length() > 0){
						final String[] coordinates = coordsString.split(",");
						try{
							final double lat = Double.parseDouble(coordinates[this.LAT_INDEX]);
							final double lon = Double.parseDouble(coordinates[this.LON_INDEX]);

							this.mOSMapView.getController().setZoom(15);
							this.mOSMapView.getController().setCenter(new GeoPoint((int)(lat * 1E6), (int)(lon * 1E6)));

							this.mDoCenter = WhereAmIMap.CENTERMODE_NONE;
							return true;
						}catch(final NumberFormatException nfe){
							final int qParamIndex = coordsString.indexOf("q=");
							if(qParamIndex != -1){
								final String textualQuery = coordsString.substring(qParamIndex + "q=".length());
								if(textualQuery.length() > 0){
									searchORSLocations(textualQuery);

									this.mEtSearch.setText(textualQuery);

									this.mDoCenter = WhereAmIMap.CENTERMODE_NONE;
									return true;
								}
							}else{
								Log.d(Constants.DEBUGTAG, "Could not parse \"" + iStartedWith.getData().toString() + "\"-Uri");
							}
						}
					}
				}
			}
		}
		return false;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	private void startDelayedHideControlsAnimation(){
		/* Left icons */
		if(this.mEtSearch.getVisibility() != View.VISIBLE) {
			this.mIbtnSearch.startAnimation(this.mFadeToLeftAnimation);
		}

		if(this.mNavPointsCrosshairMode == false){
			this.mIbtnNavPointsDoStart.startAnimation(this.mFadeToLeftAnimation);
			this.mIbtnCenter.startAnimation(this.mFadeToRightAnimation);
		}

		/* Right icons */
		this.mIbtnWhereAmI.startAnimation(this.mFadeToRightAnimation);
		this.mIbtnChooseRenderer.startAnimation(this.mFadeToRightAnimation);
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		switch(requestCode){
			case REQUESTCODE_STRUCTURED_SEARCH_SD_MAINCHOOSE:
				if(resultCode == SUBACTIVITY_RESULTCODE_CHAINCLOSE_SUCCESS || resultCode == SUBACTIVITY_RESULTCODE_SUCCESS){
					final Bundle b = data.getExtras();
					final ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();

					final int latE6 = b.getInt(EXTRAS_DESTINATION_LATITUDE_ID);
					final int lonE6 = b.getInt(EXTRAS_DESTINATION_LONGITUDE_ID);
					final GeoPoint gp = new GeoPoint(latE6, lonE6);

					items.add(new OverlayItem(b.getString(EXTRAS_DESTINATION_TITLE), "", gp));
					refreshPinOverlay(items);
					WhereAmIMap.this.updateUIForAutoCenterChange(WhereAmIMap.CENTERMODE_NONE);
					WhereAmIMap.this.mOSMapView.getController().animateTo(gp, AnimationType.MIDDLEPEAKSPEED);
				}
				break;
            case REQUESTCODE_PICTURE:
                String result = data.getStringExtra(CommonDialogFactory.class.getName());
                long favoriteid = -1;
                if (WhereAmIMap.this.mGPLastMapClick == null)
                    break;
                try {
                    favoriteid = DBManager.addFavorite(WhereAmIMap.this, result, WhereAmIMap.this.mGPLastMapClick.getLatitudeE6(), WhereAmIMap.this.mGPLastMapClick.getLongitudeE6());
                } catch (final DataBaseException e) {
                    Toast.makeText(WhereAmIMap.this, R.string.toast_error_adding_favorite, Toast.LENGTH_LONG).show();
                }

                if (favoriteid == -1)
                    break;

                Favorite f;
                try {
                    f = DBManager.getFavoriteById(this, "" + favoriteid);
                } catch (final DataBaseException e) {
                    break;
                }
                byte[] d = data.getByteArrayExtra(CameraFavorite.class.getName());

                final String favoriteFolderPath = org.androad.osm.util.Util.getAndRoadExternalStoragePath() + OSMConstants.SDCARD_SAVEDFAVORITES_PATH;
                new File(favoriteFolderPath).mkdirs();
                final String filename = f.getPhotoFilename();
                FileOutputStream outStream = null;
                try {
                    // Write to sdcard
                    outStream = new FileOutputStream(filename);
                    outStream.write(d);
                    outStream.flush();
                    outStream.close();
				} catch (final Exception e) {
					Log.e(OSMConstants.DEBUGTAG, "File-Writing-Error", e);
				}
                break;
            case REQUESTCODE_POICATEGORYSELECTOR:
				this.mAddOSMPOIType = POIType.values()[resultCode];
                if (CommonDialogFactory.inputOSMPOI != null) {
                    final TextView tvCategoryName = (TextView)CommonDialogFactory.inputOSMPOI.findViewById(R.id.tv_dlg_input_osmpoiname_name);
                    tvCategoryName.setText(this.mAddOSMPOIType.READABLENAMERESID);
                }
				showDialog(DIALOG_INPUT_OSM_POI);
				break;
		}
	}

	@Override
	public void release(){
		// Nothing...
	}

	@Override
	public void onDestroy() {
		Log.d(Constants.DEBUGTAG, "OnDESTROY");

		this.mWakeLock.release();

		if(this.mStatisticsManager != null) {
			this.mStatisticsManager.finish();
		}

		super.onDestroy();
	}

	private final String STATE_AUTOCENTER_ID = "state_autocenter_id";
	private final String STATE_ETSEARCHVISIBLE_ID = "state_etsearchvisible_id";
	private final String STATE_ZOOM_ID = "state_zoom_id";
	private final String STATE_MAPCENTER_ID = "state_mapcenter_id";
	private final String STATE_VEHICLEREGISTRATIONPLATE_NATIONALITY_ID = "state_crp_nationality_id";

	protected Country mVehicleRegistrationPlateLOokupNationality;

	@Override
	protected void onRestoreInstanceState(final Bundle savedInstanceState) {
		if(savedInstanceState == null) {
			return;
		}

		super.onRestoreInstanceState(savedInstanceState);

		updateUIForAutoCenterChange(savedInstanceState.getInt(this.STATE_AUTOCENTER_ID, WhereAmIMap.CENTERMODE_AUTO));

		if(savedInstanceState.getBoolean(this.STATE_ETSEARCHVISIBLE_ID)) {
			handleSearchOpen();
		}

		this.mOSMapView.getController().setZoom(savedInstanceState.getInt(this.STATE_ZOOM_ID, 13));

		final GeoPoint mapCenter = savedInstanceState.getParcelable(this.STATE_MAPCENTER_ID);
		this.mOSMapView.getController().setCenter(mapCenter);

		final String vrpLookupNationalityString = savedInstanceState.getString(this.STATE_VEHICLEREGISTRATIONPLATE_NATIONALITY_ID);
		if(vrpLookupNationalityString != null){
			this.mVehicleRegistrationPlateLOokupNationality = Country.fromAbbreviation(vrpLookupNationalityString);
		}
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		outState.putInt(this.STATE_AUTOCENTER_ID, this.mDoCenter);

		outState.putBoolean(this.STATE_ETSEARCHVISIBLE_ID, this.mEtSearch.getVisibility() == View.VISIBLE);

		outState.putInt(this.STATE_ZOOM_ID, this.mOSMapView.getZoomLevel());

		outState.putParcelable(this.STATE_MAPCENTER_ID, this.mOSMapView.getMapCenter());

		if(this.mVehicleRegistrationPlateLOokupNationality != null) {
			outState.putString(this.STATE_VEHICLEREGISTRATIONPLATE_NATIONALITY_ID, this.mVehicleRegistrationPlateLOokupNationality.COUNTRYCODE);
		}

		if(this.mStatisticsEnabled && this.mStatisticsManager != null) {
			this.mStatisticsManager.writeThrough();
		}

		super.onSaveInstanceState(outState);
	}

	/**
	 * Gets called when an item of the PinOverlay gets tapped.
	 */
	@Override
	public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
		if(index >= this.mSearchPinList.size()) {
			throw new IllegalArgumentException();
		}

		this.mSearchPinListIndex = index;
		final TrafficOverlayItem focusedItem = WhereAmIMap.this.mTrafficOverlay.getFocusedItem();
		if(!item.equals(focusedItem)){
			this.mItemOverlay.setFocusedItem(item);
		}else{
			this.mItemOverlay.unSetFocusedItem();
		}

		this.mOSMapView.getController().animateTo(item.getPoint(), AnimationType.MIDDLEPEAKSPEED);

		return true;
	}

    @Override
    public boolean onItemLongPress(final int index, final OverlayItem item) {
        return true;
    }

	@Override
	protected void onResume() {
		super.onResume();

		if((this.mSensorManager.getSensors() & SensorManager.SENSOR_ORIENTATION) != 0){
			this.mSensorManager.registerListener(this.mCompassRotateView, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_UI);
			this.mSensorManager.registerListener(this.mIvCompass, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_UI);
		}
	}

	@Override
	protected void onPause() {
		if((this.mSensorManager.getSensors() & SensorManager.SENSOR_ORIENTATION) != 0){
			this.mSensorManager.unregisterListener(this.mCompassRotateView);
			this.mSensorManager.unregisterListener(this.mIvCompass);
		}
		super.onPause();
	}

	@Override
	public boolean onKeyDown(final int keyCode, final KeyEvent event) {
		switch(keyCode){
			case KeyEvent.KEYCODE_SEARCH:
				if(this.mEtSearch.getVisibility() != View.VISIBLE) {
					handleSearchOpen();
				} else {
					handleSearchSubmit(this.mEtSearch.getText().toString());
				}
				return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		int menuPos = Menu.FIRST;
		menu.setQwertyMode(true);

		{ // Weather-Item
			menu.add(menuPos, MENU_WEATHER_ID, menuPos, getString(R.string.maps_menu_weather))
			.setIcon(R.drawable.weather_get)
			.setAlphabeticShortcut('w');
			menuPos++;
		}

		{ // Layer-Item
            final SubMenu subMenu = menu.addSubMenu(menuPos, MENU_LAYER_ID, menuPos, getString(R.string.maps_menu_layer))
            .setIcon(android.R.drawable.ic_menu_mapmode);
			menuPos++;
            {
                subMenu.add(menuPos, MENU_SUBMENU_POI, menuPos, getString(R.string.maps_menu_submenu_layer_poi))
                    .setIcon(R.drawable.settingsmenu_poi);
                subMenu.add(menuPos, MENU_SUBMENU_FOXYTAG, menuPos, getString(R.string.maps_menu_submenu_layer_foxytag))
                    .setIcon(R.drawable.foxytag);
                subMenu.add(menuPos, MENU_SUBMENU_FAVORITE, menuPos, getString(R.string.maps_menu_submenu_layer_favorite))
                    .setIcon(R.drawable.settingsmenu_favorites);
                subMenu.add(menuPos, MENU_SUBMENU_OSMBUG, menuPos, getString(R.string.maps_menu_submenu_layer_osmbug))
                    .setIcon(R.drawable.settingsmenu_osmbug);
                subMenu.add(menuPos, MENU_SUBMENU_MAPANNOTATIONS, menuPos, getString(R.string.maps_menu_submenu_layer_mapannotations))
                    .setIcon(R.drawable.settingsmenu_mapannotations);
                subMenu.setGroupCheckable(menuPos, true, false);
                menuPos++;
            }
		}

		{ // Traffic-SubMenuItem
			final SubMenu subMenu = menu.addSubMenu(menuPos, MENU_SUBMENU_TRAFFIC_ID, menuPos, getString(R.string.maps_menu_submenu_traffic))
            .setIcon(R.drawable.warning_severe);
			menuPos++;
			{
				subMenu.add(0, MENU_SUBMENU_TRAFFIC_CLEAR, Menu.NONE, R.string.maps_menu_submenu_traffic_clear);
				subMenu.add(1, MENU_SUBMENU_TRAFFIC_CUSTOM, Menu.NONE, R.string.maps_menu_submenu_traffic_custom_feeds);
			}
		}

		{ // VRP-Lookup-Item
			menu.add(menuPos, MENU_VEHICLEREGISTRATIONPLATE_LOOKUP_ID, menuPos, getString(R.string.maps_menu_vehicleregistrationplate_lookup))
			.setIcon(R.drawable.vehicleregistrationplate)
			.setAlphabeticShortcut('v');
			menuPos++;
		}

		{ // AAS-Item
			menu.add(menuPos, MENU_ACCESSIBILITYANALYSIS_ID, menuPos, getString(R.string.maps_menu_accessibility_analysis))
			.setIcon(R.drawable.accessibility)
			.setAlphabeticShortcut('a');
			menuPos++;
		}

		{ // GPS-Status-Item
			menu.add(menuPos, MENU_GPSSTATUS_ID, menuPos, getString(R.string.maps_menu_gpsstatus))
			.setIcon(R.drawable.gps_status)
			.setAlphabeticShortcut('g');
			menuPos++;
		}

		{ // Preload-Item
			menu.add(menuPos, MENU_PRELOAD_ID, menuPos, getString(R.string.maps_menu_preload))
			.setIcon(R.drawable.preload)
			.setAlphabeticShortcut('p');
			menuPos++;
		}

		{ // Show Lat/Lng-Item
			menu.add(menuPos, MENU_SHOWLATLON_ID, menuPos, getString(R.string.maps_menu_getcentetcoordinates))
			.setIcon(R.drawable.world)
			.setAlphabeticShortcut('l');
			menuPos++;
		}

		{ // Input Lat/Lon-Item
			menu.add(menuPos, MENU_INPUTLATLON_ID, menuPos, getString(R.string.maps_menu_focus_coordinates))
			.setIcon(R.drawable.world)
			.setAlphabeticShortcut('i');
			menuPos++;
		}

		{ // Close-Item
			if(menu.size() <= 5){ // If there will be no 'more'-item
				menu.add(menuPos, MENU_QUIT_ID, menuPos, getString(R.string.maps_menu_quit))
				.setIcon(R.drawable.exit)
				.setAlphabeticShortcut('q');
			}else{
				// Place it as the fifth.
				menu.add(4, MENU_QUIT_ID, 4, getString(R.string.maps_menu_quit))
				.setIcon(R.drawable.exit)
				.setAlphabeticShortcut('q');
			}
		}

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(final Menu menu) {
		/* Refresh Accessibility item. */
		final MenuItem item = menu.findItem(MENU_ACCESSIBILITYANALYSIS_ID);
		item.setTitle((this.mAASOverlay.getAreasOfInterest().size() == 0) ? R.string.maps_menu_accessibility_analysis : R.string.hide);

		menu.findItem(MENU_SUBMENU_TRAFFIC_CLEAR).setVisible(this.mTrafficOverlay.getOverlayItems() != null && this.mTrafficOverlay.getOverlayItems().size() > 0);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(final int featureId, final MenuItem item) {
		final int itemId = item.getItemId();
		switch(itemId){
			case MENU_ACCESSIBILITYANALYSIS_ID:
				if(this.mAASOverlay.getAreasOfInterest().size() == 0) {
					showAccessibilityAnalysisDialog(item);
				} else {
					this.mAASOverlay.getAreasOfInterest().clear();
				}
				return true;
			case MENU_WEATHER_ID:
				openWeatherDialog(this.mOSMapView.getMapCenter());
				return true;
            case MENU_SUBMENU_POI:
                showPoi(item);
                return true;
            case MENU_SUBMENU_FOXYTAG:
                showFoxyTag(item);
                return true;
            case MENU_SUBMENU_FAVORITE:
                showFavorite(item);
                return true;
            case MENU_SUBMENU_OSMBUG:
                showOsmBug(item);
                return true;
            case MENU_SUBMENU_MAPANNOTATIONS:
                showMapAnnotations(item);
                return true;
			case MENU_GPSSTATUS_ID:
				org.androad.ui.util.Util.startUnknownActivity(this, "com.eclipsim.gpsstatus.VIEW", "com.eclipsim.gpsstatus");
				return true;
			case MENU_QUIT_ID:
				this.setResult(Constants.SUBACTIVITY_RESULTCODE_CHAINCLOSE_QUITTED);
				this.finish();
				return true;
			case MENU_PRELOAD_ID:
				showPreloadDialog();
				return true;
			case MENU_INPUTLATLON_ID:
				showDialog(DIALOG_INPUT_LAT_LON);
				return true;
			case MENU_SHOWLATLON_ID:
				showCenterLatLonDialog();
				return true;
			case MENU_VEHICLEREGISTRATIONPLATE_LOOKUP_ID:
				showDialog(DIALOG_SELECT_VEHICLEREGISTRATIONPLATE_LOOKUP_COUNTRIES);
				return true;
			case MENU_SUBMENU_TRAFFIC_CLEAR:
				this.mTrafficOverlay.setOverlayItems(null);
				return true;
			case MENU_SUBMENU_TRAFFIC_CUSTOM:
				try {
					final int feedCount = DBManager.getCustomTrafficFeedCount(this);

					if(feedCount > 0) {
						this.showDialog(DIALOG_SELECT_CUSTOM_TRAFFIC_FEED);
					} else {
						this.showDialog(DIALOG_ADD_CUSTOM_TRAFFIC_FEED);
					}
				} catch (final DataBaseException e) {
					// TODO ERROR MESSAGE!
				}
				return true;
			default:
				if(itemId >= MENU_SUBMENU_LAYERS_OFFSET){
					changeProviderInfo(TileSourceFactory.getTileSources().toArray(new ITileSource[0])[item.getItemId() - MENU_SUBMENU_LAYERS_OFFSET]);
					return true;
				}
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	protected void onPrepareDialog(final int id, final Dialog d) {
		switch(id){
			case DIALOG_SELECT_CUSTOM_TRAFFIC_FEED:
		}
	}

	@Override
	protected Dialog onCreateDialog(final int id) {
		switch(id){
			case DIALOG_SELECT_VEHICLEREGISTRATIONPLATE_LOOKUP_COUNTRIES:
				return CommonDialogFactory.createNationalitySelectionDialog(this, Country.getAllWithVRPTable(), new CommonCallbackAdapter<Country>(){
					@Override
					public void onSuccess(final Country result) {
						WhereAmIMap.this.mVehicleRegistrationPlateLOokupNationality = result;
						showDialog(DIALOG_INPUT_VEHICLEREGISTRATIONPLATE_LOOKUP);
					}
				});
			case DIALOG_INPUT_VEHICLEREGISTRATIONPLATE_LOOKUP:
				/* TODO proper msg and title. */
				return CommonDialogFactory.createInputDialog(this, R.string.dlg_input_vehicleregistrationplate_lookup_hint, R.string.dlg_input_vehicleregistrationplate_lookup_title, new CommonCallbackAdapter<String>(){
					@Override
					public void onSuccess(final String result) {
						final IVRPElement vrp = VRPRegistry.resolve(WhereAmIMap.this.mVehicleRegistrationPlateLOokupNationality.getVRPTableID(), result);
						if(vrp == null){
							Toast.makeText(WhereAmIMap.this, "Sorry, not found", Toast.LENGTH_LONG).show();
						}else{
							Toast.makeText(WhereAmIMap.this, "Found: " + vrp.getAbbreviation() + " = " + vrp.getRepresentation(), Toast.LENGTH_LONG).show();
						}
					}
				});
			case DIALOG_SELECT_CUSTOM_TRAFFIC_FEED:
				List<TrafficFeed> customTrafficFeeds;
				try {
					customTrafficFeeds = DBManager.getCustomTrafficFeeds(this);
				} catch (final DataBaseException e) {
					customTrafficFeeds = new ArrayList<TrafficFeed>();
				}
				return CommonDialogFactory.createSelectCustomTrafficFeedDialog(this, customTrafficFeeds, new CommonCallbackAdapter<TrafficFeed>(){
					@Override
					public void onSuccess(final TrafficFeed result) {
						if(result != null){
							receiveTrafficFromFeed(result);
						}else{
							showDialog(DIALOG_ADD_CUSTOM_TRAFFIC_FEED);
						}
					}
				});
			case DIALOG_ADD_CUSTOM_TRAFFIC_FEED:
				return CommonDialogFactory.createAddCustomTrafficFeedDialog(WhereAmIMap.this, new CommonCallbackAdapter<TrafficFeed>(){
					@Override
					public void onSuccess(final TrafficFeed result) {
						if(result != null){
							receiveTrafficFromFeed(result);
						}
					}
				});
			case DIALOG_INPUT_LAT_LON:
				return CommonDialogFactory.createInputLatLonDialog(this, new CommonCallback<GeoPoint>(){
					@Override
					public void onFailure(final Throwable t) {
						Toast.makeText(WhereAmIMap.this, R.string.dlg_input_direct_lat_lon_malformed, Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onSuccess(final GeoPoint result) {
						refreshPinOverlay(result);
					}
				});
			case DIALOG_SELECT_FREEFORM_OR_STRUCTURED_SEARCH:
				return CommonDialogFactory.createFreeformOrStructuredAddressSelectorDialog(this, new CommonCallbackAdapter<Integer>(){
					@Override
					public void onSuccess(final Integer result) {
						switch(result){
							case 0:
								handleSearchOpen();
								break;
							case 1:
								/* Load SDMainChoose-Activity. */
								final Intent sdIntent = new Intent(WhereAmIMap.this, SDMainChoose.class);

								final Bundle b = new Bundle();
								b.putInt(MODE_SD, MODE_SD_RESOLVE);

								sdIntent.putExtras(b);
								WhereAmIMap.this.startActivityForResult(sdIntent, REQUESTCODE_STRUCTURED_SEARCH_SD_MAINCHOOSE);
								break;
						}
					}
				});
			case DIALOG_INPUT_FAVORITE_NAME:
				return CommonDialogFactory.createInputFavoriteNameDialog(WhereAmIMap.this, new CommonCallback<String>(){
					@Override
					public void onFailure(final Throwable t) {
						Toast.makeText(WhereAmIMap.this, R.string.toast_error_adding_favorite, Toast.LENGTH_LONG).show();
					}

					@Override
					public void onSuccess(final String result) {
						try {
							DBManager.addFavorite(WhereAmIMap.this, result, WhereAmIMap.this.mGPLastMapClick.getLatitudeE6(), WhereAmIMap.this.mGPLastMapClick.getLongitudeE6());
						} catch (final DataBaseException e) {
							Toast.makeText(WhereAmIMap.this, R.string.toast_error_adding_favorite, Toast.LENGTH_LONG).show();
						}
					}
				});
            case DIALOG_SELECT_POI_OR_OSB_OR_FTPC:
                return CommonDialogFactory.createAddOSBorFTPCDialog(WhereAmIMap.this, new CommonCallbackAdapter<OSBMapLongAddSelectorResult>(){

					@Override
					public void onSuccess(final OSBMapLongAddSelectorResult result) {
						switch(result){
							case OSB:
								showDialog(DIALOG_INPUT_OSB_BUG);
								break;
							case FTPC:
								showAddFTPCDialog();
								break;
							case OSMPOI:
								showPOICategorySelectorActivity();
								break;
						}
					}
				});
			case DIALOG_INPUT_OSB_BUG:
				return CommonDialogFactory.createAddOSBBugDialog(this, new CommonCallbackAdapter<String>(){
					@Override
					public void onSuccess(final String result) {
                        mapAnnotationDB.addOsbBug(WhereAmIMap.this.mGPLastMapClick, result);
					}
				});
			case DIALOG_INPUT_OSM_POI:
				return CommonDialogFactory.createInputOSMPOIDialog(this, this.mAddOSMPOIType, new CommonCallback<String>(){
					@Override
					public void onSuccess(final String result) {
						// TODO Ensure mapcenter did not change
						if(result == null || result.length() == 0){
							onFailure(new OSMAPIException("Invalid name."));
						}else{
                            final GeoPoint mapCenter = WhereAmIMap.this.mGPLastMapClick;
                            final POIType poi = WhereAmIMap.this.mAddOSMPOIType;

                            Assert.assertNotNull(poi);
                            Assert.assertNotNull(mapCenter);
                            Assert.assertFalse(poi.POIGROUPS[0] == POIGroup.MAINGROUP);

                            mapAnnotationDB.addPoi(WhereAmIMap.this.mGPLastMapClick, WhereAmIMap.this.mAddOSMPOIType, result);
						}
					}
					@Override
					public void onFailure(final Throwable t) {
					}
				});
			default:
				return null;
		}
	}

	private void receiveTrafficFromFeed(final TrafficFeed pTrafficFeed){
		/* Check if we have the special case of the BBC TrafficFeed. */
		if(pTrafficFeed.getURL().compareToIgnoreCase(TUKSRequester.BBC_TRAFFICFEED_URL) == 0){
			this.mCurrentTrafficFeed = new TrafficFeed(TUKSRequester.BBC_TRAFFICFEED_URL, pTrafficFeed.getName(), Country.UNITEDKINGDOM);
		}else{
			this.mCurrentTrafficFeed = pTrafficFeed;
		}


		/* Check if there is a Nationality set. */
		if(this.mCurrentTrafficFeed.getNationality() != null){
			receiveTPEGMLTraffic(this.mCurrentTrafficFeed.getNationality().BOUNDINGBOXE6);
		}else{
			/* Get traffic for whole feed. (not BBox-filtering). */
			receiveTPEGMLTraffic(null);
		}
	}

	private void receiveTPEGMLTraffic(final BoundingBoxE6 bBox){
		/* Check if we have the special case of the BBC TrafficFeed. */
		if(this.mCurrentTrafficFeed.getURL().equals(TUKSRequester.BBC_TRAFFICFEED_URL)){
			receiveTrafficUKBBC(bBox);
		}else{

		}
	}

	private void receiveTrafficUKBBC(final BoundingBoxE6 bBox){
		new UserTask<Void, Void, ISpatialDataOrganizer<TrafficOverlayItem>>(){
			@Override
			public ISpatialDataOrganizer<TrafficOverlayItem> doInBackground(final Void... params) {
				try {
					Log.d(Constants.DEBUGTAG, "Before TUKS.");
					final List<TrafficItem> trafficItems = TUKSRequester.request(bBox);
					Log.d(Constants.DEBUGTAG, "Received TUKS.");

					Log.d(Constants.DEBUGTAG, "Filtering items. (Before: " + trafficItems.size() + ")");
					final List<TrafficOverlayItem> overlayItems = filterTrafficItemsToTrafficOverlayItems(trafficItems); // TODO vllt nur converten...

					Log.d(Constants.DEBUGTAG, "Building index. (Count:" + trafficItems.size() + ")");
					final ISpatialDataOrganizer<TrafficOverlayItem> trafficResult = new TrafficOverlayManager(overlayItems);
					trafficResult.buildIndex();
					Log.d(Constants.DEBUGTAG, "Built index.");

					return trafficResult;
				} catch (final Exception e) {
					Log.d(Constants.DEBUGTAG, "Builign index failed.");
					Exceptor.e("Error getting UK-Traffic.", e, WhereAmIMap.this);
					return null;
				}
			}

			@Override
			public void onPostExecute(final ISpatialDataOrganizer<TrafficOverlayItem> result) {
				if(result != null){
					WhereAmIMap.this.mTrafficOverlay.setSpacialIndexManager(result);
				}else{
					Toast.makeText(WhereAmIMap.this, "Sorry there was a problem receiving the Traffic-Feed.", Toast.LENGTH_LONG).show();
				}
			}
		}.execute();
	}

	/**
	 * Filters by Severity (Excludes: VERY_SLIGHT && SLIGHT)
	 * @return
	 */
	private List<TrafficOverlayItem> filterTrafficItemsToTrafficOverlayItems(final List<TrafficItem> trafficItems) {
		final List<TrafficOverlayItem> overlayItems = new ArrayList<TrafficOverlayItem>(trafficItems.size());
		for (final TrafficItem trafficItem : trafficItems){
			switch(trafficItem.getSeverity()){
				case VERY_SLIGHT:
				case SLIGHT:
					break;
				case UNKNOWN:
				case UNSPECIFIED:
				case MEDIUM:
				case SEVERE:
				case VERY_SEVERE:
					overlayItems.add(new TrafficOverlayItem(WhereAmIMap.this, trafficItem));
					break;
			}
		}

		return overlayItems;
	}

	@Override
	public void onLocationLost(final AndNavLocation pLocation) {
		// TODO anzeigen...
	}

	@Override
	public void onLocationChanged(final AndNavLocation pLocation) {
		if(this.mOSMapView == null || this.mMyLocationOverlay == null) {
			return;
		}

		if(pLocation != null){
			if(this.mMyLocationOverlay != null){
				this.mMyLocationOverlay.setLocation(pLocation);
				if(pLocation.hasBearing()) {
					this.mMyLocationOverlay.setBearing(pLocation.getBearing());
				}
				if(pLocation.hasHorizontalPositioningError()) {
					this.mMyLocationOverlay.setAccuracy(pLocation.getHorizontalPositioningError());
				}
			}
			if(this.mDoCenter == WhereAmIMap.CENTERMODE_AUTO && System.currentTimeMillis() > this.mAutoCenterBlockedUntil){
				this.mOSMapView.getController().setCenter(pLocation);
			}

			if(this.mStatisticsEnabled && this.mStatisticsManager != null) {
                final DecimalFormat df = new DecimalFormat("#,###,##0.0");
				this.mStatisticsManager.tick(pLocation);
                int speed = (int) this.mStatisticsManager.getCurrentSpeed();
                this.mIbtnSpeed.setText(""
                                        + df.format(us.mScaleToMetersPerSecond
                                        * speed)
                                        + us.mAbbrKilometersPerHourScale);
			}
		}
		this.mOSMapView.invalidate();
	}

	// ===========================================================
	// Methods
	// ===========================================================

	private void showAddFTPCDialog() {
		final LayoutInflater inflater = LayoutInflater.from(this);
		final FrameLayout fl = (FrameLayout)inflater.inflate(R.layout.dlg_osb_add_ftpc, null);

		final EditText etPostcode1 = (EditText)fl.findViewById(R.id.et_dlg_osb_add_ftpc_postcode1);
		final EditText etPostcode2 = (EditText)fl.findViewById(R.id.et_dlg_osb_add_ftpc_postcode2);

		etPostcode1.setSelectAllOnFocus(true);
		etPostcode2.setSelectAllOnFocus(true);

		new AlertDialog.Builder(this)
		.setView(fl)
		.setTitle(R.string.dlg_osb_add_ftpc_title)
		.setPositiveButton(R.string.save, new DialogInterface.OnClickListener(){
			@Override
			public void onClick(final DialogInterface d, final int which) {
				final String postcode1 = etPostcode1.getText().toString();
				final String postcode2 = etPostcode2.getText().toString();
                mapAnnotationDB.addFtpc(WhereAmIMap.this.mGPLastMapClick, postcode1, postcode2);
			}
		})
		.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener(){
			@Override
			public void onClick(final DialogInterface d, final int which) {
				d.dismiss();
			}
		}).create().show();
	}

	protected void showPOICategorySelectorActivity() {
		final Intent i = new Intent(this, POICategorySelector.class);
		startActivityForResult(i, REQUESTCODE_POICATEGORYSELECTOR);
	}

    private void showPoi(final MenuItem item) {
        if (item.isChecked()) {
            item.setChecked(false);
        } else {
            item.setChecked(true);
        }
        showOverlayPoi = item.isChecked();

        updatePoi();
    }

    private void showFoxyTag(final MenuItem item) {
        if (item.isChecked()) {
            item.setChecked(false);
        } else {
            item.setChecked(true);
        }
        showOverlayFoxyTag = item.isChecked();

        updateFoxyTag();
    }

    private void showFavorite(final MenuItem item) {
        if (item.isChecked()) {
            item.setChecked(false);
        } else {
            item.setChecked(true);
        }
        showOverlayFavorite = item.isChecked();

        updateFavorite();
    }

    private void showOsmBug(final MenuItem item) {
        if (item.isChecked()) {
            item.setChecked(false);
        } else {
            item.setChecked(true);
        }
        showOverlayOsmBug = item.isChecked();

        updateOsmBug();
    }

    private void showMapAnnotations(final MenuItem item) {
        if (item.isChecked()) {
            item.setChecked(false);
        } else {
            item.setChecked(true);
        }
        showOverlayMapAnnotations = item.isChecked();

        updateMapAnnotations();
    }

    public void updateLayers() {
        updatePoi();
        updateFoxyTag();
        updateFavorite();
        updateOsmBug();
        updateMapAnnotations();
    }

    private void updatePoi() {
        final List<CircleItem> pois = WhereAmIMap.this.mPOIOverlay.getCircleItems();
        if (pois.size() > 0) {
            pois.clear();
        }

        if (showOverlayPoi &&
            this.mOSMapView.getZoomLevel() > 11) {
            new Thread(new Runnable(){
                    @Override
                    public void run() {
                        for (final DBPOI poi : DBManager.getPOIs(WhereAmIMap.this, WhereAmIMap.this.mOSMapView.getBoundingBox())) {
                            pois.add(new CircleItem(poi, WhereAmIMap.this, Color.BLUE, poi.getName()));
                        }
                    }
                }, "POI-Runner").start();
        }
    }

	private void updateFoxyTag() {
        final List<CircleItem> ff = WhereAmIMap.this.mFFOverlay.getCircleItems();
        if (ff.size() > 0) {
            ff.clear();
        }

        if (showOverlayFoxyTag &&
            this.mOSMapView.getZoomLevel() > 11) {
            new Thread(new Runnable(){
                    @Override
                    public void run() {
                        try {
                            for (final FoxyTagPoint fpp : FoxyTagRequester.request(WhereAmIMap.this, WhereAmIMap.this.mOSMapView.getMapCenter())) {
                                if (WhereAmIMap.this.mOSMapView.getBoundingBox().contains(fpp.getCenter()))
                                    ff.add(fpp);
                            }
                        } catch (final Exception e) {
                            Log.e(Constants.DEBUGTAG, "FoxyTagRequester-Error", e);
                        }
                    }
                }, "FoxyTag-Runner").start();
        }
    }

    private void updateFavorite() {
        final List<BitmapItem> fs = WhereAmIMap.this.mFavoriteOverlay.getBitmapItems();
        if (fs.size() > 0) {
            fs.clear();
        }

        if (showOverlayFavorite &&
            this.mOSMapView.getZoomLevel() > 11) {
            new Thread(new Runnable(){
                    @Override
                    public void run() {
                        try {
                            for (final Favorite fp : DBManager.getFavorites(WhereAmIMap.this, WhereAmIMap.this.mOSMapView.getBoundingBox())) {
                                fs.add(new FavoritePoint(fp, WhereAmIMap.this));
                            }
                        } catch (final DataBaseException e) {
                            Log.e(Constants.DEBUGTAG, "Error on loading Favorites", e);
                        }
                    }
                }, "Favorite-Runner").start();
        }
    }

	private void updateOsmBug() {
        final List<CircleItem> bugs = WhereAmIMap.this.mOsmBugOverlay.getCircleItems();
        if (bugs.size() > 0) {
            bugs.clear();
        }

        if (showOverlayOsmBug &&
            this.mOSMapView.getZoomLevel() > 11) {
            new Thread(new Runnable(){
                    @Override
                    public void run() {
                        try {
                            for (final OpenStreetBug bug : OSBRequester.getBugsFromBoundingBoxE6(WhereAmIMap.this.mOSMapView.getBoundingBox())) {
                                bugs.add(new OsmBugPoint(bug, WhereAmIMap.this));
                            }
                        } catch (final Exception e) {
                            Log.e(Constants.DEBUGTAG, "OSBRequester-Error", e);
                        }
                    }
                }, "OsmBug-Runner").start();
        }
    }

	private void updateMapAnnotations() {
        final List<CircleItem> mapannotations = WhereAmIMap.this.mMapAnnotationsOverlay.getCircleItems();
        if (mapannotations.size() > 0) {
            mapannotations.clear();
        }

        if (showOverlayMapAnnotations &&
            this.mOSMapView.getZoomLevel() > 11) {
            new Thread(new Runnable(){
                    @Override
                    public void run() {
                        for (final DBPOI poi : mapAnnotationDB.getAll(WhereAmIMap.this.mOSMapView.getBoundingBox())) {
                            mapannotations.add(new CircleItem(poi, WhereAmIMap.this, Color.YELLOW, poi.getName()));
                        }
                    }
                }, "MapAnnotations-Runner").start();
        }
    }

	private void showCenterLatLonDialog() {
		final GeoPoint mapCenter = WhereAmIMap.this.mOSMapView.getMapCenter();
		new AlertDialog.Builder(this)
		.setIcon(R.drawable.world)
		.setTitle(R.string.coordinates)
		.setMessage(getString(R.string.maps_menu_getcentetcoordinates_message, mapCenter.getLatitudeE6() / 1E6, mapCenter.getLongitudeE6() / 1E6))
		.setNeutralButton(R.string.clipboard, new DialogInterface.OnClickListener(){
			@Override
			public void onClick(final DialogInterface d, final int which) {
				final ClipboardManager cb = (ClipboardManager) WhereAmIMap.this.getSystemService(Context.CLIPBOARD_SERVICE);

				final String clipboardText = String.format("%.6f %.6f",mapCenter.getLatitudeE6() / 1E6, mapCenter.getLongitudeE6() / 1E6);
				cb.setText(clipboardText);
				d.dismiss();
			}
		})
		.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener(){
			@Override
			public void onClick(final DialogInterface d, final int which) {
				d.dismiss();
			}
		})
		.create().show();
	}

	private void showPreloadDialog() {
		final MapTileProviderBase providerInfo = this.mOSMapView.getTileProvider();

		final String[] zoomLevelsRaw = getResources().getStringArray(R.array.preloader_rectangle_zoomlevels);
		final String[] zoomLevelsForThisRenderer = new String[Math.min(providerInfo.getMaximumZoomLevel() + 1, zoomLevelsRaw.length)];
		for(int i = 0; i < zoomLevelsForThisRenderer.length; i++) {
			zoomLevelsForThisRenderer[i] = (zoomLevelsRaw[i] != null) ? zoomLevelsRaw[i] : "" + i;
		}

		new AlertDialog.Builder(this).setSingleChoiceItems(zoomLevelsForThisRenderer, 12, new DialogInterface.OnClickListener(){
			@Override
			public void onClick(final DialogInterface d, final int which) {
				d.dismiss();
				// which is the zoomLevel
				preloadMapTilesUpToZoomLevel(which);
			}
		}).setTitle(R.string.dlg_preloader_rect_maxzoom_title)
		.create().show();
	}

	private void preloadMapTilesUpToZoomLevel(final int pUptoZoomLevel) {
		final MapTileProviderBase providerInfo = this.mOSMapView.getTileProvider();

		/* For each zoomLevel, get the tiles, that hit the visible Rectangle. */
		final BoundingBoxE6 bbE6Visible = this.mOSMapView.getBoundingBox();
		final ArrayList<MapTile> tilesNeeded = new ArrayList<MapTile>();
		for(int i = 0; i <= pUptoZoomLevel; i++) {
			Util.calculateNeededTilesForZoomLevelInBoundingBox(tilesNeeded, i, bbE6Visible);
		}


		/* Calculate the number of tiles to download. */
		final long tileCount = tilesNeeded.size();

		/* Calculate the needed size. */
		final long bytesEpectedNeeded = tileCount * providerInfo.getTileSource().getTileSizePixels() * 71;
		final String formattedFileSize = FileSizeFormatter.formatFileSize(bytesEpectedNeeded);

		final AlertDialog.Builder ab = new AlertDialog.Builder(this)
		.setTitle(R.string.dlg_preloader_rect_title)
		.setMessage(String.format(getString(R.string.dlg_preloader_rect_message), tileCount , formattedFileSize));

		if(pUptoZoomLevel > 0){
			ab.setNeutralButton(R.string.dlg_preloader_rect_reducezoom, new DialogInterface.OnClickListener(){
				@Override
				public void onClick(final DialogInterface d, final int which) {
					d.dismiss();
					preloadMapTilesUpToZoomLevel(pUptoZoomLevel - 1);
				}
			});
		}

		ab.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener(){
			@Override
			public void onClick(final DialogInterface d, final int which) {
				d.dismiss();
				final String progressMessage = getString(R.string.pdg_preloader_message);
				final ProgressDialog pd = ProgressDialog.show(WhereAmIMap.this, getString(R.string.pdg_preloader_title), String.format(progressMessage, 0, tileCount), true, true);
				final OSMMapTilePreloader preloader = new OSMMapTilePreloader(WhereAmIMap.this, providerInfo.getTileSource(), tilesNeeded);
				preloader.setHandler(new Handler(){
					@Override
					public void handleMessage(Message msg) {
						try {
							int progress = preloader.getProgress();
							int total = preloader.getTotal();
							if(progress < total)
								pd.setMessage(String.format(progressMessage, progress, total));
							else
								pd.dismiss();
						} catch (final Exception e) {
							Log.e(Constants.DEBUGTAG, "View error", e);
						}
					}
				});
				new Thread(preloader).start();
			}
		});
		ab.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener(){
			@Override
			public void onClick(final DialogInterface d, final int which) {
				d.dismiss();
			}
		}).show();
	}

	private void openWeatherDialog(final GeoPoint pGeoPoint) {
		final Intent getWeatherIntent = new Intent(this, WeatherForecast.class);

		getWeatherIntent.putExtra(WeatherForecast.WEATHERQUERY_GEOPOINTSTRING_ID, pGeoPoint.toString());

		startActivityForResult(getWeatherIntent, REQUESTCODE_WEATHER);
	}

	private void showAccessibilityAnalysisDialog(final MenuItem item) {
		final String minute = " " + getString(R.string.minute);
		final String minutes = " " + getString(R.string.minutes);

		final int[] minuteValues = getResources().getIntArray(R.array.accessibility_minutes);
		final String[] minuteStrings = new String[minuteValues.length];

		minuteStrings[0] = minuteValues[0] + " " + minute;
		for (int i = 1; i < minuteValues.length; i++) {
			minuteStrings[i] = minuteValues[i] + " " + minutes;
		}

		new AlertDialog.Builder(this).setSingleChoiceItems(minuteStrings, 4, new DialogInterface.OnClickListener(){
			@Override
			public void onClick(final DialogInterface d, final int which) {
				d.dismiss();
				item.setEnabled(false); // Disable AAS

				final int minuteToResolve = minuteValues[which];
				Toast.makeText(WhereAmIMap.this, R.string.please_wait_a_moment, Toast.LENGTH_LONG).show();
				new Thread(new Runnable(){
					@Override
					public void run() {
						try {
							final AASResponse aasr = AASRequester.request(WhereAmIMap.this.mOSMapView.getMapCenter(), minuteToResolve);
							final List<AreaOfInterest> areas = WhereAmIMap.this.mAASOverlay.getAreasOfInterest();
							areas.clear();
							for(final Polygon p : aasr.getPolygons()) {
								areas.add(p);
							}

							runOnUiThread(new Runnable(){
								@Override
								public void run() {
									Toast.makeText(WhereAmIMap.this, R.string.toast_map_accessibilityanalysis_finished, Toast.LENGTH_LONG).show();
								}
							});

						} catch(final ORSException orse){
							runOnUiThread(new Runnable(){
								@Override
								public void run() {
									Toast.makeText(WhereAmIMap.this, orse.getErrors().get(0).toUserString(), Toast.LENGTH_LONG).show();
								}
							});
						} catch (final Exception e) {
							Log.e(Constants.DEBUGTAG, "AASRequester-Error", e);
						} finally {
							item.setEnabled(true); // Enable AAS
						}
					}
				}, "Accessibility-Runner").start();
			}
		}).create().show();
	}

	private void applyAutoCompleteListeners() {
		try{
			final List<DBPOI> usedPOIs = DBManager.getPOIHistory(this);

			final ArrayList<String> usedPOIStrings = new ArrayList<String>(usedPOIs.size());

			for(final DBPOI poi : usedPOIs) {
				usedPOIStrings.add(poi.getName());
			}

			new InlineAutoCompleterConstant(this.mEtSearch, usedPOIStrings, false){
				@Override
				public boolean onEnter() {
					handleSearchSubmit(WhereAmIMap.this.mEtSearch.getText().toString());
					return true;
				}
			};
		} catch (final DataBaseException e) {
			//			Log.e(DEBUGTAG, "Error on loading POIs", e);
		}
	}

	private void applyMapViewLongPressListener() {
		final GestureDetector gd = new GestureDetector(new GestureDetector.SimpleOnGestureListener(){
			@Override
			public void onLongPress(final MotionEvent e) {
				final Projection pj = WhereAmIMap.this.mOSMapView.getProjection();
				WhereAmIMap.this.mGPLastMapClick = pj.fromPixels((int)e.getX(), (int)e.getY());

				final String[] items = new String[]{
						getString(R.string.tv_whereami_contextmenu_nav_here),
						getString(R.string.tv_whereami_contextmenu_add_as_favorite),
						getString(R.string.tv_whereami_contextmenu_show_radar),
						getString(R.string.tv_whereami_contextmenu_weather_get),
                        getString(R.string.tv_whereami_contextmenu_osb),
						getString(R.string.tv_whereami_contextmenu_close)};
				new AlertDialog.Builder(WhereAmIMap.this)
				.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener(){

					@Override
					public void onClick(final DialogInterface d, final int which) {
						d.dismiss();
						switch(which){
							case 0:
								doNavToGeoPoint(WhereAmIMap.this.mGPLastMapClick);
								break;
							case 1:
								showDialog(DIALOG_INPUT_FAVORITE_NAME);
								break;
							case 2:
								final Intent i = new Intent("com.google.android.radar.SHOW_RADAR");
								i.putExtra("latitude", (float)WhereAmIMap.this.mGPLastMapClick.getLatitudeE6() / 1E6);
								i.putExtra("longitude", (float)WhereAmIMap.this.mGPLastMapClick.getLongitudeE6() / 1E6);
								org.androad.ui.util.Util.startUnknownActivity(WhereAmIMap.this, i, "com.google.android.radar");
								break;
							case 3:
								openWeatherDialog(WhereAmIMap.this.mGPLastMapClick);
								break;
                            case 4:
                                showDialog(DIALOG_SELECT_POI_OR_OSB_OR_FTPC);
                                break;
							case 5:
								return;
						}
					}
				})
				.create().show();
			}
		});
		this.mOSMapView.setOnTouchListener(new OnTouchListener(){
			@Override
			public boolean onTouch(final View v, final MotionEvent ev) {
				if(WhereAmIMap.this.mNavPointsCrosshairMode){
					return false;
				}else{
					WhereAmIMap.this.mAutoCenterBlockedUntil = System.currentTimeMillis() + AUTOCENTER_BLOCKTIME;

					if(ev.getAction() == MotionEvent.ACTION_DOWN) {
						startDelayedHideControlsAnimation();
					}

					return gd.onTouchEvent(ev);
				}
			}
		});
	}

	private void applyZoomButtonListeners(){
		this.findViewById(R.id.iv_whereami_zoomin).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(final View v) {
				WhereAmIMap.this.mOSMapView.getController().zoomIn();
				WhereAmIMap.this.mOSMapView.invalidate();
			}
		});
		this.findViewById(R.id.iv_whereami_zoomout).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(final View v) {
				WhereAmIMap.this.mOSMapView.getController().zoomOut();
				WhereAmIMap.this.mOSMapView.invalidate();
			}
		});
	}

	private void applyQuickButtonListeners() {
		this.mMapItemControlView.setItemizedOverlayControlViewListener(new ItemizedOverlayControlView.ItemizedOverlayControlViewListener(){
			@Override
			public void onCenter() {
				final OverlayItem oi = WhereAmIMap.this.mSearchPinList.get(WhereAmIMap.this.mSearchPinListIndex);
				WhereAmIMap.this.mOSMapView.getController().animateTo(oi.getPoint(), AnimationType.MIDDLEPEAKSPEED);
			}

			@Override
			public void onNavTo() {
				final GeoPoint gp = WhereAmIMap.this.mSearchPinList.get(WhereAmIMap.this.mSearchPinListIndex).getPoint();

				final String aPOIName = WhereAmIMap.this.mEtSearch.getText().toString();
				try {
					DBManager.addPOIToHistory(WhereAmIMap.this, aPOIName, gp.getLatitudeE6(), gp.getLongitudeE6());
				} catch (final DataBaseException e) {
					//					Log.e(DEBUGTAG, "Error adding POI", e);
				}

				doNavToGeoPoint(gp);
			}

			@Override
			public void onNext() {
				WhereAmIMap.this.mSearchPinListIndex++;
				WhereAmIMap.this.mSearchPinListIndex = WhereAmIMap.this.mSearchPinListIndex % WhereAmIMap.this.mSearchPinList.size();
				final OverlayItem oi = WhereAmIMap.this.mSearchPinList.get(WhereAmIMap.this.mSearchPinListIndex);
				WhereAmIMap.this.mOSMapView.getController().animateTo(oi.getPoint(), AnimationType.MIDDLEPEAKSPEED);
			}

			@Override
			public void onPrevious() {
				if(WhereAmIMap.this.mSearchPinListIndex == 0) {
					WhereAmIMap.this.mSearchPinListIndex = WhereAmIMap.this.mSearchPinList.size() - 1;
				} else {
					WhereAmIMap.this.mSearchPinListIndex--;
				}

				final OverlayItem oi = WhereAmIMap.this.mSearchPinList.get(WhereAmIMap.this.mSearchPinListIndex);
				WhereAmIMap.this.mOSMapView.getController().animateTo(oi.getPoint(), AnimationType.MIDDLEPEAKSPEED);
			}
		});

		/* Left side. */
		this.mIbtnNavPointsSetStart.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(final View v) {
				/* Set destination-flag and start crosshair-mode. */
				WhereAmIMap.this.mStartFlagItem.setCenter(WhereAmIMap.this.mOSMapView.getMapCenter());
				updateUIForNavPointsCrosshairMode(true);
			}
		});


		/* Left side. */
		this.mIbtnNavPointsSetDestination.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(final View v) {
				/* Set destination-flag and start crosshair-mode. */
				WhereAmIMap.this.mDestinationFlagItem.setCenter(WhereAmIMap.this.mOSMapView.getMapCenter());
				updateUIForNavPointsCrosshairMode(true);
			}
		});

		/* Left side. */
		this.mIbtnNavPointsDoCancel.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(final View v) {
				/* End crosshair-mode. */
				updateUIForNavPointsCrosshairMode(false);
			}
		});

		this.mIbtnNavPointsDoStart.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(final View arg0) {
				if(WhereAmIMap.this.mNavPointsCrosshairMode){
					/* User chose a good start+destination. */
                    if (WhereAmIMap.this.mStartFlagItem.getCenter() != null) {
                        doNavBetweenGeoPoints(WhereAmIMap.this.mStartFlagItem.getCenter(), WhereAmIMap.this.mDestinationFlagItem.getCenter());
                    } else {
                        doNavToGeoPoint(WhereAmIMap.this.mDestinationFlagItem.getCenter());
                    }
					/* End crosshair-mode. */
					updateUIForNavPointsCrosshairMode(false);
				}else{
					/* Disable Auto-Follow. */
					updateUIForAutoCenterChange(WhereAmIMap.CENTERMODE_NONE);

					WhereAmIMap.this.mDestinationFlagItem.setCenter(null);
					WhereAmIMap.this.mStartFlagItem.setCenter(null);
					updateUIForNavPointsCrosshairMode(true);
				}
			}
		});

		this.mEtSearch.setOnKeyListener(new OnKeyListener(){
			@Override
			public boolean onKey(final View v, final int keyCode, final KeyEvent event) {
				if(event.getAction() == KeyEvent.ACTION_UP){
					switch(event.getKeyCode()){
						case KeyEvent.KEYCODE_DPAD_CENTER:
						case KeyEvent.KEYCODE_ENTER:
						case KeyEvent.KEYCODE_CALL:
							handleSearchSubmit(WhereAmIMap.this.mEtSearch.getText().toString());
							return true;
					}
				}
				return false;
			}
		});

		this.mIbtnSearch.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(final View arg0) {
				if(WhereAmIMap.this.mEtSearch.getVisibility() == View.GONE){
					showDialog(DIALOG_SELECT_FREEFORM_OR_STRUCTURED_SEARCH);
					//					handleSearchOpen(); <-- Would directly open the edittext
				}else{
					handleSearchSubmit(WhereAmIMap.this.mEtSearch.getText().toString());
				}
			}
		});

		/* Right side. */
		this.mIbtnChooseRenderer.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(final View arg0) {
				startDelayedHideControlsAnimation();
				final ITileSource[] providers = TileSourceFactory.getTileSources().toArray(new ITileSource[0]);

				final SpannableString[] renderersNames = new SpannableString[providers.length];

				for(int j = 0; j < providers.length; j ++){
					final SpannableString itemTitle = new SpannableString(providers[j].name());
					itemTitle.setSpan(new StyleSpan(Typeface.ITALIC), providers[j].name().length(), itemTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					itemTitle.setSpan(new RelativeSizeSpan(0.5f), providers[j].name().length(), itemTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

					renderersNames[j] = itemTitle;
				}

				final int curRendererIndex = WhereAmIMap.this.mOSMapView.getTileProvider().getTileSource().ordinal();

				new AlertDialog.Builder(WhereAmIMap.this)
				.setTitle(R.string.maps_menu_submenu_renderers)
				.setSingleChoiceItems(renderersNames, curRendererIndex , new DialogInterface.OnClickListener(){
					@Override
					public void onClick(final DialogInterface d, final int which) {
						changeProviderInfo(providers[which]);
						d.dismiss();
					}
				}).create().show();
			}
		});

		this.mIbtnWhereAmI.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(final View arg0) {
				startDelayedHideControlsAnimation();
				Toast.makeText(WhereAmIMap.this, R.string.please_wait_a_moment, Toast.LENGTH_SHORT).show();

				final GeoPoint mapCenter = WhereAmIMap.this.mOSMapView.getMapCenter();
				final UnitSystem us = Preferences.getUnitSystem(WhereAmIMap.this);

				new Thread(new Runnable(){
					@Override
					public void run() {
						try {
                            final LUSRequester lus = Preferences.getORSServer(WhereAmIMap.this).LOCATIONUTILITYSERVICE;
							final ArrayList<GeocodedAddress> addr = lus.requestReverseGeocode(WhereAmIMap.this, mapCenter, ReverseGeocodePreferenceType.STREETADDRESS);
							runOnUiThread(new Runnable(){
								@Override
								public void run() {
									if(addr == null || addr.size() == 0){
										Toast.makeText(WhereAmIMap.this, R.string.whereami_location_not_resolvable, Toast.LENGTH_SHORT).show();
									}else{
										final GeocodedAddress closestGeocodedAddress = addr.get(0);
										final String msg = closestGeocodedAddress.toString(WhereAmIMap.this, us, true);

										Drawable icon;
										try{
											icon = getResources().getDrawable(closestGeocodedAddress.getNationality().FLAGRESID);
										}catch(final Exception e){
											icon = getResources().getDrawable(R.drawable.questionmark);
										}

										new AlertDialog.Builder(WhereAmIMap.this)
										.setTitle(closestGeocodedAddress.getNationality().NAMERESID)
										.setMessage(msg + '\n')
										.setIcon(icon)
										.setPositiveButton(R.string.ok, null)
										.create().show();
									}
								}
							});
						} catch (final ORSException e) {
							runOnUiThread(new Runnable(){
								@Override
								public void run() {
									Toast.makeText(WhereAmIMap.this, e.getErrors().get(0).toUserString(), Toast.LENGTH_LONG).show();
								}
							});
						} catch (final Exception e) {
							Exceptor.e("LUSRequester", e);
						}
					}
				}).start();
				/* Invalidate map. */
				WhereAmIMap.this.mOSMapView.invalidate();
			}
		});

		this.mIbtnCenter.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(final View arg0) {
				startDelayedHideControlsAnimation();
				final int newMode = (WhereAmIMap.this.mDoCenter + 1) % 3;

				final GeoPoint lastKnownLocationAsGeoPoint = WhereAmIMap.this.getLastKnownLocation(true);
				if(lastKnownLocationAsGeoPoint != null){
					switch(newMode){
						case CENTERMODE_AUTO:
						case CENTERMODE_ONCE:
							WhereAmIMap.this.mOSMapView.getController().animateTo(lastKnownLocationAsGeoPoint, AnimationType.MIDDLEPEAKSPEED);
							break;
					}
				}

				updateUIForAutoCenterChange(newMode);
				/* Invalidate map. */
				WhereAmIMap.this.mOSMapView.invalidate();
			}
		});
	}

	private void updateUIForNavPointsCrosshairMode(final boolean pNewState) {
		this.mCrosshairOverlay.setEnabled(pNewState);
		this.mNavPointsCrosshairMode = pNewState;

		if(pNewState){
			this.mIbtnNavPointsDoCancel.setVisibility(View.VISIBLE);
			this.mIbtnNavPointsSetDestination.setVisibility(View.VISIBLE);
			this.mIbtnNavPointsSetStart.setVisibility(View.VISIBLE);

			this.mIbtnNavPointsDoCancel.clearAnimation();
			this.mIbtnNavPointsDoStart.clearAnimation();
			this.mIbtnNavPointsSetStart.clearAnimation();
			this.mIbtnNavPointsSetDestination.clearAnimation();
			this.mIbtnCenter.clearAnimation();

			final boolean destinationSet = this.mDestinationFlagItem.getCenter() != null;
			this.mIbtnNavPointsDoStart.setEnabled(destinationSet);
            final GeoPoint start;
            if (WhereAmIMap.this.mStartFlagItem.getCenter() != null)
                start = WhereAmIMap.this.mStartFlagItem.getCenter();
            else
                start = WhereAmIMap.this.mMyLocationOverlay.getLocation();
			this.mNavPointsConnectionLineOverlay.setFrom(start);
			this.mNavPointsConnectionLineOverlay.setTo(this.mDestinationFlagItem.getCenter());
			this.mNavPointsConnectionLineOverlay.setEnabled(destinationSet);
		}else{
			startDelayedHideControlsAnimation();

			this.mIbtnNavPointsDoCancel.setVisibility(View.GONE);
			this.mIbtnNavPointsSetDestination.setVisibility(View.GONE);
			this.mIbtnNavPointsSetStart.setVisibility(View.GONE);
			this.mIbtnNavPointsDoStart.startAnimation(this.mFadeToLeftAnimation);
			this.mIbtnCenter.startAnimation(this.mFadeToRightAnimation);

			this.mIbtnNavPointsDoStart.setEnabled(true);
			this.mStartFlagItem.setCenter(null);
			this.mDestinationFlagItem.setCenter(null);
			this.mNavPointsConnectionLineOverlay.setEnabled(false);
		}

		this.mOSMapView.invalidate();
	}

	private void doNavToGeoPoint(final GeoPoint gp) {
		final Intent navTo = new Intent(Constants.ANDROAD_NAV_ACTION);

		final Bundle b = new Bundle();
		b.putString("to", gp.toDoubleString()); // i.e.: "to" --> "37.465259,-122.126456"
		navTo.putExtras(b);

		sendBroadcast(navTo);
	}

	private void doNavBetweenGeoPoints(final GeoPoint pGPStart, final GeoPoint pGPDestination) {
		final Intent navTo = new Intent(Constants.ANDROAD_NAV_ACTION);

		final Bundle b = new Bundle();
		b.putString("from", pGPStart.toDoubleString()); // i.e.: "to" --> "37.465259,-122.126456"
		b.putString("to", pGPDestination.toDoubleString()); // i.e.: "to" --> "37.465259,-122.126456"
		navTo.putExtras(b);

		sendBroadcast(navTo);
	}

	private void changeProviderInfo(final ITileSource aProviderInfo) {
		/* Remember changes to the provider to start the next time with the same provider. */
		Preferences.saveMapViewProviderInfoWhereAmI(this, aProviderInfo);

		/* Check if Auto-Follow has to be disabled. */
        this.mOSMapView.setTileSource(aProviderInfo);
	}

	private void updateUIForAutoCenterChange(final int pNewMode) {
		if(WhereAmIMap.this.mDoCenter == pNewMode) {
			return;
		}

		WhereAmIMap.this.mDoCenter = pNewMode;

		if(WhereAmIMap.this.mDoCenter == WhereAmIMap.CENTERMODE_AUTO){
			WhereAmIMap.this.mIbtnCenter.setImageResource(R.drawable.person_focused_small);
			Toast.makeText(WhereAmIMap.this, R.string.toast_autofollow_enabled, Toast.LENGTH_SHORT).show();
		}else if(WhereAmIMap.this.mDoCenter == WhereAmIMap.CENTERMODE_ONCE){
			WhereAmIMap.this.mIbtnCenter.setImageResource(R.drawable.person_focused_once_small);
			Toast.makeText(WhereAmIMap.this, R.string.toast_autofollow_once, Toast.LENGTH_SHORT).show();
		}else if(WhereAmIMap.this.mDoCenter == WhereAmIMap.CENTERMODE_NONE){
			WhereAmIMap.this.mIbtnCenter.setImageResource(R.drawable.person_small);
			Toast.makeText(WhereAmIMap.this, R.string.toast_autofollow_disabled, Toast.LENGTH_SHORT).show();
		}
	}

	private void handleSearchOpen() {
		this.mIbtnSearch.setImageResource(R.drawable.search_submit);
		this.mIbtnSearch.clearAnimation();
		this.mEtSearch.setVisibility(View.VISIBLE);
		/* And Focus on the */
		this.mEtSearch.requestFocus();
		this.mEtSearch.selectAll();
		this.mOSMapView.invalidate();
		WhereAmIMap.this.clearPinOverlay();
	}

	private void handleSearchSubmit(final String pQuery) {
		final String query = pQuery.trim();

		this.mIbtnSearch.setImageResource(R.drawable.search);
		this.mEtSearch.setVisibility(View.GONE);

		this.mIbtnSearch.startAnimation(this.mFadeOutQuickAnimation);

		if(query.length() > 0){

			/* Check if coordinates were entered. */
			final GeoPoint coordsIfEntered = CoordinatesExtractor.match(query);
			if(coordsIfEntered != null){
				refreshPinOverlay(coordsIfEntered);
			}else if(RouteHandleIDExtractor.match(query) != -1){
				startDDMapWithRouteHandleID(RouteHandleIDExtractor.match(query));
			}else{
				/* No coords --> textual/freeform search. */
				//			final String[] choices = new String[]{getString(R.string.whereami_search_scope_global), getString(R.string.whereami_search_scope_local)};
				//			new AlertDialog.Builder(this)
				//			.setTitle(R.string.whereami_search_scope_title)
				//			.setCancelable(true)
				//			.setSingleChoiceItems(choices, 0, new DialogInterface.OnClickListener(){
				//				@Override
				//				public void onClick(final DialogInterface dialog, final int which) {
				//					dialog.dismiss();
				Toast.makeText(WhereAmIMap.this, R.string.please_wait_a_moment, Toast.LENGTH_SHORT).show();
				searchORSLocations(query);
				//					searchLocations(query, 0); // which
				//				}
				//			}).create().show();
			}
		}
	}

	private void startDDMapWithRouteHandleID(final long pRouteHandleID) {
		final Intent directIntent = new Intent(this, OpenStreetDDMap.class);
		final Bundle b = new Bundle();
		b.putInt(EXTRAS_MODE, EXTRAS_MODE_LOAD_ROUTE_BY_ROUTEHANDLEID);

		b.putLong(EXTRAS_ROUTEHANDLEID_ID, pRouteHandleID);

		directIntent.putExtras(b);
		this.startActivityForResult(directIntent, REQUESTCODE_DDMAP);
	}

	private void searchORSLocations(final String query) {
		new Thread(new Runnable(){
			@Override
			public void run() {
				try {
                    final LUSRequester lus = Preferences.getORSServer(WhereAmIMap.this).LOCATIONUTILITYSERVICE;
					final ArrayList<GeocodedAddress> ret = lus.requestFreeformAddress(WhereAmIMap.this, null, query);

					runOnUiThread(new Runnable(){
						@Override
						public void run() {
							if(ret == null || ret.size() == 0){
								Toast.makeText(WhereAmIMap.this, R.string.whereami_search_no_places_found, Toast.LENGTH_SHORT).show();
							}else{

								final BoundingBoxE6 bBox = BoundingBoxE6.fromGeoPoints(ret);

								/* Disable Auto-Follow. */
								if(WhereAmIMap.this.mDoCenter == WhereAmIMap.CENTERMODE_AUTO) {
									updateUIForAutoCenterChange(WhereAmIMap.CENTERMODE_NONE);
								}


								final ArrayList<OverlayItem> itemsFound = new ArrayList<OverlayItem>();
								for (final GeocodedAddress ga : ret) {
									itemsFound.add(new OverlayItem(ga.getMunicipality(), ga.toString(WhereAmIMap.this), ga));
								}

								final int foundItemsSize = ret.size();
								if(foundItemsSize == 1){
									WhereAmIMap.this.mOSMapView.getController().setZoom(15);
									WhereAmIMap.this.mOSMapView.getController().animateTo(bBox.getCenter(), AnimationType.MIDDLEPEAKSPEED);
								}else{
									WhereAmIMap.this.mOSMapView.getController().zoomToSpan(bBox);
									WhereAmIMap.this.mOSMapView.getController().animateTo(bBox.getCenter(), AnimationType.MIDDLEPEAKSPEED);
								}

								WhereAmIMap.this.refreshPinOverlay(itemsFound);
								Log.d(Constants.DEBUGTAG, "Items remained: " + foundItemsSize);
								Toast.makeText(WhereAmIMap.this, getString(R.string.whereami_search_places_found) + " " + foundItemsSize, Toast.LENGTH_SHORT).show();
							}
						}
					});

				} catch (final ORSException e) {
					runOnUiThread(new Runnable(){
						@Override
						public void run() {
							WhereAmIMap.this.clearPinOverlay();
						}
					});
				} catch (final Exception e) {
					runOnUiThread(new Runnable(){
						@Override
						public void run() {
							WhereAmIMap.this.clearPinOverlay();
						}
					});
					Exceptor.e("GeocodeError", e, WhereAmIMap.this);
				}

			}
		}).start();
	}

	@Override
	public void onDataStateChanged(final int strength) {
		// TODO
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	private class AndRoadMapListener implements MapListener {
        private WhereAmIMap map;

        AndRoadMapListener(WhereAmIMap map) {
            this.map = map;
        }

        @Override
        public boolean onScroll(ScrollEvent e) {
            map.updateLayers();
            return true;
        }

		@Override
		public boolean onZoom(ZoomEvent e) {
            map.updateLayers();
            return true;
		}
	}


}
