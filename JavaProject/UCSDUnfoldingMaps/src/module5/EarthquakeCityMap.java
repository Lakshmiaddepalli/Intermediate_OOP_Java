package module5;

import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.*;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.*;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.AbstractMarker;
import de.fhpotsdam.unfolding.marker.AbstractShapeMarker;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.MultiMarker;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.utils.MapUtils;
import parsing.ParseFeed;
import processing.core.PApplet;

/**
 * EarthquakeCityMap An application with an interactive map displaying
 * earthquake data. Author: UC San Diego Intermediate Software Development MOOC
 * team
 * 
 * @author Your name here Date: July 17, 2015
 */
public class EarthquakeCityMap extends PApplet {

	// We will use member variables, instead of local variables, to store the
	// data
	// that the setup and draw methods will need to access (as well as other
	// methods)
	// You will use many of these variables, but the only one you should need to
	// add
	// code to modify is countryQuakes, where you will store the number of
	// earthquakes
	// per country.

	public UnfoldingMap getMap() {
		return this.map;
	}

	public void setMap(UnfoldingMap map) {
		this.map = map;
	}

	// You can ignore this. It's to get rid of eclipse warnings
	private static final long serialVersionUID = 1L;

	// IF YOU ARE WORKING OFFILINE, change the value of this variable to true
	private static final boolean offline = true;

	/**
	 * This is where to find the local tiles, for working without an Internet
	 * connection
	 */
	public static String mbTilesString = "blankLight-1-3.mbtiles";

	// feed with magnitude 2.5+ Earthquakes
	private String earthquakesURL = "http://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_week.atom";

	// The files containing city names and info and country names and info
	private String cityFile = "city-data.json";
	private String countryFile = "countries.geo.json";

	// The map
	private UnfoldingMap map;

	// Markers for each city
	private List<Marker> cityMarkers;
	// Markers for each earthquake
	private List<Marker> quakeMarkers;

	// A List of country markers
	private List<Marker> countryMarkers;

	// NEW IN MODULE 5
	private CommonMarker lastSelected;
	private CommonMarker lastClicked;
	int totaloceanquakes = 0;

	public static boolean ASC = true;
	public static boolean DESC = false;

	Object[] arr = null;

	public void setup() {
		// (1) Initializing canvas and map tiles
		size(900, 700, OPENGL);
		if (offline) {
			map = new UnfoldingMap(this, 200, 50, 650, 600, new MBTilesMapProvider(mbTilesString));
			// earthquakesURL = "2.5_week.atom";
			earthquakesURL = "quiz2.atom";// The same feed, but saved August
											// 7, 2015
		} else {
			map = new UnfoldingMap(this, 200, 50, 650, 600, new Google.GoogleMapProvider());
			// IF YOU WANT TO TEST WITH A LOCAL FILE, uncomment the next line
			// earthquakesURL = "2.5_week.atom";
		}
		MapUtils.createDefaultEventDispatcher(this, map);

		// (2) Reading in earthquake data and geometric properties
		// STEP 1: load country features and markers
		List<Feature> countries = GeoJSONReader.loadData(this, countryFile);
		countryMarkers = MapUtils.createSimpleMarkers(countries);

		// STEP 2: read in city data
		List<Feature> cities = GeoJSONReader.loadData(this, cityFile);
		cityMarkers = new ArrayList<Marker>();
		for (Feature city : cities) {
			cityMarkers.add(new CityMarker(city));
		}

		// STEP 3: read in earthquake RSS feed
		List<PointFeature> earthquakes = ParseFeed.parseEarthquake(this, earthquakesURL);
		quakeMarkers = new ArrayList<Marker>();

		for (PointFeature feature : earthquakes) {
			// check if LandQuake
			if (isLand(feature)) {
				quakeMarkers.add(new LandQuakeMarker(feature));
			}
			// OceanQuakes
			else {
				quakeMarkers.add(new OceanQuakeMarker(feature));
				totaloceanquakes++;
			}
		}

		// could be used for debugging
		printQuakes();

		// (3) Add markers to map
		// NOTE: Country markers are not added to the map. They are used
		// for their geometric properties
		map.addMarkers(quakeMarkers);
		map.addMarkers(cityMarkers);

		System.out.println(quakeMarkers.size());
		sortAndPrint(15);

	} // End setup

	public void draw() {
		background(0);
		map.draw();
		addKey();
		// drawpopup();
	}

