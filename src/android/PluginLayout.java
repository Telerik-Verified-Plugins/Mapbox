package com.telerik.plugins.mapbox;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.view.View;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build.VERSION;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

//todo handle multimap
//todo change _mapRect to a Hash to store link a _mapRect to a map id
//todo rename vars which reference to a map use to decouple this class
//todo store set of HTMLs in a Map

public class PluginLayout extends FrameLayout  {
    private View _webView;
    private ViewGroup _root;
    private RectF _mapRect = new RectF();
    private Context _context;
    private FrontLayerLayout _frontLayer;
    private ScrollView _scrollView = null;
    private FrameLayout _scrollFrameLayout = null;
    private View _backgroundView = null;
    private TouchableWrapper _touchableWrapper;
    private ViewGroup _mapsGroup = null;
    private boolean _isScrolling = false;
    private ViewGroup.LayoutParams _orgLayoutParams = null;
    private boolean _isDebug = false;
    private boolean _isClickable = true;
    private Map<String, RectF> _HTMLNodes = new HashMap<String, RectF>();
    private Activity _activity = null;

    public ScrollView getScrollView(){return _scrollView;}

    @SuppressLint("NewApi")
    public PluginLayout(View webView, Activity activity) {
        super(webView.getContext());
        _activity = activity;
        _webView = webView;
        _root = (ViewGroup) _webView.getParent();
        _context = _webView.getContext();
        if (VERSION.SDK_INT >= 21 || "org.xwalk.core.XWalkView".equals(_webView.getClass().getName())) {
            _webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }
        _frontLayer = new FrontLayerLayout(_context);

        _scrollView = new ScrollView(_context);
        _scrollView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        _backgroundView = new View(_context);
        _backgroundView.setBackgroundColor(Color.WHITE);
        _backgroundView.setVerticalScrollBarEnabled(false);
        _backgroundView.setHorizontalScrollBarEnabled(false);
        _backgroundView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        _scrollFrameLayout = new FrameLayout(_context);
        _scrollFrameLayout.addView(_backgroundView);
        _scrollFrameLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        _scrollView.setHorizontalScrollBarEnabled(false);
        _scrollView.setVerticalScrollBarEnabled(false);

        this._touchableWrapper = new TouchableWrapper(_context);

    }

    public void setHTMLElement(String domId, float left, float top, float right, float bottom) {
        RectF rect = null;
        if (this._HTMLNodes.containsKey(domId)) {
            rect = this._HTMLNodes.get(domId);
        } else {
            rect = new RectF();
        }
        rect.left = left;
        rect.top = top;
        rect.right = right;
        rect.bottom = bottom;
        this._HTMLNodes.put(domId, rect);
        if (_isDebug) {
            this.inValidate();
        }
    }
    public void deleteHTMLElement(String domId) {
        this._HTMLNodes.remove(domId);
        if (_isDebug) {
            this.inValidate();
        }
    }
    public void clearHTMLElement() {
        this._HTMLNodes.clear();
        if (_isDebug) {
            this.inValidate();
        }
    }

    public void setClickable(boolean clickable) {
        this._isClickable = clickable;
        if (_isDebug) {
            this.inValidate();
        }
    }

