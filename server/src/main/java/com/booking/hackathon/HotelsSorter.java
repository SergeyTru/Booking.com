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
    hotelInfos.stream().limit(30).forEach(x -> result.put(x.hotel));
    return result;
  }

  static JSONArray sortByClusters(List<Coordinate> places, JSONArray hotels, double maxWalkDistance) {
    List<HotelInfo> hotelInfos = new ArrayList<>(hotels.length());
    int placesNearby;
    double avgDistance;
    List<Coordinate> placesTooFar = new ArrayList<>(hotels.length());
    for (int i = 0; i < hotels.length(); ++i)
    {
      placesNearby = 0;
      avgDistance = 0;
      placesTooFar.clear();
      final HotelInfo hotelInfo = new HotelInfo(hotels.getJSONObject(i), places);
      for (Coordinate place: places)
      {
        final double distance = place.distanceTo(hotelInfo.coord);
        if (distance <= maxWalkDistance)
        {
          ++placesNearby;
          avgDistance += distance;
        }
        else
          placesTooFar.add(place);
      }
      hotelInfo.placesNearby = placesNearby;
      hotelInfo.otherClustersCount = countClusters(placesTooFar, maxWalkDistance);
      hotelInfo.distanceToNear = avgDistance / placesNearby;
      hotelInfos.add(hotelInfo);
    }
    hotelInfos.sort(null);
    JSONArray result = new JSONArray();
    System.out.println(hotelInfos.stream().mapToInt(x -> x.placesNearby).summaryStatistics());
    hotelInfos.stream().limit(20).forEach(x -> {
      x.hotel.put("places_nearby", x.placesNearby).put("clusters", x.otherClustersCount);
      result.put(x.hotel);
    });
    return result;
  }

  private static int countClusters(List<Coordinate> places, double maxDistance) {
    if (places.isEmpty())
      return 0;
    BitSet placesWeHave = new BitSet(places.size());
    placesWeHave.set(0, places.size() - 1);
    int clusters = 0;
    int idx, cnt;
    double sumLat, sumLon;
    while ((idx = placesWeHave.nextSetBit(0)) >= 0)
    {
      ++clusters;
      Coordinate coord = places.get(idx);
      sumLat = coord.lat;
      sumLon = coord.lon;
      cnt = 1;
      Coordinate clusterCenter = coord;
      placesWeHave.clear(idx);
      while ((idx = placesWeHave.nextSetBit(idx)) >= 0)
        if (places.get(idx).distanceTo(clusterCenter) < maxDistance)
        {
          placesWeHave.clear(idx);
          coord = places.get(idx);
          sumLat += coord.lat;
          sumLon += coord.lon;
          cnt++;
          clusterCenter = new Coordinate(sumLon/cnt, sumLat/cnt);
        }
    }
    return clusters;
  }

  private static class HotelInfo implements Comparable<HotelInfo> {
    final JSONObject hotel;
    final Coordinate coord;
    final double distance;
    int placesNearby;
    int otherClustersCount;
    double distanceToNear;

    public HotelInfo(JSONObject hotel, List<Coordinate> places) {
      this.hotel = hotel;
      this.placesNearby = places.size();
      JSONObject location = hotel.getJSONObject("location");
      coord = new Coordinate(location.getDouble("longitude"), location.getDouble("latitude"));
      this.distance = coord.avgDistanceTo(places);
    }

    @Override
    public int compareTo(HotelInfo oth) {
      int ret = Integer.compare(placesNearby, oth.placesNearby);
      if (ret != 0)
        return -ret;
      if (Math.abs(distanceToNear - oth.distanceToNear) > 0.1)
        return Double.compare(distanceToNear, oth.distanceToNear);
      ret = Integer.compare(otherClustersCount, oth.otherClustersCount);
      if (ret != 0)
        return ret;
      return Double.compare(distance, oth.distance);
    }
  }
//
//  private static class PairWithDistance {
//    final int idx1;
//    final int idx2;
//    final double distance;
//
//    private PairWithDistance(int idx1, int idx2, double distance) {
//      this.idx1 = idx1;
//      this.idx2 = idx2;
//      this.distance = distance;
//    }
//  }
}