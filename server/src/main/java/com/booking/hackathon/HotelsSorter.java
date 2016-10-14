
package com.booking.hackathon;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

class HotelsSorter {

  static JSONArray sort(List<Coordinate> places, JSONArray hotels) {
    List<HotelInfo> hotelInfos = new ArrayList<>(hotels.length());
    for (int i = 0; i < hotels.length(); ++i)
      hotelInfos.add(new HotelInfo(hotels.getJSONObject(i), places));
    hotelInfos.sort(Comparator.comparingDouble(x -> x.distance));
    JSONArray result = new JSONArray();
    hotelInfos.forEach(x -> result.put(x.hotel));
    return result;
  }

  private static class HotelInfo {
    final JSONObject hotel;
    final double distance;

    public HotelInfo(JSONObject hotel, List<Coordinate> places) {
      this.hotel = hotel;
      JSONObject location = hotel.getJSONObject("location");
      this.distance = new Coordinate(location.getDouble("longitude"), location.getDouble("latitude")).avgDistanceTo(places);
    }
  }
}