
package com.booking.hackathon;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

class HotelsSorter {

  static JSONArray sortByAvgDistance(List<Coordinate> places, JSONArray hotels) {
    List<HotelInfo> hotelInfos = new ArrayList<>(hotels.length());
    for (int i = 0; i < hotels.length(); ++i)
      hotelInfos.add(new HotelInfo(hotels.getJSONObject(i), places));
    hotelInfos.sort(Comparator.comparingDouble(x -> x.distance));
    JSONArray result = new JSONArray();
    hotelInfos.stream().limit(10).forEach(x -> result.put(x.hotel));
    return result;
  }

  static JSONArray sortByClusters(List<Coordinate> places, JSONArray hotels, double maxWalkDistance) {
    List<HotelInfo> hotelInfos = new ArrayList<>(hotels.length());
    int placesNearby;
    List<Coordinate> placesTooFar = new ArrayList<>(hotels.length());
    for (int i = 0; i < hotels.length(); ++i)
    {
      placesNearby = 0;
      placesTooFar.clear();
      final HotelInfo hotelInfo = new HotelInfo(hotels.getJSONObject(i), places);
      for (Coordinate place: places)
        if (place.distanceTo(hotelInfo.coord) <= maxWalkDistance)
          ++placesNearby;
        else
          placesTooFar.add(place);
      hotelInfo.placesNearby = placesNearby;
      hotelInfo.otherClustersCount = countClusters(placesTooFar, maxWalkDistance);
      hotelInfos.add(hotelInfo);
    }
    hotelInfos.sort(Comparator.<HotelInfo>comparingInt(x -> -x.placesNearby)
        .thenComparingDouble(x -> x.distance)
        .thenComparingInt(x -> -x.otherClustersCount));
    JSONArray result = new JSONArray();
    hotelInfos.stream().limit(10).forEach(x -> {
      x.hotel.put("places_nearby", x.placesNearby).put("clusters", x.otherClustersCount);
      result.put(x.hotel);
    });
    return result;
  }

  private static int countClusters(List<Coordinate> places, double maxDistance) {
    BitSet placesWeHave = new BitSet(places.size());
    placesWeHave.set(0, places.size() - 1);
    int clusters = 0;
    int idx;
    while ((idx = placesWeHave.nextSetBit(0)) >= 0)
    {
      ++clusters;
      placesWeHave.clear(idx);
      while ((idx = placesWeHave.nextSetBit(idx)) >= 0)
        placesWeHave.clear(idx);
    }
    return clusters;
  }

  private static class HotelInfo {
    final JSONObject hotel;
    final Coordinate coord;
    final double distance;
    int placesNearby;
    int otherClustersCount;

    public HotelInfo(JSONObject hotel, List<Coordinate> places) {
      this.hotel = hotel;
      this.placesNearby = places.size();
      JSONObject location = hotel.getJSONObject("location");
      coord = new Coordinate(location.getDouble("longitude"), location.getDouble("latitude"));
      this.distance = coord.avgDistanceTo(places);
    }
  }

  private static class PairWithDistance {
    final int idx1;
    final int idx2;
    final double distance;

    private PairWithDistance(int idx1, int idx2, double distance) {
      this.idx1 = idx1;
      this.idx2 = idx2;
      this.distance = distance;
    }
  }
}