    //todo compute width height to handle multiple maps
    public void updateViewPosition() {
        if (_mapsGroup == null) {
            return;
        }
        ViewGroup.LayoutParams lParams = _mapsGroup.getLayoutParams();
        int scrollY = _webView.getScrollY();
        int scrollX = _webView.getScrollX();

        if (lParams instanceof AbsoluteLayout.LayoutParams) {
            AbsoluteLayout.LayoutParams params = (AbsoluteLayout.LayoutParams) lParams;
            params.width = (int) _mapRect.width();
            params.height = (int) _mapRect.height();
            params.x = (int) _mapRect.left - scrollX;
            params.y = (int) _mapRect.top + scrollY;
            _mapsGroup.setLayoutParams(params);
        } else if (lParams instanceof LinearLayout.LayoutParams) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) lParams;
            params.width = (int) _mapRect.width();
            params.height = (int) _mapRect.height();
            params.topMargin = (int) _mapRect.top + scrollY;
            params.leftMargin = (int) _mapRect.left - scrollX;;
            _mapsGroup.setLayoutParams(params);
        } else if (lParams instanceof FrameLayout.LayoutParams) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) lParams;
            params.width = (int) _mapRect.width();
            params.height = (int) _mapRect.height();
            params.topMargin = (int) _mapRect.top + scrollY;
            params.leftMargin = (int) _mapRect.left - scrollX;;
            params.gravity = Gravity.TOP;
            _mapsGroup.setLayoutParams(params);
        }
        if (android.os.Build.VERSION.SDK_INT < 11) {
            // Force redraw
            _mapsGroup.requestLayout(); //todo watch this line if nothing is resized
        }
        _frontLayer.invalidate();
    }

    public void setDebug(boolean debug) {
        _isDebug = debug;
        if (_isDebug) {
            inValidate();
        }
    }

    /**
     * Add a map rectangle to the drawing zone
     */
    public void setMapDrawingRect(int id, RectF mapRect) {
        _setDrawingZone(mapRect);
    }

    private void _setDrawingZone(RectF mapRect){
        _mapRect = mapRect;

        if (_isDebug) {
            this.inValidate();
        }
    }

    public void detachMapsGroup() {
        if (_mapsGroup == null) {
            return;
        }
        _root.removeView(this);
        this.removeView(_frontLayer);
        _frontLayer.removeView(_webView);

        _scrollFrameLayout.removeView(_mapsGroup);
        _mapsGroup.removeView(this._touchableWrapper);

        this.removeView(_scrollView);
        _scrollView.removeView(_scrollFrameLayout);
        if (_orgLayoutParams != null) {
            _mapsGroup.setLayoutParams(_orgLayoutParams);
        }

        _root.addView(_webView);
        _mapsGroup = null;
        _activity.getWindow().getDecorView().requestFocus();
        _webView.setBackgroundColor(Color.WHITE);
    }

    /**
     * Don't care about scroll distance, just place mapsGroup in the right layer.
     * @param mapsGroup the ViewGroup embedding the maps
     */
    public void attachMapsGroup(ViewGroup mapsGroup) {
        _webView.setBackgroundColor(Color.TRANSPARENT);
        if("org.xwalk.core.XWalkView".equals(_webView.getClass().getName())
                || "org.crosswalk.engine.XWalkCordovaView".equals(_webView.getClass().getName())) {
            try {
                /* _webView.setZOrderOnTop(true)
                 * Called just in time as with _root.setBackground(...) the color
                 * come in front and take the whole screen */
                _webView.getClass().getMethod("setZOrderOnTop", boolean.class).invoke(_webView, true);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        _scrollView.setHorizontalScrollBarEnabled(false);
        _scrollView.setVerticalScrollBarEnabled(false);

        _scrollView.scrollTo(_webView.getScrollX(), _webView.getScrollY());
        if (_mapsGroup == mapsGroup) {
            return;
        } else {
            detachMapsGroup();
        }
        _mapsGroup = mapsGroup;

        /* Get all the current sub views of this view controller and pass it under the Plugin Layer.
         * Then, all user touch will be first intercepted by the plugin layer
         * which will decide whether or not it is a map action.
         */
        ViewGroup.LayoutParams lParams = _mapsGroup.getLayoutParams();
        _orgLayoutParams = null;
        if (lParams != null) {
            _orgLayoutParams = new ViewGroup.LayoutParams(lParams);
        }
        _root.removeView(_webView);
        _scrollView.addView(_scrollFrameLayout);
        this.addView(_scrollView);

        mapsGroup.addView(this._touchableWrapper);
        _scrollFrameLayout.addView(mapsGroup);

        _frontLayer.addView(_webView);
        this.addView(_frontLayer);
        _root.addView(this);
        _activity.getWindow().getDecorView().requestFocus();

        _scrollView.setHorizontalScrollBarEnabled(true);
        _scrollView.setVerticalScrollBarEnabled(true);
    }

    public void setPageSize(int width, int height) {
        android.view.ViewGroup.LayoutParams lParams = _backgroundView.getLayoutParams();
        lParams.width = width;
        lParams.height = height;
        _backgroundView.setLayoutParams(lParams);
    }

    public void scrollTo(int x, int y){
        _scrollView.scrollTo(x, y);
    }

    public void setBackgroundColor(int color) {
        _backgroundView.setBackgroundColor(color);
    }

    public void inValidate() {
        _frontLayer.invalidate();
    }


    private class FrontLayerLayout extends FrameLayout {

        public FrontLayerLayout(Context _context) {
            super(_context);
            setWillNotDraw(false);
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent event) {
            if (!_isClickable || _mapsGroup == null || _mapsGroup.getVisibility() != View.VISIBLE) {
                _webView.requestFocus(View.FOCUS_DOWN);
                return false;
            }
            int x = (int)event.getX();
            int y = (int)event.getY();
            int scrollX = _webView.getScrollX();
            int scrollY = _webView.getScrollY();
            boolean contains = _mapRect.contains(x, y);
            int action = event.getAction();
            _isScrolling = ((!contains) && (action == MotionEvent.ACTION_DOWN)) || _isScrolling;
            _isScrolling = action != MotionEvent.ACTION_UP && _isScrolling;
            contains = !_isScrolling && contains;

            if (contains) {
                // Is the touch point on any HTML elements?
                Set<Entry<String, RectF>> elements = PluginLayout.this._HTMLNodes.entrySet();
                Iterator<Entry<String, RectF>> iterator = elements.iterator();
                Entry <String, RectF> entry;
                RectF rect;

                while(iterator.hasNext() && contains) {
                    entry = iterator.next();
                    rect = entry.getValue();
                    rect.left -= scrollX;
                    rect.right -= scrollX;
                    rect.top -= scrollY;
                    rect.bottom -= scrollY;
                    if (entry.getValue().contains(x, y)) {
                        contains = false;
                    }
                    rect.left += scrollX;
                    rect.right += scrollX;
                    rect.top += scrollY;
                    rect.bottom += scrollY;
                }
            }

            if (!contains) {
                _webView.requestFocus(View.FOCUS_DOWN);
            }

            return contains;
        }
        @Override
        protected void onDraw(Canvas canvas) {
            if (_mapRect == null || !_isDebug) {
                return;
            }
            int width = canvas.getWidth();
            int height = canvas.getHeight();
            int scrollX = _webView.getScrollX();
            int scrollY = _webView.getScrollY();

            Paint paint = new Paint();
            paint.setColor(Color.argb(100, 0, 255, 0));
            if (!_isClickable) {
                canvas.drawRect(0f, 0f, width, height, paint);
                return;
            }

            canvas.drawRect(_mapRect.left, _mapRect.top, _mapRect.right, _mapRect.bottom, paint);

            paint.setColor(Color.argb(100, 255, 0, 0));

            Set<Entry<String, RectF>> elements = PluginLayout.this._HTMLNodes.entrySet();
            Iterator<Entry<String, RectF>> iterator = elements.iterator();
            Entry <String, RectF> entry;
            RectF rect;
            while(iterator.hasNext()) {
                entry = iterator.next();
                rect = entry.getValue();
                //inflate the rectangle to see it behind dom element
                rect = new RectF(rect.left-5, rect.top-5, rect.right+5, rect.bottom+5);
                rect.left -= scrollX;
                rect.right -= scrollX;
                rect.top -= scrollY;
                rect.bottom -= scrollY;
                canvas.drawRect(rect, paint);
                rect.left += scrollX;
                rect.right += scrollX;
                rect.top += scrollY;
                rect.bottom += scrollY;
            }
        }
    }

    private class TouchableWrapper extends FrameLayout {

        public TouchableWrapper(Context _context) {
            super(_context);
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent event) {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_UP) {
                _scrollView.requestDisallowInterceptTouchEvent(true);
            }
            return super.dispatchTouchEvent(event);
        }
    }
}