	/**
	 * Event handler that gets called automatically when the mouse moves.
	 */
	@Override
	public void mouseMoved() {
		// clear the last selection
		if (lastSelected != null) {
			lastSelected.setSelected(false);
			lastSelected = null;

		}
		selectMarkerIfHover(quakeMarkers);
		selectMarkerIfHover(cityMarkers);
	}

	// If there is a marker under the cursor, and lastSelected is null
	// set the lastSelected to be the first marker found under the cursor
	// Make sure you do not select two markers.
	//
	private void selectMarkerIfHover(List<Marker> markers) {
		// TODO: Implement this method
		for (Marker i : markers) {
			if ((i.isInside(map, mouseX, mouseY)) && (lastSelected == null)) {
				lastSelected = (CommonMarker) i;
				lastSelected.setSelected(true);
			}
		}
	}

	/**
	 * The event handler for mouse clicks It will display an earthquake and its
	 * threat circle of cities Or if a city is clicked, it will display all the
	 * earthquakes where the city is in the threat circle
	 */
	@Override
	public void mouseClicked() {
		// TODO: Implement this method
		// Hint: You probably want a helper method or two to keep this code
		// from getting too long/disorganized

		if (lastClicked != null) {
			lastClicked = null;
			unhideMarkers();
		} else {
			checkeathquakes();
			if (lastClicked == null) {
				Object[] noofearthquakesnearby = checkcityquakes();
				drawpopup(noofearthquakesnearby[0], noofearthquakesnearby[1], noofearthquakesnearby[2]);
			}
		}
	}

	public List<Marker> getCityMarkers() {
		return cityMarkers;
	}

	public void setCityMarkers(List<Marker> cityMarkers) {
		this.cityMarkers = cityMarkers;
	}

	public List<Marker> getQuakeMarkers() {
		return quakeMarkers;
	}

	public void setQuakeMarkers(List<Marker> quakeMarkers) {
		this.quakeMarkers = quakeMarkers;
	}

	private void checkeathquakes() {
		double val = 0;
		for (Marker j : quakeMarkers) {
			if (!j.isHidden() && j.isInside(map, mouseX, mouseY)) {
				lastClicked = (CommonMarker) j;
				val = ((EarthquakeMarker) j).threatCircle();

				for (Marker k : quakeMarkers) {
					if (k != lastClicked)
						k.setHidden(true);
				}
				for (Marker i : cityMarkers) {
					i.setHidden(true);
					if (i.getDistanceTo(lastClicked.getLocation()) <= val)
						i.setHidden(false);
				}
			}
		}
	}

	private Object[] checkcityquakes() {
		arr = new Object[3];
		double val = 0;
		float sum = 0;
		float averageans = 0;
		String agevalue = "";
		Map<String, Date> sortedMapDesc = null;
		HashMap<String, Date> av = new HashMap<String, Date>();
		for (Marker j : cityMarkers) {
			av.clear();
			if (!j.isHidden() && j.isInside(map, mouseX, mouseY)) {
				lastClicked = (CommonMarker) j;
				int noOfEarthquakes = 0;
				for (Marker k : cityMarkers) {
					if (k != lastClicked)
						k.setHidden(true);
				}
				for (Marker i : quakeMarkers) {
					i.setHidden(true);
					val = ((EarthquakeMarker) i).threatCircle();
					if (((EarthquakeMarker) i).getDistanceTo(lastClicked.getLocation()) <= val) {
						i.setHidden(false);
						noOfEarthquakes = noOfEarthquakes + 1;
						sum = sum + ((EarthquakeMarker) i).getMagnitude();
						agevalue = ((EarthquakeMarker) i).getDateTime();
						Date recentEarthquakes = convert(agevalue);
						av.put(((EarthquakeMarker) i).getTitle(), recentEarthquakes);

					}
				}

				averageans = sum / noOfEarthquakes;
				arr[0] = noOfEarthquakes;
				if (averageans == 0.0)
					arr[1] = 0.0;
				else {
					arr[1] = averageans;
				}
				sortedMapDesc = sortByComparator(av, DESC);
				arr[2] = printMap(sortedMapDesc);

			}
		}
		return arr;
	}

