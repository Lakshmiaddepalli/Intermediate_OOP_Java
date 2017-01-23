package module5;

import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.marker.AbstractMarker;
import de.fhpotsdam.unfolding.marker.Marker;
import processing.core.PGraphics;
import de.fhpotsdam.unfolding.utils.ScreenPosition;

/**
 * Implements a visual marker for ocean earthquakes on an earthquake map
 * 
 * @author UC San Diego Intermediate Software Development MOOC team
 * @author Your name here
 *
 */
public class OceanQuakeMarker extends EarthquakeMarker {

	EarthquakeMarker lastClicked1;
	double val = 0;

	public OceanQuakeMarker(PointFeature quake) {
		super(quake);

		// setting field in earthquake marker
		isOnLand = false;
	}

	/** Draw the earthquake as a square */
	@Override
	public void drawEarthquake(PGraphics pg, float x, float y) {
		pg.rect(x - radius, y - radius, 2 * radius, 2 * radius);
		OceanQuakeMarker o;
		/*
		 * if(mousepressed )
		 * 
		 * // ScreenPosition s = new ScreenPosition(x, y); EarthquakeCityMap e =
		 * new EarthquakeCityMap(); if (e.getQuakeMarkers() != null) { for
		 * (Marker j : e.getQuakeMarkers()) { if (j.isInside(e.getMap(), x, y))
		 * { lastClicked1 = (EarthquakeMarker) j; val = ((EarthquakeMarker)
		 * j).threatCircle(); for (Marker i : e.getCityMarkers()) {
		 * i.setHidden(true); if (i.getDistanceTo(lastClicked1.getLocation()) <=
		 * val) { i.setHidden(false); pg.fill(0);
		 * pg.line((i.getLocation().getLat()), (i.getLocation().getLon()),
		 * (j.getLocation().getLat()), (j.getLocation().getLon())); } } } else {
		 * pg.noStroke(); } } }
		 */
	}

	/*
	 * private void checkeathquakes() { double val = 0; for (Marker j :
	 * quakeMarkers) { if (!j.isHidden() && j.isInside(map, mouseX, mouseY)) {
	 * lastClicked = (CommonMarker) j; val = ((EarthquakeMarker)
	 * j).threatCircle();
	 * 
	 * for (Marker k : quakeMarkers) { if (k != lastClicked) k.setHidden(true);
	 * } for (Marker i : cityMarkers) { i.setHidden(true); if
	 * (i.getDistanceTo(lastClicked.getLocation()) <= val) i.setHidden(false); }
	 * } } }
	 */

}
