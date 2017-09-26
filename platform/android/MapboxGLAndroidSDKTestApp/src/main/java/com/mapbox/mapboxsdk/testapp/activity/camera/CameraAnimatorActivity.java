package com.mapbox.mapboxsdk.testapp.activity.camera;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.v4.util.LongSparseArray;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;

import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.testapp.R;

/**
 * Test activity showcasing using Android SDK animators to animate camera position changes.
 */
public class CameraAnimatorActivity extends AppCompatActivity implements OnMapReadyCallback {

  private static final double ANIMATION_DELAY_FACTOR = 1.5;
  private static final LatLng START_LAT_LNG = new LatLng(37.787947, -122.407432);

  private final LongSparseArray<Animator> animatorMap = new LongSparseArray<>();

  private MapView mapView;
  private MapboxMap mapboxMap;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_camera_animator);
    initAnimatorMap();

    mapView = (MapView) findViewById(R.id.mapView);
    if (mapView != null) {
      mapView.onCreate(savedInstanceState);
      mapView.getMapAsync(this);
    }
  }

  @Override
  public void onMapReady(final MapboxMap map) {
    mapboxMap = map;
    initFab();
  }

  private void initFab() {
    findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        view.setVisibility(View.GONE);

        CameraPosition animatedPosition = new CameraPosition.Builder()
          .target(new LatLng(37.789992, -122.402214))
          .tilt(60)
          .zoom(14.5f)
          .bearing(135)
          .build();

        createExampleAnimator(mapboxMap.getCameraPosition(), animatedPosition).start();
      }
    });
  }

  //
  // Animator API used for the animation on the FAB
  //

  private void initAnimatorMap() {
    AnimatorSet animatorSet = new AnimatorSet();
    animatorSet.playTogether(
      createLatLngAnimator(START_LAT_LNG, new LatLng(37.826715, -122.422795)),
      createExampleInterpolator(new FastOutSlowInInterpolator(), 2500)
    );
    animatorMap.put(R.id.menu_action_accelerate_decelerate_interpolator, animatorSet);

    AnimatorSet bounceAnimatorSet = new AnimatorSet();
    bounceAnimatorSet.playTogether(
      createLatLngAnimator(START_LAT_LNG, new LatLng(37.787947, -122.407432)),
      createExampleInterpolator(new BounceInterpolator(), 3750)
    );
    animatorMap.put(R.id.menu_action_bounce_interpolator, bounceAnimatorSet);

    animatorMap.put(R.id.menu_action_anticipate_overshoot_interpolator,
      createExampleInterpolator(new AnticipateOvershootInterpolator(), 2500)
    );

    animatorMap.put(R.id.menu_action_path_interpolator,
      createExampleInterpolator(
        PathInterpolatorCompat.create(.22f, .68f, 0, 1.71f), 2500)
    );
  }

  private Animator createExampleAnimator(CameraPosition currentPosition, CameraPosition targetPosition) {
    AnimatorSet animatorSet = new AnimatorSet();
    animatorSet.play(createLatLngAnimator(currentPosition.target, targetPosition.target));
    animatorSet.play(createZoomAnimator(currentPosition.zoom, targetPosition.zoom));
    animatorSet.play(createBearingAnimator(currentPosition.bearing, targetPosition.bearing));
    animatorSet.play(createTiltAnimator(currentPosition.tilt, targetPosition.tilt));
    return animatorSet;
  }

  private Animator createLatLngAnimator(LatLng currentPosition, LatLng targetPosition) {
    ValueAnimator latLngAnimator = ValueAnimator.ofObject(new LatLngEvaluator(), currentPosition, targetPosition);
    latLngAnimator.setDuration((long) (1000 * ANIMATION_DELAY_FACTOR));
    latLngAnimator.setInterpolator(new FastOutSlowInInterpolator());
    latLngAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        mapboxMap.setLatLng((LatLng) animation.getAnimatedValue());
      }
    });
    return latLngAnimator;
  }

  private Animator createZoomAnimator(double currentZoom, double targetZoom) {
    ValueAnimator zoomAnimator = ValueAnimator.ofFloat((float) currentZoom, (float) targetZoom);
    zoomAnimator.setDuration((long) (2200 * ANIMATION_DELAY_FACTOR));
    zoomAnimator.setStartDelay((long) (600 * ANIMATION_DELAY_FACTOR));
    zoomAnimator.setInterpolator(new AnticipateOvershootInterpolator());
    zoomAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        mapboxMap.setZoom((Float) animation.getAnimatedValue());
      }
    });
    return zoomAnimator;
  }

  private Animator createBearingAnimator(double currentBearing, double targetBearing) {
    ValueAnimator bearingAnimator = ValueAnimator.ofFloat((float) currentBearing, (float) targetBearing);
    bearingAnimator.setDuration((long) (1000 * ANIMATION_DELAY_FACTOR));
    bearingAnimator.setStartDelay((long) (1000 * ANIMATION_DELAY_FACTOR));
    bearingAnimator.setInterpolator(new FastOutLinearInInterpolator());
    bearingAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        mapboxMap.setBearing((Float) animation.getAnimatedValue());
      }
    });
    return bearingAnimator;
  }

  private Animator createTiltAnimator(double currentTilt, double targetTilt) {
    ValueAnimator tiltAnimator = ValueAnimator.ofFloat((float) currentTilt, (float) targetTilt);
    tiltAnimator.setDuration((long) (1000 * ANIMATION_DELAY_FACTOR));
    tiltAnimator.setStartDelay((long) (1500 * ANIMATION_DELAY_FACTOR));
    tiltAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        mapboxMap.setTilt((Float) animation.getAnimatedValue());
      }
    });
    return tiltAnimator;
  }

  //
  // Interpolator examples
  //

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_animator, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (mapboxMap == null) {
      return false;
    }
    findViewById(R.id.fab).setVisibility(View.GONE);
    resetCameraPosition();
    playAnimation(item.getItemId());
    return super.onOptionsItemSelected(item);
  }

  private void resetCameraPosition() {
    mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(
      new CameraPosition.Builder()
        .target(START_LAT_LNG)
        .zoom(11)
        .bearing(0)
        .tilt(0)
        .build()
    ));
  }

  private void playAnimation(int itemId) {
    Animator animator = createExamepleInterpolator(itemId);
    if (animator != null) {
      animator.start();
    }
  }

  private Animator createExamepleInterpolator(int menuItemId) {
    switch (menuItemId) {
      case R.id.menu_action_accelerate_decelerate_interpolator:
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(
          createLatLngAnimator(START_LAT_LNG, new LatLng(37.826715, -122.422795)),
          createExampleInterpolator(new FastOutSlowInInterpolator(), 2500));
        return animatorSet;
      case R.id.menu_action_bounce_interpolator:
        AnimatorSet bounceAnimatorSet = new AnimatorSet();
        bounceAnimatorSet.playTogether(
          createLatLngAnimator(START_LAT_LNG, new LatLng(37.787947, -122.407432)),
          createExampleInterpolator(new BounceInterpolator(), 3750)
        );
        return bounceAnimatorSet;
      case R.id.menu_action_anticipate_overshoot_interpolator:
        return createExampleInterpolator(new AnticipateOvershootInterpolator(), 2500);
      case R.id.menu_action_path_interpolator:
        return createExampleInterpolator(PathInterpolatorCompat.create(.22f, .68f, 0, 1.71f), 2500);
    }
    return null;
  }

  private Animator createExampleInterpolator(Interpolator interpolator, long duration) {
    ValueAnimator zoomAnimator = ValueAnimator.ofFloat(11.0f, 16.0f);
    zoomAnimator.setDuration((long) (duration * ANIMATION_DELAY_FACTOR));
    zoomAnimator.setInterpolator(interpolator);
    zoomAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        mapboxMap.setZoom((Float) animation.getAnimatedValue());
      }
    });
    return zoomAnimator;
  }

  //
  // MapView lifecycle
  //

  @Override
  protected void onStart() {
    super.onStart();
    mapView.onStart();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mapView.onResume();
  }

  @Override
  protected void onPause() {
    super.onPause();
    mapView.onPause();
  }

  @Override
  protected void onStop() {
    super.onStop();
    mapView.onStop();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mapView.onDestroy();
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    mapView.onLowMemory();
  }

  /**
   * Helper class to evaluate LatLng objects with a ValueAnimator
   */
  private static class LatLngEvaluator implements TypeEvaluator<LatLng> {

    private final LatLng latLng = new LatLng();

    @Override
    public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
      latLng.setLatitude(startValue.getLatitude()
        + ((endValue.getLatitude() - startValue.getLatitude()) * fraction));
      latLng.setLongitude(startValue.getLongitude()
        + ((endValue.getLongitude() - startValue.getLongitude()) * fraction));
      return latLng;
    }
  }
}
