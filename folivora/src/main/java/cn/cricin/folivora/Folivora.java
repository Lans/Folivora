/*
 * Copyright (C) 2019 Cricin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.cricin.folivora;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class Folivora {
  private static final String TAG = "Folivora";

  private static final int DRAWABLE_TYPE_SHAPE = 0;
  private static final int DRAWABLE_TYPE_SELECTOR = 1;
  private static final int DRAWABLE_TYPE_LAYER = 2;
  private static final int DRAWABLE_TYPE_RIPPLE = 3;

  private static final int SET_AS_BACKGROUND = 0;
  private static final int SET_AS_SRC = 1;

  private static final int[] STATE_FIRST = {android.R.attr.state_first};
  private static final int[] STATE_MIDDLE = {android.R.attr.state_middle};
  private static final int[] STATE_LAST = {android.R.attr.state_last};
  private static final int[] STATE_ACTIVE = {android.R.attr.state_active};
  private static final int[] STATE_ACTIVATED = {android.R.attr.state_activated};
  private static final int[] STATE_ACCELERATE = {android.R.attr.state_accelerated};
  private static final int[] STATE_CHECKED = {android.R.attr.state_checked};
  private static final int[] STATE_CHECKABLE = {android.R.attr.state_checkable};
  private static final int[] STATE_ENABLED = {android.R.attr.state_enabled};
  private static final int[] STATE_FOCUSED = {android.R.attr.state_focused};
  private static final int[] STATE_PRESSED = {android.R.attr.state_pressed};
  private static final int[] STATE_NORMAL = {};

  public interface RippleFallback {
    /*Nullable*/
    Drawable onFallback(ColorStateList color/*Nonnull*/, Drawable content/*Nullable*/,
                        Drawable mask/*Nullable*/, Context ctx/*Nonnull*/);
  }

  @SuppressWarnings("unused")
  private static Appendable sOut;//for ui preview debug purpose
  private static RippleFallback sRippleFallback;

  private static GradientDrawable newShape(Context ctx, AttributeSet attrs) {
    GradientDrawable gd = new GradientDrawable();
    TypedArray a = ctx.obtainStyledAttributes(attrs, R.styleable.Folivora_Shape);
    gd.setShape(a.getInt(R.styleable.Folivora_Shape_shapeType, GradientDrawable.RECTANGLE));

    final int size = a.getDimensionPixelSize(R.styleable.Folivora_Shape_shapeSolidSize, -1);
    gd.setSize(a.getDimensionPixelSize(R.styleable.Folivora_Shape_shapeSolidWidth, size),
      a.getDimensionPixelSize(R.styleable.Folivora_Shape_shapeSolidHeight, size));

    gd.setGradientType(a.getInt(R.styleable.Folivora_Shape_shapeGradientType, 0));
    gd.setGradientRadius(a.getDimension(R.styleable.Folivora_Shape_shapeGradientRadius, 0));
    gd.setGradientCenter(a.getDimension(R.styleable.Folivora_Shape_shapeGradientCenterX, 0),
      a.getDimension(R.styleable.Folivora_Shape_shapeGradientCenterY, 0));
    gd.setColors(new int[]{
      a.getColor(R.styleable.Folivora_Shape_shapeGradientStartColor, 0),
      a.getColor(R.styleable.Folivora_Shape_shapeGradientCenterColor, 0),
      a.getColor(R.styleable.Folivora_Shape_shapeGradientEndColor, 0)
    });
    final int orientationIndex = a.getInt(R.styleable.Folivora_Shape_shapeGradientAngle, 0);
    gd.setOrientation(GradientDrawable.Orientation.values()[orientationIndex]);

    if (a.hasValue(R.styleable.Folivora_Shape_shapeSolidColor)) {
      gd.setColor(a.getColor(R.styleable.Folivora_Shape_shapeSolidColor, Color.WHITE));
    }

    gd.setStroke(
      a.getDimensionPixelSize(R.styleable.Folivora_Shape_shapeStrokeWidth, -1),
      a.getColor(R.styleable.Folivora_Shape_shapeStrokeColor, Color.WHITE),
      a.getDimensionPixelSize(R.styleable.Folivora_Shape_shapeStokeDashGap, 0),
      a.getDimensionPixelSize(R.styleable.Folivora_Shape_shapeStokeDashWidth, 0)
    );
    final float radius = a.getDimension(R.styleable.Folivora_Shape_shapeCornerRadius, 0);
    gd.setCornerRadii(new float[]{
      a.getDimension(R.styleable.Folivora_Shape_shapeCornerRadiusTopLeft, radius),
      a.getDimension(R.styleable.Folivora_Shape_shapeCornerRadiusTopLeft, radius),
      a.getDimension(R.styleable.Folivora_Shape_shapeCornerRadiusTopRight, radius),
      a.getDimension(R.styleable.Folivora_Shape_shapeCornerRadiusTopRight, radius),
      a.getDimension(R.styleable.Folivora_Shape_shapeCornerRadiusBottomRight, radius),
      a.getDimension(R.styleable.Folivora_Shape_shapeCornerRadiusBottomRight, radius),
      a.getDimension(R.styleable.Folivora_Shape_shapeCornerRadiusBottomLeft, radius),
      a.getDimension(R.styleable.Folivora_Shape_shapeCornerRadiusBottomLeft, radius),
    });
    a.recycle();
    return gd;
  }

  private static StateListDrawable newSelector(Context ctx, AttributeSet attrs) {
    StateListDrawable d = new StateListDrawable();
    TypedArray a = ctx.obtainStyledAttributes(attrs, R.styleable.Folivora_Selector);
    Drawable temp = a.getDrawable(R.styleable.Folivora_Selector_selectorStateFirst);
    if (temp != null) d.addState(STATE_FIRST, temp);
    temp = a.getDrawable(R.styleable.Folivora_Selector_selectorStateMiddle);
    if (temp != null) d.addState(STATE_MIDDLE, temp);
    temp = a.getDrawable(R.styleable.Folivora_Selector_selectorStateLast);
    if (temp != null) d.addState(STATE_LAST, temp);
    temp = a.getDrawable(R.styleable.Folivora_Selector_selectorStateActive);
    if (temp != null) d.addState(STATE_ACTIVE, temp);
    temp = a.getDrawable(R.styleable.Folivora_Selector_selectorStateActivated);
    if (temp != null) d.addState(STATE_ACTIVATED, temp);
    temp = a.getDrawable(R.styleable.Folivora_Selector_selectorStateAccelerate);
    if (temp != null) d.addState(STATE_ACCELERATE, temp);
    temp = a.getDrawable(R.styleable.Folivora_Selector_selectorStateChecked);
    if (temp != null) d.addState(STATE_CHECKED, temp);
    temp = a.getDrawable(R.styleable.Folivora_Selector_selectorStateCheckable);
    if (temp != null) d.addState(STATE_CHECKABLE, temp);
    temp = a.getDrawable(R.styleable.Folivora_Selector_selectorStateEnabled);
    if (temp != null) d.addState(STATE_ENABLED, temp);
    temp = a.getDrawable(R.styleable.Folivora_Selector_selectorStateFocused);
    if (temp != null) d.addState(STATE_FOCUSED, temp);
    temp = a.getDrawable(R.styleable.Folivora_Selector_selectorStatePressed);
    if (temp != null) d.addState(STATE_PRESSED, temp);
    temp = a.getDrawable(R.styleable.Folivora_Selector_selectorStateNormal);
    if (temp != null) d.addState(STATE_NORMAL, temp);
    a.recycle();
    return d;
  }

  private static Drawable newLayer(Context ctx, AttributeSet attrs) {
    TypedArray a = ctx.obtainStyledAttributes(attrs, R.styleable.Folivora_Layer);
    List<Drawable> childDrawables = new ArrayList<>(5);
    List<Rect> childInsets = new ArrayList<>(5);

    Drawable temp = a.getDrawable(R.styleable.Folivora_Layer_layerItem0Drawable);
    if (temp != null) {
      childDrawables.add(temp);
      int insets = a.getDimensionPixelSize(R.styleable.Folivora_Layer_layerItem0Insets, 0);
      childInsets.add(new Rect(
        a.getDimensionPixelSize(R.styleable.Folivora_Layer_layerItem0Left, insets),
        a.getDimensionPixelSize(R.styleable.Folivora_Layer_layerItem0Top, insets),
        a.getDimensionPixelSize(R.styleable.Folivora_Layer_layerItem0Right, insets),
        a.getDimensionPixelSize(R.styleable.Folivora_Layer_layerItem0Bottom, insets)
      ));
    }
    temp = a.getDrawable(R.styleable.Folivora_Layer_layerItem1Drawable);
    if (temp != null) {
      childDrawables.add(temp);
      int insets = a.getDimensionPixelSize(R.styleable.Folivora_Layer_layerItem1Insets, 0);
      childInsets.add(new Rect(
        a.getDimensionPixelSize(R.styleable.Folivora_Layer_layerItem1Left, insets),
        a.getDimensionPixelSize(R.styleable.Folivora_Layer_layerItem1Top, insets),
        a.getDimensionPixelSize(R.styleable.Folivora_Layer_layerItem1Right, insets),
        a.getDimensionPixelSize(R.styleable.Folivora_Layer_layerItem1Bottom, insets)
      ));
    }
    temp = a.getDrawable(R.styleable.Folivora_Layer_layerItem2Drawable);
    if (temp != null) {
      childDrawables.add(temp);
      int insets = a.getDimensionPixelSize(R.styleable.Folivora_Layer_layerItem2Insets, 0);
      childInsets.add(new Rect(
        a.getDimensionPixelSize(R.styleable.Folivora_Layer_layerItem2Left, insets),
        a.getDimensionPixelSize(R.styleable.Folivora_Layer_layerItem2Top, insets),
        a.getDimensionPixelSize(R.styleable.Folivora_Layer_layerItem2Right, insets),
        a.getDimensionPixelSize(R.styleable.Folivora_Layer_layerItem2Bottom, insets)
      ));
    }
    temp = a.getDrawable(R.styleable.Folivora_Layer_layerItem3Drawable);
    if (temp != null) {
      childDrawables.add(temp);
      int insets = a.getDimensionPixelSize(R.styleable.Folivora_Layer_layerItem3Insets, 0);
      childInsets.add(new Rect(
        a.getDimensionPixelSize(R.styleable.Folivora_Layer_layerItem3Left, insets),
        a.getDimensionPixelSize(R.styleable.Folivora_Layer_layerItem3Top, insets),
        a.getDimensionPixelSize(R.styleable.Folivora_Layer_layerItem3Right, insets),
        a.getDimensionPixelSize(R.styleable.Folivora_Layer_layerItem3Bottom, insets)
      ));
    }
    temp = a.getDrawable(R.styleable.Folivora_Layer_layerItem4Drawable);
    if (temp != null) {
      childDrawables.add(temp);
      int insets = a.getDimensionPixelSize(R.styleable.Folivora_Layer_layerItem4Insets, 0);
      childInsets.add(new Rect(
        a.getDimensionPixelSize(R.styleable.Folivora_Layer_layerItem4Left, insets),
        a.getDimensionPixelSize(R.styleable.Folivora_Layer_layerItem4Top, insets),
        a.getDimensionPixelSize(R.styleable.Folivora_Layer_layerItem4Right, insets),
        a.getDimensionPixelSize(R.styleable.Folivora_Layer_layerItem4Bottom, insets)
      ));
    }
    a.recycle();
    LayerDrawable d = new LayerDrawable(childDrawables.toArray(new Drawable[0]));
    for (int i = 0; i < childInsets.size(); i++) {
      Rect inset = childInsets.get(i);
      d.setLayerInset(i, inset.left, inset.top, inset.right, inset.bottom);
    }
    return d;
  }

  private static Drawable newRipple(Context ctx, AttributeSet attrs) {
    TypedArray a = ctx.obtainStyledAttributes(attrs, R.styleable.Folivora_Ripple);
    ColorStateList color;
    Drawable content;
    Drawable mask;
    try {
      color = a.getColorStateList(R.styleable.Folivora_Ripple_rippleColor);
      if (color == null) {
        throw new IllegalStateException("rippleColor not set");
      }
      content = a.getDrawable(R.styleable.Folivora_Ripple_rippleContent);
      mask = a.getDrawable(R.styleable.Folivora_Ripple_rippleMask);
    } finally {
      a.recycle();
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      return new RippleDrawable(color, content, mask);
    } else if (sRippleFallback != null) {
      return sRippleFallback.onFallback(color, content, mask, ctx);
    } else {
      Log.e(TAG, "RippleDrawable is not available in current platform");
      return null;
    }
  }

  private static Drawable newDrawable(int drawableType, Context ctx, AttributeSet attrs) {
    Drawable result = null;
    switch (drawableType) {
      case DRAWABLE_TYPE_SHAPE:
        result = newShape(ctx, attrs);
        break;
      case DRAWABLE_TYPE_SELECTOR:
        result = newSelector(ctx, attrs);
        break;
      case DRAWABLE_TYPE_LAYER:
        result = newLayer(ctx, attrs);
        break;
      case DRAWABLE_TYPE_RIPPLE:
        result = newRipple(ctx, attrs);
        break;
      default:
        Log.w(TAG, "Unexpected drawableType: " + drawableType);
        break;
    }
    return result;
  }

  static void applyDrawableToView(View view, AttributeSet attrs) {
    TypedArray a = view.getContext().obtainStyledAttributes(attrs, R.styleable.Folivora);
    int drawableType = a.getInt(R.styleable.Folivora_drawableType, -1);
    int setAs = a.getInt(R.styleable.Folivora_setAs, SET_AS_BACKGROUND);
    a.recycle();
    if (drawableType < 0 || setAs < 0) return;

    if (sOut != null) {
      try {
        sOut
          .append("Folivora: ")
          .append(view.getClass().getSimpleName())
          .append(" { drawableType: ")
          .append(drawableTypeToString(drawableType))
          .append(" setAs: ")
          .append(setAsToString(setAs))
          .append(" }\n");
      } catch (IOException e) {
        //never happen
      }
    }

    Drawable d = newDrawable(drawableType, view.getContext(), attrs);
    if (d == null) return;
    if (setAs == 1 && view instanceof ImageView) {
      ((ImageView) view).setImageDrawable(d);
    } else {
      view.setBackground(d);
    }
  }

  private static String drawableTypeToString(int drawableType) {
    String result;
    switch (drawableType) {
      case DRAWABLE_TYPE_SHAPE:
        result = "shape";
        break;
      case DRAWABLE_TYPE_SELECTOR:
        result = "selector";
        break;
      case DRAWABLE_TYPE_LAYER:
        result = "layer-list";
        break;
      case DRAWABLE_TYPE_RIPPLE:
        result = "ripple";
        break;
      default:
        result = "unknown";
        break;
    }
    return result;
  }

  private static String setAsToString(int setAs) {
    String result;
    switch (setAs) {
      case SET_AS_BACKGROUND:
        result = "background";
        break;
      case SET_AS_SRC:
        result = "src";
        break;
      default:
        result = "unknown";
        break;
    }
    return result;
  }

  public static void installViewFactory(Context ctx) {
    LayoutInflater inflater = LayoutInflater.from(ctx);
    LayoutInflater.Factory2 factory2 = inflater.getFactory2();
    if (factory2 instanceof FolivoraViewFactory) return;
    FolivoraViewFactory mine = new FolivoraViewFactory();
    mine.mFactory2 = factory2;
    if (factory2 != null) {
      FolivoraViewFactory.forceSetFactory2(inflater, mine);
    } else {
      inflater.setFactory2(mine);
    }
  }

  public static Context wrap(final Context newBase) {
    final LayoutInflater inflater = LayoutInflater.from(newBase);
    if (inflater instanceof FolivoraInflater) return newBase;
    return new ContextWrapper(newBase) {
      private FolivoraInflater mInflater;

      @Override
      public Object getSystemService(String name) {
        if (LAYOUT_INFLATER_SERVICE.equals(name)) {
          if (mInflater == null) {
            mInflater = new FolivoraInflater(newBase, inflater);
          }
          return mInflater;
        }
        return super.getSystemService(name);
      }
    };
  }

  public static void setRippleFallback(RippleFallback fallback) {
    sRippleFallback = fallback;
  }
}
