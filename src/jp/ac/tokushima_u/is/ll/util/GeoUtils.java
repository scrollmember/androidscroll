package jp.ac.tokushima_u.is.ll.util;

import java.util.HashMap;
import java.util.Map;

public class GeoUtils {

	private static int EARTH_RADIUS_KM = 6371;

	public static Map<String, Double> parseGeoUri(String geouri) {
		Map<String, Double> map = new HashMap<String, Double>();
		try {
			if (geouri != null && geouri.startsWith("geo:")) {
				String substr = geouri.substring(4);
				String[] strs = substr.split(",");
				Double lat = Double.valueOf(strs[0]);
				Double lng = Double.valueOf(strs[1]);
				if (lat != null && lng != null) {
					map.put("latitude", lat);
					map.put("longitude", lng);
				}
			}
		} catch (Exception e) {

		}

		return map;
	}

	public static Map<String, Double> getKMRange(Double lat, Double lng, double distance) {
		Map<String, Double> map = new HashMap<String, Double>();
		if (lat == null || lng == null) {
			return map;
		}
		Double x1, y1, x2, y2, xt, yt;
		Double m = 360 / 39940.638*distance;
		x1 = lat + m;
		x2 = lat - m;
		if (x1 < -90 || x1 > 90) {
			x1 = 180 * x1 / Math.abs(x1) - x1;
		}
		if (x2 < -90 || x2 > 90) {
			x2 *= 180 * x2 / Math.abs(x2) - x2;
		}
		if (x1 < x2) {
			xt = x1;
			x1 = x2;
			x2 = xt;
		}
		Double clat = 360 / (2 * Math.PI * Math.cos(lat) * 40075.004);
		if (clat < 0) {
			clat *= -1;
		}

		y1 = lng - clat;
		y2 = lng + clat;
		if (y1 < -180 || y1 > 180) {
			y1 = (360 * (y1 / Math.abs(y1)) - y1) * (-1);
		}
		if (y2 < -180 || y2 > 180) {
			y2 = (360 * y2 / Math.abs(y2) - y2) * (-1);
		}
		if (y1 < y2) {
			yt = y1;
			y1 = y2;
			y2 = yt;
		}

		map.put("x1", x1);
		map.put("x2", x2);
		map.put("y1", y1);
		map.put("y2", y2);

		return map;
	}

	public static double distanceKm(double lat1, double lon1, double lat2,
			double lon2) {
		double lat1Rad = Math.toRadians(lat1);
		double lat2Rad = Math.toRadians(lat2);
		double deltaLonRad = Math.toRadians(lon2 - lon1);

		return Math
				.acos(Math.sin(lat1Rad) * Math.sin(lat2Rad) + Math.cos(lat1Rad)
						* Math.cos(lat2Rad) * Math.cos(deltaLonRad))
				* EARTH_RADIUS_KM;
	}
	
	public static void main(String[] args) {
		double lat = 33.9274086943118;
		double lng = 134.375152587891;
		Map<String,Double> r = parseGeoUri("geo:33.9274086943118,134.375152587891");
		System.out.println(r.toString());
	}
}