	private Date convert(String agevalue) {
		// TODO Auto-generated method stub
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date matchDateTime = null;
		try {
			matchDateTime = sdf.parse(agevalue.substring(0, (agevalue.length() - 4)));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return matchDateTime;
	}

	private static Map<String, Date> sortByComparator(Map<String, Date> unsortMap, final boolean order) {

		List<Entry<String, Date>> list = new LinkedList<Entry<String, Date>>(unsortMap.entrySet());

		// Sorting the list based on values
		Collections.sort(list, new Comparator<Entry<String, Date>>() {
			public int compare(Entry<String, Date> o1, Entry<String, Date> o2) {
				if (order) {
					return o1.getValue().compareTo(o2.getValue());
				} else {
					return o2.getValue().compareTo(o1.getValue());

				}
			}
		});
		Map<String, Date> sortedMap = new LinkedHashMap<String, Date>();
		for (Entry<String, Date> entry : list) {
			sortedMap.put(entry.getKey(), entry.getValue());
		}

		return sortedMap;
	}

	public static String printMap(Map<String, Date> map) throws NullPointerException {
		String key = "";

		try {
			Map.Entry<String, Date> entry = map.entrySet().iterator().next();
			key = entry.getKey();
			Date value = entry.getValue();
			System.out.println(key + " " + value);
		} catch (Exception e) {
			key = " No Nearby Earthquakes";
		}
		return key;
	}

	// loop over and unhide all markers
	private void unhideMarkers() {
		for (Marker marker : quakeMarkers) {
			marker.setHidden(false);
		}

		for (Marker marker : cityMarkers) {
			marker.setHidden(false);
		}
	}

	public void drawpopup(Object noofearthquakesnearby, Object noofearthquakesnearby2, Object noofearthquakesnearby3) {
		// TODO Auto-generated method stub
		final JFrame frame = new JFrame("Information ");
		JPanel p = new JPanel(new GridLayout(4, 1, 8, 4));

		JLabel value1 = new JLabel("Earthquakes Nearby : " + ((Integer) noofearthquakesnearby).toString(),
				SwingConstants.LEFT);
		value1.setVerticalAlignment(SwingConstants.CENTER);
		value1.setLocation(50, 20);

		DecimalFormat df = new DecimalFormat("###.##");

		JLabel value2 = new JLabel("Average Magnitude : " + (df.format((Float) noofearthquakesnearby2)).toString(),
				SwingConstants.LEFT);
		value2.setVerticalAlignment(SwingConstants.CENTER);
		value2.setLocation(50, 40);

		JLabel value3 = new JLabel("Recent Earthquake : ", SwingConstants.LEFT);
		value3.setVerticalAlignment(SwingConstants.CENTER);
		value3.setLocation(50, 60);

		JLabel value4 = new JLabel((noofearthquakesnearby3).toString(), SwingConstants.LEFT);
		value4.setVerticalAlignment(SwingConstants.CENTER);
		value4.setLocation(50, 60);

		value1.setBorder(BorderFactory.createLineBorder(Color.black));
		value2.setBorder(BorderFactory.createLineBorder(Color.black));
		value3.setBorder(BorderFactory.createLineBorder(Color.black));
		value4.setBorder(BorderFactory.createLineBorder(Color.black));

		p.add(value1);
		p.add(value2);
		p.add(value3);
		p.add(value4);

		// add mouse listener
		frame.addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				showPopup(e);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				showPopup(e);
			}

			private void showPopup(MouseEvent e) {
				if (e.isPopupTrigger()) {
					// popup.show(e.getComponent(),
					// e.getX(), e.getY());
				}
			}
		});

		// frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		frame.setContentPane(p);
		frame.setSize(200, 320);
		frame.setLocation(10, 380);
		frame.setVisible(true);
	}

	// TODO: Add the method:
	private void sortAndPrint(int numToPrint) {
		// and then call that method from setUp

		ArrayList<Float> value = new ArrayList<Float>();
		EarthquakeMarker[] lol = quakeMarkers.toArray(new EarthquakeMarker[quakeMarkers.size()]);
		Arrays.sort(lol);
		Arrays.toString(lol);
		if (numToPrint > quakeMarkers.size()) {
			for (int i = 0; i < quakeMarkers.size(); i++) {
				System.out.println(lol[i]);
			}
		}
		for (int i = 0; i < numToPrint; i++) {
			int count = 0;
			float val = lol[i].getMagnitude();
			System.out.println(lol[i].toString());
			for (int j = 0; j < quakeMarkers.size(); j++) {
				if (val == lol[j].getMagnitude()) {
					count = count + 1;
				}
			}
			if (count >= 3)
				value.add(val);
		}
		Object obj = Collections.max(value);
		System.out.println(obj.toString());

	}

	// helper method to draw key in GUI
	private void addKey() {
		// Remember you can use Processing's graphics methods here
		fill(255, 250, 240);

		int xbase = 25;
		int ybase = 50;

		rect(xbase, ybase, 150, 250);

		fill(0);
		textAlign(LEFT, CENTER);
		textSize(12);
		text("Earthquake Key", xbase + 25, ybase + 25);

		fill(150, 30, 30);
		int tri_xbase = xbase + 35;
		int tri_ybase = ybase + 50;
		triangle(tri_xbase, tri_ybase - CityMarker.TRI_SIZE, tri_xbase - CityMarker.TRI_SIZE,
				tri_ybase + CityMarker.TRI_SIZE, tri_xbase + CityMarker.TRI_SIZE, tri_ybase + CityMarker.TRI_SIZE);

		fill(0, 0, 0);
		textAlign(LEFT, CENTER);
		text("City Marker", tri_xbase + 15, tri_ybase);

		text("Land Quake", xbase + 50, ybase + 70);
		text("Ocean Quake", xbase + 50, ybase + 90);
		text("Size ~ Magnitude", xbase + 25, ybase + 110);

		fill(255, 255, 255);
		ellipse(xbase + 35, ybase + 70, 10, 10);
		rect(xbase + 35 - 5, ybase + 90 - 5, 10, 10);

		fill(color(255, 255, 0));
		ellipse(xbase + 35, ybase + 140, 12, 12);
		fill(color(0, 0, 255));
		ellipse(xbase + 35, ybase + 160, 12, 12);
		fill(color(255, 0, 0));
		ellipse(xbase + 35, ybase + 180, 12, 12);

		textAlign(LEFT, CENTER);
		fill(0, 0, 0);
		text("Shallow", xbase + 50, ybase + 140);
		text("Intermediate", xbase + 50, ybase + 160);
		text("Deep", xbase + 50, ybase + 180);

		text("Past hour", xbase + 50, ybase + 200);

		fill(255, 255, 255);
		int centerx = xbase + 35;
		int centery = ybase + 200;
		ellipse(centerx, centery, 12, 12);

		strokeWeight(2);
		line(centerx - 8, centery - 8, centerx + 8, centery + 8);
		line(centerx - 8, centery + 8, centerx + 8, centery - 8);

	}

	// Checks whether this quake occurred on land. If it did, it sets the
	// "country" property of its PointFeature to the country where it occurred
	// and returns true. Notice that the helper method isInCountry will
	// set this "country" property already. Otherwise it returns false.
	private boolean isLand(PointFeature earthquake) {

		// IMPLEMENT THIS: loop over all countries to check if location is in
		// any of them
		// If it is, add 1 to the entry in countryQuakes corresponding to this
		// country.
		for (Marker country : countryMarkers) {
			if (isInCountry(earthquake, country)) {
				return true;
			}
		}

		// not inside any country
		return false;
	}

	String c;

	// prints countries with number of earthquakes
	private void printQuakes() {
		// TODO: Implement this method
		for (Marker i : countryMarkers) {
			int count = 0;
			String countryname = i.getProperty("name").toString();
			for (Marker j : quakeMarkers) {
				if (j.getProperty("country") == i.getProperty("name")) {
					c = j.getProperty("country").toString();
					count = count + 1;
				}

			}
			if (count >= 1) {
				System.out.println(" " + c + " : " + count);

			}
		}

		System.out.println(" OCEAN QUAKES : " + (totaloceanquakes));
	}

	// helper method to test whether a given earthquake is in a given country
	// This will also add the country property to the properties of the
	// earthquake feature if
	// it's in one of the countries.
	// You should not have to modify this code
	private boolean isInCountry(PointFeature earthquake, Marker country) {
		// getting location of feature
		Location checkLoc = earthquake.getLocation();

		// some countries represented it as MultiMarker
		// looping over SimplePolygonMarkers which make them up to use
		// isInsideByLoc
		if (country.getClass() == MultiMarker.class) {

			// looping over markers making up MultiMarker
			for (Marker marker : ((MultiMarker) country).getMarkers()) {

				// checking if inside
				if (((AbstractShapeMarker) marker).isInsideByLocation(checkLoc)) {
					earthquake.addProperty("country", country.getProperty("name"));

					// return if is inside one
					return true;
				}
			}
		}

		// check if inside country represented by SimplePolygonMarker
		else if (((AbstractShapeMarker) country).isInsideByLocation(checkLoc)) {
			earthquake.addProperty("country", country.getProperty("name"));

			return true;
		}
		return false;
	}

}
