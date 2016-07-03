package com.telerik.plugins.mapbox;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import org.apache.cordova.CordovaWebView;

import java.util.*;
import java.util.Map;

/**
 * Created by vikti on 24/06/2016.
 */
public class PluginLayout extends FrameLayout {


    private RectF _drawRect = new RectF();
    private boolean _isScrolling = false;
    private Paint _paint = new Paint();

    public HashMap<String, RectF> HTMLNodes = new HashMap<>();
    public boolean isClickable;
    public boolean isDebug = true;
    public static CordovaWebView cdvWebView;
    public View mapView;
    public FrameLayout.LayoutParams mapFrame;
    public final MapOverlayDebugLayer debugLayer;

    public PluginLayout(Context context, CordovaWebView webView) {
        super(context);
        cdvWebView = webView;
        setWillNotDraw(false);
  //      setBackgroundColor(Color.TRANSPARENT);
        debugLayer = new MapOverlayDebugLayer(context);
    }

    public void setDrawingRect(float left, float top, float right, float bottom) {
        this._drawRect.left = left;
        this._drawRect.top = top;
        this._drawRect.right = right;
        this._drawRect.bottom = bottom;
        if (this.isDebug) {
            this.invalidate();
            debugLayer.invalidate();
        }
    }

    public void setHTMLElement(String domId, float left, float top, float right, float bottom) {
        RectF rect;
        if (this.HTMLNodes.containsKey(domId)) {
            rect = this.HTMLNodes.get(domId);
        } else {
            rect = new RectF();
        }
        rect.left = left;
        rect.top = top;
        rect.right = right;
        rect.bottom = bottom;
        this.HTMLNodes.put(domId, rect);
        if (this.isDebug) {
            this.invalidate();
            debugLayer.invalidate();
        }
    }
    public void deleteHTMLElement(String domId) {
        this.HTMLNodes.remove(domId);
        if (this.isDebug) {
            this.invalidate();
            debugLayer.invalidate();
        }
    }
    public void clearHTMLElement() {
        this.HTMLNodes.clear();
        if (this.isDebug) {
            this.invalidate();
            debugLayer.invalidate();
        }
    }

    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (!isClickable) {
            cdvWebView.getView().requestFocus(View.FOCUS_DOWN);
            return false;
        }
        int x = (int)event.getX();
        int y = (int)event.getY();
        int scrollY = cdvWebView.getView().getScrollY();
        boolean contains = this._drawRect.contains(x, y);
        int action = event.getAction();
        _isScrolling = (!contains && action == MotionEvent.ACTION_DOWN) || _isScrolling;
        _isScrolling = action != MotionEvent.ACTION_UP && _isScrolling;
        contains = !_isScrolling && contains;

        if (contains) {
            // Is the touch point on any HTML elements?
            Set<Map.Entry<String, RectF>> elements = this.HTMLNodes.entrySet();
            Iterator<Map.Entry<String, RectF>> iterator = elements.iterator();
            Map.Entry<String, RectF> entry;
            RectF rect;
            while(iterator.hasNext() && contains) {
                entry = iterator.next();
                rect = entry.getValue();
                rect.top -= scrollY;
                rect.bottom -= scrollY;
                if (entry.getValue().contains(x, y)) {
                    contains = false;
                }
                rect.top += scrollY;
                rect.bottom += scrollY;
            }
        }
        if (!contains) {
            cdvWebView.getView().requestFocus(View.FOCUS_DOWN);
        }
        return contains;
    }

    public class MapOverlayDebugLayer extends FrameLayout{

        final PluginLayout pluginLayout = PluginLayout.this;

        public MapOverlayDebugLayer(Context context) {
            super(context);
            setWillNotDraw(false);
        }

        @Override
        protected void onDraw(Canvas canvas) {
/*            if (pluginLayout._drawRect == null || !isDebug) {
                return;
            }
  */          int width = canvas.getWidth();
            int height = canvas.getHeight();
            int scrollY = cdvWebView.getView().getScrollY();

            _paint.setColor(Color.argb(50, 0, 255, 0));
            if (!isClickable) {
                canvas.drawRect(0f, 0f, width, height, _paint);
                //return;
            }
            canvas.drawRect(0f, 0f, width, pluginLayout._drawRect.top, _paint);
            canvas.drawRect(0, pluginLayout._drawRect.top, pluginLayout._drawRect.left, pluginLayout._drawRect.bottom, _paint);
            canvas.drawRect(pluginLayout._drawRect.right, pluginLayout._drawRect.top, width, pluginLayout._drawRect.bottom, _paint);
            canvas.drawRect(0, pluginLayout._drawRect.bottom, width, height, _paint);


            _paint.setColor(Color.argb(50, 255, 0, 0));

            Set<java.util.Map.Entry<String, RectF>> elements = HTMLNodes.entrySet();
            Iterator<java.util.Map.Entry<String, RectF>> iterator = elements.iterator();
            java.util.Map.Entry <String, RectF> entry;
            RectF rect;
            while(iterator.hasNext()) {
                entry = iterator.next();
                rect = entry.getValue();
                rect.top -= scrollY +2;
                rect.bottom -= scrollY +2;
                canvas.drawRect(rect, _paint);
            }
        }
    }

}
