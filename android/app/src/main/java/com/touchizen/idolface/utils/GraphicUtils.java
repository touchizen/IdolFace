package com.touchizen.idolface.utils;

import com.touchizen.idolface.facedetector.GraphicOverlay;

public class GraphicUtils {
	public static float translateX(GraphicOverlay overlay, float scaleFactor, float x) {
		if (overlay.isImageFlipped()) {
			return overlay.getWidth() - (scaleFactor*x - overlay.getPostScaleWidthOffset());
		} else {
			return scaleFactor*x - overlay.getPostScaleWidthOffset();
		}
	}

	/**
	 * Adjusts the y coordinate from the image's coordinate system to the view coordinate system.
	 */
	public static float translateY(GraphicOverlay overlay, float scaleFactor, float y) {
		return scaleFactor*y - overlay.getPostScaleHeightOffset();
	}

}
